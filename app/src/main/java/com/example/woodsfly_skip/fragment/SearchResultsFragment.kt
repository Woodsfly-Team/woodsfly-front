package com.example.woodsfly_skip.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.woodsfly_skip.R


class BirdAdapter(private val context: Context, private val birds: List<Bird>) :
    ArrayAdapter<Bird>(context, 0, birds) {

    override fun getView(position: Int, convertView: View?, parentView: ViewGroup): View {
        // 重用已有的视图或创建新的视图
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_bird, parentView, false)

        // 获取当前位置的Bird对象
        val bird = getItem(position)

        // 通过findViewById获取TextView，并设置文本信息
        val textViewBirdName: TextView = view.findViewById(R.id.textViewBirdName)
        val textViewBirdIntroduction: TextView = view.findViewById(R.id.textViewBirdIntroduction)

        textViewBirdName.text = bird?.name
        textViewBirdIntroduction.text = bird?.introduction

        // 如果您想为ImageView设置图片，您可以这样做：
        // val imageViewBird: ImageView = view.findViewById(R.id.imageViewBird)
        // imageViewBird.setImageResource(bird.imageResourceId) // 假设您有一个字段imageResourceId

        return view
    }
}
 class SearchResultsFragment : Fragment() {

    private lateinit var searchResultsListView: ListView
    private lateinit var birdAdapter: BirdAdapter
    private var searchResults: ArrayList<Bird> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_results, container, false)
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)

         // Initialize ListView
         searchResultsListView = view.findViewById(R.id.search_results_list_view)

         // Get the search results from arguments
         // 使用 getParcelableArrayList 替换 getSerializable
         searchResults = arguments?.getParcelableArrayList<Bird>("BIRD_SEARCH_RESULTS")
             ?: ArrayList()

         // Initialize the adapter with the Fragment's context
         birdAdapter = BirdAdapter(requireContext(), searchResults)
         searchResultsListView.adapter = birdAdapter
     }

     companion object {
         @JvmStatic
         fun newInstance(searchResults: java.util.ArrayList<Bird>): Fragment {
             val fragment = SearchResultsFragment()
             val args = Bundle()
             // 这里不需要转换类型，直接传递 ArrayList<Bird>
             args.putParcelableArrayList("BIRD_SEARCH_RESULTS", searchResults)
             fragment.arguments = args
             return fragment
         }
     }
 }