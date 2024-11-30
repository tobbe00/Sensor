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
        // Hitta standard "Download"-katalogen för appen
        val downloadsDir = context.getExternalFilesDir("Download")

        // Kontrollera och skapa katalogen om den inte finns
        if (downloadsDir != null && !downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        // Skapa filen
        val file = File(downloadsDir, "measurement.csv")
        try {
            file.bufferedWriter().use { writer ->
                writer.appendLine("Timestamp,Angle")
                recordedData.forEach { (timestamp, angle) ->
                    writer.appendLine("$timestamp,$angle")
                }
            }
            println("File saved at: ${file.absolutePath}") // För loggar
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to export data: ${e.message}")
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