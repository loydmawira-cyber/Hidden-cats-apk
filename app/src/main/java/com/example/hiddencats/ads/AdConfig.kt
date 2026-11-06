package com.example.hiddencats.ads

/** Tunable ad / reward settings. Change these without touching game logic. */
object AdConfig {
    /** Hints granted for each completed rewarded video. */
    const val REWARDED_HINT_AMOUNT = 1

    /** Show an interstitial after this many finished puzzles... */
    const val INTERSTITIAL_EVERY_N_PUZZLES = 3

    /** ...but never more often than this, so fast players aren't spammed. */
    const val MIN_SECONDS_BETWEEN_INTERSTITIALS = 90L
}
