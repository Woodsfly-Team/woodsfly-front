package com.example.woodsfly


import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class GotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goto)

        val textView_time: TextView = findViewById(R.id.textViewHistoryItem_Time)
        val textView_image: ImageView = findViewById(R.id.textViewHistoryItem_Image)
        val textView_title: TextView = findViewById(R.id.textViewHistoryItem_Title)

        textView_time.text = intent.getStringExtra("time")
        Glide.with(this)
            .load(intent.getStringExtra("image"))
            .into(textView_image)
        textView_title.text = intent.getStringExtra("title")
    }

}
