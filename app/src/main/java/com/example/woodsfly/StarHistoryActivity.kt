package com.example.woodsfly

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * 本地收藏界面
 *
 * @author Xiancaijiang
 * @Time 2024-08-28
 */
class StarHistoryActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var favoritesTextView: TextView
    private lateinit var clearFavoritesButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_star_history)

        // 初始化按钮
        favoritesTextView = findViewById(R.id.favoriteItemsTextView)
        clearFavoritesButton = findViewById(R.id.buttonClearFavorites) // 初始化按钮

        // 获取 SharedPreferences
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // 显示收藏记录
        displayFavorites()
        // 设置按钮点击事件
        clearFavoritesButton.setOnClickListener {
            onClearFavoritesClick()
        }
    }

    private fun onClearFavoritesClick() {
        // 清除收藏记录
        sharedPreferences.edit().putString("favorite_items", "").apply()

        // 更新显示
        updateFavoritesDisplay()
    }

    private fun updateFavoritesDisplay() {
        val favoriteItems = sharedPreferences.getString("favorite_items", "")?.split("\n") ?: emptyList()
        val favoriteText = buildString {
            for (item in favoriteItems) {
                append("$item\n")
            }
        }
        favoritesTextView.text = favoriteText.trim()
    }

    private fun displayFavorites() {
        // 获取收藏项
        val favoriteItems = sharedPreferences.getString("favorite_items", "")?.split("\n") ?: emptyList()

        // 打印日志以调试
        Log.d("StarHistoryActivity", "Favorite items: $favoriteItems")

        // 构建显示文本
        val favoriteText = buildString {
            for (item in favoriteItems) {
                append("$item\n")
            }
        }

        // 更新 TextView
        favoritesTextView.text = favoriteText.trim()
    }
}