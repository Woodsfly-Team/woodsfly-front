package com.example.woodsfly_skip

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PersonalActivity : AppCompatActivity() {

    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageView4: ImageView
    private lateinit var button2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal)

        button2 = findViewById(R.id.button2)
        imageView2 = findViewById(R.id.imageView2)
        imageView3 = findViewById(R.id.imageView3)
        imageView4 = findViewById(R.id.imageView4)

        button2.setOnClickListener {
            startActivity(Intent(this, PersonalLoginActivity::class.java))
        }

        imageView2.setOnClickListener {
            Toast.makeText(this, "如有任何问题可联系：\n客服邮箱： \n客服电话：",Toast.LENGTH_SHORT).show()
        }

        imageView3.setOnClickListener {
            val intent = Intent(this, PersonalHistoryActivity::class.java)
            startActivity(intent)
        }

        imageView4.setOnClickListener {
            val intent = Intent(this, PersonalSettingsActivity::class.java)
            startActivity(intent)
        }
    }
}