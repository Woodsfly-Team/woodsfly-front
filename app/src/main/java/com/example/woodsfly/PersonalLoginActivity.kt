package com.example.woodsfly


import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
 * 个人登录界面
 *
 * @author zoeyyyy-git
 * @Time 2024-8-28
 */
class PersonalLoginActivity : AppCompatActivity() {


    private lateinit var usernameEditText: EditText// 用户名输入框
    private lateinit var passwordEditText: EditText// 密码输入框
    private lateinit var registerButton: TextView // 注册按钮
    private lateinit var loginButton: Button// 登录按钮
    private lateinit var sharedPreferences: SharedPreferences // 用于存储用户信息的SharedPreferences

    /**
     * 构造方法
     *
     * @param savedInstanceState 保存状态的Bundle对象
     */
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)

        // 初始化界面控件
        usernameEditText = findViewById(R.id.tie_account)
        passwordEditText = findViewById(R.id.tie_password)
        registerButton = findViewById(R.id.bt_register)
        loginButton = findViewById(R.id.bt_login)
        // 从SharedPreferences加载已有账号密码
        loadAccountAndPassword()


        registerButton.setOnClickListener {
            val intent = Intent(this, PersonalSettingsActivity::class.java)
            startActivity(intent)

        }

        loginButton.setOnClickListener {
            val account = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // 检查账号和密码是否为空
            if (account.isEmpty() || password.isEmpty()) {
                // 如果账号或密码为空，提示用户
                Toast.makeText(this, "账号和密码不能为空", Toast.LENGTH_SHORT).show()
            } else {
                // 账号和密码不为空，继续检查用户是否已经注册
                if (sharedPreferences.contains("user_id")) {
                    // 用户已注册，可以进行登录操作
                    with(sharedPreferences.edit()) {
                        putString("account", account)
                        putString("password", password)
                        apply()
                    }
                    // 这里假设 registerUser 方法实际是处理登录的逻辑
                    registerUser(account, password)
                } else {
                    // 用户未注册，提示用户先注册
                    Toast.makeText(this, "您还没有注册，请先注册账号！", Toast.LENGTH_LONG).show()
                    // 启动注册界面的Intent
                    val intent = Intent(this, PersonalSettingsActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * 注册用户
     *
     * @param username 用户名
     * @param password 密码
     */
    private fun registerUser(username: String, password: String) {
        val client = OkHttpClient()
        // 定义API的URL
        val url = "https://apifoxmock.com/m1/4938021-4595545-default/user"

        // 使用FormBody构建器添加注册所需的参数
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("password", password)
            .build()

        // 构建网络请求对象，设置请求方式为POST
        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        // 异步执行网络请求
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.i("请求", "onFailure: " + e.printStackTrace())
                runOnUiThread {
                    Toast.makeText(
                        this@PersonalLoginActivity,
                        "登录失败",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    val result: BaseApiResult<LoginResult> = Gson().fromJson(
                        responseString,
                        object : TypeToken<BaseApiResult<LoginResult?>?>() {}.type
                    )
                    Log.i("请求", "onResponse: ${result.data.user_id}")
                    with(sharedPreferences.edit()) {
                        putString("user_id", result.data.user_id)
                        apply()
                    }
                    runOnUiThread {
                        runOnUiThread {
                            Toast.makeText(
                                this@PersonalLoginActivity,
                                "登录成功",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        finish()
                    }
                } ?: run {
                    runOnUiThread {
                        Toast.makeText(
                            this@PersonalLoginActivity,
                            "登录失败",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }

        })

    }
    /**
     * 从SharedPreferences加载已有账号密码。
     * 如果SharedPreferences中存储有账号密码，则自动填充到输入框中。
     */
    private fun loadAccountAndPassword() {
        // 从SharedPreferences获取账号和密码
        val savedAccount = sharedPreferences.getString("account", "")
        val savedPassword = sharedPreferences.getString("password", "")
        // 将获取到的账号密码填充到输入框中
        usernameEditText.setText(savedAccount)
        passwordEditText.setText(savedPassword)
    }

}




