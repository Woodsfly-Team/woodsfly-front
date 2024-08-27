package com.example.woodsfly

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
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
import okio.Buffer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Base64
import java.net.URLEncoder
import java.nio.charset.Charset


var json_en: Int = 0

class RecordedActivity : AppCompatActivity() {

    private var sdcardfile: File? = null
    private var recorder: MediaRecorder? = null
    private var audioFileAbsolutePath: String? = null
    private var mCurrentAudioUrl: String? = null  // 当前选择的音频文件
    private var audioFilePath: String? = null; // 选择本地文件的文件路径

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        showRecordOptions()

    }

    private fun showRecordOptions() {
        val options = arrayOf("录音", "从文件选择")
        AlertDialog.Builder(this)
            .setTitle("Select an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        setContentView(R.layout.activity_recorded)
                        recordPrepare()
                    }

                    1 -> pickRecordFromGallery()
                }
            }
            .show()
    }



    private fun recordPrepare() {

        val btn_start: Button = findViewById(R.id.btn_start)
        val btn_stop: Button = findViewById(R.id.btn_stop)
        val btn_upload: Button = findViewById(R.id.btn_upload)

        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(RECORD_AUDIO), 1)
        }else if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), 2)
        }else if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(RECORD_AUDIO), 1)
        }
        else {
            getSDCardFile()
            btn_start.setOnClickListener {
                startRecord()
            }
            btn_stop.setOnClickListener {
                stopRecord()
            }
            btn_upload.setOnClickListener {
                btn_upload.isEnabled = false
                // 实例化 UploadHelper，传入回调
                val uploadHelper = RecordXieChengBase64()
                uploadHelper.uploadRecord(audioFileAbsolutePath.toString(), 2, 1) { jsonString ->
                    // 上传成功回调
                    if (jsonString != null) {
                        // 启动 ResultActivity，并传递 jsonData
                        val bundle = Bundle()
                        bundle.putString("JSON_DATA_3", jsonString)
                        val intent = Intent(this, ResultActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    } else {
                        Log.e("Upload Failure", "Failed to upload file")
                    }
                }
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

    // 开始录音
    private fun startRecord() {
        val btn_start: Button = findViewById(R.id.btn_start)
        val btn_stop: Button = findViewById(R.id.btn_stop)

        recorder = MediaRecorder()

        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

        try {
            val file = File.createTempFile("录音_", ".amr", sdcardfile)
            audioFileAbsolutePath = file.absolutePath
            recorder!!.setOutputFile(file)
            recorder!!.prepare()
            recorder!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        btn_start.isEnabled = false
        btn_stop.isEnabled = true
    }

    // 停止录音
    private fun stopRecord() {
        val btn_start: Button = findViewById(R.id.btn_start)
        val btn_stop: Button = findViewById(R.id.btn_stop)
        val btn_upload: Button = findViewById(R.id.btn_upload)

        try {
            recorder!!.stop()
            recorder!!.release()
            recorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        btn_start.isEnabled = false
        btn_stop.isEnabled = false
        btn_upload.isEnabled = true
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

    // 选择音频文件的回调函数
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 4 && resultCode == RESULT_OK) {
            if (data !== null) {
                audioFilePath = getFilePath(this, data.data!!) // 文件路径

                mCurrentAudioUrl = data.data.toString()
                val uploadHelper = RecordXieChengBase64()
                uploadHelper.uploadRecord(audioFilePath.toString(), 2, 1) { jsonString ->
                    // 上传成功回调
                    if (jsonString != null) {
                        val bundle = Bundle()
                        bundle.putString("JSON_DATA_3", jsonString)
                        val intent = Intent(this, ResultActivity::class.java)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    } else {
                        Log.e("Upload Failure", "Failed to upload file")
                    }
                }
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

class RecordXieChengBase64 {

    private val client = OkHttpClient()

    fun uploadRecord(filePath: String, tag: Int, user_id: Int, onComplete: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(filePath)
                val fileContent = file.readBytes() // 读取文件字节
                val base64String = Base64.getEncoder().encodeToString(fileContent) // 转换为Base64字符串

                val jsonObject = JsonObject()
                jsonObject.addProperty("bird_info", "base64String")
                jsonObject.addProperty("tag", tag)
                jsonObject.addProperty("user_id", user_id)

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = RequestBody.create(mediaType, jsonObject.toString())

                val url = "http://59.110.123.151/searchbird/?bird_info=base64String&tag=2&user_id=1"
                //val encodedTag = URLEncoder.encode(tag.toString(), "UTF-8")
                //val encodedUserId = URLEncoder.encode(user_id.toString(), "UTF-8")
                val requestUrl = "$url/?bird_info=&tag=&user_id="
                val bodyContent = body.toString()
                val buffer = Buffer()
                body.writeTo(buffer)
                val requestBodyAsString = buffer.readString(Charset.forName("UTF-8"))

// 打印 RequestBody 的实际内容
                Log.d("RequestBody", requestBodyAsString)
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("User-Agent", "Apify/1.0.0 (https://apifox.com)")
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("Upload Success", "File uploaded successfully with parameters: bird_info = ${jsonObject["bird_info"]}, tag = ${jsonObject["tag"]}, user_id = ${jsonObject["user_id"]}")
                    json_en=3
                    val jsonString = response.body?.string()
                    withContext(Dispatchers.Main) {
                        onComplete(jsonString) // 回调上传结果
                    }
                } else {
                    Log.e("Upload unSuccess", bodyContent)
                    withContext(Dispatchers.Main) {
                        onComplete(null) // 上传失败时回调空
                    }
                }
            } catch (e: Exception) {
                Log.e("Upload Error", e.message ?: "Unknown error")
                withContext(Dispatchers.Main) {
                    onComplete(null) // 出现异常时回调空
                }
            }
        }
    }
}