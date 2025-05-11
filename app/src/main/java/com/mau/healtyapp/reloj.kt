package com.mau.healtyapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class reloj : AppCompatActivity() {
    private lateinit var uid_txt: TextView
    private lateinit var res: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reloj)

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

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"

                    val response = inputStream.bufferedReader().use { it.readText() }

                    runOnUiThread {
                        try {
                            val jsonArray = JSONArray(response)
                            val resultado = StringBuilder()

                            for (i in 0 until jsonArray.length()) {
                                val medicamento = jsonArray.getJSONObject(i)
                                val nombre = medicamento.getString("nombre_med")
                                val hora = medicamento.getString("hora_inicio")
                                val frecuencia = medicamento.getString("frecuencia")
                                val dosis = medicamento.getString("dosis")

                                resultado.append("Medicamento: $nombre\nHora: $hora\nFrecuencia: $frecuencia\nDosis: $dosis\n\n")
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

}
