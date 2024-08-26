package com.example.woodsfly

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.RequestBody
import okhttp3.ResponseBody
import android.content.ContentResolver
import java.io.IOException
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.woodsfly.R
import com.google.gson.JsonObject
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


@Suppress("DEPRECATION")
class CameraActivity : ComponentActivity() {
    //相机
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var imageUri: Uri? = null
    private val REQUEST_CODE = 100
    private val REQUEST_CODE_CAMERA = 101


    interface UploadService {
        @POST("uploadCameraImage")
        fun uploadCameraImage(@Body imageData: RequestBody): Call<ResponseBody>

        @POST("uploadGalleryImage")
        fun uploadGalleryImage(@Body imageData: RequestBody): Call<ResponseBody>
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)//初始化界面

        enableEdgeToEdge()

            showPhotoOptions()

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
                    capturePhoto()
                } else {
                    Toast.makeText(
                        this,
                        "Permissions are required to take a photo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    imageUri?.let {
                        uploadPhoto(it,true)
                    }
                }
            }//registerForActivity判断抓取行为是否成功，成功执行后续内容（上传等）

        pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                uploadPhoto(it,false)
            }
        }//如果有图片调用图库

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // 添加摄像头和存储权限的处理
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 相机权限授予，调用拍照功能
                capturePhoto()
            }else {
                // 处理权限被拒绝的情况，比如展示一个对话框
                showPermissionDeniedDialog("拍照")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val uri = data?.data
            // 处理选择的音频文件
        }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("拍照", "从图库中选择")
        AlertDialog.Builder(this)
            .setTitle("Select an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // 权限检查和请求
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // 请求相机权限
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.CAMERA),
                                REQUEST_CODE_CAMERA
                            )
                        } else {
                            // 如果权限已经授予，直接调用拍照功能
                            capturePhoto()
                        }
                    }
                    1 -> pickPhotoFromGallery()
                }
            }
            .show()
    }

    private fun showPermissionDeniedDialog(permissionName: String) {
        AlertDialog.Builder(this)
            .setTitle("$permissionName 权限被拒绝")
            .setMessage("该功能需要 $permissionName 权限，请前往设置开启权限。")
            .show()
    }//设置权限

    private fun capturePhoto() {
        try {
            val uri = createImageUri()
            imageUri = uri
            takePictureLauncher.launch(uri)
//            uploadPhoto(uri, true)  // 上传相机拍摄的图片
        } catch (e: Exception) {
            // 捕获和处理可能的异常
            e.printStackTrace()
            Toast.makeText(this, "无法打开相机:${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickPhotoFromGallery() {
        pickImageLauncher.launch("image/*")
        //uploadPhoto(imageUri!!, false)  // 上传图库选择的图片
    }

    private fun createImageUri(): Uri {
        // 使用当前时间戳来生成唯一的文件名
        val fileName = "photo_${System.currentTimeMillis()}"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")


        }

        // 插入内容解析器，获取可用于保存图片的URI
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        // 如果URI为空，抛出异常或记录错误日志
        if (uri == null) {
            Log.e("CreateImageUri", "Failed to create new MediaStore record.")
        }

        return uri!!
    }

    private fun uploadPhoto(uri: Uri, isCameraImage: Boolean) {
        val contentResolver = this.contentResolver
        val mimeType = contentResolver.getType(uri) // 获取 MIME 类型的文
        val imageData = getDataFromUri(contentResolver, uri)
        val base64Image = imageData.let { Base64.encodeToString(it, Base64.DEFAULT) }
        // 转换为 Base64 编码格式
        val Imagetemp= ImageXieChengBase64()
        Imagetemp.uploadRecord(base64Image,1,1){ jsonString ->
            // 上传成功回调
            if (jsonString != null) {
                val bundle = Bundle()
                bundle.putString("JSON_DATA_2", jsonString)
                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            } else {
                Log.e("Upload Failure", "Failed to upload file")
            }
        }
    }

    // 确保 getDataFromUri 方法正常工作
    private fun getDataFromUri(contentResolver: ContentResolver, uri: Uri): ByteArray? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getPathFromUri(uri: Uri): String {
        var path = ""
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            path = cursor.getString(index)
        }
        return path
    }//获取文件路径
}

class ImageXieChengBase64 : AppCompatActivity(), CoroutineScope by MainScope() {
    // 使用主线程的CoroutineScope，注意在Activity销毁时取消协程
    private val job = SupervisorJob()
    val coroutineScope = CoroutineScope(Dispatchers.Main + job)

    private val client = OkHttpClient()

    fun uploadRecord(image64: String,tag: Int, user_id: Int,onComplete: (String?) -> Unit) {
        launch(Dispatchers.IO) {
            try {
                val imageBytes = Base64.decode(image64, Base64.DEFAULT)

                // 创建临时文件来保存解码后的 JPEG 数据
                val tempFile = File.createTempFile("upload_image", ".jpg")
                val fos = FileOutputStream(tempFile)
                fos.write(imageBytes)
                fos.flush()
                fos.close()

                val mediaType = "image/jpeg".toMediaType()
                val body = tempFile.asRequestBody(mediaType)
                val url = "http://10.0.2.2:4523/m1/4938021-4595545-default/predict"
                val fullUrl = "$url?tag=$tag&user_id=$user_id"

                val request = Request.Builder()
                    .url(fullUrl)
                    .post(body)
                    .build()

                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    Log.d("uploadimage", "Success")
                    json_en=2
                    println("成功")
                    val jsonString = response.body?.string()
                    withContext(Dispatchers.Main) {
                        onComplete(jsonString) // 回调上传结果
                    }
                } else {
                    println("失败")
                    Log.e("uploadimage", "shibai")
                }

                withContext(Dispatchers.Main) {
                    // 在这里处理UI更新或结果展示
                }
            } catch (e: Exception) {
                Log.e("uploadimage", "shibai")
                e.printStackTrace()
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





