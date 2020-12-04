package com.example.rasberrypiryapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.IpCons

class PictureActivity : AppCompatActivity() {
    lateinit var imageView:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)
        val editText = findViewById<EditText>(R.id.text_edt)
        val sendBtn = findViewById<Button>(R.id.send_btn)
        val pictureBtn = findViewById<Button>(R.id.picture_btn)

        imageView = findViewById<ImageView>(R.id.image_view)
        imageView.apply {
            scaleType = ScaleType.CENTER_CROP
            viewTreeObserver.addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener() {
                layoutParams.height = width
                layoutParams = layoutParams
            })
        }
        val name = getSharedPreferences("data", Context.MODE_PRIVATE).getString("Name", null)
        name?.let {
            editText.setText("$it- ");
        }

        pictureBtn.setOnClickListener {
            startActivityForResult(ImagePicker.create(this).getIntent(this), IpCons.RC_IMAGE_PICKER)
        }
        startActivityForResult(ImagePicker.create(this).getIntent(this), IpCons.RC_IMAGE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val image = ImagePicker.getFirstImageOrNull(data)
            Glide.with(this).load(image.uri).into(imageView)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}