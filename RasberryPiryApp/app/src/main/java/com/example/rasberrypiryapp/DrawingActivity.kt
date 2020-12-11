package com.example.rasberrypiryapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import net.margaritov.preference.colorpicker.ColorPickerDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

val PEN = 0
val BRUSH = 1
val ERASER = 2
class DrawingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)

        val drawView = findViewById<MyView>(R.id.drawView)

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

        val sendBtn = findViewById<AppCompatImageButton>(R.id.send_btn)
        val editText = findViewById<EditText>(R.id.text_edt)
        val colorDialog = ColorPickerDialog(this, R.color.black)
        val name = getSharedPreferences("data", Context.MODE_PRIVATE).getString("Name", null)
        name?.let {
            editText.setText("$it- ");
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
//            val pixel = intent.getSerializableExtra("pixelData") as ArrayList<ArrayList<String>>?
//            if (pixel == null) {
//                colorBtn.performClick()
//            } else {
//                drawView.post {
//                    drawView.drawWithPixelData(pixel)
//                    drawView.invalidate()
//                }
//            }
        }
        penLayout.setOnClickListener {
            drawView.changeToPen()
            changeTool(PEN, penBtn, brushBtn, eraseBtn)
        }
        brushLayout.setOnClickListener {
            drawView.changeToBrush()
            changeTool(BRUSH, penBtn, brushBtn, eraseBtn)
        }

        sendBtn.setOnClickListener {
            val content = drawView.getPixelString()
            val item = UploadingObject(editText.text.toString(), Array(1, {0}), 0, Array(1, {i ->content}))
            service.upload(item).enqueue(object : Callback<UploadResult> {
                override fun onFailure(call: Call<UploadResult>?, t: Throwable?) {
                    Log.d("error", t.toString())
                }
                override fun onResponse(call: Call<UploadResult>, response: Response<UploadResult>) {
                    Log.d("Response :: ", response?.body().toString())
                }
            })
        }

        penLayout.performClick()
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
}