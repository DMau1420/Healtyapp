package com.mau.healtyapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView


class Admin_pacientes : AppCompatActivity() {
    private lateinit var uid_txt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_pacientes)

        val uid = intent.getStringExtra("uid")
        uid_txt = findViewById(R.id.usid)
        uid_txt.text = "UID: $uid"
    }
}