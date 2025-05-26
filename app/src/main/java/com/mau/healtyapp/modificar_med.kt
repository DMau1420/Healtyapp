package com.mau.healtyapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mau.healtyapp.Registrar_med
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

class modificar_med : AppCompatActivity() {
    private lateinit var etNombreMed: EditText
    private lateinit var etHora: EditText
    private lateinit var etFrecuencia: EditText
    private lateinit var etDosis: EditText
    private lateinit var btnGuardar: Button
    private lateinit var switchActiva: SwitchMaterial
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
        switchActiva = findViewById(R.id.switchActiva)


        // Obtener datos del intent
        idMedicamento = intent.getStringExtra("id_medicamento") ?: ""
        uidPaciente = intent.getStringExtra("uid_paciente") ?: ""
        val nombreMed = intent.getStringExtra("nombre_med") ?: ""
        val hora = intent.getStringExtra("hora_inicio") ?: ""
        val frecuencia = intent.getStringExtra("frecuencia") ?: ""
        val dosis = intent.getStringExtra("dosis") ?: ""
        switchActiva.isChecked = intent.getBooleanExtra("alarma_activa",true)

        etNombreMed.setText(nombreMed)
        etHora.setText(hora)
        etFrecuencia.setText(frecuencia)
        etDosis.setText(dosis)


        // Configurar listeners
        btnGuardar.setOnClickListener {
            if (validar()) {
                actualizar_med(
                    idMedicamento.toInt(),
                    etNombreMed.text.toString(),
                    etHora.text.toString(),
                    etFrecuencia.text.toString().toInt(),
                    etDosis.text.toString().toInt(),
                    switchActiva.isChecked
                )
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


    private fun actualizar_med (id: Int, med : String, hora : String, frecu : Int, dosis : Int, estado : Boolean){
        val url = URL("http://162.243.81.73/modificar_med.php")
        val params = "id_medicamento=$id&nombre_med=$med&hora_inicio=$hora&frecuencia=$frecu&dosis=$dosis&estado=${if (estado) 1 else 0}"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    doOutput = true
                    outputStream.write(params.toByteArray())

                    val response = inputStream.bufferedReader().use { it.readText() }

                    runOnUiThread {
                        Toast.makeText(this@modificar_med, response, Toast.LENGTH_LONG).show()

                        // Actualizar alarmas según el estado
                        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                        val intent = Intent(this@modificar_med, AlarmReceiver::class.java).apply {
                            putExtra("alarma_id", id)
                        }

                        val pendingIntent = PendingIntent.getBroadcast(
                            this@modificar_med,
                            id,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE
                        )

                        if (estado) {
                            // Programar la alarma
                            val partesHora = hora.split(":")
                            val calendar = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, partesHora[0].toInt())
                                set(Calendar.MINUTE, partesHora[1].toInt())
                                set(Calendar.SECOND, 0)
                                if (timeInMillis <= System.currentTimeMillis()) {
                                    add(Calendar.DAY_OF_YEAR, 1)
                                }
                            }

                            alarmManager.setExact(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        } else {
                            // Cancelar la alarma
                            alarmManager.cancel(pendingIntent)
                        }

                        finish()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@modificar_med, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun cancelarAlarma(id: Int) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }
    private fun programarAlarmaSiNecesario(id: Int,
                                           nombreMedicamento: String,
                                           horaInicio: String,
    ) {
        try {
            val partesHora = horaInicio.split(":")
            val hora = partesHora[0].toInt()
            val minuto = partesHora[1].toInt()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hora)
                set(Calendar.MINUTE, minuto)
                set(Calendar.SECOND, 0)

                // Si la hora ya pasó hoy, programar para mañana
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val intent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("medicamento", nombreMedicamento)
                putExtra("alarma_id", id)
                putExtra("vibrar", true)
                putExtra("linterna", true)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

            // Configurar alarma exacta que se repite cada minuto
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )

                // Programar la siguiente alarma manualmente en el receptor
                intent.putExtra("programar_siguiente", true)
            } else {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    60 * 1000, // 1 minuto en milisegundos
                    pendingIntent
                )
            }

            Log.d("Alarma", "Alarma programada para $nombreMedicamento a las $horaInicio")
        } catch (e: Exception) {
            Log.e("Alarma", "Error al programar alarma para $nombreMedicamento", e)
        }
    }
}