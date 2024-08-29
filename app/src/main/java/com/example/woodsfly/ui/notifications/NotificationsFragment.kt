package com.example.woodsfly.ui.notifications

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.example.woodsfly.PersonalHistoryActivity
import com.example.woodsfly.PersonalLoginActivity
import com.example.woodsfly.R
import com.example.woodsfly.StarHistoryActivity
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener

/**
 * 通知界面的Fragment，用于展示和处理应用内的通知功能
 * @contributor zoeyyyy-git Xiancaijiang
 * @date 2024.8.27
 */

private val FragmentActivity.RESULT_OK: Any?
    get() {
        TODO("Not yet implemented")
    }

class NotificationsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var imageView: ImageView
    private lateinit var imageView2: LinearLayout
    private lateinit var imageView3: LinearLayout
    private lateinit var imageView4: LinearLayout
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var tv_take_pictures: Button // 拍照按钮
    private lateinit var tv_open_album: Button // 相册按钮


    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_PERMISSION_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 为Fragment创建视图
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // 初始化 SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", MODE_PRIVATE)

        super.onViewCreated(view, savedInstanceState)

        // 初始化视图
        imageView = view.findViewById(R.id.imageView) // 设置头像图片控件的点击事件，弹出权限请求对话框
        imageView2 = view.findViewById(R.id.imageView2)// 设置客服联系方式点击区域的点击事件，显示客服信息提示
        imageView3 = view.findViewById(R.id.imageView3)// 设置个人历史记录点击区域的点击事件，跳转到个人历史记录页面
        imageView4 = view.findViewById(R.id.imageView4)// 设置收藏记录点击区域的点击事件，跳转到收藏记录页面
        button2 = view.findViewById(R.id.button2)// 设置注册/登录按钮的点击事件，跳转到注册/登录页面
        button3 = view.findViewById(R.id.button3)// 设置退出登录按钮的点击事件，跳转到登录页面
        val user_id = sharedPreferences.getString("user_id", "")

        if (user_id?.isEmpty() == true) {
            button2.setText("注册登录")
            button2.isEnabled = true
        } else {
            button2.setText(user_id)
            button2.isEnabled = false
        }
        /**
         * 初始化Fragment中所有的视图控件
         *
         * @param view Fragment的视图
         */
        imageView.setOnClickListener {
            showPermissionDeniedDialog()
        }



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
        // Set click listener for imageView3


        imageView4.setOnClickListener {
            val intent = Intent(requireContext(), StarHistoryActivity::class.java)
            startActivity(intent)
        }
        // Set click listener for imageView3


        button2.setOnClickListener {
            val intent = Intent(requireContext(), PersonalLoginActivity::class.java)
            startActivity(intent)
        }



        button3.setOnClickListener {


            with(sharedPreferences.edit()) {
                putString("account", "")
                putString("password", "")
                putString("user_id", "")
                putString("browsing_history", "")

                // 清除收藏记录，使用正确的键名
                putString("favorite_items", "")
                apply()
            }

            val intent = Intent(requireContext(), PersonalLoginActivity::class.java)
            startActivity(intent)
        }
    }




    override fun onResume() {
        super.onResume()
        val account = sharedPreferences.getString("account", "")
        if (account?.isEmpty() == true) {
            button2.setText("注册/登录")
            button2.isEnabled = true
        } else {
            button2.setText(account)
            button2.isEnabled = false
        }
    }


    /**
     * 显示权限请求对话框，让用户选择拍照或从相册选择图片
     */
    private fun showPermissionDeniedDialog() {
        // 创建自定义对话框提示用户需要权限
        val view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_bottom, null)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        builder.setTitle("选择方式")
        builder.setMessage("请选择相册或者拍照")
        builder.setNegativeButton("取消", null)
        val create = builder.create()
        // 初始化拍照和相册按钮
        var tv_take_pictures: Button
        var tv_open_album: Button
        tv_take_pictures = view.findViewById(R.id.tv_take_pictures)
        tv_open_album = view.findViewById(R.id.tv_open_album)

        // 设置拍照按钮的点击事件，使用PictureSelector库打开相机
        tv_take_pictures.setOnClickListener {
            PictureSelector.create(this)
                .openCamera(SelectMimeType.ofImage())
                .forResultActivity(object : OnResultCallbackListener<LocalMedia?> {
                    override fun onResult(result: ArrayList<LocalMedia?>) {
                        val get = result.get(0)
                        activity?.let { it1 -> Glide.with(it1).load(get?.path).into(imageView) }
                    }

                    override fun onCancel() {
                    }
                })
            create.dismiss()
        }

        // 设置相册按钮的点击事件，使用PictureSelector库打开系统相册
        tv_open_album.setOnClickListener {
            PictureSelector.create(this)
                .openSystemGallery(SelectMimeType.ofImage())
                .forSystemResultActivity(object : OnResultCallbackListener<LocalMedia?> {
                    override fun onResult(result: ArrayList<LocalMedia?>) {
                        val get = result.get(0)
                        activity?.let { it1 -> Glide.with(it1).load(get?.path).into(imageView) }
                    }

                    override fun onCancel() {
                    }
                })
            create.dismiss()
        }
        create.show()

    }


}
