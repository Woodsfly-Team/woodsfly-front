package com.example.woodsfly_skip

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
/**
 * 个人设置活动类，允许用户修改和保存账号密码。
 *
 * 用户可以通过此界面输入或更新他们的账号和密码，并将这些信息保存到SharedPreferences中。
 *
 * @author zoeyyyy-git
 * @Time 2024-08-21
 */
class PersonalSettingsActivity : AppCompatActivity() {

    private lateinit var accountEditText: EditText// 定义EditText组件，用于输入账号
    private lateinit var passwordEditText: EditText// 定义EditText组件，用于输入密码
    private lateinit var saveButton: Button// 定义Button组件，用于触发保存账号密码的操作
    private lateinit var sharedPreferences: SharedPreferences// 定义SharedPreferences组件，用于访问和修改应用设置
    /**
     * 活动创建时调用的方法，初始化界面和事件监听。
     *
     * @param savedInstanceState 保存的实例状态，可用于恢复活动状态
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        accountEditText = findViewById(R.id.tietSettingsAccount)
        passwordEditText = findViewById(R.id.tietSettingsPassword)
        saveButton = findViewById(R.id.buttonSaveSettings)

        sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)// 获取应用设置的SharedPreferences


        loadAccountAndPassword() // 从SharedPreferences加载已有账号密码

        saveButton.setOnClickListener {
            saveAccountAndPassword()
        }
    }
    private fun loadAccountAndPassword() {
        val savedAccount = sharedPreferences.getString("account", "")
        val savedPassword = sharedPreferences.getString("password", "")
        accountEditText.setText(savedAccount)
        passwordEditText.setText(savedPassword)
    }

    private fun saveAccountAndPassword() {
        val account = accountEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (account.isNotEmpty() && password.isNotEmpty()) {
            with (sharedPreferences.edit()) {
                putString("account", account)
                putString("password", password)
                apply()
            }
            Toast.makeText(this, "账号和密码已保存", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "账号和密码不能为空", Toast.LENGTH_SHORT).show()
        }
    }
}