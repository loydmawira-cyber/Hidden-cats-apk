package com.example.hiddencats.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayBillingRepository(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : BillingRepository, PurchasesUpdatedListener {

    private val TAG = "PlayBillingRepo"

    private val _isReady = MutableStateFlow(false)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _productPrices = MutableStateFlow<Map<String, String>>(
        BillingProduct.ALL.associate { it.productId to it.defaultPriceFormatted }
    )
    override val productPrices: StateFlow<Map<String, String>> = _productPrices.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>(replay = 0, extraBufferCapacity = 8)
    override val purchaseEvents: SharedFlow<PurchaseResult> = _purchaseEvents.asSharedFlow()

    private val _ownedEntitlements = MutableStateFlow<Set<String>?>(null)
    override val ownedEntitlements: StateFlow<Set<String>?> = _ownedEntitlements.asStateFlow()

    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    private val billingClient: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    override fun startConnection() {
        if (billingClient.isReady) {
            _isReady.value = true
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isReady.value = true
                    queryProductDetails()
                    restorePurchases()
                } else {
                    Log.w(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    _isReady.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                _isReady.value = false
            }
        })
    }

    override fun queryProductDetails() {
        if (!billingClient.isReady) return

        val productList = BillingProduct.ALL.map { product ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(product.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val updatedPrices = _productPrices.value.toMutableMap()
                for (details in productDetailsList) {
                    productDetailsMap[details.productId] = details
                    val formattedPrice = details.oneTimePurchaseOfferDetails?.formattedPrice
                    if (formattedPrice != null) {
                        updatedPrices[details.productId] = formattedPrice
                    }
                }
                _productPrices.value = updatedPrices
            }
        }
    }

    override fun launchBillingFlow(activity: Activity?, productId: String) {
        if (activity == null) {
            scope.launch {
                _purchaseEvents.emit(PurchaseResult.Error("Activity context required for purchase"))
            }
            return
        }

        val details = productDetailsMap[productId]
        if (details == null) {
            scope.launch {
                _purchaseEvents.emit(PurchaseResult.Error("Product details not yet loaded from Play Store"))
            }
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            scope.launch {
                _purchaseEvents.emit(PurchaseResult.Error("Launch billing failed: ${result.debugMessage}"))
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                scope.launch { _purchaseEvents.emit(PurchaseResult.UserCancelled()) }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                scope.launch { _purchaseEvents.emit(PurchaseResult.AlreadyOwned("Item already owned")) }
            }
            else -> {
                scope.launch {
                    _purchaseEvents.emit(PurchaseResult.Error("Purchase error: ${billingResult.debugMessage}"))
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            for (productId in purchase.products) {
                val billingProduct = BillingProduct.findByProductId(productId)
                if (billingProduct?.isConsumable == true) {
                    val consumeParams = ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.consumeAsync(consumeParams) { result, _ ->
                        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                            scope.launch { _purchaseEvents.emit(PurchaseResult.Success(productId)) }
                        }
                    }
                } else {
                    _ownedEntitlements.value?.let { _ownedEntitlements.value = it + productId }
                    if (!purchase.isAcknowledged) {
                        val ackParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.acknowledgePurchase(ackParams) { result ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                scope.launch { _purchaseEvents.emit(PurchaseResult.Success(productId)) }
                            }
                        }
                    } else {
                        scope.launch { _purchaseEvents.emit(PurchaseResult.Success(productId)) }
                    }
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            scope.launch { _purchaseEvents.emit(PurchaseResult.Pending()) }
        }
    }

    override fun restorePurchases(onComplete: (count: Int) -> Unit) {
        if (!billingClient.isReady) {
            onComplete(0)
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _ownedEntitlements.value = purchasesList
                    .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    .flatMap { it.products }
                    .toSet()
                var restored = 0
                for (purchase in purchasesList) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase)
                        restored++
                    }
                }
                onComplete(restored)
            } else {
                onComplete(0)
            }
        }
    }

    override fun endConnection() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
