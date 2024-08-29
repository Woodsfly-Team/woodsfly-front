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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import org.json.JSONObject
import java.util.UUID


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
            val imageProcessor = ImageXieChengBase64()
            imageProcessor.uploadRecord(getPathFromUri(uri), 1, 1) { jsonString, imageFile ->
                // 上传成功回调
                if (jsonString != null && imageFile != null) {
                    val bundle = Bundle()
                    bundle.putString("JSON_DATA_2", jsonString)
                    bundle.putString("imageFile_2", imageFile.absolutePath)
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
    private val client1 = OkHttpClient()
    private val client2 = OkHttpClient()
    fun uploadRecord(image64: String,
                     tag: Int,
                     user_id: Int,
                     onComplete: (String?, File?) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(image64)
                // 创建 RequestBody，用于封装文件数据
                val fileRequestBody = RequestBody.create("image/jpg".toMediaType(), file.readBytes())

                // 创建 MultipartBody.Part，用于封装文件和参数名
                val filePart = MultipartBody.Part.createFormData("file", file.name, fileRequestBody)

                // 构建 MultipartBody
                val multipartBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addPart(filePart)


                val url1 = "http://59.110.123.151:80/predict?tag=1&user_id=1"

                val request1 = Request.Builder()
                    .url(url1)
                    .post(multipartBody.build())
                    .addHeader("User-Agent", "Apify/1.0.0 (https://apifox.com)")
                    .build()

                val response1 = client1.newCall(request1).execute()
                Log.d("Upload Success1", "$image64,,$tag,,$user_id,,${file.readBytes()}")
                if (response1.isSuccessful) {
                    Log.d("Upload Success2", "File uploaded successfully")
                    json_en = 2
                    val jsonString = response1.body?.string()
                    Log.d("Upload Success3", jsonString.toString())
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
                        Log.d("Upload Success4", "图片路径上传成功")
                        val imageBytes = response2.body?.bytes()
                        //val randomFileName = "image_${UUID.randomUUID()}.jpg"
                        val imageFile = imageBytes?.let { byteArray ->
                            File.createTempFile("fugv1", ".jpg").apply {
                                writeBytes(byteArray) // 使用 'it' 引用 let 块的参数
                                //renameTo(File(parent, randomFileName))
                            }
                        }
                        withContext(Dispatchers.Main) {
                            // 在主线程上执行 onComplete 回调
                            onComplete(jsonString, imageFile) // 回调上传结果和图片文件
                        }
                    } else {
                        Log.e("Upload unSuccess1", "图片文件请求失败")
                        withContext(Dispatchers.Main) {
                            onComplete(null, null) // 图片文件请求失败时回调空
                        }
                    }
                } else {
                    Log.e("Upload unSuccess2", "第一次请求失败")
                    withContext(Dispatchers.Main) {
                        onComplete(null, null) // tupian文件上传失败时回调空
                    }
                }
            } catch (e: Exception) {
                Log.e("Upload Error3", e.message ?: "Unknown error")
                withContext(Dispatchers.Main) {
                    onComplete(null, null) // 出现异常时回调空
                }
            }
        }
    }
}





