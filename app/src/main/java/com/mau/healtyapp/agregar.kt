package com.mau.healtyapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.mau.healtyapp.Registro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class agregar : AppCompatActivity() {
    private lateinit var uid_txt: TextView
    private lateinit var paciente: EditText
    private lateinit var agrega: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar)

        val uid = intent.getStringExtra("uid")
        uid_txt = findViewById(R.id.uid_propia)
        uid_txt.text = uid

        paciente = findViewById(R.id.uid_paciente)
        agrega = findViewById(R.id.agregar)
        agrega.setOnClickListener {
            if (paciente.text.isEmpty()){
                paciente.error = "Ingrese la UID del paciente"
            }
            else{
                vincular(uid_txt.text.toString(), paciente.text.toString())
                finish()
            }
        }
    }
    private fun vincular (my_uid: String, paciente_uid: String){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://162.243.81.73/vincular_paciente.php")
                val params = "uid=$my_uid&paciente=$paciente_uid"

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    outputStream.write(params.toByteArray())

                    val response = inputStream.bufferedReader().use { it.readText() }

                    runOnUiThread {
                        Toast.makeText(this@agregar, response, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@agregar, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}