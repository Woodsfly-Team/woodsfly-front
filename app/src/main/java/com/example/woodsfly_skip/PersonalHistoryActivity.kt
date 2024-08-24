package com.example.woodsfly_skip


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 个人历史记录活动类，用于显示用户的查阅记录和登录记录。
 *
 * @author zoeyyyy-git
 * @Time 2024-08-21
 */
class PersonalHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter

    /**
     * 活动创建时调用的方法，初始化界面和数据。
     *
     * @param savedInstanceState 保存的实例状态，可用于恢复活动状态
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recyclerViewHistory)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val historyRecords = listOf(
            "查阅记录",
            "登录记录",

            )

        adapter = HistoryAdapter(historyRecords, this) { record ->
            when (record) {
                "查阅记录" -> startActivity(Intent(this, PersonalActivity::class.java))
                "登录记录" -> startActivity(Intent(this, PersonalLoginActivity::class.java))

            }
            finish()
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }


    inner class HistoryAdapter(
        private val records: List<String>,
        private val context: Context,
        private val onItemClickListener: (String) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
        /**
         * 创建ViewHolder的方法，用于视图的复用。
         *
         * @param parent 父视图
         * @param viewType 视图类型
         * @return 创建的ViewHolder实例
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false)
            return HistoryViewHolder(view)
        }

        /**
         * 绑定数据到ViewHolder的方法，用于设置列表项的显示内容。
         *
         * @param holder ViewHolder实例
         * @param position 列表项的位置
         */
        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            val record = records[position]
            holder.textView.text = record
            holder.itemView.setOnClickListener { onItemClickListener(record) }
        }

        /**
         * 获取数据项总数的方法。
         *
         * @return 数据项的总数
         */
        override fun getItemCount(): Int = records.size

        /**
         * 历史记录ViewHolder内部类，用于缓存列表项的视图。
         */
        inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.textViewHistoryItem)
        } // 文本视图，用于显示历史记录项的文本
    }
}
