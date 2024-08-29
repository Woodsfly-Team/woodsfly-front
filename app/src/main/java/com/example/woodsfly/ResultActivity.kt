package com.example.woodsfly

import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.ui.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.woodsfly.ui.dashboard.ApiService
import com.example.woodsfly.ui.dashboard.SearchDetailsResponse
import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

/**
 * 搜索结果页面+收藏功能
 *
 * @contributor Karenbluu zzh0404
 * @Time 2024-08-23
 */

class ResultActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var isFavorited = false // 用于保存收藏状态
    private lateinit var chineseName: String
    private lateinit var englishName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)

        // 初始化 SharedPreferences
        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        //接收json
        val bundle = intent.extras
        //val jsonStr1 = bundle?.getString("JSON_DATA_1")//搜索
        val jsonStr2 = bundle?.getString("JSON_DATA_2")//拍照数据
        val jsonStr3 = bundle?.getString("JSON_DATA_3")//录音数据
        val imagePath1 = intent.getStringExtra("imageFile_1")//搜索传的图片路径
        val imagePath2 = intent.getStringExtra("imageFile_2")//拍照传的图片路径
        val imagePath3 = intent.getStringExtra("imageFile_3")//录音传的图片路径

        // 设置收藏按钮的点击事件,更新收藏记录
        val star: Button = findViewById(R.id.star)

        // 自动保存浏览记录
        saveBrowsingHistory(bundle)

        // 收藏按钮事件
        star.setOnClickListener {
            toggleFavorite(star)
        }

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

        // 保存收藏项时使用
        saveFavoriteItem(chineseName, englishName)
    }

    //加载网络图片
    private fun loadImageFromPath(imagePath: String) {
        Log.d("ResultActivity", imagePath)
        var imageView : ImageView = findViewById(R.id.imageUrl)
        if (imagePath.isNotBlank()) {
            // 确保文件存在
            val file = File(imagePath)
            if (file.exists()) {
                // 使用 Glide 加载本地文件
                Glide.with(this)
                    .load(file)
                    .into(imageView)
            } else {
                Log.e("ResultActivity", "Image file does not exist: $imagePath")
                // 处理文件不存在的情况
            }
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

    // 保存浏览记录
    private fun saveBrowsingHistory(bundle: Bundle?) {
        val editor = sharedPreferences.edit()
        val browsingHistory = sharedPreferences.getString("browsing_history", "")?.split("\n")?.toMutableList() ?: mutableListOf()

        // 将新的浏览记录添加到列表头部
        if (bundle != null && bundle.containsKey("JSON_DATA_1")) {
            val responseJson = bundle.getString("JSON_DATA_1")
            val gson = Gson()
            val response: SearchDetailsResponse = gson.fromJson(responseJson, SearchDetailsResponse::class.java)
            val chineseName = response.data.chinese_name
            val englishName = response.data.english_name
            browsingHistory.add(0, "$chineseName ($englishName)")
        }

        // 保持最多 100 条浏览记录
        if (browsingHistory.size > 100) {
            browsingHistory.removeAt(browsingHistory.size - 1)
        }

        // 更新浏览记录
        editor.putString("browsing_history", browsingHistory.joinToString("\n"))
        editor.apply()
    }

    // 收藏和取消收藏鸟类
    private fun toggleFavorite(star: Button) {
        Log.d("ResultActivity", "toggleFavorite called. isFavorited: $isFavorited")
        isFavorited = !isFavorited
        val newBackground = if (isFavorited) R.drawable.collect_button else R.drawable.cha01
        star.setBackgroundResource(newBackground) // 更改按钮形状
        Log.d("ResultActivity", "Button background changed. New background: $newBackground")
        // 确保不调用 finish() 或其他导致 Activity 结束的方法
    }

    private fun saveFavoriteItem(chineseName: String, englishName: String) {
        val editor = sharedPreferences.edit()
        val favorites = sharedPreferences.getString("favorite_items", "")?.split("\n")?.toMutableList() ?: mutableListOf()

        // 将新的收藏记录添加到列表头部
        favorites.add(0, "$chineseName ($englishName)")

        // 保持最多 100 条收藏记录
        if (favorites.size > 100) {
            favorites.removeAt(favorites.size - 1)
        }

        editor.putString("favorite_items", favorites.joinToString("\n"))
        editor.apply()
    }

    private fun removeFavoriteItem(chineseName: String, englishName: String) {
        val editor = sharedPreferences.edit()
        val favorites = sharedPreferences.getString("favorite_items", "")?.split("\n")?.toMutableList() ?: mutableListOf()

        // 删除指定的收藏记录
        favorites.remove("$chineseName ($englishName)")

        editor.putString("favorite_items", favorites.joinToString("\n"))
        editor.apply()
    }



    /**
     * 浏览与收藏记录 后端交互数据类
     *
     * @contributor Karenbluu
     * @Time 2024-08-29
     */

    data class NetworkResponse(
        val code: Int,
        val message: String,
        val data: Data?
    )

    data class Data(
        val star_id: Int? = null,
        val browse_id: Int? = null
    )
}
