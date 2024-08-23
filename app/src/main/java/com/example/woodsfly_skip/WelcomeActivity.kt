package com.example.woodsfly_skip

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class WelcomeActivity : ComponentActivity() {
    private lateinit var tvCountdown: TextView
    private lateinit var countDownTimer: CountDownTimer
    private val timeLeftInMillis: Long = 3000 // Set to 3 seconds


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.welcome)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the countdown text view
        tvCountdown = findViewById(R.id.tv_countdown)

        // Start the countdown
        startCountdown();
    }

    private fun startCountdown() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the countdown TextView if needed
                val secondsLeft = (millisUntilFinished / 1000).toInt();
                tvCountdown.text = secondsLeft.toString();
            }

            override fun onFinish() {
                startActivity(Intent(this@WelcomeActivity, HomeActivity::class.java))
                finish();
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 确保倒计时器被取消，以防止内存泄漏
        if (::countDownTimer.isInitialized) { // 检查 countDownTimer 是否已初始化
            countDownTimer.cancel() // 取消计时器
        }
    }
}

//        setContent {
//            MyApplicationTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
