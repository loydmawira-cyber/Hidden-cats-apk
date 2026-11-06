# Connecting Ads (AdMob)

The app already contains the full ad *logic*; only the SDK is missing. Until you connect it,
`StubAdManager` is used (debug: rewarded ads succeed instantly; release: no ads, no free rewards).

## What is wired
- **Rewarded video:** when a player is out of hints, a dialog offers "Watch ad: +1 hint" or the Shop.
- **Interstitials:** shown between puzzles (on "Next puzzle" / "Play again"), every 3 puzzles and at most
  once per 90 s. Never shown to Remove Ads or Premium owners. Tune in `ads/AdConfig.kt`.
- **Hints:** 3 free at install; Premium = unlimited. **8x8 / 9x9 boards** are Premium-only.

## Steps
1. Create an AdMob account/app; create two ad units (Rewarded, Interstitial).
2. `app/build.gradle.kts`: `implementation("com.google.android.gms:play-services-ads:<latest>")`
3. `AndroidManifest.xml`, inside `<application>`:
   ```xml
   <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID"
              android:value="ca-app-pub-3940256099942544~3347511713"/> <!-- test ID; replace -->
   ```
4. Add `ads/AdMobAdManager.kt` (below) and in `AppContainer` change `StubAdManager()` to `AdMobAdManager(appContext)`.
5. Use Google's **test ad units** while developing; switch to real IDs only for release.
6. Add a consent flow (Google UMP SDK) for EU/UK users, update the Play Data Safety form (ads / advertising ID).

## AdMobAdManager.kt (drop-in)
```kotlin
package com.example.hiddencats.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.*
import com.google.android.gms.ads.rewarded.*
import kotlinx.coroutines.flow.*

class AdMobAdManager(private val appContext: Context) : AdManager {
    // Google TEST unit IDs - replace with your own for release.
    private val rewardedId = "ca-app-pub-3940256099942544/5224354917"
    private val interstitialId = "ca-app-pub-3940256099942544/1033173712"

    private var rewarded: RewardedAd? = null
    private var interstitial: InterstitialAd? = null
    private val _ready = MutableStateFlow(false)
    override val rewardedReady: StateFlow<Boolean> = _ready.asStateFlow()

    override fun initialize(activity: Activity) {
        MobileAds.initialize(appContext) { loadRewarded(); loadInterstitial() }
    }

    private fun loadRewarded() {
        RewardedAd.load(appContext, rewardedId, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) { rewarded = ad; _ready.value = true }
                override fun onAdFailedToLoad(e: LoadAdError) { rewarded = null; _ready.value = false }
            })
    }

    private fun loadInterstitial() {
        InterstitialAd.load(appContext, interstitialId, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { interstitial = null }
            })
    }

    override fun showRewarded(activity: Activity, onResult: (Boolean) -> Unit) {
        val ad = rewarded ?: run { onResult(false); loadRewarded(); return }
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewarded = null; _ready.value = false; loadRewarded(); onResult(earned)
            }
            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                rewarded = null; _ready.value = false; loadRewarded(); onResult(false)
            }
        }
        ad.show(activity) { earned = true }
    }

    override fun showInterstitial(activity: Activity, onClosed: () -> Unit) {
        val ad = interstitial ?: run { onClosed(); loadInterstitial(); return }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { interstitial = null; loadInterstitial(); onClosed() }
            override fun onAdFailedToShowFullScreenContent(e: AdError) { interstitial = null; loadInterstitial(); onClosed() }
        }
        ad.show(activity)
    }
}
```

## Firestore
Nothing in the ads/monetization code depends on Firestore. Connect it later via `google-services.json`
as described in the release checklist.
