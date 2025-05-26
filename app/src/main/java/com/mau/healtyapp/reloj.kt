package com.mau.healtyapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.hardware.camera2.CameraManager
import android.os.Build
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

class reloj : AppCompatActivity() {
    private lateinit var uid_txt: TextView
    private lateinit var res: TextView
    private lateinit var vibrator: Vibrator
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private val alarmIds = listOf(1, 2, 3) // IDs para las 3 alarmas posibles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reloj)

        // Inicializar servicios de vibración y cámara
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList[0] // Primera cámara (generalmente la trasera)
        } catch (e: Exception) {
            Log.e("CameraError", "No se pudo obtener la cámara", e)
        }

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
        res = findViewById(R.id.respuesta)

        consultarMedicamentos(uid_txt.text.toString())
    }

    private fun consultarMedicamentos(uid: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://162.243.81.73/ver_medicamentos.php?uid=$uid")
                val response = url.readText()
                val jsonArray = JSONArray(response)


                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"

                    val response = inputStream.bufferedReader().use { it.readText() }

                    runOnUiThread {

                        try {
                            val jsonArray = JSONArray(response)
                            val resultado = StringBuilder()

                            // Cancelar todas las alarmas existentes primero
                            cancelAllAlarms()

                            for (i in 0 until jsonArray.length()) {
                                if (i >= 3) break // Solo procesamos hasta 3 alarmas
                                val medicamento = jsonArray.getJSONObject(i)
                                if (medicamento.getInt("estado") == 1){

                                val nombre = medicamento.getString("nombre_med")
                                val hora = medicamento.getString("hora_inicio")
                                val frecuencia = medicamento.getString("frecuencia")
                                val dosis = medicamento.getString("dosis")
                                val estado = medicamento.getInt("estado")
                                val modulo = medicamento.getString("modulo")


                                resultado.append("Medicamento: $nombre\nHora: $hora\nFrecuencia: $frecuencia\nDosis: $dosis\nEstado: $estado\n\n")

                                // Programar la alarma
                                programarAlarma(alarmIds[i], nombre, hora, frecuencia,modulo, dosis)
                                }
                            }

                            res.text = resultado.toString().ifEmpty { "No se encontraron medicamentos." }

                        } catch (e: Exception) {
                            Toast.makeText(this@reloj, "Error al leer datos", Toast.LENGTH_SHORT).show()
                            res.text = "Error al procesar respuesta.\n$response"
                            Log.e("JSON Error", "Respuesta: $response", e)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@reloj, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    res.text = "No se pudo conectar al servidor."
                }
            }
        }
    }

    private fun programarAlarma(
        id: Int,
        nombreMedicamento: String,
        horaInicio: String,
        frecuencia: String,
        modulo: String,
        dosis: String
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
                putExtra("modulo",modulo)
                putExtra("dosis", dosis)
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

    private fun cancelAllAlarms() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        for (id in alarmIds) {
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
    }
}