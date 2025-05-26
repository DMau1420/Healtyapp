package com.mau.healtyapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class ConfirmacionActivity : AppCompatActivity() {
    private lateinit var medicamentoNombre: String
    private var alarmaId: Int = 0
    private lateinit var uidPaciente: String
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion)
        dbHelper = DatabaseHelper(this)

        // Obtener datos del intent
        medicamentoNombre = intent.getStringExtra("medicamento") ?: "Medicamento"
        alarmaId = intent.getIntExtra("alarma_id", 0)
        uidPaciente = intent.getStringExtra("uid_paciente") ?: ""

        // Configurar UI
        val tvMedicamento = findViewById<TextView>(R.id.tvMedicamento)
        tvMedicamento.text = "¿Tomaste tu $medicamentoNombre?"

        val btnSi = findViewById<Button>(R.id.btnSi)
        val btnNo = findViewById<Button>(R.id.btnNo)

        btnSi.setOnClickListener {
            registrarConfirmacion(true)
            finish()
        }

        btnNo.setOnClickListener {
            registrarConfirmacion(false)
            finish()
        }
    }

    private fun registrarConfirmacion(tomoMedicamento: Boolean) {
        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val confirmacion = if (tomoMedicamento) 1 else 0

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Insertar en base de datos local
                val idInsertado = dbHelper.agregarConfirmacion(
                    idAlarma = alarmaId,
                    uidPaciente = uidPaciente,
                    fecha = fechaActual,
                    confirmado = confirmacion,
                    medicamento = medicamentoNombre
                )

                // También enviar al servidor (si es necesario)
                enviarConfirmacionAlServidor(alarmaId, uidPaciente, fechaActual, confirmacion, medicamentoNombre)

                runOnUiThread {
                    if (idInsertado != -1L) {
                        Toast.makeText(this@ConfirmacionActivity, "Confirmación registrada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ConfirmacionActivity, "Error al guardar localmente", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ConfirmacionActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun enviarConfirmacionAlServidor(
        idAlarma: Int,
        uidPaciente: String,
        fecha: String,
        confirmado: Int,
        medicamento: String
    ) {
        val url = URL("http://162.243.81.73/registrar_confirmacion.php")
        val params = "id_alarma=$idAlarma&uid_paciente=$uidPaciente&fecha=$fecha&confirmado=$confirmado&medicamento=$medicamento"

        with(url.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            doOutput = true
            outputStream.write(params.toByteArray())

            val response = inputStream.bufferedReader().use { it.readText() }
            Log.d("Servidor", "Respuesta: $response")
        }
    }
}