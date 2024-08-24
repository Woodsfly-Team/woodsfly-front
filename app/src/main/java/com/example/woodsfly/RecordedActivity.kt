package com.example.woodsfly

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.woodsfly.ui.home.en
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Base64
import okhttp3.Response

class RecordedActivity : AppCompatActivity() {

    private var sdcardfile: File? = null
    private var recorder: MediaRecorder? = null
    private var audioFileAbsolutePath: String? = null
    private var mCurrentAudioUrl: String? = null  // 当前选择的音频文件
    private var audioFilePath: String? = null; // 选择本地文件的文件路径


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_recorded)

        if(en==0){
            recordPrepare()
        }else if(en==1){
            pickRecordFromGallery()
        }



    }


    private fun recordPrepare() {

        val btn_start: Button = findViewById(R.id.btn_start)
        val btn_stop: Button = findViewById(R.id.btn_stop)
        val btn_upload: Button = findViewById(R.id.btn_upload)

        if (ContextCompat.checkSelfPermission(
                this,
                RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf<String>(RECORD_AUDIO), 1)
        } else if (ContextCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf<String>(WRITE_EXTERNAL_STORAGE), 2)
        } else {
            getSDCardFile()
            btn_start.setOnClickListener {
                startRecord()
            }
            btn_stop.setOnClickListener {
                stopRecord()
            }
            btn_upload.setOnClickListener {
                val agdafg = RecordXieChengBase64()
                agdafg.uploadRecord(audioFileAbsolutePath.toString())
                // uploadRecord2(audioFileAbsolutePath.toString())
            }
        }
    }


    @SuppressLint("Range")
    private fun pickRecordFromGallery() {
        if (Build.VERSION.SDK_INT >= 6.0) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf<String>(READ_MEDIA_AUDIO), 3)
            } else {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setType("audio/*")
                startActivityForResult(intent, 4)
            }

        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    this,
                    WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf<String>(READ_EXTERNAL_STORAGE), 3)
            } else {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setType("audio/*")
                startActivityForResult(intent, 4)
            }

        }
    }


    private fun getSDCardFile() {
        val tv_path: TextView = findViewById(R.id.tv_path)
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            sdcardfile = getExternalFilesDir(null)
            tv_path.text = sdcardfile.toString()
        }
    }


    //开始录音
    private fun startRecord() {
        val btn_start: Button = findViewById(R.id.btn_start)
        val btn_stop: Button = findViewById(R.id.btn_stop)

        recorder = MediaRecorder()

        //设置音频源
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        //设置输出格式
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        //设置音频编码
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            val file = File.createTempFile("录音_", ".amr", sdcardfile)
            //设置录音保存路径
            audioFileAbsolutePath = file.absolutePath
            recorder!!.setOutputFile(file)
            //准备和启动录制音频
            recorder!!.prepare()
            recorder!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        btn_start.isEnabled = false
        btn_stop.isEnabled = true
    }

    //停止录音
    private fun stopRecord() {
        val btn_start: Button = findViewById(R.id.btn_start)
        val btn_stop: Button = findViewById(R.id.btn_stop)
        try {
            recorder!!.stop()
            recorder!!.release()
            recorder = null
        } catch (e: Exception) {
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
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recordPrepare()
        } else if (requestCode == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recordPrepare()
        } else if (requestCode == 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickRecordFromGallery()
        } else {

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    //选择本地音频的回调函数
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 4 && resultCode == RESULT_OK) {
            if (data !== null) {
                audioFilePath = getFilePath(this, data.data!!) // 文件路径

                mCurrentAudioUrl = data.data.toString()
                val agdafg = RecordXieChengBase64()
                agdafg.uploadRecord(audioFileAbsolutePath.toString())
            }
        }
    }


    private fun getFilePath(context: Context, uri: Uri): String? {
        try {
            val returnCursor: Cursor? =
                context.getContentResolver().query(uri, null, null, null, null)
            val nameIndex: Int = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name: String = returnCursor.getString(nameIndex)
            val file = File(context.getFilesDir(), name)
            val inputStream: InputStream? = context.getContentResolver().openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream!!.available()
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream!!.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            returnCursor.close()
            inputStream.close()
            outputStream.close()
            return file.getPath()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}


class RecordXieChengBase64 : AppCompatActivity(), CoroutineScope by MainScope() {
    // 使用主线程的CoroutineScope，注意在Activity销毁时取消协程
    private val job = SupervisorJob()
    val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val client = OkHttpClient()

    fun uploadRecord(filePath: String) {
        launch(Dispatchers.IO) {
            try {
                val file = File(filePath)
                val fileContent = file.readBytes() // 读取文件字节
                val base64String = Base64.getEncoder().encodeToString(fileContent) // 转换为Base64字符串

                val base64Body = RequestBody.create("text/plain".toMediaType(), base64String)
                //val body = MultipartBody.Builder()
                //    .setType(MultipartBody.FORM)
                //    .addFormDataPart("record_file", file.name, base64Body)
                //    .build()

                val request = Request.Builder()
                    .url("http://10.0.2.2:4523/m1/4938021-4595545-default/searchbird/")
                    .post(base64Body)
                    .addHeader("User-Agent", "Apifox/1.0.0 (https://apifox.com)")
                    .addHeader("Accept", "*/*")
                    .addHeader("Host", "10.0.2.2:4523")
                    .addHeader("Connection", "keep-alive")
                    .build()

                val response : Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("Upload Success", "File uploaded successfully")
                    println("成功")
                    val jsonString = response.body.toString()
                    Log.d("JSON Response", jsonString);
                } else {
                    Log.e("Upload Failure", "Failed to upload file")
                }

                withContext(Dispatchers.Main) {
                    // 在这里处理UI更新或结果展示
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // 在这里处理异常，例如显示错误信息
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // 取消所有协程
    }
}