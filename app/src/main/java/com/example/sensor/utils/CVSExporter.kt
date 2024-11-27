package com.example.sensor.utils

import android.content.Context
import java.io.File

class CVSExporter {

    private val recordedData = mutableListOf<Pair<Long, Float>>()

    fun recordData(timestamp: Long, angle: Float) {
        recordedData.add(timestamp to angle)
    }

    fun exportToCSV(context: Context) {
        val file = File(context.getExternalFilesDir(null), "measurement.csv")
        file.bufferedWriter().use { writer ->
            writer.appendLine("Timestamp,Angle")
            recordedData.forEach { (timestamp, angle) ->
                writer.appendLine("$timestamp,$angle")
            }
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