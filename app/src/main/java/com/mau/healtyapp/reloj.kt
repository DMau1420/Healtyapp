package com.mau.healtyapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast

class reloj : AppCompatActivity() {
    private lateinit var uid_txt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reloj)

        val uid = intent.getStringExtra("uid")
        uid_txt = findViewById(R.id.uid_propia)
        uid_txt.text = uid
        uid_txt.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("UID", uid_txt.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "UID copiado", Toast.LENGTH_SHORT).show()
            uid_txt.setTextColor(Color.GREEN)
            Handler(Looper.getMainLooper()).postDelayed({
                uid_txt.setTextColor(Color.BLACK)
            }, 300)

        }

    }
}