package com.example.woodsfly.ui.dashboard


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.woodsfly.databinding.FragmentDashboardBinding
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import com.example.woodsfly.R
import com.example.woodsfly.ResultActivity
import com.example.woodsfly.json_en
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.Query


class DashboardFragment : Fragment() {
    companion object {
        val globalStringList: MutableList<BirdDetails> = mutableListOf()
    }
    private lateinit var searchEditText: AutoCompleteTextView // 声明一个AutoCompleteTextView变量，用于搜索输入
    private lateinit var apiService: ApiService // 声明一个ApiService变量，用于网络请求
    private lateinit var searchImageView: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // 重写onCreateView方法，用于创建视图
        // Inflate the layout for this fragment // 加载布局文件
        return inflater.inflate(R.layout.fragment_dashboard, container, false) // 返回加载的布局视图
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // 重写onViewCreated方法，用于设置视图
        super.onViewCreated(view, savedInstanceState) // 调用父类的onViewCreated方法
        searchEditText = view.findViewById(R.id.searchEditText) // 通过ID找到搜索输入框
        searchImageView=view.findViewById(R.id.searchImageView)//定义删除按钮


        // 初始化Retrofit // 初始化网络请求框架
        val retrofit = Retrofit.Builder()
            .baseUrl("http://59.110.123.151:80/") // 设置Retrofit的baseUrl
            .addConverterFactory(GsonConverterFactory.create()) // 添加Gson转换工厂
            .build() // 构建Retrofit实例

        apiService = retrofit.create(ApiService::class.java) // 创建ApiService实例

        // 设置输入监听 // 设置搜索输入框的监听器
        searchEditText.addTextChangedListener(object : TextWatcher { // 使用匿名类实现TextWatcher接口
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {} // 文本变化前调用

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { // 文本变化时调用
                Log.d("EditText", "Text changed: $s") // 打印日志
                if (s?.isNotEmpty() == true) { // 如果文本不为空
                    search(s.toString()) // 调用search方法
                }
            }

            override fun afterTextChanged(s: Editable?) {} // 文本变化后调用
        })
        //删除按钮功能实现
        searchImageView.setOnClickListener {
            searchEditText.setText("") // 清除搜索框内的文字
        }

        // 设置搜索建议列表点击监听
        searchEditText.setOnItemClickListener { parent, view, position, id ->
            val selectedBirdInfo = parent.getItemAtPosition(position) as BirdInfo // 获取点击的搜索建议
            searchDetails(selectedBirdInfo.chinese_name) // 调用searchDetails方法，传入鸟类中文名
        }
    }

    private fun search(query: String) { // 定义search方法，用于搜索
        apiService.matchInfo(query).enqueue(object : Callback<MatchInfoResponse> { // 调用ApiService的matchInfo方法，并设置回调
            override fun onResponse(call: Call<MatchInfoResponse>, response: Response<MatchInfoResponse>) { // 请求成功时调用
                Log.d("API", "Response: ${response.body()}") // 打印响应体
                if (response.isSuccessful) { // 如果请求成功
                    val suggestions = response.body()?.data // 获取搜索建议
                    suggestions?.let {
                        searchEditText.setAdapter(SimpleAdapter(requireContext(), it)) // 设置搜索建议的适配器
                        searchEditText.showDropDown() // 显示下拉列表
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.message()}", Toast.LENGTH_SHORT).show() // 显示错误信息
                }
            }

            override fun onFailure(call: Call<MatchInfoResponse>, t: Throwable) { // 请求失败时调用
                Log.e("API", "Error: ${t.message}") // 打印错误日志
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show() // 显示错误信息
            }
        })
    }

    private fun searchDetails(birdName: String) { // 定义searchDetails方法，用于搜索详细信息
        apiService.searchBird(birdName, 0, 1).enqueue(object : Callback<SearchDetailsResponse> { // 调用ApiService的searchBird方法，并设置回调
            override fun onResponse(call: Call<SearchDetailsResponse>, response: Response<SearchDetailsResponse>) { // 请求成功时调用
                Log.d("API", "Response: ${response.body()}") // 打印响应体
                if (response.isSuccessful) { // 如果请求成功
                    Log.d("Upload Success", birdName)
                    val details : SearchDetailsResponse? = response.body()// 获取详细信息
                    if (details != null) {
                        val gson = Gson()
                        val responseJson = gson.toJson(details)
                        json_en=1
                        // 创建 Intent 用于启动 ResultActivity
                        val intent = Intent(requireContext(), ResultActivity::class.java)
                        // 创建 Bundle 实例
                        val bundle = Bundle()
                        // 将详情数据放入 Bundle，这里以字符串形式存储，实际也可以序列化对象
                        bundle.putString("JSON_DATA_1", responseJson)
                        // 将 Bundle 放入 Intent
                        intent.putExtras(bundle)

                        // 启动 ResultActivity
                        startActivity(intent)
                    }
                } else {
                    Log.e("Upload Failure", "Failed to upload file")
                    Toast.makeText(requireContext(), "Search Error: ${response.message()}", Toast.LENGTH_SHORT).show() // 显示搜索错误信息
                }
            }

            override fun onFailure(call: Call<SearchDetailsResponse>, t: Throwable) { // 请求失败时调用
                Toast.makeText(requireContext(), "Search Error: ${t.message}", Toast.LENGTH_SHORT).show() // 显示搜索错误信息
            }
        })
    }
}

