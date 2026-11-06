package com.example.hiddencats.billing

import com.example.hiddencats.data.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * App-wide owner of "what has this player bought?".
 *
 * There is no sign-in, so entitlements come from the Google Play account on the device:
 *  - On every app start the billing connection is opened and purchases are re-queried, so
 *    Premium / Remove Ads come back after a reinstall or a new phone without opening the Shop.
 *  - Successful purchases (and restores) unlock items here, not in a screen, so nothing is lost
 *    if the Shop is closed.
 *  - If Play stops listing a one-time purchase (refund / chargeback), the unlock is switched off.
 */
class PurchaseProcessor(
    private val billing: BillingRepository,
    private val repository: GameRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private var started = false

    @Synchronized
    fun start() {
        if (started) return
        started = true

        // Subscribe BEFORE connecting: the purchase event flow does not replay missed events.
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            billing.purchaseEvents.collect { grant(it) }
        }
        scope.launch {
            billing.ownedEntitlements.filterNotNull().collect { reconcile(it) }
        }
        billing.startConnection()
    }

    private suspend fun grant(result: PurchaseResult) {
        if (result !is PurchaseResult.Success) return
        when (result.productId) {
            BillingProduct.PremiumUpgrade.productId -> {
                repository.setPremiumUnlocked(true)
                repository.setAdsRemoved(true)
            }
            BillingProduct.RemoveAds.productId -> repository.setAdsRemoved(true)
            BillingProduct.HintPackSmall.productId -> repository.addHints(5)
            BillingProduct.HintPackLarge.productId -> repository.addHints(20)
        }
    }

    /** Makes the local flags match what Google Play says the account owns. */
    private suspend fun reconcile(owned: Set<String>) {
        val premium = BillingProduct.PremiumUpgrade.productId in owned
        val noAds = premium || BillingProduct.RemoveAds.productId in owned
        val prefs = repository.preferences.first()
        if (prefs.premiumUnlocked != premium) repository.setPremiumUnlocked(premium)
        if (prefs.adsRemoved != noAds) repository.setAdsRemoved(noAds)
    }
}
