package com.example.hiddencats.domain.model

enum class GameMode(val label: String, val description: String) {
    CLASSIC("Classic", "Score based on accuracy, time, and hints with 3 lives"),
    TIMED("Timed", "Race against the clock before the timer reaches zero"),
    DAILY("Daily Challenge", "Deterministic daily puzzle shared with players globally"),
    RELAXED("Relaxed", "No countdown, no lives, pure tranquil logic")
}
