package com.example.hiddencats.billing

import android.app.Activity
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BillingRepository {
    val isReady: StateFlow<Boolean>
    val productPrices: StateFlow<Map<String, String>>
    val purchaseEvents: SharedFlow<PurchaseResult>

    fun startConnection()
    fun queryProductDetails()
    fun launchBillingFlow(activity: Activity?, productId: String)
    fun restorePurchases(onComplete: (count: Int) -> Unit = {})
    fun endConnection()
}
