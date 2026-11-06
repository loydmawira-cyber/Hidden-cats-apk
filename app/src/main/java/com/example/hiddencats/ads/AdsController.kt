package com.example.hiddencats.ads

import android.app.Activity
import android.os.SystemClock
import com.example.hiddencats.data.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Decides WHEN ads appear (frequency, ad-free / premium rules) and grants rewards.
 * The actual SDK work stays inside [AdManager].
 *
 * Rules:
 *  - Interstitials: only between puzzles, every [AdConfig.INTERSTITIAL_EVERY_N_PUZZLES] puzzles,
 *    at most once per [AdConfig.MIN_SECONDS_BETWEEN_INTERSTITIALS]. Never for Remove Ads / Premium.
 *  - Rewarded video: always opt-in (player taps "Watch ad"), so it stays available to everyone.
 */
class AdsController(
    private val adManager: AdManager,
    private val repository: GameRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) {
    val rewardedReady: StateFlow<Boolean> = adManager.rewardedReady

    private var puzzlesSinceInterstitial = 0
    private var lastInterstitialAtMs = 0L

    fun initialize(activity: Activity) = adManager.initialize(activity)

    /** Call once each time a puzzle ends (win or lose). */
    fun onPuzzleFinished() {
        puzzlesSinceInterstitial++
    }

    /** Shows a rewarded video; on success grants hints, then calls [onDone] with the outcome. */
    fun showRewardedHint(activity: Activity, onDone: (earned: Boolean) -> Unit) {
        adManager.showRewarded(activity) { earned ->
            if (!earned) {
                onDone(false)
                return@showRewarded
            }
            scope.launch {
                repository.addHints(AdConfig.REWARDED_HINT_AMOUNT)
                onDone(true)
            }
        }
    }

    /** Suspends until the interstitial closes. Returns immediately when no ad is due. */
    suspend fun showInterstitialIfDue(activity: Activity?) {
        if (activity == null || !isDue()) return
        val prefs = repository.preferences.first()
        if (prefs.adsRemoved || prefs.premiumUnlocked) return

        puzzlesSinceInterstitial = 0
        lastInterstitialAtMs = SystemClock.elapsedRealtime()
        suspendCancellableCoroutine { cont ->
            adManager.showInterstitial(activity) {
                if (cont.isActive) cont.resume(Unit)
            }
        }
    }

    private fun isDue(): Boolean {
        if (puzzlesSinceInterstitial < AdConfig.INTERSTITIAL_EVERY_N_PUZZLES) return false
        val sinceLastMs = SystemClock.elapsedRealtime() - lastInterstitialAtMs
        return lastInterstitialAtMs == 0L || sinceLastMs >= AdConfig.MIN_SECONDS_BETWEEN_INTERSTITIALS * 1000
    }
}
