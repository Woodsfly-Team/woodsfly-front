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
import org.json.JSONObject


@Suppress("DEPRECATION")
class CameraActivity : ComponentActivity() {
    //创建CameraActivity类继承基类
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>//定义takePictureLauncher变量
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>//定义pickImageLauncher变量
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>//定义requestPermissionLauncher变量
    private var imageUri: Uri? = null//定义一个可以为空的变量imageUri
    private val REQUEST_CODE = 100
    private val REQUEST_CODE_CAMERA = 101


    interface UploadService {
        @POST("uploadCameraImage")
        fun uploadCameraImage(@Body imageData: RequestBody): Call<ResponseBody>
//使用POST方法
        @POST("uploadGalleryImage")
        fun uploadGalleryImage(@Body imageData: RequestBody): Call<ResponseBody>
    }//定义接口


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)//初始化界面

        enableEdgeToEdge()

            showPhotoOptions()

        requestPermissionLauncher =//用来启动摄像头权限
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->//用于请求多个权限并返回结果
                if (permissions[Manifest.permission.CAMERA] == true && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
                    capturePhoto()//相机权限和写入外部权限都被授与执行拍照功能
                } else {
                    Toast.makeText(
                        this,
                        "Permissions are required to take a photo",//如果没有授予权限则提示用户需要权限
                        Toast.LENGTH_SHORT//上述消息短暂提示
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

    private fun showPhotoOptions() {//用户选择
        val options = arrayOf("拍照", "从图库中选择")//数组选择
        AlertDialog.Builder(this)
            .setTitle("Select an option")//提示框标题
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
                    1 -> pickPhotoFromGallery()//从图库中选择
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

    private fun capturePhoto() {//拍照行为
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

    private fun pickPhotoFromGallery() {//从图库选择图片
        pickImageLauncher.launch("image/*")
        //uploadPhoto(imageUri!!, false)  // 上传图库选择的图片
    }

    private fun createImageUri(): Uri {
        // 使用当前时间戳来生成唯一的文件名
        val fileName = "photo_${System.currentTimeMillis()}"//生成唯一的文件名，用来创建文件

        val contentValues = ContentValues().apply {//键值容器并初始化
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)//将创建的文件名存储到容器中
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")//要创建的文件为jpeg格式

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
        val mimeType = contentResolver.getType(uri)
        val imageData = getDataFromUri(contentResolver, uri)
        val base64Image = imageData?.let { Base64.encodeToString(it, Base64.DEFAULT) }

        if (base64Image != null) {
            val imageProcessor = ImageXieChengBase64()
            imageProcessor.uploadRecord(base64Image, 1, 1) { imagePath ->
                if (imagePath != null) {
                    // 调用第二个接口获取图片文件
                    imageProcessor.downloadImage(imagePath) { fileData ->
                        if (fileData != null) {
                            val bundle = Bundle().apply {
                                putByteArray("IMAGE_FILE_DATA", fileData)
                            }
                            // 在这里处理获取到的图片文件
                            Log.d("Download Success", "Image file received")
                            // 例如，将文件传递到 ResultActivity
                            bundle.putByteArray("IMAGE_FILE_DATA", fileData)
                            val intent = Intent(this, ResultActivity::class.java).apply {
                                putExtras(bundle)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            Log.e("Download Failure", "Failed to download image file")
                        }
                    }
                } else {
                    Log.e("Upload Failure", "Failed to upload image and get path")
                }
            }
        } else {
            Log.e("Base64 Encoding Failure", "Failed to encode image to Base64")
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

    private fun getPathFromUri(uri: Uri): String {//查询与URI相关的数据
        var path = ""
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)//获取地址
            path = cursor.getString(index)
        }
        return path
    }//获取文件路径
}

class ImageXieChengBase64 : AppCompatActivity(), CoroutineScope by MainScope() {
    // 使用主线程的CoroutineScope，注意在Activity销毁时取消协程
    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.Main + job

    private val client = OkHttpClient()

    fun uploadRecord(image64: String,tag: Int, user_id: Int,onComplete: (String?) -> Unit) {
        launch(Dispatchers.IO) {
            try {
                val imageBytes = Base64.decode(image64, Base64.DEFAULT)

                // 创建临时文件来保存解码后的 JPEG 数据
                val tempFile = File.createTempFile("upload_image", ".jpg")
                FileOutputStream(tempFile).use { fos ->
                    fos.write(imageBytes)
                }

                //val fos = FileOutputStream(tempFile)
                //fos.write(imageBytes)
                //fos.flush()
                //fos.close()

                val mediaType = "image/jpeg".toMediaType()
                val body = tempFile.asRequestBody(mediaType)
                val url = "http://10.0.2.2:4523/m1/4938021-4595545-default/predict"
                val fullUrl = "$url?tag=$tag&user_id=$user_id"

                val request = Request.Builder()
                    .url(fullUrl)
                    .post(body)
                    .build()

                val response: Response = client.newCall(request).execute()
                val jsonString = response.body?.string()

                if (response.isSuccessful) {
                    Log.d("uploadimage", "Success")
                    val imagePath = parseImagePathFromResponse(jsonString)
                    withContext(Dispatchers.Main) {
                        onComplete(imagePath)
                    }
                } else {
                    Log.e("Upload Failure", "Failed to upload image")
                    withContext(Dispatchers.Main) {
                        onComplete(null)
                    }
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
    fun downloadImage(imagePath: String, onComplete: (ByteArray?) -> Unit) {
        launch(Dispatchers.IO) {
            try {
                val url = "http://10.0.2.2:4523/m1/4938021-4595545-default/get_image"
                val fullUrl = "$url?path=$imagePath"

                val request = Request.Builder()
                    .url(fullUrl)
                    .get()
                    .build()

                val response: Response = client.newCall(request).execute()
                val imageBytes = response.body?.bytes()

                if (response.isSuccessful) {
                    Log.d("Download Success", "Image downloaded successfully")
                    withContext(Dispatchers.Main) {
                        onComplete(imageBytes)
                    }
                } else {
                    Log.e("Download Failure", "Failed to download image")
                    withContext(Dispatchers.Main) {
                        onComplete(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("Download Error", "Exception during download", e)
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            }
        }
    }

    private fun parseImagePathFromResponse(jsonString: String?): String? {
        // 解析 JSON 响应并提取图片路径
        // 示例：假设响应是 { "path": "/images/123.jpg" }
        return jsonString?.let {
            val jsonObject = JSONObject(it)
            jsonObject.optString("path")
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // 取消所有协程
    }
}





