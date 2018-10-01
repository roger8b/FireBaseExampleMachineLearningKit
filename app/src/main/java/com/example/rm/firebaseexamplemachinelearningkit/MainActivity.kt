package com.example.rm.firebaseexamplemachinelearningkit

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.media.ExifInterface
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.widget.LinearLayoutManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import java.io.File

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 100
    private val REQUEST_CAMERA_PERMISSION = 200
    private lateinit var uri: Uri



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        image_url_field.setText("https://steemitimages.com/DQmR4ms4BbAp763ttDF8juEu8KyoR2CrVc7TDdmxYfTRYDG/happy-people-1050x600.jpg")
        //image_url_field.setText("http://d2jaiao3zdxbzm.cloudfront.net/wp-content/uploads/figure-65.png")

        loadImage()

        loadControls()



    }




    private fun loadImage() {
        image_url_field.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                Picasso.get().load(image_url_field.text.toString())
                        .into(image_holder)
                true
            }
            false
        }
    }

    private fun loadControls() {
        bt_text.setOnClickListener {
            recognizeText()
        }

        bt_face.setOnClickListener {
            detectFaces()
        }

        bt_label.setOnClickListener {
            generateLabels()
        }

        bt_take_picture.setOnClickListener {
            loadCamera()
        }
    }

    private fun loadCamera() {
        val isAllowed = isAllowedToUseACamera()
        if (isAllowed) {
            startCamera()
        }
    }

    private fun isAllowedToUseACamera(): Boolean {
        val checkSelfPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val permission = arrayOf(Manifest.permission.CAMERA)
        ActivityCompat.requestPermissions(this, permission, REQUEST_CAMERA_PERMISSION)
        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            return true
        }

        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val isAllowed = requestCode == REQUEST_CAMERA_PERMISSION && grantResults.isNotEmpty()

        if (isAllowed) {
            startCamera()
        } else {
            Toast.makeText(this, "Habilite o acesso a camera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val authority = "com.example.rm.firebaseexamplemachinelearningkit.fileprovider"
        val createTempFile = File.createTempFile("picture", ".jpg", externalFilesDir)
        uri = FileProvider.getUriForFile(this, authority, createTempFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, uri) }
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val resultOk = resultCode == Activity.RESULT_OK
        val requestImageCapture = requestCode == REQUEST_IMAGE_CAPTURE
        if (requestImageCapture && resultOk) {
            val bitmap = getCapturedImage(uri)
            image_holder.setImageBitmap(bitmap)
        }
    }

    private fun getCapturedImage(uri: Uri): Bitmap {
        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        return when (ExifInterface(contentResolver.openInputStream(uri)).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(90F) }, true)
            ExifInterface.ORIENTATION_ROTATE_180 -> Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(180F) }, true)
            ExifInterface.ORIENTATION_ROTATE_270 -> Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(270F) }, true)
            else -> bitmap
        }
    }


    fun recognizeText() {
        val textImage = FirebaseVisionImage.fromBitmap(
                (image_holder.drawable as BitmapDrawable).bitmap)
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

        detector.processImage(textImage)
                .addOnCompleteListener {
                    var detectedText = ""
                    it.result.textBlocks.forEach {
                        detectedText += it.text + "\n"
                        runOnUiThread {
                            alert(detectedText, "Text").show()
                        }
                    }
                }
        detector.close()
    }

    fun detectFaces() {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
                .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setMinFaceSize(0.15f)
                .setTrackingEnabled(true)
                .build()

        val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
        val firebaseVisionImage = FirebaseVisionImage
                .fromBitmap((image_holder
                        .drawable as BitmapDrawable)
                        .bitmap)
        detector
                .detectInImage(firebaseVisionImage)
                .addOnSuccessListener { faces: MutableList<FirebaseVisionFace> ->
                    loadRecyclerView(faces)
                }
            .addOnCompleteListener {
                var markedBitmap =
                        (image_holder.drawable as BitmapDrawable)
                                .bitmap
                                .copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(markedBitmap)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                paint.color = Color.parseColor("#99003399")
                it.result.forEach {
                    canvas.drawRect(it.boundingBox, paint)
                    runOnUiThread {
                        image_holder.setImageBitmap(markedBitmap)
                    }
                }
            }
    }

    private fun loadRecyclerView(faces: MutableList<FirebaseVisionFace>) {
        val recyclerView = rv_face_list
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = FaceListAdapter(faces,this)
    }



    fun generateLabels() {
        val detector = FirebaseVision.getInstance().visionCloudLabelDetector
        detector.detectInImage(FirebaseVisionImage.fromBitmap(
                (image_holder.drawable as BitmapDrawable).bitmap
        )).addOnCompleteListener {
            var output = ""
            it.result.forEach {
                if (it.confidence > 0.7)
                    output += it.label + "\n"
            }
            runOnUiThread {
                alert(output, "Labels").show()
            }
        }
    }
}

