package com.mau.healtyapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Locale

class EstadisticasActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_estadisticas)

        dbHelper = DatabaseHelper(this)
        val uidPaciente = intent.getStringExtra("uid_paciente") ?: ""

        // Obtener estadísticas
        val estadisticas = dbHelper.obtenerEstadisticas(uidPaciente)

        // Convertir a formato simple (fecha -> valor)
        val datosGrafico = estadisticas.map {
            it.dia to it.tomados
        }

        // Crear y mostrar gráfico
        val chartGenerator = LocalChartGenerator(this)
        val chartBitmap = chartGenerator.createSimpleChart(datosGrafico)

        findViewById<ImageView>(R.id.chartImageView).setImageBitmap(chartBitmap)
    }

}