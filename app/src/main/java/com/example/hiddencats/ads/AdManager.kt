package com.example.hiddencats.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * Thin abstraction over the ad SDK so the rest of the app never touches AdMob directly.
 * Swap [StubAdManager] for a real implementation in AppContainer (see docs/ADS_SETUP.md).
 */
interface AdManager {
    /** True when a rewarded video is loaded and can be shown right now. */
    val rewardedReady: StateFlow<Boolean>

    /** Initialise the SDK and start preloading ads. Safe to call more than once. */
    fun initialize(activity: Activity)

    /** Show a rewarded video. [onResult] receives true only if the user earned the reward. */
    fun showRewarded(activity: Activity, onResult: (earned: Boolean) -> Unit)

    /**
     * Show an interstitial if one is loaded. [onClosed] must ALWAYS be called exactly once
     * (also when nothing could be shown) so navigation is never blocked.
     */
    fun showInterstitial(activity: Activity, onClosed: () -> Unit)
}
