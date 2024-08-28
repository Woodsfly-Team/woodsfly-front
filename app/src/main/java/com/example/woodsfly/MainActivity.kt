package com.example.woodsfly

import android.os.Bundle
import android.view.Window
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.woodsfly.databinding.ActivityMainBinding

/**
 * v-3.0.1
 * 主页面整体规划，导航栏部署
 *
 * @author Karenbluu
 * @Time 2024-08-15
 */

class MainActivity : AppCompatActivity() {

private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置窗口，不需要标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityMainBinding.inflate(layoutInflater)
     setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        //页面布局，主要功能设置规划
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Passing each menu ID as a set of Ids because each

        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))

        navView.setupWithNavController(navController)
    }
}