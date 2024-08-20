package com.example.woodsfly_skip

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PersonalSettingsActivity : AppCompatActivity() {

    private lateinit var accountEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences

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