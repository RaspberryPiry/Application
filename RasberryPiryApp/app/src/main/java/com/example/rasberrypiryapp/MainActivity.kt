package com.example.rasberrypiryapp

import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import net.margaritov.preference.colorpicker.ColorPickerDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit = Retrofit.Builder().baseUrl("http://code-giraffe.iptime.org:36000/").
addConverterFactory(GsonConverterFactory.create()).build()
val service = retrofit.create(RetrofitNetwork::class.java)

val PEN = 0
val BRUSH = 1
val ERASER = 2
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawView = findViewById<MyView>(R.id.drawView)
        val colorBtn = findViewById<Button>(R.id.color_btn)
        val eraseBtn = findViewById<Button>(R.id.erase_btn)
        val penBtn = findViewById<Button>(R.id.pen_btn)
        val brushBtn = findViewById<Button>(R.id.brush_btn)
        val colorImg = findViewById<ImageView>(R.id.color_img)
        val sendBtn = findViewById<Button>(R.id.send_btn)
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
            colorImg.setBackgroundColor(it)
        }
        colorImg.setOnClickListener { colorDialog.show() }
        colorBtn.setOnClickListener { colorDialog.show() }
        eraseBtn.setOnClickListener {
            drawView.changeToEraser()
            changeTool(ERASER, penBtn, brushBtn, eraseBtn)
            Log.d("canvas", drawView.getPixelString())
        }
        penBtn.setOnClickListener {
            drawView.changeToPen()
            changeTool(PEN, penBtn, brushBtn, eraseBtn)
        }
        brushBtn.setOnClickListener {
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
        colorBtn.performClick()
        penBtn.performClick()
    }

    private fun changeTool(tool: Int, pen: Button, brush: Button, eraser: Button) {
        pen.background = getDrawable(R.drawable.ic_pen)
        brush.background = getDrawable(R.drawable.ic_brush)
        eraser.background = getDrawable(R.drawable.ic_eraser)
        when (tool) {
            PEN -> {
                pen.background = getDrawable(R.drawable.ic_pen_black)
            }
            BRUSH -> {
                brush.background = getDrawable(R.drawable.ic_brush_black)
            }
            ERASER -> {
                eraser.background = getDrawable(R.drawable.ic_eraser_black)
            }
        }
    }
}