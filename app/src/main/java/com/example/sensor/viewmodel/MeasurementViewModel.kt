package com.example.sensor.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.sensor.data.sensors.SensorManagerHelper
import com.example.sensor.utils.CVSExporter
import java.sql.Timestamp
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.times

class MeasurementViewModel : ViewModel() {
    private val _angle = mutableStateOf(0f)
    val angle: State<Float> = _angle

    private val rawData = mutableListOf<Float>() // (Timestamp, x, y)
    val linearAccelerationData = mutableListOf<Pair<Long, Float>>() // (Timestamp, FilteredAngle)

    private var previousFilteredAngle: Float = 0f // För att lagra den tidigare filtrerade vinkeln
    private var n = 0;
    private var isMeasuring = false
    private val csvExporter = CVSExporter()
    private var sensorManagerHelper: SensorManagerHelper? = null

    fun initialize(context: Context) {
        sensorManagerHelper = SensorManagerHelper(context)
    }

    fun startMeasurement() {
        csvExporter.clearData()
        n = 0;
        isMeasuring = true
        sensorManagerHelper?.startSensorListener { x, y, z ->
            if (isMeasuring) {
                val calculatedAngle = Math.toDegrees(
                    atan2(y.toDouble(), sqrt((x * x + z * z).toDouble()))
                ).toFloat()
                rawData.add(calculatedAngle)
                n++
                linearAccelerationData.add(System.currentTimeMillis() to linearAcceleration(calculatedAngle,rawData,n))
                _angle.value = 90+(linearAccelerationData.lastOrNull()?.second ?: "N/A") as Float
            }
        }
    }

    fun stopMeasurement() {
        isMeasuring = false
        _angle.value = 0f

    }
    fun getExportedData(context: Context): List<String> {
        return csvExporter.readCSV(context)
    }


    fun exportData(context: Context): Boolean {
        return try {
            csvExporter.exportToCSV(context) // Exportera data via CVSExporter
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun linearAcceleration(currentRotation: Float, rawData: List<Float>, n: Int): Float {
        val alpha = 0.7f // EWMA alpha-värde
        // Kontrollera att indexet n är giltigt
        if (n > 0 && n <= rawData.size) {
            return alpha * currentRotation + (1 - alpha) * rawData.get(n - 1)
        } else {
            return alpha * currentRotation + (1 - alpha)
        }
    }

}
