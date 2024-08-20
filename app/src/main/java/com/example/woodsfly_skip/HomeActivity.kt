package com.example.woodsfly_skip

import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.IOException

class HomeActivity : ComponentActivity() {
    //microphone
    private var sdcardfile : File? = null
    private var recorder : MediaRecorder? = null
    //camera
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private var imageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        showBeginButton()     //调用microphone
        showPhotoButton()    //调用camera

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                imageUri?.let {
                    uploadPhoto(it)
                }
            }
        }

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                uploadPhoto(it)
            }
        }
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    //fun for camera
    private fun showBeginButton(){
        setContentView(R.layout.activity_home)
        val microphone : Button = findViewById(R.id.microphone)
        microphone.setOnClickListener {
            showRecordOptions()
        }
    }

    private fun showRecordOptions() {
        val options = arrayOf("录音", "从文件选择")
        AlertDialog.Builder(this)
            .setTitle("Select an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        setContentView(R.layout.activity_recorder)
                        recordPrepare()
                    }
                    1 -> pickRecordFromGallery()
                }
            }
            .show()
    }

    private fun recordPrepare(){
        val btn_start : Button = findViewById(R.id.btn_start)
        val btn_stop : Button = findViewById(R.id.btn_stop)


        if(ContextCompat.checkSelfPermission(this,RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf<String>(RECORD_AUDIO),1)
        }else if(ContextCompat.checkSelfPermission(this,WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf<String>(WRITE_EXTERNAL_STORAGE),2)
        }else {
            getSDCardFile()

            btn_start.setOnClickListener {
                startRecord()
                startCounting()
            }
            btn_stop.setOnClickListener{
                stopRecord()
                stopCounting()
            }
        }

    }


    @SuppressLint("Range")
    private fun pickRecordFromGallery(){
        if(ContextCompat.checkSelfPermission(this,READ_MEDIA_AUDIO)!= PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf<String>(READ_MEDIA_AUDIO),3)
        }else{
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("audio/*")
            startActivityForResult(intent, 1)
        }
    }



    private fun getSDCardFile(){
        val tv_path : TextView = findViewById(R.id.tv_path)
        if(Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED){
            sdcardfile = getExternalFilesDir(null)
            tv_path.text = sdcardfile.toString()
        }
    }

    //开始录音
    private fun startRecord(){
        val btn_start : Button = findViewById(R.id.btn_start)
        val btn_stop : Button = findViewById(R.id.btn_stop)

        recorder = MediaRecorder()

        //设置音频源
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        //设置输出格式
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        //设置音频编码
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try{
            val file = File.createTempFile("录音_",".amr",sdcardfile)
            //设置录音保存路径
            recorder!!.setOutputFile(file)
            //准备和启动录制音频
            recorder!!.prepare()
            recorder!!.start()
        }catch (e : IOException){
            e.printStackTrace()
        }
        btn_start.isEnabled = false
        btn_stop.isEnabled = true

    }



    fun startCounting() {
        var time_recorder : Int = 0
        val tv_recording_time : TextView = findViewById(R.id.tv_recording_time)
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                // 要做的事情，这里再次调用此 Runnable 对象，以实现每秒实现一次的定时器操作
                time_recorder++
                tv_recording_time.text = "录音时长: $time_recorder s"
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(runnable, 1000)

    }

    fun stopCounting() {

    }


    //停止录音
    private fun stopRecord(){
        val btn_start : Button = findViewById(R.id.btn_start)
        val btn_stop : Button = findViewById(R.id.btn_stop)
        try{
            recorder!!.stop()
            recorder!!.release()
            recorder = null
        }catch (e:Exception){
            e.printStackTrace()
        }
        btn_start.isEnabled = true
        btn_stop.isEnabled = false

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            recordPrepare()
        }else if(requestCode == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            recordPrepare()
        }else if(requestCode == 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            pickRecordFromGallery()
        }else{

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val uri = data?.data
            // 处理选择的音频文件
        }
    }

    private fun uploadAudio(){

    }

    //fun for camera
    private fun showPhotoButton(){
        setContentView(R.layout.activity_home)
        val microphone : Button = findViewById(R.id.bt_camera)
        microphone.setOnClickListener {
            showPhotoOptions()
        }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Take Photo", "Pick from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Select an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> capturePhoto()
                    1 -> pickPhotoFromGallery()
                }
            }
            .show()
    }

    private fun capturePhoto() {
        val uri = createImageUri()
        imageUri = uri
        takePictureLauncher.launch(uri)
    }

    private fun pickPhotoFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun createImageUri(): Uri {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "new_photo")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    private fun uploadPhoto(uri: Uri) {
        // Implement your photo upload logic here
    }

}

