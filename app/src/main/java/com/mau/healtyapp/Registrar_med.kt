package com.mau.healtyapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Registrar_med : AppCompatActivity() {
    private lateinit var uid: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_med)

        val uuid = intent.getStringExtra("uid_paciente")
        uid = findViewById(R.id.uid)
        uid.text = uuid

    }
}