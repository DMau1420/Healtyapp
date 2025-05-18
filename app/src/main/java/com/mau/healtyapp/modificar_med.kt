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
import com.mau.healtyapp.Registrar_med
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class modificar_med : AppCompatActivity() {
    private lateinit var etNombreMed: EditText
    private lateinit var etHora: EditText
    private lateinit var etFrecuencia: EditText
    private lateinit var etDosis: EditText
    private lateinit var btnGuardar: Button
    private var idMedicamento: String = ""
    private var uidPaciente: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modificar_med)
        var med = findViewById<TextView>(R.id.med_id)
        med.text = intent.getStringExtra("id_medicamento")
        etNombreMed = findViewById(R.id.etNombreMed)
        etHora = findViewById(R.id.etHora)
        etFrecuencia = findViewById(R.id.etFrecuencia)
        etDosis = findViewById(R.id.etDosis)
        btnGuardar = findViewById(R.id.btnGuardar)

        // Obtener datos del intent
        idMedicamento = intent.getStringExtra("id_medicamento") ?: ""
        uidPaciente = intent.getStringExtra("uid_paciente") ?: ""
        val nombreMed = intent.getStringExtra("nombre_med") ?: ""
        val hora = intent.getStringExtra("hora_inicio") ?: ""
        val frecuencia = intent.getStringExtra("frecuencia") ?: ""
        val dosis = intent.getStringExtra("dosis") ?: ""

        etNombreMed.setText(nombreMed)
        etHora.setText(hora)
        etFrecuencia.setText(frecuencia)
        etDosis.setText(dosis)

        // Configurar listeners
        btnGuardar.setOnClickListener {
            if (validar()) {
                val med = etNombreMed.text.toString().trim()
                val hora = etHora.text.toString().trim()
                val frecu = etFrecuencia.text.toString().trim().toInt()
                val dosis = etDosis.text.toString().trim().toInt()

                actualizar_med(idMedicamento.toInt(), med, hora, frecu, dosis)
                finish()
            }
        }

    }
    private fun validar(): Boolean {
        val nombreMedEditText = findViewById<EditText>(R.id.etNombreMed)
        val horaInicioEditText = findViewById<EditText>(R.id.etHora)
        val frecuenciaEditText = findViewById<EditText>(R.id.etFrecuencia)
        val dosisEditText = findViewById<EditText>(R.id.etDosis)

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


    private fun actualizar_med (id: Int, med : String, hora : String, frecu : Int, dosis : Int){
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://162.243.81.73/modificar_med.php")
                val params = "id_medicamento=$id&nombre_med=$med&hora_inicio=$hora&frecuencia=$frecu&dosis=$dosis"

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    outputStream.write(params.toByteArray())

                    val response = inputStream.bufferedReader().use { it.readText() }

                    runOnUiThread {
                        Toast.makeText(this@modificar_med, response, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@modificar_med, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}