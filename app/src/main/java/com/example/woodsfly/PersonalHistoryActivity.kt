
package com.example.woodsfly


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.woodsfly.data.BirdDetails
import com.example.woodsfly.ui.dashboard.DashboardFragment


class PersonalHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var back: ImageView
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recyclerViewHistory)
        back = findViewById(R.id.back)
        back.setOnClickListener { finish() }


        //设置
        setupRecyclerView()
    }

    private fun setupRecyclerView() {

        adapter = HistoryAdapter(DashboardFragment.globalStringList, this) { record ->
            val intent = Intent(this, GotoActivity::class.java)
            intent.putExtra("time", record.time)
            intent.putExtra("title", record.chinese_name)
            intent.putExtra("image", record.image)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }




    inner class HistoryAdapter(
        private val records: List<BirdDetails>,
        private val context: Context,
        private val onItemClickListener: (BirdDetails) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false)
            return HistoryViewHolder(view)
        }


        //TODO   下面这里  进行  数据绑定！！！
        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            val record = records[position]
            holder.textView_time.text = record.time
            holder.textView_title.text = record.chinese_name
            Glide.with(context)
                .load(record.image)
                .into(holder.textView_image)
            holder.itemView.setOnClickListener { onItemClickListener(record) }
        }

        override fun getItemCount(): Int = records.size

        inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView_time: TextView = itemView.findViewById(R.id.textViewHistoryItem_Time)
            val textView_image: ImageView = itemView.findViewById(R.id.textViewHistoryItem_Image)
            val textView_title: TextView = itemView.findViewById(R.id.textViewHistoryItem_Title)
        }
    }
}

