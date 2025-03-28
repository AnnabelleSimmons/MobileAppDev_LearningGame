package com.example.mobileappdev2025

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stats)

        val score = intent.getStringExtra("score") ?: "SCORE NOT FOUND"
        val totalCorrect = intent.getStringExtra("totalCorrect") ?: "TOTAL CORRECT NOT FOUND"
        val totalWrong = intent.getStringExtra("totalWrong") ?: "TOTAL WRONG NOT FOUND"
        val currentStreak = intent.getStringExtra("currentStreak") ?: "STREAK NOT FOUND"

        findViewById<TextView>(R.id.score_text).text = "Score: $score"
        findViewById<TextView>(R.id.correct_text).text = "Correct Count: $totalCorrect"
        findViewById<TextView>(R.id.wrong_text).text = "Incorrect Count: $totalWrong"
        findViewById<TextView>(R.id.streak_text).text = "Current Streak: $currentStreak"
    }
}
