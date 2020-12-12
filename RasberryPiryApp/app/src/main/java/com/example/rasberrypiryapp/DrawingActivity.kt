package com.example.rasberrypiryapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.IpCons
import net.margaritov.preference.colorpicker.ColorPickerDialog
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

val PEN = 0
val BRUSH = 1
val ERASER = 2

class DrawingActivity : AppCompatActivity() {
    lateinit var drawView: DrawView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        drawView = findViewById<DrawView>(R.id.drawView)

        val colorLayout = findViewById<ConstraintLayout>(R.id.color_layout)
        val colorBtn = findViewById<ImageView>(R.id.color_btn)
        val eraseLayout = findViewById<ConstraintLayout>(R.id.erase_layout)
        val eraseBtn = findViewById<ImageView>(R.id.erase_btn)

        val penLayout = findViewById<ConstraintLayout>(R.id.pen_layout)
        val penBtn = findViewById<ImageView>(R.id.pen_btn)
        val pColor = findViewById<ImageView>(R.id.pen_color_view)

        val brushLayout = findViewById<ConstraintLayout>(R.id.brush_layout)
        val brushBtn = findViewById<ImageView>(R.id.brush_btn)
        val bColor = findViewById<ImageView>(R.id.brush_color_view)

        val pictureBtn = findViewById<ImageButton>(R.id.image_btn)

        val a1 = findViewById<AppCompatButton>(R.id.alarm_btn1)
        val a2 = findViewById<AppCompatButton>(R.id.alarm_btn2)
        val a3 = findViewById<AppCompatButton>(R.id.alarm_btn3)

        val sendBtn = findViewById<AppCompatImageButton>(R.id.send_btn)
        val editText = findViewById<EditText>(R.id.text_edt)
        val nameTxt = findViewById<TextView>(R.id.name_txt)
        val colorDialog = ColorPickerDialog(this, R.color.black)
        val name = getSharedPreferences("data", Context.MODE_PRIVATE).getString("Name", null)
        name?.let {
            nameTxt.text = it
        }

        colorDialog.hexValueEnabled = false
        colorDialog.alphaSliderVisible = false
        colorDialog.setOnColorChangedListener {
            drawView.changeColor(it)
            bColor.setBackgroundColor(it)
            pColor.setBackgroundColor(it)
        }
        colorLayout.setOnClickListener { colorDialog.show() }
        eraseLayout.setOnClickListener {
            drawView.changeToEraser()
            changeTool(ERASER, penBtn, brushBtn, eraseBtn)
            Log.d("canvas", drawView.getPixelString())
        }
        penLayout.setOnClickListener {
            drawView.changeToPen()
            changeTool(PEN, penBtn, brushBtn, eraseBtn)
        }
        brushLayout.setOnClickListener {
            drawView.changeToBrush()
            changeTool(BRUSH, penBtn, brushBtn, eraseBtn)
        }

        a1.setOnClickListener { changeAlarm(1, a1, a2, a3) }
        a2.setOnClickListener { changeAlarm(2, a1, a2, a3) }
        a3.setOnClickListener { changeAlarm(3, a1, a2, a3) }

        sendBtn.setOnClickListener {
            val content = drawView.getPixelString()
            val item = UploadingObject(
                editText.text.toString(),
                Array(1, { 0 }),
                0,
                Array(1, { i -> content })
            )
            service.upload(item).enqueue(object : Callback<UploadResult> {
                override fun onFailure(call: Call<UploadResult>?, t: Throwable?) {
                    Log.d("error", t.toString())
                }

                override fun onResponse(
                    call: Call<UploadResult>,
                    response: Response<UploadResult>
                ) {
                    Log.d("Response :: ", response?.body().toString())
                }
            })
        }

        penLayout.performClick()
        pictureBtn.setOnClickListener {
            startActivityForResult(ImagePicker.create(this).getIntent(this), IpCons.RC_IMAGE_PICKER)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val image = ImagePicker.getFirstImageOrNull(data)
            val file = File(image.path)
            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val body =
                MultipartBody.Part.createFormData("img", file.getName(), requestFile)
            service.uploadImage(body).enqueue(object : Callback<UploadImagePixelfyResult> {
                override fun onFailure(call: Call<UploadImagePixelfyResult>?, t: Throwable?) {

                    Log.d("wooni", t.toString())
                }

                override fun onResponse(
                    call: Call<UploadImagePixelfyResult>,
                    response: Response<UploadImagePixelfyResult>
                ) {
                    response.body()?.pixel?.let {
                        drawView.post {
                            drawView.drawWithPixelData(it)
                            drawView.invalidate()
                        }
                    }
                    Log.d("wooni :: ", response?.body().toString())
                }
            })
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun changeTool(tool: Int, pen: ImageView, brush: ImageView, eraser: ImageView) {
        pen.isSelected = false
        brush.isSelected = false
        eraser.isSelected = false
        when (tool) {
            PEN -> {
                pen.isSelected = true
            }
            BRUSH -> {
                brush.isSelected = true
            }
            ERASER -> {
                eraser.isSelected = true
            }
        }
    }

    private fun changeAlarm(selected : Int, a1: AppCompatButton, a2: AppCompatButton, a3: AppCompatButton) {
        when (selected) {
            1 -> {
                if(a1.isSelected) {
                    a1.isSelected = false
                    a1.setTextColor(resources.getColor(R.color.orange))
                } else {
                    a1.isSelected = true
                    a1.setTextColor(resources.getColor(R.color.black))
                }
                a2.isSelected = false
                a3.isSelected = false
            }
            2 -> {
                if(a2.isSelected) {
                    a2.isSelected = false
                    a2.setTextColor(resources.getColor(R.color.orange))
                } else {
                    a2.isSelected = true
                    a2.setTextColor(resources.getColor(R.color.black))
                }
                a1.isSelected = false
                a3.isSelected = false
            }
            3 -> {
                if(a3.isSelected) {
                    a3.isSelected = false
                    a3.setTextColor(resources.getColor(R.color.orange))
                } else {
                    a3.isSelected = true
                    a3.setTextColor(resources.getColor(R.color.black))
                }
                a1.isSelected = false
                a2.isSelected = false
            }
        }
    }
}