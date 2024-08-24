package com.example.woodsfly.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.example.woodsfly.PersonalHistoryActivity
import com.example.woodsfly.PersonalLoginActivity
import com.example.woodsfly.PersonalSettingsActivity
import com.example.woodsfly.R
import com.example.woodsfly.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageView4: ImageView
    private lateinit var button2: Button

private var _binding: FragmentNotificationsBinding? = null
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding get() = _binding!!

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

    _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
    val root: View = binding.root

    val textView: TextView = binding.textNotifications

    return root
  }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 假设 fragment_mine.xml 已经包含了 activity_personal 中定义的控件
        imageView2 = view.findViewById(R.id.imageView2)
        imageView3 = view.findViewById(R.id.imageView3)
        imageView4 = view.findViewById(R.id.imageView4)
        button2 = view.findViewById(R.id.button2)

        imageView2.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "如有任何问题可联系：\n客服邮箱： \n客服电话：",
                Toast.LENGTH_SHORT
            ).show()
        }

        imageView3.setOnClickListener {
            val intent = Intent(requireContext(), PersonalHistoryActivity::class.java)
            startActivity(intent)
        }

        imageView4.setOnClickListener {
            val intent = Intent(requireContext(), PersonalSettingsActivity::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener {
            val intent = Intent(requireActivity(), PersonalLoginActivity::class.java)
        }
    }
}