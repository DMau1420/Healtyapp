package com.mau.healtyapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Opts_independiente : AppCompatActivity() {
    private lateinit var act_pacientes: Button
    private lateinit var act_alarmas: Button
    private lateinit var myuid: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opts_independiente)
        act_alarmas = findViewById(R.id.alarmas)
        act_pacientes = findViewById(R.id.pacientes)

        val uid = intent.getStringExtra("uid")
        myuid = findViewById(R.id.uid)
        myuid.text = uid

        act_alarmas.setOnClickListener {
            val intent = Intent(this, reloj::class.java).apply {
                putExtra("uid",uid)
            }
            startActivity(intent)
        }

        act_pacientes.setOnClickListener {
            val intent = Intent(this, Admin_pacientes::class.java).apply {
                putExtra("uid",uid)
            }
            startActivity(intent)
        }
    }
}