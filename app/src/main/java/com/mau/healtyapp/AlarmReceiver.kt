package com.mau.healtyapp

import android.app.AlarmManager
import com.android.volley.Request
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.graphics.Color
import android.os.Vibrator
import android.hardware.camera2.CameraManager
import android.os.Build
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import org.json.JSONObject
import java.io.PrintWriter
import java.net.Socket
import java.util.Calendar



@Suppress("DEPRECATION")
class AlarmReceiver : BroadcastReceiver() {
    private val serverIp = "192.168.1.110"
    private val serverPort = 12345
    private var socket: Socket? = null
    private var writer: PrintWriter? = null

    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var isFlashlightOn = false
    private var notificationManager: NotificationManager? = null

    override fun onReceive(context: Context, intent: Intent) {
        val medicamento = intent.getStringExtra("medicamento") ?: "Medicamento"
        val id = intent.getIntExtra("alarma_id", 0)
        val uidPaciente = intent.getStringExtra("uid_paciente") ?: ""

        val modulo = intent.getStringExtra("modulo")
        val dosis = intent.getStringExtra("dosis")

        val programarSiguiente = intent.getBooleanExtra("programar_siguiente", false)

        verificarEstadoAlarma(context, id) { estaActiva ->
            if (!estaActiva) {
                Log.d("Alarma", "Alarma $id desactivada, no se ejecuta")
                return@verificarEstadoAlarma
            }
        }

        // Lanzar Activity de confirmación
        handleSocketCommunication(modulo.toString(),dosis.toString())
        val confirmIntent = Intent(context, ConfirmacionActivity::class.java).apply {
            putExtra("medicamento", medicamento)
            putExtra("alarma_id", id)
            putExtra("uid_paciente", uidPaciente)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(confirmIntent)

        // Mostrar notificación y activar efectos
        mostrarAlarma(context, medicamento, id)
        // Mostrar Toast grande y centrado
        showLargeToast(context, "¡Hora de tomar: $medicamento!")

        // Configurar notificación
        createNotificationChannel(context)
        showNotification(context, medicamento)

        // Reprogramar para el próximo minuto si es necesario
        if (programarSiguiente || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val siguienteAlarma = Calendar.getInstance().apply {
                add(Calendar.MINUTE, 1)
            }

            val nuevoIntent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("medicamento", medicamento)
                putExtra("alarma_id", id)
                putExtra("vibrar", true)
                putExtra("linterna", true)
                putExtra("programar_siguiente", true)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id,
                nuevoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    siguienteAlarma.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    siguienteAlarma.timeInMillis,
                    pendingIntent
                )
            }
        }
    }


    private fun mostrarAlarma(context: Context, medicamento: String, id: Int) {
        // Mostrar Toast grande
        val toast = Toast.makeText(context, "¡Hora de tomar: $medicamento!", Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()

        // Vibrar
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 500, 200, 500, 200, 1000),
                    -1
                )
            )
        } else {
            vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500,5000), -1)
        }

        // Controlar linterna (parpadeo rápido)
        controlarLinterna(context)
    }
    private fun showLargeToast(context: Context, message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        val view = toast.view
        view?.setBackgroundColor(Color.parseColor("#4CAF50"))
        val text = view?.findViewById<TextView>(android.R.id.message)
        text?.setTextColor(Color.WHITE)
        text?.textSize = 18f
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarmas de Medicamentos"
            val descriptionText = "Notificaciones para recordar tomar medicamentos"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("med_alerts", name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, medicamento: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, "med_alerts")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("¡Hora de medicamento!")
            .setContentText("Debes tomar: $medicamento")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(Color.BLUE)
            .setLights(Color.RED, 1000, 1000)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        notificationManager?.notify(1, notification)
    }

    private fun controlarLinterna(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            val handler = Handler(Looper.getMainLooper())
            var counter = 0

            val runnable = object : Runnable {
                override fun run() {
                    if (counter < 10) { // Parpadear 10 veces
                        val state = counter % 2 == 0
                        cameraManager.setTorchMode(cameraId, state)
                        counter++
                        handler.postDelayed(this, 300) // Cambiar cada 300ms
                    } else {
                        cameraManager.setTorchMode(cameraId, false)
                    }
                }
            }
            handler.post(runnable)
        } catch (e: Exception) {
            Log.e("Linterna", "Error al controlar linterna", e)
        }
    }

    private fun verificarEstadoAlarma(context: Context, id: Int, callback: (Boolean) -> Unit) {

        val url = "http://162.243.81.73/ver_estado_alarma.php?id=$id"

        val request = object : StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    val json = JSONObject(response)
                    val estaActiva = if (json.has("estado")) {
                        json.getInt("estado") == 1
                    } else {
                        Log.e("Alarma", "Respuesta inesperada: $response")
                        false
                    }
                    callback(estaActiva)
                } catch (e: Exception) {
                    Log.e("Alarma", "Error parseando respuesta", e)
                    callback(false)
                }
            },
            { error ->
                Log.e("Alarma", "Error verificando estado", error)
                callback(false)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    private fun handleSocketCommunication(modulo: String?, dosis: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Conectar
                val socket = Socket(serverIp, serverPort)
                val writer = PrintWriter(socket.getOutputStream(), true)

                // Enviar mensaje
                writer.println("${modulo},${dosis}")

                // Esperar un momento para asegurar el envío
                delay(100)

                // Cerrar recursos
                writer.close()
                socket.close()

                Log.d("Socket", "Mensaje enviado correctamente")
            } catch (e: Exception) {
                Log.e("Socket", "Error en comunicación por socket", e)
            }
        }
    }

}