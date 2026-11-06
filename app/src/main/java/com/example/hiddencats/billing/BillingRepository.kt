package com.example.hiddencats.billing

import android.app.Activity
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val isReady: StateFlow<Boolean>
    val productPrices: StateFlow<Map<String, String>>
    val purchaseEvents: SharedFlow<PurchaseResult>

    /** Product IDs Google Play says this account owns. Null until the first successful check. */
    val ownedEntitlements: StateFlow<Set<String>?>

    fun startConnection()
    fun queryProductDetails()
    fun launchBillingFlow(activity: Activity?, productId: String)
    fun restorePurchases(onComplete: (count: Int) -> Unit = {})
    fun endConnection()
}
