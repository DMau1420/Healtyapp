package com.mau.healtyapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "HealtyAppDB"
        private const val DATABASE_VERSION = 1

        // Tabla de confirmaciones
        const val TABLE_CONFIRMACIONES = "confirmaciones"
        const val COL_ID = "id"
        const val COL_ID_ALARMA = "id_alarma"
        const val COL_UID_PACIENTE = "uid_paciente"
        const val COL_FECHA = "fecha"
        const val COL_CONFIRMADO = "confirmado"
        const val COL_MEDICAMENTO = "medicamento"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_CONFIRMACIONES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ID_ALARMA INTEGER NOT NULL,
                $COL_UID_PACIENTE TEXT NOT NULL,
                $COL_FECHA TEXT NOT NULL,
                $COL_CONFIRMADO INTEGER NOT NULL,
                $COL_MEDICAMENTO TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CONFIRMACIONES")
        onCreate(db)
    }

    fun agregarConfirmacion(idAlarma: Int, uidPaciente: String, fecha: String, confirmado: Int, medicamento: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_ID_ALARMA, idAlarma)
            put(COL_UID_PACIENTE, uidPaciente)
            put(COL_FECHA, fecha)
            put(COL_CONFIRMADO, confirmado)
            put(COL_MEDICAMENTO, medicamento)
        }
        return db.insert(TABLE_CONFIRMACIONES, null, values)
    }

    fun obtenerConfirmaciones(uidPaciente: String): List<Confirmacion> {
        val lista = mutableListOf<Confirmacion>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CONFIRMACIONES,
            arrayOf(COL_ID, COL_ID_ALARMA, COL_FECHA, COL_CONFIRMADO, COL_MEDICAMENTO),
            "$COL_UID_PACIENTE = ?",
            arrayOf(uidPaciente),
            null, null,
            "$COL_FECHA DESC"
        )

        while (cursor.moveToNext()) {
            lista.add(
                Confirmacion(
                    id = cursor.getInt(0),
                    idAlarma = cursor.getInt(1),
                    fecha = cursor.getString(2),
                    confirmado = cursor.getInt(3),
                    medicamento = cursor.getString(4)
                )
            )
        }
        cursor.close()
        return lista
    }

    fun obtenerEstadisticas(uidPaciente: String): List<Estadistica> {
        val lista = mutableListOf<Estadistica>()
        val db = try {
            readableDatabase
        } catch (e: Exception) {
            Log.e("Database", "Error al acceder a la BD", e)
            return emptyList()
        }

        val cursor = try {
            db.rawQuery("""
            SELECT $COL_MEDICAMENTO, date($COL_FECHA) as dia,
                   SUM($COL_CONFIRMADO = 1) as tomados,
                   SUM($COL_CONFIRMADO = 0) as no_tomados
            FROM $TABLE_CONFIRMACIONES
            WHERE $COL_UID_PACIENTE = ?
            GROUP BY $COL_MEDICAMENTO, dia
            ORDER BY dia DESC
            LIMIT 7
        """.trimIndent(), arrayOf(uidPaciente))
        } catch (e: Exception) {
            Log.e("Database", "Error en consulta SQL", e)
            return emptyList()
        }

        // Si no hay datos, generamos datos de ejemplo para pruebas
        if (cursor.count == 0) {
            cursor.close()
        }
        while (cursor.moveToNext()) {
            lista.add(
                Estadistica(
                    medicamento = cursor.getString(0),
                    dia = cursor.getString(1),
                    tomados = cursor.getInt(2),
                    noTomados = cursor.getInt(3)
                )
            )
        }
        cursor.close()
        return lista
    }

    data class Confirmacion(
        val id: Int,
        val idAlarma: Int,
        val fecha: String,
        val confirmado: Int,
        val medicamento: String
    )

    data class Estadistica(
        val medicamento: String,
        val dia: String,
        val tomados: Int,
        val noTomados: Int
    )
}