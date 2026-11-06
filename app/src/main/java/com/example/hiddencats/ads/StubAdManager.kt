package com.example.hiddencats.ads

import android.app.Activity
import com.example.hiddencats.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Placeholder used until a real ad SDK is connected.
 *  - Debug builds: rewarded "ads" succeed instantly so you can test the hint flow.
 *  - Release builds: no ads are shown and no free rewards are given, so nothing can be exploited.
 */
class StubAdManager : AdManager {
    private val _rewardedReady = MutableStateFlow(BuildConfig.DEBUG)
    override val rewardedReady: StateFlow<Boolean> = _rewardedReady.asStateFlow()

    override fun initialize(activity: Activity) = Unit

    override fun showRewarded(activity: Activity, onResult: (earned: Boolean) -> Unit) {
        onResult(BuildConfig.DEBUG)
    }

    override fun showInterstitial(activity: Activity, onClosed: () -> Unit) {
        onClosed()
    }
}
