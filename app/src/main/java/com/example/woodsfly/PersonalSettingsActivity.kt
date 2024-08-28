package com.example.woodsfly

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.woodsfly.http.BaseApiResult
import com.example.woodsfly.http.LoginResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
/**
 * 个人设置界面，用于设置和保存账号密码。
 *
 * @author zoeyyyy-git
 * @Time 2024-8-28
 */
class PersonalSettingsActivity : AppCompatActivity() {

    private lateinit var accountEditText: EditText // 账号输入框
    private lateinit var passwordEditText: EditText// 密码输入框
    private lateinit var saveButton: Button// 保存设置按钮
    private lateinit var sharedPreferences: SharedPreferences// 用于存储用户信息的SharedPreferences
    /**
     * 活动创建时调用，初始化界面和数据
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        accountEditText = findViewById(R.id.tietSettingsAccount)
        passwordEditText = findViewById(R.id.tietSettingsPassword)
        saveButton = findViewById(R.id.buttonSaveSettings)

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // 从SharedPreferences加载已有账号密码
        loadAccountAndPassword()

        saveButton.setOnClickListener {
            saveAccountAndPassword()
        }
    }
    /**
     * 从SharedPreferences加载已有账号密码
     */
    private fun loadAccountAndPassword() {
        val savedAccount = sharedPreferences.getString("account", "")
        val savedPassword = sharedPreferences.getString("password", "")
        accountEditText.setText(savedAccount)
        passwordEditText.setText(savedPassword)
    }
    /**
     * 保存账号密码到SharedPreferences，并尝试注册用户
     */
    private fun saveAccountAndPassword() {
        val account = accountEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (account.isNotEmpty() && password.isNotEmpty()) {
            with(sharedPreferences.edit()) {
                putString("account", account)
                putString("password", password)
                apply()
            }

            registerUser(account, password) // 确保这个方法在 LoginRegistrationActivity 类中定义

        } else {
            Toast.makeText(this, "账号和密码不能为空", Toast.LENGTH_SHORT).show()
        }
    }
    /**
     * 注册用户，向服务器发送账号密码信息
     *
     * @param username 用户名
     * @param password 密码
     */
    private fun registerUser(username: String, password: String) {
        val client = OkHttpClient()
        // 定义URL
        val url = "https://apifoxmock.com/m1/4938021-4595545-default/user"

        // 使用FormBody构建器添加注册所需的参数
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()

        //  // 构建网络请求对象，设置请求方式为PUT
        val request = Request.Builder()
            .url(url)
            .put(formBody)
            .build()

        // 执行网络请求并异步处理结果
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                // 当网络请求遇到错误时调用
                Log.i("请求", "onFailure: " + e.printStackTrace())
                runOnUiThread {
                    // 在UI线程中显示错误信息
                    Toast.makeText(
                        this@PersonalSettingsActivity,
                        "注册失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                // 当网络请求成功返回时调用
                response.body?.let { responseBody ->
                    // 确保响应体不为空
                    val responseString = responseBody.string()
                    // 使用Gson库将JSON格式的响应体转换为BaseApiResult对象
                    val result: BaseApiResult<LoginResult> = Gson().fromJson(
                        responseString,
                        object : TypeToken<BaseApiResult<LoginResult?>?>() {}.type
                    )
                    Log.i("请求", "onResponse: ${result.data.user_id}")
                    runOnUiThread {
                        // 在UI线程中处理UI更新
                        runOnUiThread {
                            // 检查HTTP响应码是否表示成功
                            Toast.makeText(
                                this@PersonalSettingsActivity,
                                "注册成功",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        // 注册成功后关闭当前活动
                        finish()
                    }
                } ?: run {
                    // 如果响应体为空，则在UI线程中显示错误信息
                    runOnUiThread {
                        Toast.makeText(
                            this@PersonalSettingsActivity,
                            "注册失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }

        })

    }

}