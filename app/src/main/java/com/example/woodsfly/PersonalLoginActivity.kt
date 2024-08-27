 package com.example.woodsfly


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PUT


import retrofit2.Callback

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST

class PersonalLoginActivity : AppCompatActivity() {


    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button
    private lateinit var logoutButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sharedPreferences = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val value: String? = sharedPreferences.getString("key", "default_value")

        // 初始化界面控件
        usernameEditText = findViewById(R.id.tie_account)
        passwordEditText = findViewById(R.id.tie_password)
        registerButton = findViewById(R.id.bt_register)
        loginButton = findViewById(R.id.bt_login)
        logoutButton = findViewById(R.id.button3)


        registerButton.setOnClickListener {
            // 去除用户名和密码的首尾空白字符
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()


            // 在这里添加验证逻辑，例如检查用户名和密码是否为空
            if (username.isEmpty()) {
                // 用户名为空，提示用户
                Toast.makeText(
                    LoginRegistrationActivity@ this,
                    "用户名不能为空",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                // 密码为空，提示用户
                Toast.makeText(LoginRegistrationActivity@ this, "密码不能为空", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            // 如果用户名和密码都通过了验证，调用注册用户的方法
            registerUser(username, password) // 确保这个方法在 LoginRegistrationActivity 类中定义
        }
    }

    private fun registerUser(username: String, password: String) {
        TODO("Not yet implemented")
    }

    // 定义用户数据模型
    data class User(val username: String, val password: String)

    // 定义网络请求接口
    interface ApiService {
        @PUT("api/users/register")
        fun registerUser(@Body user: User): Call<Unit>

        @POST("api/users/push")
        fun loginUser(@Body user: String, password: String): Call<Unit>


        // 定义 Retrofit 实例
        object RetrofitClient {
            private const val BASE_URL =
                "https://apifoxmock.com/m1/4938021-4595545-default/user"

            val instance: ApiService by lazy {
                val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                retrofit.create(ApiService::class.java)
            }
        }
    }
}
