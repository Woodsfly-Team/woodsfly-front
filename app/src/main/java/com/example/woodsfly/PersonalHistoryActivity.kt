package com.example.woodsfly

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.example.woodsfly.ui.dashboard.DashboardFragment


/**
 * 本地浏览界面
 *
 * @author Xiancaijiang
 * @Time 2024-08-28
 */

class PersonalHistoryActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var historyTextView: TextView
    private lateinit var clearHistoryButton: Button // 添加按钮变量

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        historyTextView = findViewById(R.id.historyTextView)
        clearHistoryButton = findViewById(R.id.buttonClearHistory) // 初始化按钮

        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // 显示浏览记录
        updateHistoryDisplay()

        // 设置按钮点击事件
        clearHistoryButton.setOnClickListener {
            onClearHistoryClick()
        }
    }

    private fun onClearHistoryClick() {
        // 清除浏览记录
        sharedPreferences.edit().putString("browsing_history", "").apply()

        // 更新显示
        updateHistoryDisplay()
    }

    private fun updateHistoryDisplay() {
        val browsingHistory = sharedPreferences.getString("browsing_history", "")?.split("\n") ?: emptyList()
        val historyText = buildString {
            for (record in browsingHistory) {
                append("$record\n")
            }
        }
        historyTextView.text = historyText.trim()
    }
}