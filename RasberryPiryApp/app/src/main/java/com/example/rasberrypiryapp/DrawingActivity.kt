package com.example.rasberrypiryapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
    lateinit var pColor: ImageView
    lateinit var bColor: ImageView
    lateinit var editText: EditText
    lateinit var a1: AppCompatButton
    lateinit var a2: AppCompatButton
    lateinit var a3: AppCompatButton
    var melody: Int? = null

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
        pColor = findViewById<ImageView>(R.id.pen_color_view)

        val brushLayout = findViewById<ConstraintLayout>(R.id.brush_layout)
        val brushBtn = findViewById<ImageView>(R.id.brush_btn)
        bColor = findViewById<ImageView>(R.id.brush_color_view)

        val pictureBtn = findViewById<ImageButton>(R.id.image_btn)

        a1 = findViewById<AppCompatButton>(R.id.alarm_btn1)
        a2 = findViewById<AppCompatButton>(R.id.alarm_btn2)
        a3 = findViewById<AppCompatButton>(R.id.alarm_btn3)

        val sendBtn = findViewById<AppCompatImageButton>(R.id.send_btn)
        editText = findViewById<EditText>(R.id.text_edt)
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
            changeColorUi(it)
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

        a1.setOnClickListener { changeAlarm(1) }
        a2.setOnClickListener { changeAlarm(2) }
        a3.setOnClickListener { changeAlarm(3) }

        sendBtn.setOnClickListener {
            val text = nameTxt.text.toString() + " - " + editText.text.toString()
            if (melody != null){
                showUploadAlert(text, melody)
            } else {
                showUploadAlert(text)
            }
        }

        drawView.changeColor(resources.getColor(R.color.white))
        changeColorUi(resources.getColor(R.color.white))
        penLayout.performClick()
        pictureBtn.setOnClickListener {
            startActivityForResult(ImagePicker.create(this).getIntent(this), IpCons.RC_IMAGE_PICKER)
        }
    }

    private fun upload(text: String, melody: Int? = null) {
        val content = drawView.getPixelString()
        val item = if(melody != null) UploadingObject(text, Array(1) { 1000 }, Array(1) { i -> content }, 1, melody )
                    else UploadingObject(text, Array(1) { 1000 }, Array(1) { _ -> content })

        service.upload(item).enqueue(object : Callback<UploadResult> {
            override fun onFailure(call: Call<UploadResult>?, t: Throwable?) {
                Log.d("error", t.toString())
                Toast.makeText(this@DrawingActivity, "전송에 실패하였습니다 - " + t.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<UploadResult>, response: Response<UploadResult>) {
                Log.d("Response :: ", response?.body().toString())
                Toast.makeText(this@DrawingActivity, "전송되었습니다.", Toast.LENGTH_SHORT).show()
                drawView.clear()
                editText.setText("")
                changeButtonSelected(a1)
                changeButtonSelected(a2)
                changeButtonSelected(a3)
            }
        })
    }

    private fun showUploadAlert(text: String, melody: Int? = null) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("전송하시겠습니까?")
        builder.setPositiveButton("예") { dialog, which ->
            upload(text, melody)
        }
        builder.setNegativeButton("아니오") { dialog, which -> }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val image = ImagePicker.getFirstImageOrNull(data)
            val file = File(image.path)
            val requestFile: RequestBody =
                RequestBody.create(MediaType.parse("multipart/form-data"), file)
            val body =
                MultipartBody.Part.createFormData("img", file.getName(), requestFile)
            Toast.makeText(this, "변환 중 입니다...", Toast.LENGTH_LONG).show()
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
                            val c = drawView.drawWithPixelData(it)
                            drawView.invalidate()
                            changeColorUi(c)
                        }
                    }
                    Log.d("wooni :: ", response?.body().toString())
                }
            })
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun changeColorUi(c: Int) {
        bColor.setBackgroundColor(c)
        pColor.setBackgroundColor(c)
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

    private fun changeAlarm(selected : Int) {
        when (selected) {
            1 -> {
                changeButtonSelected(a1, true)
                changeButtonSelected(a2)
                changeButtonSelected(a3)
                changeMelody(1)
            }
            2 -> {
                changeButtonSelected(a1)
                changeButtonSelected(a2, true)
                changeButtonSelected(a3)
                changeMelody(2)
            }
            3 -> {
                changeButtonSelected(a1)
                changeButtonSelected(a2)
                changeButtonSelected(a3, true)
                changeMelody(3)
            }
        }
    }

    private fun changeMelody(type: Int) {
        melody = if (melody == type) {
            null
        } else {
            type
        }
    }

    private fun changeButtonSelected(a: AppCompatButton, isChanged: Boolean = false) {
        if (isChanged) {
            if (a.isSelected) {
                a.isSelected = false
                a.setTextColor(resources.getColor(R.color.orange))
            } else {
                a.isSelected = true
                a.setTextColor(resources.getColor(R.color.white))
            }
        } else {
            a.isSelected = false
            a.setTextColor(resources.getColor(R.color.orange))
        }
    }
}