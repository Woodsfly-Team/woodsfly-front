package com.example.woodsfly

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
import android.media.MediaCodec
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import com.google.gson.JsonObject

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.widget.SeekBar
import java.nio.ByteBuffer
import java.util.UUID

//跳转结果页，1查询，2拍照，3录音
var json_en: Int = 0

/**
 * v-3.0.1
 * 录音功能
 * @author zzh
 * @Time 2024-08-27
 */
class RecordedActivity : AppCompatActivity() {

    private var sdcardfile: File? = null
    private var recorder: MediaRecorder? = null
    private var audioFileAbsolutePath: String? = null
    private var mCurrentAudioUrl: String? = null  // 当前选择的音频文件
    private var audioFilePath: String? = null; // 选择本地文件的文件路径
    private var crop_begin: Long = 0
    private var crop_end: Long = 0
    private val mediaPlayer = MediaPlayer()
    private var AUDIO_TOTAL_DURATION_MS= 30000L
    private var audioFileAbsolutePath_crop: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        showRecordOptions()

    }

    //录音和本地上传的选项
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


    //准备录音，权限，按钮
    private fun recordPrepare() {

        val btn_start: Button = findViewById(R.id.btn_start)
        val btn_stop: Button = findViewById(R.id.btn_stop)
        val btn_upload: Button = findViewById(R.id.btn_upload)
        val btn_play: Button = findViewById(R.id.btn_play)
        val btn_playend: Button = findViewById(R.id.btn_playend)
        val btn_crop: Button = findViewById(R.id.btn_crop)
        val btn_crop_end: Button = findViewById(R.id.btn_crop_end)
        var chronometer: Chronometer = findViewById(R.id.chronometer)

        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(RECORD_AUDIO), 1)
        } else {
            getSDCardFile()
            btn_start.setOnClickListener {
                startRecord()
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.start()
            }
            btn_stop.setOnClickListener {
                stopRecord()
                chronometer.stop()
                btn_crop.isEnabled=true
            }
            btn_play.setOnClickListener {
                playRecording(audioFileAbsolutePath.toString())
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.start()
                btn_playend.isEnabled = true
            }
            btn_playend.setOnClickListener {
                stopPlaying()
                chronometer.stop()
                btn_playend.isEnabled = false
                btn_play.isEnabled = true
            }
            btn_crop.setOnClickListener {
                btn_crop.isEnabled=false
                btn_crop_end.isEnabled=true
                btn_play.isEnabled=false
                btn_playend.isEnabled=false
                btn_upload.isEnabled=false
                val file = File.createTempFile("录音_裁剪", ".amr", sdcardfile)
                audioFileAbsolutePath_crop = file.absolutePath
                AUDIO_TOTAL_DURATION_MS=getRecordingDuration(audioFileAbsolutePath.toString())
                initSeekBar()
            }
            btn_crop_end.setOnClickListener {
                btn_play.isEnabled=true
                btn_upload.isEnabled=true
                btn_crop_end.isEnabled=false

                cropRecording(audioFileAbsolutePath.toString(), audioFileAbsolutePath_crop.toString(), crop_begin, crop_end)
                audioFileAbsolutePath = audioFileAbsolutePath_crop
            }
            btn_upload.setOnClickListener {
                btn_upload.isEnabled = false
                chronometer.base = SystemClock.elapsedRealtime()
                // 实例化 UploadHelper，传入回调
                val uploadHelper = RecordXieChengBase64()
                uploadHelper.uploadRecord(audioFileAbsolutePath.toString(), 2, 1,"amr") { jsonString, imageFile ->
                    // 上传成功回调
                    if (jsonString != null && imageFile != null) {
                        val bundle = Bundle()
                        bundle.putString("JSON_DATA_3", jsonString)
                        bundle.putString("imageFile_3", imageFile.absolutePath)
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

    //选择音频文件
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

    //手动录音的文件路径
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
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
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
        val btn_play: Button = findViewById(R.id.btn_play)
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
        btn_play.isEnabled = true
    }

    //获取录音时长
    private fun getRecordingDuration(filePath: String): Long {
        Log.d("crop", "获取录音时长")
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
            return duration ?: 0L // 如果无法获取时长，返回 0
        } catch (e: Exception) {
            e.printStackTrace()
            return 0L // 如果发生异常，返回 0
        } finally {
            retriever.release() // 确保释放资源
        }
    }

    // 播放录音
    private fun playRecording(audioFilePath: String) {
        Log.d("crop", "播放录音")
        try {
            val btn_play: Button = findViewById(R.id.btn_play)
            val btn_playend: Button = findViewById(R.id.btn_playend)
            var chronometer: Chronometer = findViewById(R.id.chronometer)
            btn_play.isEnabled = false
            // 设置数据源
            mediaPlayer.setDataSource(audioFilePath)
            mediaPlayer.prepareAsync() // 异步准备 MediaPlayer

            // 注册 OnPreparedListener 来知道何时准备完成
            mediaPlayer.setOnPreparedListener {
                // 准备完成后开始播放
                mediaPlayer.start()
                Log.d("play Success", "开始播放")
            }

            // 设置播放完成的监听器
            mediaPlayer.setOnCompletionListener {
                // 播放完成时的处理，例如停止播放或更新 UI
                mediaPlayer.stop()
                mediaPlayer.reset()
                chronometer.stop()
                btn_play.isEnabled = true
                btn_playend.isEnabled=false
                Toast.makeText(this, "播放完成", Toast.LENGTH_SHORT).show()
                Log.d("crop", "播放完成")
            }

            // 设置错误监听器
            mediaPlayer.setOnErrorListener { mp, what, extra ->
                // 处理播放错误
                Toast.makeText(this, "播放错误: $what, $extra", Toast.LENGTH_LONG).show()
                false // 我们已处理了错误，MediaPlayer 不需要停止播放
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "无法播放文件: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // 停止播放录音
    private fun stopPlaying() {
        Log.d("crop", "停止播放")
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
        }
    }

    // 裁剪录音
    private fun cropRecording(sourcePath: String, outputPath: String, startTimeUs: Long, endTimeUs: Long) {
        Log.d("crop", "裁剪录音")
        Log.d("crop", "$startTimeUs,$endTimeUs")
        val extractor = MediaExtractor()
        extractor.setDataSource(sourcePath)

        val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

        var trackIndex = -1
        var format: MediaFormat? = null

        // 寻找音频轨道
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)?.startsWith("audio/",true) == true) {
                trackIndex = i
                break
            }
        }

        if (trackIndex == -1) {
            throw RuntimeException("No audio track found in the source file.")
        }

        extractor.selectTrack(trackIndex)
        format = extractor.getTrackFormat(trackIndex)

        val cropTrackIndex = muxer.addTrack(format)
        muxer.start()

        val buffer = ByteBuffer.allocate(1024)
        var sampleTimeUs: Long
        var sampleSize: Int

        try {
            while (true) {
                sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) {
                    break
                }
                sampleTimeUs = extractor.getSampleTime()

                if (sampleTimeUs >= startTimeUs && sampleTimeUs <= endTimeUs) {
                    // 调整时间戳以匹配新文件的起始时间
                    val adjustedTimeUs = sampleTimeUs - startTimeUs
                    muxer.writeSampleData(cropTrackIndex, buffer, MediaCodec.BufferInfo().apply {
                        presentationTimeUs = adjustedTimeUs
                        size = sampleSize
                        offset = 0
                        flags = 0
                    })
                }

                extractor.advance()
            }
        } catch (e: Exception) {
            Log.d("crop", "裁剪异常")
            e.printStackTrace()
        } finally {
            extractor.release()
            muxer.stop()
            muxer.release()
        }
        Log.d("crop", "裁剪完成")
    }

    //选择裁剪时间
    private fun initSeekBar() {
        Log.d("crop", "选择裁剪时间")
        var seekBar : SeekBar = findViewById(R.id.seekBar)
        var startTimeTextView : TextView = findViewById(R.id.startTimeTextView)
        var endTimeTextView : TextView = findViewById(R.id.endTimeTextView)
        seekBar.max = (AUDIO_TOTAL_DURATION_MS / 10).toInt() // 例如，每 10 毫秒一个刻度
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    crop_begin = getStartTimeFromProgress(progress)
                    crop_end = getEndTimeFromProgress(progress)
                    updateTextViews(crop_begin, crop_end)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // 用户开始拖动 SeekBar 时的处理
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // 用户停止拖动 SeekBar 时的处理
            }
        })

        // 初始化 TextViews
        updateTextViews(getStartTimeFromProgress(seekBar.progress), getEndTimeFromProgress(seekBar.progress))
    }

    private fun getStartTimeFromProgress(progress: Int): Long {
        var seekBar : SeekBar = findViewById(R.id.seekBar)
        // 起始时间 = 总时长 * (进度 / SeekBar的最大值)
        return (AUDIO_TOTAL_DURATION_MS * progress / seekBar.max.toLong()) * 1000L
    }

    private fun getEndTimeFromProgress(progress: Int): Long {
        var seekBar : SeekBar = findViewById(R.id.seekBar)
        // 结束时间 = 总时长 * ((进度 + 1) / SeekBar的最大值)，因为进度条的范围是 0 到 max-1
        return (AUDIO_TOTAL_DURATION_MS * (seekBar.max.toLong()-1) / seekBar.max.toLong()) * 1000L
    }

    private fun updateTextViews(startTimeUs: Long, endTimeUs: Long) {
        var startTimeTextView : TextView = findViewById(R.id.startTimeTextView)
        var endTimeTextView : TextView = findViewById(R.id.endTimeTextView)
        startTimeTextView.text = formatTimeUs(startTimeUs)
        endTimeTextView.text = formatTimeUs(endTimeUs)
    }

    private fun formatTimeUs(timeUs: Long): String {
        // 将微秒转换为 "mm:ss:SSS" 格式的字符串
        val milliseconds = timeUs / 1000
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingMilliseconds = milliseconds % 1000
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d:%03d", minutes, remainingSeconds, remainingMilliseconds)
    }
    //权限请求的回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
                uploadHelper.uploadRecord(audioFilePath.toString(), 2, 1,"mpeg") { jsonString, imageFile ->
                    // 上传成功回调
                    if (jsonString != null && imageFile != null) {
                        val bundle = Bundle()
                        bundle.putString("JSON_DATA_3", jsonString)
                        bundle.putString("imageFile_3", imageFile.absolutePath)
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

    //本地音频的路径
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

/**
 * v-3.0.1
 * 录音接口
 * @author zzh
 * @Time 2024-08-27
 */
class RecordXieChengBase64 {

    private val client1 = OkHttpClient()
    private val client2 = OkHttpClient()
    fun uploadRecord(
        filePath: String,
        tag: Int,
        user_id: Int,
        audioType: String,
        onComplete: (String?, File?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(filePath)
                val fileContent = file.readBytes() // 读取文件字节
                val filePart = MultipartBody.Part.createFormData(
                    "file", // 服务器端接收文件的字段名
                    file.name,
                    RequestBody.create(
                        "audio/$audioType".toMediaType(),
                        fileContent
                    ) // 假设音频文件是MPEG格式
                )
                // 创建 MultipartBody.Builder 并添加音频文件部分
                val builder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(filePart)

                val url1 = "http://59.110.123.151:80/predict?tag=2&user_id=1"

                val request1 = Request.Builder()
                    .url(url1)
                    .post(builder.build())
                    .addHeader("User-Agent", "Apify/1.0.0 (https://apifox.com)")
                    .build()

                val response1 = client1.newCall(request1).execute()

                if (response1.isSuccessful) {
                    Log.d("Upload Success1", "File uploaded successfully")
                    json_en = 3
                    val jsonString = response1.body?.string()
                    //解析json获取image
                    val jsonObject = jsonString?.let { JSONObject(it) }
                    val dataObject = jsonObject?.getJSONObject("data")
                    val file_id = dataObject?.getInt("image")
                    //第二次请求
                    val url2 = "http://59.110.123.151:80/image?file_id=$file_id"
                    val body = RequestBody.create("text/plain".toMediaType(), file_id.toString())
                    val request2 = Request.Builder()
                        .url(url2)
                        .post(body) // 使用 POST 方法
                        .addHeader("User-Agent", "Apify/1.0.0 (https://apifox.com)")
                        .build()
                    // 执行第二个请求并获取图片文件
                    val response2 = client2.newCall(request2).execute()
                    if (response2.isSuccessful) {
                        Log.d("Upload Success6", "图片路径上传成功")
                        val imageBytes = response2.body?.bytes()
                        val randomFileName = "image_${UUID.randomUUID()}.mp4"
                        val imageFile = imageBytes?.let { byteArray ->
                            File.createTempFile("fugv1", ".jpg").apply {
                                writeBytes(byteArray) // 使用 'it' 引用 let 块的参数
                                renameTo(File(parent, randomFileName))
                            }
                        }
                        withContext(Dispatchers.Main) {
                            // 在主线程上执行 onComplete 回调
                            onComplete(jsonString, imageFile) // 回调上传结果和图片文件
                        }
                    } else {
                        Log.e("Upload unSuccess", "图片文件请求失败")
                        withContext(Dispatchers.Main) {
                            onComplete(null, null) // 图片文件请求失败时回调空
                        }
                    }
                } else {
                    Log.e("Upload unSuccess", "音频文件上传失败")
                    withContext(Dispatchers.Main) {
                        onComplete(null, null) // 音频文件上传失败时回调空
                    }
                }
            } catch (e: Exception) {
                Log.e("Upload Error", e.message ?: "Unknown error")
                withContext(Dispatchers.Main) {
                    onComplete(null, null) // 出现异常时回调空
                }
            }
        }
    }

}

