package com.mau.healtyapp

import android.content.Context
import android.graphics.*
import android.util.TypedValue

class LocalChartGenerator(private val context: Context) {

    fun createSimpleChart(data: List<Pair<String, Int>>): Bitmap {
        // Tamaño fijo para simplificar
        val width = 600
        val height = 400

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fondo blanco
        canvas.drawColor(Color.WHITE)

        // Configuración básica
        val margin = 50
        val chartWidth = width - 2 * margin
        val chartHeight = height - 2 * margin

        // Dibujar ejes
        val axisPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 3f
        }

        // Eje X
        canvas.drawLine(
            margin.toFloat(),
            (height - margin).toFloat(),
            (width - margin).toFloat(),
            (height - margin).toFloat(),
            axisPaint
        )

        // Eje Y
        canvas.drawLine(
            margin.toFloat(),
            margin.toFloat(),
            margin.toFloat(),
            (height - margin).toFloat(),
            axisPaint
        )

        // Si no hay datos, mostrar mensaje
        if (data.isEmpty()) {
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 30f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("No hay datos", width/2f, height/2f, textPaint)
            return bitmap
        }

        // Dibujar barras
        val barPaint = Paint().apply {
            color = Color.BLUE
            style = Paint.Style.FILL
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }

        val barWidth = chartWidth / data.size.coerceAtLeast(1) * 0.8f
        val space = chartWidth / data.size.coerceAtLeast(1) * 0.2f

        // Encontrar valor máximo para escalar
        val maxValue = data.maxOfOrNull { it.second }?.toFloat() ?: 1f

        data.forEachIndexed { index, (label, value) ->
            val left = margin + index * (barWidth + space)
            val barHeight = (value / maxValue) * chartHeight

            // Dibujar barra
            canvas.drawRect(
                left.toFloat(),
                (height - margin - barHeight).toFloat(),
                (left + barWidth).toFloat(),
                (height - margin).toFloat(),
                barPaint
            )

            // Etiqueta
            canvas.drawText(
                label,
                (left + barWidth / 2).toFloat(),
                (height - margin + 30).toFloat(),
                textPaint
            )

            // Valor
            canvas.drawText(
                value.toString(),
                (left + barWidth / 2).toFloat(),
                (height - margin - barHeight - 10).toFloat(),
                textPaint.apply { color = Color.WHITE }
            )
        }

        return bitmap
    }
}