package com.example.woodsfly.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.woodsfly.R
import com.example.woodsfly.RecordedActivity
import com.example.woodsfly.CameraActivity
import com.example.woodsfly.databinding.FragmentHomeBinding
import com.example.woodsfly.introduce.IntroduceActivity1
import com.example.woodsfly.introduce.IntroduceActivity2
import com.example.woodsfly.introduce.IntroduceActivity3

/**
 * v-3.2.1
 * 主页布局、点击事件
 *
 * @author Karenbluu
 * @Time 2024-08-18
 */

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 按钮点击事件
        binding.bt1.setOnClickListener {
            val intent = Intent(requireContext(), IntroduceActivity1::class.java)
            startActivity(intent)
        }
        // 按钮点击事件
        binding.bt2.setOnClickListener {
            val intent = Intent(requireContext(), IntroduceActivity2::class.java)
            startActivity(intent)
        }
        // 按钮点击事件
        binding.bt3.setOnClickListener {
            val intent = Intent(requireContext(), IntroduceActivity3::class.java)
            startActivity(intent)
        }

        // 麦克风 点击事件
        binding.microphone.setOnClickListener {
            val intent = Intent(requireContext(), RecordedActivity::class.java)
            startActivity(intent)
        }
        // 相机 点击事件
        binding.btCamera.setOnClickListener {
            val intent = Intent(requireContext(), CameraActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}