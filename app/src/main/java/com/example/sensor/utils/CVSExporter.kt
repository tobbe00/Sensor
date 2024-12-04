package com.example.sensor.utils

import android.content.Context
import android.util.Log
import java.io.File

class CVSExporter {

    private val linearData = mutableListOf<Pair<Long, Float>>() // För Linear-listan
    private val gyroData = mutableListOf<Pair<Long, Float>>() // För Gyro-listan

    fun recordLinearData(timestamp: Long, angle: Float) {
        linearData.add(timestamp to angle)
    }

    fun recordGyroData(timestamp: Long, angle: Float) {
        gyroData.add(timestamp to angle)
    }

    fun exportToCSV(context: Context) {
        // Kontrollera om det finns data
        if (linearData.isEmpty() && gyroData.isEmpty()) {
            Log.e("Export", "No data to export")
            throw RuntimeException("No data to export")
        }

        // Hämta appens specifika katalog för externa filer
        val downloadsDir = context.getExternalFilesDir("Download") ?: run {
            Log.e("Export", "Failed to access external files directory")
            return
        }

        // Skapa katalogen om den inte finns
        if (!downloadsDir.exists()) {
            val created = downloadsDir.mkdirs()
            if (!created) {
                Log.e("Export", "Failed to create directory: ${downloadsDir.absolutePath}")
                return
            }
        }

        // Skapa filen i katalogen
        val file = File(downloadsDir, "measurement.csv")
        try {
            file.bufferedWriter().use { writer ->
                writer.appendLine("Seconds,LinearAngle,GyroAngle")

                // Hämta första timestamp för att skapa intervallet
                val startTimestamp = linearData.firstOrNull()?.first ?: gyroData.firstOrNull()?.first ?: 0L

                val maxSize = maxOf(linearData.size, gyroData.size) // Hantera olika längder på listorna
                for (i in 0 until maxSize) {
                    val linearEntry = linearData.getOrNull(i)
                    val gyroEntry = gyroData.getOrNull(i)

                    val seconds = ((linearEntry?.first ?: gyroEntry?.first ?: 0L) - startTimestamp) / 1000.0
                    val linearAngle = linearEntry?.second?.let { "%.2f".format(it) } ?: "" // Avrunda till 2 decimaler
                    val gyroAngle = gyroEntry?.second?.let { "%.2f".format(it) } ?: "" // Avrunda till 2 decimaler

                    writer.appendLine("$seconds,$linearAngle,$gyroAngle")
                }
            }

            // Logga att filen sparades framgångsrikt
            Log.d("Export", "File saved successfully at: ${file.absolutePath}")
        } catch (e: Exception) {
            // Hantera eventuella undantag
            Log.e("Export", "Failed to save file: ${e.message}")
            throw RuntimeException("Failed to export data: ${e.message}")
        }
    }

    fun clearData() {
        linearData.clear()
        gyroData.clear()
    }

}
