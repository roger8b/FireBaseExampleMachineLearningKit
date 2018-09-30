package com.example.rm.firebaseexamplemachinelearningkit

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.example.rm.firebaseexamplemachinelearningkit.R.id.image_holder
import com.example.rm.firebaseexamplemachinelearningkit.R.id.image_url_field
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.ctx
import org.jetbrains.anko.doAsync
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

class MainActivity : AppCompatActivity() {


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
            recognizeText(it)
        }

        bt_face.setOnClickListener {
            detectFaces(it)
        }

        bt_label.setOnClickListener {
            generateLabels(it)
        }
    }

    fun recognizeText(v: View) {

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

fun detectFaces(v: View) {
    val detector = FirebaseVision.getInstance().visionFaceDetector
    detector.detectInImage(FirebaseVisionImage.fromBitmap(
            (image_holder.drawable as BitmapDrawable).bitmap
    )).addOnCompleteListener {
        var markedBitmap =
                (image_holder.drawable as BitmapDrawable)
                        .bitmap
                        .copy(Bitmap.Config.ARGB_8888,true)
        val canvas = Canvas(markedBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.parseColor("#99003399")
        it.result.forEach {
            canvas.drawRect(it.boundingBox,paint)
            runOnUiThread {
                image_holder.setImageBitmap(markedBitmap)
            }
        }
    }
}

fun generateLabels(v: View) {
    val detector = FirebaseVision.getInstance().visionCloudLabelDetector
    detector.detectInImage(FirebaseVisionImage.fromBitmap(
            (image_holder.drawable as BitmapDrawable).bitmap
    )).addOnCompleteListener {
        var output = ""
        it.result.forEach {
            if(it.confidence > 0.7)
                output += it.label + "\n"
        }
        runOnUiThread {
            alert (output, "Labels").show()
        }
    }
}
}
