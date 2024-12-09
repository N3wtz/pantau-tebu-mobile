package com.bangkit.sugarcanedetection

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bangkit.sugarcanedetection.ClassificationDatabase
import com.bangkit.sugarcanedetection.ClassificationEntity
import com.bangkit.sugarcanedetection.ml.Sugarcanemodel
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    private lateinit var cameraButton: Button
    private lateinit var galleryButton: Button
    private lateinit var historyButton: Button
    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private val imageSize = 32

    private lateinit var database: ClassificationDatabase

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = ClassificationDatabase.getInstance(this)

        cameraButton = findViewById(R.id.button)
        galleryButton = findViewById(R.id.button2)
        historyButton = findViewById(R.id.button3)
        resultText = findViewById(R.id.result)
        imageView = findViewById(R.id.imageView)

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val image = result.data?.extras?.get("data") as Bitmap
                processImage(image)
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    val image = getBitmapFromUri(it)
                    image?.let { bitmap -> processImage(bitmap) }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(cameraIntent)
            } else {
                resultText.text = "Camera permission denied"
            }
        }

        cameraButton.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                cameraLauncher.launch(cameraIntent)
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        galleryButton.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryLauncher.launch(galleryIntent)
        }

        historyButton.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun processImage(image: Bitmap) {
        val scaledBitmap = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)
        imageView.setImageBitmap(image)
        classifyImage(scaledBitmap)
    }

    private fun getBitmapFromUri(uri: android.net.Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun classifyImage(image: Bitmap) {
        try {
            val processedImage = image.copy(Bitmap.Config.ARGB_8888, true)
            val model = Sugarcanemodel.newInstance(applicationContext)

            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, imageSize, imageSize, 3), DataType.FLOAT32)
            val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3).apply {
                order(ByteOrder.nativeOrder())
            }

            val intValues = IntArray(imageSize * imageSize)
            processedImage.getPixels(intValues, 0, imageSize, 0, 0, imageSize, imageSize)
            intValues.forEach { value ->
                byteBuffer.putFloat(((value shr 16) and 0xFF) / 255f)
                byteBuffer.putFloat(((value shr 8) and 0xFF) / 255f)
                byteBuffer.putFloat((value and 0xFF) / 255f)
            }

            inputFeature0.loadBuffer(byteBuffer)

            val outputs = model.process(inputFeature0)
            val confidences = outputs.outputFeature0AsTensorBuffer.floatArray

            val classes = arrayOf("Bacterial Blights", "Healthy", "Yellow")
            val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1

            if (maxIndex != -1) {
                val result = "${classes[maxIndex]} \n (Confidence: ${confidences[maxIndex]})"
                resultText.text = result

                // Simpan riwayat klasifikasi ke database
                val imagePath = saveImageToStorage(processedImage)
                val entity = ClassificationEntity(
                    result = classes[maxIndex],
                    confidence = confidences[maxIndex],
                    imagePath = imagePath
                )

                lifecycleScope.launch {
                    database.classificationDao().insertHistory(entity)
                }
            } else {
                resultText.text = "Unable to classify the image."
            }

            model.close()
        } catch (e: Exception) {
            e.printStackTrace()
            resultText.text = "Error classifying image"
        }
    }

    private fun saveImageToStorage(bitmap: Bitmap): String {
        val filename = "IMG_${System.currentTimeMillis()}.png"
        val file = File(getExternalFilesDir(null), filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }
}
