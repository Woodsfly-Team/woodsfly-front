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

import com.example.woodsfly.ui.dashboard.DashboardFragment


class PersonalHistoryActivity : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var back: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)


    }


        recyclerView = findViewById(R.id.recyclerViewHistory)
        back = findViewById(R.id.back)
        back.setOnClickListener { finish() }
    }


}

