package com.example.woodsfly.ui.notifications

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.woodsfly.PersonalHistoryActivity
import com.example.woodsfly.PersonalLoginActivity
import com.example.woodsfly.PersonalSettingsActivity
import com.example.woodsfly.R


private val FragmentActivity.RESULT_OK: Any?
    get() {
        TODO("Not yet implemented")
    }

class NotificationsFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageView4: ImageView
    private lateinit var button2: Button
    private lateinit var tv_take_pictures: Button // 拍照按钮
    private lateinit var tv_open_album: Button // 相册按钮

    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_PERMISSION_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        imageView = view.findViewById(R.id.imageView)
        imageView2 = view.findViewById(R.id.imageView2)
        imageView3 = view.findViewById(R.id.imageView3)
        imageView4 = view.findViewById(R.id.imageView4)
        button2 = view.findViewById(R.id.button2)

        // Set click listener for the avatar ImageView
        imageView.setOnClickListener {
            openGallery()
        }

        // Set click listener for imageView2
        imageView2.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "如有任何问题可联系：\n客服邮箱： \n客服电话：",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Set click listener for imageView3
        imageView3.setOnClickListener {
            val intent = Intent(requireContext(), PersonalHistoryActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for imageView4
        imageView4.setOnClickListener {
            val intent = Intent(requireContext(), PersonalSettingsActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for button2
        button2.setOnClickListener {
            val intent = Intent(requireContext(), PersonalLoginActivity::class.java)
            startActivity(intent)
        }
    }

    // ... 继续 NotificationsFragment 类的定义 ...


    private fun openGallery() {
        // 检查存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // 请求存储权限
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_CODE
            )
        } else {
            // 存储权限已被授予，打开相册
            pickImageFromGallery()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 如果权限被授予，打开相册
                pickImageFromGallery()
            } else {
                // 权限被拒绝，显示提示
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        // 创建自定义对话框提示用户需要权限
        val view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_bottom, null)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setTitle("权限请求")
        builder.setMessage("为了访问相册，我们需要存储权限。请在设置中开启权限。")
        builder.setPositiveButton("去设置") { dialog, _ ->


        }
        builder.setNegativeButton("取消", null)
        builder.create().show()
    }

    private fun pickImageFromGallery() {
        // 创建打开相册的Intent
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*" // 限制只选择图片
        // 确保Intent可以被处理
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            // 启动相册选择Intent
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

// ...


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST) { // 使用常量直接判断请求码
            if (resultCode == FragmentActivity.RESULT_OK && data != null) { // 使用FragmentActivity.RESULT_OK
                val imageUri = data.data
                if (imageUri != null) {
                    try {
                        imageView.setImageURI(imageUri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            requireActivity(),
                            "Failed to load the image.",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                }

            }
        }

    }
}
