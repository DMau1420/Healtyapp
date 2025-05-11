package com.mau.healtyapp

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mau.healtyapp.Admin_pacientes
import com.mau.healtyapp.Registro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Time
import java.util.Calendar

class Registrar_med : AppCompatActivity() {
    private lateinit var nombre: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_med)

        val uuid = intent.getStringExtra("uid_paciente")

        val name = intent.getStringExtra("nombre_paciente")
        nombre = findViewById(R.id.nombre_pac)
        nombre.text = name

        val nombremed = findViewById<EditText>(R.id.nombre_med)

        val horaInicEditText = findViewById<EditText>(R.id.hora_inic)

        horaInicEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hora = calendar.get(Calendar.HOUR_OF_DAY)
            val minuto = calendar.get(Calendar.MINUTE)

            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val horaFormateada = String.format("%02d:%02d", selectedHour, selectedMinute)
                horaInicEditText.setText(horaFormateada)
            }, hora, minuto, true)

            timePicker.show()
        }
        val frecuencia = findViewById<EditText>(R.id.frecuencia)
        val dos = findViewById<EditText>(R.id.dosis)
        val btnreg = findViewById<Button>(R.id.regmed)

        btnreg.setOnClickListener {
            if (validar()) {
                val nombreMed = nombremed.text.toString().trim()
                val horaInicio = horaInicEditText.text.toString().trim()
                val frecuenciaInt = frecuencia.text.toString().trim().toInt()
                val dosisInt = dos.text.toString().trim().toInt()

                if (uuid != null) {
                    registrar(uuid, nombreMed, horaInicio, frecuenciaInt, dosisInt)
                } else {
                    Toast.makeText(this, "Error: UID no disponible", Toast.LENGTH_SHORT).show()
                }
            }
            finish()
        }
    }
        private fun validar(): Boolean {
        val nombreMedEditText = findViewById<EditText>(R.id.nombre_med)
        val horaInicioEditText = findViewById<EditText>(R.id.hora_inic)
        val frecuenciaEditText = findViewById<EditText>(R.id.frecuencia)
        val dosisEditText = findViewById<EditText>(R.id.dosis)

        val nombreMed = nombreMedEditText.text.toString().trim()
        val horaInicio = horaInicioEditText.text.toString().trim()
        val frecuencia = frecuenciaEditText.text.toString().trim()
        val dosis = dosisEditText.text.toString().trim()

        var valido = true

        if (nombreMed.isEmpty()) {
            nombreMedEditText.error = "Ingrese el nombre del medicamento"
            valido = false
        }

        if (horaInicio.isEmpty()) {
            horaInicioEditText.error = "Ingrese la hora de inicio"
            valido = false
        }

        if (frecuencia.isEmpty()) {
            frecuenciaEditText.error = "Ingrese la frecuencia"
            valido = false
        }

        if (dosis.isEmpty()) {
            dosisEditText.error = "Ingrese la dosis"
            valido = false
        }

        if (!valido) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
        }

        return valido
    }


    private fun registrar (uid: String, med : String, hora : String, frecu : Int, dosis : Int){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://162.243.81.73/registrar_medicamento.php")
                val params = "uid=$uid&nombre_med=$med&hora_inicio=$hora&frecuencia=$frecu&dosis=$dosis"

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    outputStream.write(params.toByteArray())

                    val response = inputStream.bufferedReader().use { it.readText() }

                    runOnUiThread {
                        Toast.makeText(this@Registrar_med, response, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@Registrar_med, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}