package com.example.woodsfly_skip

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class PersonalActivity : AppCompatActivity() {


    private lateinit var imageView: ImageView
    private val PICK_IMAGE_REQUEST = 1
    private val REQUEST_PERMISSION_CODE = 100

    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var imageView4: ImageView
    private lateinit var button2: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal)


        imageView = findViewById(R.id.imageView)
        imageView.setOnClickListener {
            openGallery()
        }

        button2 = findViewById(R.id.button2)
        imageView2 = findViewById(R.id.imageView2)
        imageView3 = findViewById(R.id.imageView3)
        imageView4 = findViewById(R.id.imageView5)

        button2.setOnClickListener {
            startActivity(Intent(this, PersonalLoginActivity::class.java))
        }

        imageView2.setOnClickListener {
            Toast.makeText(this, "如有任何问题可联系：\n客服邮箱： \n客服电话：", Toast.LENGTH_SHORT)
                .show()
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


    private fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_PERMISSION_CODE)
        } else {
            pickImageFromGallery()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                // Permission was denied, show an error message
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                try {
                    val path = getPathFromUri(this, imageUri)
                    imageView.setImageBitmap(BitmapFactory.decodeFile(path))
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Handle the error, possibly show a message to the user
                }
            }
        }
    }

    // Helper method to get real path from URI
    private fun getPathFromUri(context: Context, uri: Uri): String {
        var path = ""
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        if (cursor != null) {
            path = column_index?.let { cursor.getString(it) }.toString()
        }
        if (cursor != null) {
            cursor.close()
        }
        return path
    }










}