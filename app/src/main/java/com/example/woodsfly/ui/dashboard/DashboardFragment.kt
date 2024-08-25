package com.example.woodsfly.ui.dashboard


import android.content.Context
import android.os.Bundle
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
import android.widget.Toast
import com.example.woodsfly.R
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

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root





        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    private lateinit var searchEditText: AutoCompleteTextView
    private lateinit var apiService: ApiService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchEditText = view.findViewById(R.id.searchEditText)

        // 初始化Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.207.83:4523/m1/4938021-4595545-default/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        // 设置输入监听
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("EditText", "Text changed: $s")
                if (s?.isNotEmpty() == true) {
                    search(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 设置搜索建议列表点击监听
        searchEditText.setOnItemClickListener { parent, view, position, id ->
            val selectedBirdInfo = parent.getItemAtPosition(position) as BirdInfo
            searchDetails(selectedBirdInfo.chinese_name)
        }
    }

    private fun search(query: String) {
        apiService.matchInfo(query).enqueue(object : Callback<MatchInfoResponse> {
            override fun onResponse(call: Call<MatchInfoResponse>, response: Response<MatchInfoResponse>) {
                Log.d("API", "Response: ${response.body()}")
                if (response.isSuccessful) {
                    val suggestions = response.body()?.data
                    suggestions?.let {
                        searchEditText.setAdapter(SimpleAdapter(requireContext(), it))
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MatchInfoResponse>, t: Throwable) {
                Log.e("API", "Error: ${t.message}")
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun searchDetails(birdName: String) {
        apiService.searchBird(birdName, 0, 1).enqueue(object : Callback<SearchDetailsResponse> {
            override fun onResponse(call: Call<SearchDetailsResponse>, response: Response<SearchDetailsResponse>) {
                if (response.isSuccessful) {
                    val details = response.body()?.data
                    details?.let {
                        Toast.makeText(requireContext(), "Details for: ${it.chinese_name}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Search Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SearchDetailsResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Search Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

// API Service Interface
interface ApiService {
    @POST("matchinfo/") // 定义POST请求的URL
    fun matchInfo(@Query("bird_info") bird_info: String): Call<MatchInfoResponse> // 定义matchInfo方法，用于发送请求

    @POST("searchbird/") // 定义POST请求的URL
    fun searchBird(@Query("bird_info") bird_info: String, @Query("tag") tag: Int, @Query("user_id") user_id: Int): Call<SearchDetailsResponse> // 定义searchBird方法，用于发送请求
}

// Response Data Class for Match Info
data class MatchInfoResponse( // 定义MatchInfoResponse数据类，用于接收响应数据
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


// Response Data Class for Search Details
data class SearchDetailsResponse( // 定义SearchDetailsResponse数据类，用于接收响应数据
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


// Simple Adapter for AutoCompleteTextView
class SimpleAdapter(context: Context, private val birdInfoList: List<BirdInfo>) : ArrayAdapter<BirdInfo>(context, android.R.layout.simple_dropdown_item_1line, birdInfoList) { // 定义SimpleAdapter类，继承自ArrayAdapter
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View { // 重写getView方法，用于设置下拉列表的视图
        val view = super.getView(position, convertView, parent) // 调用父类的getView方法
        view.findViewById<TextView>(android.R.id.text1).text = birdInfoList[position].chinese_name // 设置下拉列表项的文本
        return view // 返回视图

    }
}