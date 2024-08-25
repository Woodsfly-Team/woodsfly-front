package com.example.woodsfly.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded

data class BirdDetails(
    val chinese_name: String,
    val english_name: String,
    val incidence: String,
    val image: String,
    val define: Define,  // 嵌入Define数据类
    val habitat: String,
    val introduction: String,
    val level: String,
    val link: String,
    val time: String
)

data class Define(
    val bird_order: String,
    val bird_family: String,
    val bird_genus: String
)
