package com.example.rasberrypiryapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.inputmethod.InputMethodManager
import android.widget.*
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
        val nameEdt = findViewById<EditText>(R.id.name_edt)
        val nameTxt = findViewById<TextView>(R.id.name_txt)
        val pref = getSharedPreferences("data", Context.MODE_PRIVATE)
        val editor = pref.edit()
        val title = findViewById<TextView>(R.id.ripy_app)
        val nameBtn = findViewById<ImageButton>(R.id.name_btn)

        val builder = SpannableStringBuilder("APP");
        builder.setSpan(ForegroundColorSpan(Color.parseColor("#FFA41B")), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        title.append(builder);

        nameBtn.setOnClickListener {
            editor.putString("Name", nameEdt.text.toString()).apply()
            nameTxt.text = nameEdt.text.toString()
            nameEdt.text.clear()
            Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_LONG).show()
            val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager;
            imm.hideSoftInputFromWindow(nameEdt.windowToken, 0);
        }
        nameTxt.text = pref.getString("Name", null)

        goPaint.setOnClickListener {
            if (nameTxt.text == "") {
                Toast.makeText(this, "이름을 입력해 주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            goPaint.isSelected = true
            startActivity(Intent(this, DrawingActivity::class.java))
        }
    }
}