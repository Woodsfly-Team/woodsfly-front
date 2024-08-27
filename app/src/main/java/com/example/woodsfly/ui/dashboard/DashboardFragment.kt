package com.example.woodsfly.ui.dashboard


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.woodsfly.data.BirdDetails
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
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
/**搜索页面完善中……*/

class DashboardFragment : Fragment() {

    // 定义DashboardFragment类，继承自Fragment

    private lateinit var searchEditText: AutoCompleteTextView // 声明一个AutoCompleteTextView变量，用于搜索输入
    private lateinit var apiService: ApiService // 声明一个ApiService变量，用于网络请求
    private lateinit var searchImageView: ImageView// 删除按钮
    // 定义两组要循环显示的字符串数组（每日精选）
    private val texts1 = arrayOf("我是秃头鹰，也叫Bald Eagle", "我是金翅雀，也叫American Goldfinch", "我是红尾鹰,也叫Red-Tailed Hawk",
        "我是绿头鸭，也可以叫我Mallard",
        "我是大蓝鹭，也可以叫我Great Blue Heron","我是雪莉鸫，也可以叫我 Cedar Waxwing","我是冠山雀，也可以叫我Tufted Titmouse",
        "我是普通潜鸟,也可以叫我Common Loon","我是卡罗莱纳雀，也可以叫我Carolina Wren","我是鸻鹬，也可以叫我Killdeer","我是蓝松鸦，也可以叫我Blue Jay", "我是鱼鹰，也可以叫我Osprey", "我是红喉蜂鸟，也可以叫我 Ruby-Throated Hummingbird",
        "我是卡罗莱纳山雀， 也可以叫我 Carolina Chickadee", "我是林鸭，也可以叫我Wood Duck",
        "我是腰带翠鸟，也可以叫我 Belted Kingfisher", "我是白头鹰，也可以叫我Snowy Owl", "我是加拿大鹅，也可以叫我Canadian Goose", "我是烟囱雨燕，也可以叫我Chimney Swift",
        "我是红腹啄木鸟，也可以叫我Red-Bellied Woodpecker" )
    private val texts2 = arrayOf("我是蓝松鸦，也可以叫我Blue Jay", "我是鱼鹰，也可以叫我Osprey", "我是红喉蜂鸟，也可以叫我 Ruby-Throated Hummingbird",
        "我是卡罗莱纳山雀，也可以叫我 Carolina Chickadee", "我是林鸭，也可以叫我Wood Duck", "我是歌雀麻雀，也可以叫我Song Sparrow", "我是北部啄木鸟，也可以叫我Northern Flicker", "我是东蓝鸟，也可以叫我Eastern Bluebird","我是东蓝鸟，也可以叫我Eastern Bluebird","我是雕鸮，也可以叫我Great Horned Owl",
        "我是紫金雀，也可以叫我 Purple Finch","我是秃头鹰，也可以叫我Bald Eagle", "我是金翅雀，也可以叫我American Goldfinch","我是红尾鹰，也可以叫我Red-Tailed Hawk", "我是绿头鸭，也可以叫我Mallard","我是大蓝鹭，也可以叫我Great Blue Heron",
        "我是旋木鸟，也可以叫我Baltimore Oriole","我是斑纹枭，也可以叫我 Barred Owl","我是绣眼雀，也可以叫我House Finch","我是美国知更鸟，也可以叫我American Robin","我是北美红雀，也可以叫我Northern Cardinal")
    private var currentTextIndex1 = 0
    private var currentTextIndex2 = 0
    // Handler用于定时任务
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? { // 重写onCreateView方法，用于创建视图
        // Inflate the layout for this fragment // 加载布局文件
        return inflater.inflate(R.layout.fragment_dashboard, container, false) // 返回加载的布局视图
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // 重写onViewCreated方法，用于设置视图
        super.onViewCreated(view, savedInstanceState) // 调用父类的onViewCreated方法
        searchEditText = view.findViewById(R.id.searchEditText) // 通过ID找到搜索输入框
        searchImageView=view.findViewById(R.id.searchImageView)//定义删除按钮
        val image02 = view.findViewById<ImageView>(R.id.image02)//鸟界明星按钮，夜鹭
        val image03 = view.findViewById<ImageView>(R.id.image03)//鸟界明星按钮，翠鸟
        val image04 = view.findViewById<ImageView>(R.id.image04)//鸟界明星按钮，戴胜鸟
        val image05 = view.findViewById<ImageView>(R.id.image05)//鸟界明星按钮，珠颈斑鸠
        // 获取TextView
        val textView1 = view.findViewById<TextView>(R.id.textViewImageBackground)//对话框1
        val textView2 = view.findViewById<TextView>(R.id.textViewImageBackground2)//对话框2

        // 设置初始文本
        textView1.text = texts1[currentTextIndex1]
        textView2.text = texts2[currentTextIndex2]

        // 定义Runnable来更换文本（每日精选）
        val runnable = object : Runnable {
            override fun run() {
                // 更新索引并循环
                currentTextIndex1 = (currentTextIndex1 + 1) % texts1.size
                currentTextIndex2 = (currentTextIndex2 + 1) % texts2.size

                // 更新TextView文本
                textView1.text = texts1[currentTextIndex1]
                textView2.text = texts2[currentTextIndex2]

                // 1分钟更新一次
                handler.postDelayed(this, 60 * 1000)
            }
        }

        // 启动定时任务
        handler.post(runnable)

        // 设置点击事件监听器
        // 鸟界明星的四个按钮，因为之前接口可以传输名字，早上测试可以跳转，但是现在是id号，不知道具体id，暂时注释了
        image02.setOnClickListener {
            // 执行点击image02的操作，比如调用搜索方法
           // searchDetails("夜鹭")
        }
        image03.setOnClickListener {
            // 执行点击image03的操作
            //searchDetails("翠鸟")
        }
        image04.setOnClickListener {
            // 执行点击image04的操作
            //searchDetails("戴胜")
        }
        image05.setOnClickListener {
            // 执行点击image05的操作
          //  searchDetails("珠颈斑鸠")
        }



        // 初始化Retrofit // 初始化网络请求框架
        val retrofit = Retrofit.Builder()
            .baseUrl("https://apifoxmock.com/m1/4938021-4595545-default/") // 设置Retrofit的baseUrl
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
            searchDetails(selectedBirdInfo.bird_id) // 调用searchDetails方法，传入鸟类中文名
            // 点击搜索建议后自动调用删除按钮的功能
            searchImageView.performClick()
        }
    }

    private fun search(query: String) { // 定义search方法，用于搜索
        Log.d("API_CALL", "Sending search request for: $query")
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
                    Toast.makeText(requireContext(), "Error1: ${response.message()}", Toast.LENGTH_SHORT).show() // 显示错误信息
                }
            }

            override fun onFailure(call: Call<MatchInfoResponse>, t: Throwable) { // 请求失败时调用
                Log.e("API", "Errors: ${t.message}") // 打印错误日志
                Toast.makeText(requireContext(), "Errors2: ${t.message}", Toast.LENGTH_SHORT).show() // 显示错误信息
            }
        })
    }

    private fun searchDetails(query: Int) { // 定义searchDetails方法，用于搜索详细信息
        Log.d("API_CALL", "Sending search request for: $query")
        apiService.searchBird(query).enqueue(object : Callback<SearchDetailsResponse> { // 调用ApiService的searchBird方法，并设置回调
            override fun onResponse(call: Call<SearchDetailsResponse>, response: Response<SearchDetailsResponse>) { // 请求成功时调用
                Log.d("API", "Response: ${response.body()}") // 打印响应体
                if (response.isSuccessful) { // 如果请求成功
                   // Log.d("Upload Success", bird_id)
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
                    Toast.makeText(requireContext(), "Search Error1: ${response.message()}", Toast.LENGTH_SHORT).show() // 显示搜索错误信息
                }
            }

            override fun onFailure(call: Call<SearchDetailsResponse>, t: Throwable) { // 请求失败时调用
                Toast.makeText(requireContext(), "Search Error2: ${t.message}", Toast.LENGTH_SHORT).show() // 显示搜索错误信息
            }
        })
    }
}

 // 定义API服务接口
interface ApiService {
    @GET("search?bird_name=")//搜索匹配接口，返回搜索列表
    fun matchInfo(@Query("bird_name") bird_name: String): Call<MatchInfoResponse>

    @POST("search/?bird_id=")//搜索接口。返回详细信息
        fun searchBird(@Query("bird_id") bird_id: Int): Call<SearchDetailsResponse>
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
    val bird_id:Int,
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

