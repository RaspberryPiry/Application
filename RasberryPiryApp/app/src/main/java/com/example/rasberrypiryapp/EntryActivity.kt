package com.example.rasberrypiryapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton

class EntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        val goPaint = findViewById<AppCompatButton>(R.id.go_painting_btn)
        val goPicture = findViewById<AppCompatButton>(R.id.go_picture_btn)
        val name = findViewById<EditText>(R.id.name_edt)
        val pref = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = pref.edit()

        goPaint.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            editor.putString("Name", name.text.toString()).apply()
        }
        goPicture.setOnClickListener {
            startActivity(Intent(this, PictureActivity::class.java))
            editor.putString("Name", name.text.toString()).apply()
        }

        name.setText(pref.getString("Name", null))
    }
}