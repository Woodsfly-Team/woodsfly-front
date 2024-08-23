package com.example.woodsfly_skip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.woodsfly_skip.R
import android.widget.*
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

interface SearchApiService {
    // 定义 API 接口
    @GET("searchbird")

    suspend fun search(@Query("name") query: String): Response<List<Bird>>
}

class DiscoverFragment : Fragment() {
    private lateinit var searchEditText: EditText    //搜索框
    private lateinit var searchButton: ImageView     //搜索按钮

    // 定义 Retrofit 实例和 SearchApiService
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.10.24.130:4523/m1/4938021-4595545-default/") // 修复了 URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val searchApiService: SearchApiService = retrofit.create(SearchApiService::class.java)
    private lateinit var lifecycleScope: CoroutineScope // 定义协程作用域

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_discover, container, false)
        // 初始化控件
        searchEditText =view. findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchImageView)

        // 定义协程作用域
        lifecycleScope = CoroutineScope(Dispatchers.Main)

        // 设置搜索按钮的点击事件
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            if (query.isNotEmpty()) {
                performSearch(query)

            }
        }
        return inflater.inflate(R.layout.fragment_discover, container, false)


    }
    private fun performSearch(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = searchApiService.search(query)
                if (response.isSuccessful) {
                    val results = response.body()
                    withContext(Dispatchers.Main) {
                        if (results.isNullOrEmpty()) {
                            Toast.makeText(requireContext(), "没有找到结果", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            // 使用 FragmentTransaction 替换 Fragment
                            val searchResultsFragment =
                                SearchResultsFragment.newInstance(ArrayList(results))
                            val fragmentManager = requireActivity().supportFragmentManager
                            val fragmentTransaction = fragmentManager.beginTransaction()
                            fragmentTransaction.replace(R.id.search_results_list_view, searchResultsFragment) // 替换成你的容器 ID
                            fragmentTransaction.commit()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "请求失败: ${response.code()} ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace() // 打印异常堆栈跟踪
                    Toast.makeText(
                        requireContext(),
                        "无法查询到结果！！！: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}