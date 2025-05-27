package com.mau.healtyapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.mau.healtyapp.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray




class Admin_pacientes : AppCompatActivity() {
    private lateinit var uid_txt: TextView
    private lateinit var agregar_pac: Button
    private lateinit var respuesta: TextView
    private lateinit var actualizar: Button
    private lateinit var contenedorPacientes: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_pacientes)
        respuesta = findViewById(R.id.resultado)

        contenedorPacientes = findViewById(R.id.contenedorPacientes)


        val uid = intent.getStringExtra("uid")
        uid_txt = findViewById(R.id.uid_propia)
        uid_txt.text = uid

        extraer(uid.toString())

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

        agregar_pac = findViewById(R.id.nuevo)
        agregar_pac.setOnClickListener { agregar() }

        actualizar = findViewById(R.id.refresh)

        actualizar.setOnClickListener {
            extraer(uid.toString())
        }

    }
    fun agregar(){
        val intent = Intent(this, agregar::class.java).apply {
            putExtra("uid",uid_txt.text.toString())
        }
        startActivity(intent)
    }

    private fun GetPacientes(uid: String, callback: (JSONArray?) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://162.243.81.73/ver_pacientes.php?uid=$uid")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(response)

                runOnUiThread {
                    callback(jsonArray)
                }
            } catch (e: Exception) {
                Log.e("GetPacientes", "Error: ${e.localizedMessage}", e)
                runOnUiThread {
                    Toast.makeText(
                        this@Admin_pacientes,
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                    callback(null)
                }
            }
        }
    }
    private fun extraer(uid: String){
        GetPacientes(uid) { jsonArray ->
            contenedorPacientes.removeAllViews() // Limpiar antes de actualizar
            if (jsonArray != null) {
                for (i in 0 until jsonArray.length()) {
                    val paciente = jsonArray.getJSONObject(i)
                    val texto = """
                    Nombre: ${paciente.getString("username")}
                    Correo: ${paciente.getString("correo")}
                    Tipo: ${paciente.getString("tipo")}
                    UID: ${paciente.getString("uid")}
                """.trimIndent()

                    val uidPaciente = paciente.getString("uid")
                    val nombrepac = paciente.getString("username")

                    val tv = TextView(this)
                    tv.text = texto
                    tv.setPadding(16, 16, 16, 16)
                    tv.setBackgroundColor(Color.parseColor("#E1F5FE"))
                    tv.setTextColor(Color.BLACK)
                    tv.setTextSize(18F)

                    // ✅ Hacer clic para ir a Formulario con el UID del paciente
                    tv.setOnClickListener {
                        val intent = Intent(this, Ver_Alarmas_paciente::class.java)
                        intent.putExtra("uid_paciente", uidPaciente)
                        intent.putExtra("nombre_paciente",nombrepac)
                        startActivity(intent)
                    }

                    contenedorPacientes.addView(tv)
                }
            } else {
                val errorText = TextView(this)
                errorText.text = "No se pudieron obtener los pacientes"
                errorText.setTextColor(Color.RED)
                contenedorPacientes.addView(errorText)
            }
        }
    }

}