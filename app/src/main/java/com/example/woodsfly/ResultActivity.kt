package com.example.woodsfly

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.woodsfly.ui.dashboard.SearchDetailsResponse
import com.google.gson.Gson
import org.json.JSONObject

/**
 * 搜索结果页面+收藏功能
 *
 * @contributor Karenbluu、zzh0404
 * @Time 2024-08-23
 */

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)

        //接收json
        val bundle = intent.extras
        //val jsonStr1 = bundle?.getString("JSON_DATA_1")//搜索
        val jsonStr2 = bundle?.getString("JSON_DATA_2")//拍照数据
        val jsonStr3 = bundle?.getString("JSON_DATA_3")//录音数据
        val imagePath1 = intent.getStringExtra("imageFile_1")//搜索传的图片路径
        val imagePath2 = intent.getStringExtra("imageFile_2")//拍照传的图片路径
        val imagePath3 = intent.getStringExtra("imageFile_3")//录音传的图片路径

        //初始化收藏按键

        if(json_en==2){
            parseJson(jsonStr2.toString())
            loadImageFromPath(imagePath2.toString())
        }else if(json_en==3){
            parseJson(jsonStr3.toString())
            loadImageFromPath(imagePath3.toString())
        }else if (json_en==1 && bundle != null && bundle.containsKey("JSON_DATA_1")) {
            // 从 Bundle 中获取 JSON 字符串
            val responseJson = bundle.getString("JSON_DATA_1")
            // 使用 Gson 反序列化 JSON 字符串为 SearchDetailsResponse 对象
            val gson = Gson()
            val response: SearchDetailsResponse = gson.fromJson(responseJson, SearchDetailsResponse::class.java)
            parseJson_zjy(response)
            loadImageFromPath(imagePath1.toString())
        }





    }

    //解析拍照和录音传来的json字符串
    private fun parseJson(jsonStr: String){
        json_en=0

        val jsonObject = JSONObject(jsonStr)
        // 获取顶层的 "code" 和 "message"
        val code = jsonObject.getInt("code")
        val message = jsonObject.getString("message")

        // 获取 "data" 对象
        val dataObject = jsonObject.getJSONObject("data")

        // 从 "data" 对象中获取各个字段
        val chineseName = dataObject.getString("chinese_name")
        val englishName = dataObject.getString("english_name")
        val incidence = dataObject.getString("incidence")

        // 获取 "define" 里的嵌套字段
        val defineObject = dataObject.getJSONObject("define")
        val birdOrder = defineObject.getString("bird_order")
        val birdFamily = defineObject.getString("bird_family")
        val birdGenus = defineObject.getString("bird_genus")

        // 其他字段赋值...
        val habitat = dataObject.getString("habitat")
        val introduction = dataObject.getString("introduction")
        val level = dataObject.getString("level")
        val link = dataObject.getString("link")

        textLayout(chineseName, englishName, incidence, birdOrder, birdFamily, birdGenus, habitat, introduction, level)


        var button : Button = findViewById(R.id.link)
        button.setOnClickListener{
            openLink(link)
        }
    }

    //解析搜索传来的数据
    private fun parseJson_zjy(jsonStr: SearchDetailsResponse){
        json_en=0


        val code = jsonStr.code
        val message = jsonStr.message

        // 获取 "data" 对象
        val dataObject = jsonStr.data

        // 从 "data" 对象中获取各个字段
        val chineseName = dataObject.chinese_name
        val englishName = dataObject.english_name
        val incidence = dataObject.incidence
        //val imageUrl = dataObject.image

        // 获取 "define" 里的嵌套字段
        val defineObject = dataObject.define
        val birdOrder = defineObject.bird_order
        val birdFamily = defineObject.bird_family
        val birdGenus = defineObject.bird_genus

        // 其他字段赋值...
        val habitat = dataObject.habitat
        val introduction = dataObject.introduction
        val level = dataObject.level
        val link = dataObject.link


        textLayout(chineseName, englishName, incidence, birdOrder, birdFamily, birdGenus, habitat, introduction, level)


        var button : Button = findViewById(R.id.link)
        button.setOnClickListener{
            openLink(link)
        }
    }






    //TextView控件赋值
    private fun textLayout(chineseName:String, englishName:String, incidence:String, birdOrder:String, birdFamily:String, birdGenus:String, habitat:String, introduction:String, level:String){
        var chineseName0 : TextView = findViewById(R.id.chineseName)
        var englishName0 : TextView = findViewById(R.id.englishName)
        var incidence0 : TextView = findViewById(R.id.incidence)
        var defineObject0 : TextView = findViewById(R.id.defineObject)
        var habitat0 : TextView = findViewById(R.id.habitat)
        var introduction0 : TextView = findViewById(R.id.introduction)
        var level0 : TextView = findViewById(R.id.level)

        chineseName0.text=chineseName
        englishName0.text=englishName
        incidence0.text=incidence
        defineObject0.text="$birdOrder,$birdFamily,$birdGenus"
        habitat0.text=habitat
        introduction0.text=introduction
        level0.text=level

    }

    //加载网络图片
    private fun loadImageFromPath(imagePath: String) {
        var imageView : ImageView = findViewById(R.id.imageUrl)
        if (!imagePath.isNullOrBlank()) {
            // 使用 Glide 加载图片
            Glide.with(this)
                .load(imagePath) // 直接传入图片路径
                .into(imageView)
        } else {
            // 图片路径不存在时的处理逻辑
            Log.e("ResultActivity", "Image path is null or blank")
        }
    }

    //进入百度链接
    private fun openLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // 处理错误，例如提示用户无法打开链接
            Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show()
        }
    }
}