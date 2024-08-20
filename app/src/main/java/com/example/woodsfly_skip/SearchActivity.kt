package com.example.woodsfly_skip

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView


class SearchActivity : Activity() {
    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var recentSearchText: TextView
    private lateinit var clearRecentSearch: ImageView
    private lateinit var searchHistoryList: ListView
    private val searchHistory = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // 初始化控件
        searchInput = findViewById(R.id.search_input)
        searchButton = findViewById(R.id.search_button)
        recentSearchText = findViewById(R.id.recent_search_text)
        clearRecentSearch = findViewById(R.id.clear_recent_search)
        searchHistoryList = findViewById(R.id.search_history_list)

        // 设置搜索按钮的点击事件
        searchButton.setOnClickListener {
            val query = searchInput.text.toString()
            if (query.isNotEmpty()) {
                // 添加到搜索历史
                searchHistory.add(query)
                updateSearchHistoryUI()
            }
        }

        // 设置清除搜索历史的点击事件
        clearRecentSearch.setOnClickListener {
            searchHistory.clear()
            updateSearchHistoryUI()
        }
//s删除按钮

        // 适配器用于显示搜索历史
        searchHistoryList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, searchHistory)
    }

    private fun updateSearchHistoryUI() {
        (searchHistoryList.adapter as ArrayAdapter<String>).notifyDataSetChanged()
    }
}
