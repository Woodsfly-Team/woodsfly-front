package com.example.woodsfly


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.woodsfly.ui.notifications.NotificationsFragment

class PersonalLoginActivity : AppCompatActivity() {

    private lateinit var accountEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        accountEditText = findViewById(R.id.tie_account)
        passwordEditText = findViewById(R.id.tie_password)
        loginButton = findViewById(R.id.bt_login)

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE) // 应用设置的私有存储

        loginButton.setOnClickListener {
            performLogin()
        }
    }
    private fun performLogin() {
        val account = accountEditText.text.toString()
        val password = passwordEditText.text.toString()

        // 从SharedPreferences获取设置的账号和密码
        val savedAccount = sharedPreferences.getString("account", "default_account")
        val savedPassword = sharedPreferences.getString("password", "default_password")

        if (isValidCredentials(account, password, savedAccount, savedPassword)) {
            // 假设登录凭证有效，跳转到主界面
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, NotificationsFragment::class.java))
            finish() // 关闭登录界面
        } else {
            // 登录凭证无效，提示用户
            Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show()
        }
    }

    // 验证账号密码是否与SharedPreferences中保存的账号密码匹配
    private fun isValidCredentials(username: String, password: String, savedUsername: String?, savedPassword: String?): Boolean {
        return savedUsername == username && savedPassword == password
    }
}



