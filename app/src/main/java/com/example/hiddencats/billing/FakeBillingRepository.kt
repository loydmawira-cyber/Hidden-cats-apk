package com.example.hiddencats.billing

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FakeBillingRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : BillingRepository {

    private val _isReady = MutableStateFlow(true)
    override val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _productPrices = MutableStateFlow<Map<String, String>>(
        BillingProduct.ALL.associate { it.productId to it.defaultPriceFormatted }
    )
    override val productPrices: StateFlow<Map<String, String>> = _productPrices.asStateFlow()

    private val _purchaseEvents = MutableSharedFlow<PurchaseResult>(replay = 0, extraBufferCapacity = 8)
    override val purchaseEvents: SharedFlow<PurchaseResult> = _purchaseEvents.asSharedFlow()

    val ownedNonConsumables = mutableSetOf<String>()

    override fun startConnection() {
        _isReady.value = true
    }

    override fun queryProductDetails() {}

    override fun launchBillingFlow(activity: Activity?, productId: String) {
        val product = BillingProduct.findByProductId(productId)
        if (product == null) {
            scope.launch { _purchaseEvents.emit(PurchaseResult.Error("Unknown product $productId")) }
            return
        }

        if (!product.isConsumable && ownedNonConsumables.contains(productId)) {
            scope.launch { _purchaseEvents.emit(PurchaseResult.AlreadyOwned(productId)) }
            return
        }

        if (!product.isConsumable) {
            ownedNonConsumables.add(productId)
        }

        scope.launch {
            _purchaseEvents.emit(PurchaseResult.Success(productId))
        }
    }

    override fun restorePurchases(onComplete: (count: Int) -> Unit) {
        scope.launch {
            for (id in ownedNonConsumables) {
                _purchaseEvents.emit(PurchaseResult.Success(id))
            }
            onComplete(ownedNonConsumables.size)
        }
    }

    override fun endConnection() {
        _isReady.value = false
    }

    fun simulateCancellation() {
        scope.launch { _purchaseEvents.emit(PurchaseResult.UserCancelled()) }
    }

    fun simulateError(msg: String) {
        scope.launch { _purchaseEvents.emit(PurchaseResult.Error(msg)) }
    }
}
