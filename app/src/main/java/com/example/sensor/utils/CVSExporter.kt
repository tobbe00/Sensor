package com.example.sensor.utils

import android.content.Context
import android.util.Log
import java.io.File

class CVSExporter {

    private val recordedData = mutableListOf<Pair<Long, Float>>()

    fun recordData(timestamp: Long, angle: Float) {
        recordedData.add(timestamp to angle)
    }

    fun exportToCSV(context: Context) {
        // Kontrollera om det finns data
        if (recordedData.isEmpty()) {
            throw RuntimeException("No data to export")
        }

        val downloadsDir = context.getExternalFilesDir("Download") ?: run {
            Log.e("Export", "Failed to access external files directory")
            return
        }

        if (!downloadsDir.exists()) {
            val created = downloadsDir.mkdirs()
            if (!created) {
                Log.e("Export", "Failed to create directory: ${downloadsDir.absolutePath}")
                return
            }
        }

        // Skapa filen
        val file = File(downloadsDir, "measurement.csv")
        try {
            file.bufferedWriter().use { writer ->
                writer.appendLine("Seconds,Angle")

                // Hämta första timestamp för att skapa intervallet
                val startTimestamp = recordedData.first().first

                recordedData.forEach { (timestamp, angle) ->
                    val seconds = (timestamp - startTimestamp) / 1000.0 // Konvertera till sekunder
                    writer.appendLine("$seconds,$angle")
                }
            }
            Log.d("Export", "File saved successfully at: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("Export", "Failed to save file: ${e.message}")
        }
    }


    fun clearData() {
        recordedData.clear()
    }
    fun readCSV(context: Context): List<String> {
        val file = File(context.getExternalFilesDir(null), "measurement.csv")
        if (!file.exists()) return emptyList() // Om filen inte finns, returnera tom lista
        return file.readLines() // Läs alla rader i filen som en lista av strängar
    }



}