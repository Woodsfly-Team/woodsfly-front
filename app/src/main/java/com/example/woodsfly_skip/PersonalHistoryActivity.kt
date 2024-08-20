package com.example.woodsfly_skip


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PersonalHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter

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

    inner class HistoryAdapter(private val records: List<String>, private val context: Context, private val onItemClickListener: (String) -> Unit) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false)
            return HistoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            val record = records[position]
            holder.textView.text = record
            holder.itemView.setOnClickListener { onItemClickListener(record) }
        }
            override fun getItemCount(): Int = records.size

            inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                val textView: TextView = itemView.findViewById(R.id.textViewHistoryItem)
            }
        }
    }