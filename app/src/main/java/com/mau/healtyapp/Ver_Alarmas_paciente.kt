package com.mau.healtyapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL


class Ver_Alarmas_paciente : AppCompatActivity() {
    private lateinit var nombre: TextView
    private lateinit var med1Name: TextView
    private lateinit var med2Name: TextView
    private lateinit var med3Name: TextView
    private lateinit var alarm1Time: TextView
    private lateinit var alarm2Time: TextView
    private lateinit var alarm3Time: TextView
    private lateinit var nuevoBtn: Button
    private lateinit var card1: CardView
    private lateinit var card2: CardView
    private lateinit var card3: CardView
    private var uuid: String? = null
    private lateinit var btnEstadisticas: Button
    private val medicamentos = mutableListOf<Medicamento>()

    data class Medicamento(
        val id: String,
        val nombre: String,
        val hora: String,
        val frecuencia: String,
        val dosis: String,
        val activa: Int

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_alarmas_paciente)

        // Inicializar vistas
        nombre = findViewById(R.id.ver_nombre)
        med1Name = findViewById(R.id.med1_name)
        med2Name = findViewById(R.id.med2_name)
        med3Name = findViewById(R.id.med3_name)
        alarm1Time = findViewById(R.id.alarm1_time)
        alarm2Time = findViewById(R.id.alarm2_time)
        alarm3Time = findViewById(R.id.alarm3_time)
        nuevoBtn = findViewById(R.id.nuevo)
        card1 = med1Name.parent.parent as CardView
        card2 = med2Name.parent.parent as CardView
        card3 = med3Name.parent.parent as CardView
        btnEstadisticas = findViewById(R.id.btnEstadisticas)

        // Obtener datos del intent
        uuid = intent.getStringExtra("uid_paciente")
        val name = intent.getStringExtra("nombre_paciente")
        nombre.text = name ?: "Paciente"

        // Configurar botón de añadir
        nuevoBtn.setOnClickListener {
            agregar()
        }
        btnEstadisticas.setOnClickListener {
            val intent = Intent(this, EstadisticasActivity::class.java).apply {
                putExtra("uid_paciente", uuid)
            }
            startActivity(intent)
        }


        // Configurar clicks en las CardViews
        card1.setOnClickListener { abrirModificar(0) }
        card2.setOnClickListener { abrirModificar(1) }
        card3.setOnClickListener { abrirModificar(2) }

        // Consultar medicamentos si tenemos un UUID
        uuid?.let { consultarMedicamentos(it) }
    }

    private fun abrirModificar(index: Int) {
        if (index < medicamentos.size) {
            val medicamento = medicamentos[index]
            val intent2 = Intent(this, modificar_med::class.java).apply {
                putExtra("id_medicamento", medicamento.id)
                putExtra("alarma_activa", medicamento.activa == 1)
                putExtra("uid_paciente", uuid)
                putExtra("nombre_med", medicamento.nombre)
                putExtra("hora_inicio", medicamento.hora)
                putExtra("frecuencia", medicamento.frecuencia)
                putExtra("dosis", medicamento.dosis)

            }
            startActivity(intent2)
        }
        }

    private fun agregar() {
        uuid?.let {
            val intent = Intent(this, Registrar_med::class.java).apply {
                putExtra("uid_paciente", it)
                putExtra("nombre_paciente", nombre.text.toString())
            }
            startActivity(intent)
        } ?: run {
            Toast.makeText(this, "Error: No se encontró ID de paciente", Toast.LENGTH_SHORT).show()
        }
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
                            medicamentos.clear()
                            val jsonArray = JSONArray(response)

                            // Ocultar todas las alarmas primero
                            listOf(card1, card2, card3).forEach {
                                it.visibility = android.view.View.GONE
                            }

                            // Mostrar hasta 3 alarmas
                            for (i in 0 until minOf(jsonArray.length(), 3)) {
                                val medicamento = jsonArray.getJSONObject(i)
                                val id = medicamento.getString("id_medicamento")
                                val nombreMed = medicamento.getString("nombre_med")
                                val hora = medicamento.getString("hora_inicio")
                                val frecuencia = medicamento.getString("frecuencia")
                                val dosis = medicamento.getString("dosis")
                                val activa = medicamento.getInt("estado")

                                medicamentos.add(Medicamento(id, nombreMed, hora, frecuencia, dosis,activa))

                                when (i) {
                                    0 -> {
                                        med1Name.text = nombreMed
                                        alarm1Time.text = hora
                                        card1.visibility = android.view.View.VISIBLE
                                    }
                                    1 -> {
                                        med2Name.text = nombreMed
                                        alarm2Time.text = hora
                                        card2.visibility = android.view.View.VISIBLE
                                    }
                                    2 -> {
                                        med3Name.text = nombreMed
                                        alarm3Time.text = hora
                                        card3.visibility = android.view.View.VISIBLE
                                    }
                                }
                            }

// Check visibility after the loop
                            nuevoBtn.visibility = if (card1.isVisible && card2.isVisible && card3.isVisible) {
                                android.view.View.GONE
                            } else {
                                android.view.View.VISIBLE
                            }

                            if (jsonArray.length() == 0) {
                                Toast.makeText(
                                    this@Ver_Alarmas_paciente,
                                    "No se encontraron medicamentos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@Ver_Alarmas_paciente,
                                "Error al leer datos",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("JSON Error", "Respuesta: $response", e)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@Ver_Alarmas_paciente,
                        "Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Actualizar la lista cuando se regrese de modificar/agregar
        uuid?.let { consultarMedicamentos(it) }
    }
}