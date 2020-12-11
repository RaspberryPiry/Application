package com.example.rasberrypiryapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit = Retrofit.Builder().baseUrl("http://code-giraffe.iptime.org:36000/").
addConverterFactory(GsonConverterFactory.create()).build()
val service = retrofit.create(RetrofitNetwork::class.java)

class EntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        val goPaint = findViewById<AppCompatButton>(R.id.go_painting_btn)
        val name = findViewById<EditText>(R.id.name_edt)
        val pref = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = pref.edit()
        val title = findViewById<TextView>(R.id.ripy_app)

        val builder = SpannableStringBuilder("APP");
        builder.setSpan(ForegroundColorSpan(Color.parseColor("#FFA41B")), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        title.append(builder);
        goPaint.setOnClickListener {
            startActivity(Intent(this, DrawingActivity::class.java))
            editor.putString("Name", name.text.toString()).apply()
        }

        name.setText(pref.getString("Name", null))
    }
}