package com.example.woodsfly


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


import com.example.woodsfly.ui.dashboard.DashboardFragment


class PersonalHistoryActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var historyTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        historyTextView = findViewById(R.id.historyTextView)

        // 显示浏览记录
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val browsingHistory = sharedPreferences.getString("browsing_history", "")?.split("\n") ?: emptyList()
        val historyText = buildString {
            for (record in browsingHistory) {
                append("$record\n")
            }
        }
        historyTextView.text = historyText.trim()
    }
}