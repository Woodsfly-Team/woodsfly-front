package com.example.woodsfly

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class StarHistoryActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var favoritesTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_star_history)

        favoritesTextView = findViewById(R.id.favoriteItemsTextView)


        // 显示收藏记录
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        val favoriteItems = sharedPreferences.getString("favorite_items", "")?.split("\n") ?: emptyList()
        val favoriteText = buildString {
            for (item in favoriteItems) {
                append("$item\n")
            }
        }

        favoritesTextView.text = favoriteText.trim()
    }
}