// API Service Interface // 定义API服务接口
interface ApiService {
    @POST("matchinfo/") // 定义POST请求的URL
    fun matchInfo(@Query("bird_info") bird_info: String): Call<MatchInfoResponse> // 定义matchInfo方法，用于发送请求

    @POST("searchbird/") // 定义POST请求的URL
    fun searchBird(@Query("bird_info") bird_info: String, @Query("tag") tag: Int, @Query("user_id") user_id: Int): Call<SearchDetailsResponse> // 定义searchBird方法，用于发送请求
}

// Response Data Class for Match Info // 定义MatchInfoResponse数据类，用于接收响应数据
data class MatchInfoResponse( // 定义MatchInfoResponse数据类
    val code: Int,
    val message: String,
    val data: List<BirdInfo>
)

data class BirdInfo( // 定义BirdInfo数据类，用于接收鸟类信息
    val chinese_name: String,
    val english_name: String,
    val define: Define
)

data class Define( // 定义Define数据类，用于接收鸟类定义信息
    val bird_order: String,
    val bird_family: String,
    val bird_genus: String
)

// 定义SearchDetailsResponse数据类，用于接收响应数据
data class SearchDetailsResponse( // 定义SearchDetailsResponse数据类
    val code: Int,
    val message: String,
    val data: BirdDetails
)

data class BirdDetails( // 定义BirdDetails数据类，用于接收鸟类详细信息
    val chinese_name: String,
    val english_name: String,
    val incidence: String,
    val image: String,
    val define: Define,
    val habitat: String,
    val introduction: String,
    val level: String,
    val link: String
)

// Simple Adapter for AutoCompleteTextView // 定义SimpleAdapter类，用于AutoCompleteTextView的适配器
class SimpleAdapter(context: Context, private val birdInfoList: List<BirdInfo>) : ArrayAdapter<BirdInfo>(context, android.R.layout.simple_dropdown_item_1line, birdInfoList) { // 定义SimpleAdapter类，继承自ArrayAdapter
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View { // 重写getView方法，用于设置下拉列表的视图
        val view = super.getView(position, convertView, parent) // 调用父类的getView方法
        view.findViewById<TextView>(android.R.id.text1).text = birdInfoList[position].chinese_name // 设置下拉列表项的文本
        return view // 返回视图
    }
}

