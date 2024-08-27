package com.example.woodsfly.ui.home


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.woodsfly.R
import com.example.woodsfly.RecordedActivity
import com.example.woodsfly.CameraActivity
import com.example.woodsfly.databinding.FragmentHomeBinding


//全局变量，麦克风功能后续页面转换
var en = 0

/**
 * 搜索结果页面
 *
 * @contributor Karenbluu、   、
 * @Time 2024-08-16
 */

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var columnCount = 1


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 调用功能
        val microphone: Button = view.findViewById(R.id.microphone)
        val bt_camera: Button = view.findViewById(R.id.bt_camera)

        microphone.setOnClickListener {
            val intent = Intent(requireContext(), RecordedActivity::class.java)
            startActivity(intent)
        }

        bt_camera.setOnClickListener {
            val intent = Intent(requireContext(), CameraActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //fun for microphone



}

