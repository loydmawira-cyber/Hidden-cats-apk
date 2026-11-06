package com.example.hiddencats.core

import android.content.Context
import com.example.hiddencats.ads.AdManager
import com.example.hiddencats.ads.AdsController
import com.example.hiddencats.ads.StubAdManager
import com.example.hiddencats.billing.BillingRepository
import com.example.hiddencats.billing.PlayBillingRepository
import com.example.hiddencats.billing.PurchaseProcessor
import com.example.hiddencats.data.GameRepository
import com.example.hiddencats.data.local.AppDatabase
import com.example.hiddencats.data.local.PreferencesManager
import com.example.hiddencats.domain.engine.GameEngine
import com.example.hiddencats.domain.engine.HintEngine
import com.example.hiddencats.domain.engine.PuzzleGenerator
import com.example.hiddencats.domain.engine.PuzzleSolver
import com.example.hiddencats.firebase.FirebaseManager

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(appContext)
    }

    val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(appContext)
    }

    val firebaseManager: FirebaseManager by lazy {
        FirebaseManager(appContext)
    }

    val puzzleSolver: PuzzleSolver by lazy {
        PuzzleSolver()
    }

    val puzzleGenerator: PuzzleGenerator by lazy {
        PuzzleGenerator(puzzleSolver)
    }

    val hintEngine: HintEngine by lazy {
        HintEngine()
    }

    val gameEngine: GameEngine by lazy {
        GameEngine(hintEngine)
    }

    val gameRepository: GameRepository by lazy {
        GameRepository(
            dao = database.gameRecordDao(),
            preferencesManager = preferencesManager,
            firebaseManager = firebaseManager,
            generator = puzzleGenerator
        )
    }

    val soundHapticsManager: SoundHapticsManager by lazy {
        SoundHapticsManager(appContext)
    }

    val billingRepository: BillingRepository by lazy {
        PlayBillingRepository(appContext)
    }

    val purchaseProcessor: PurchaseProcessor by lazy {
        PurchaseProcessor(billing = billingRepository, repository = gameRepository)
    }

    /** Swap StubAdManager for AdMobAdManager once ads are connected (see docs/ADS_SETUP.md). */
    val adManager: AdManager by lazy {
        StubAdManager()
    }

    val adsController: AdsController by lazy {
        AdsController(adManager = adManager, repository = gameRepository)
    }

    companion object {
        @Volatile
        private var instance: AppContainer? = null

        /** One container per process, so rotation / activity re-creation never duplicates billing or ad clients. */
        fun getInstance(context: Context): AppContainer =
            instance ?: synchronized(this) {
                instance ?: AppContainer(context.applicationContext).also { instance = it }
            }
    }
}
