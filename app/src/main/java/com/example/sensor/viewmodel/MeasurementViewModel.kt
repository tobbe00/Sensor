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

    private val rawData = mutableListOf<Float>()
    val linearAccelerationData = mutableListOf<Pair<Long, Float>>()
    val twoSystemsMeasurementData = mutableListOf<Pair<Long, Float>>() // New list for two-system measurements

    private var previousFilteredAngle: Float = 0f
    private var n = 0
    private var isMeasuring = false
    var isTwoSystemsMode = false
    private val csvExporter = CVSExporter()
    private var sensorManagerHelper: SensorManagerHelper? = null
    private var currentGyroAngle = 0f

    fun initialize(context: Context) {
        sensorManagerHelper = SensorManagerHelper(context)
    }

    // Set measurement mode
    fun setMode(twoSystems: Boolean) {
        isTwoSystemsMode = twoSystems
    }

    fun startMeasurement() {
        csvExporter.clearData()
        n = 0
        isMeasuring = true
        linearAccelerationData.clear()
        twoSystemsMeasurementData.clear()
        rawData.clear()
        currentGyroAngle = 0f

        sensorManagerHelper?.startSensorListener { x, y, z ->
            if (isMeasuring) {
                if (isTwoSystemsMode) {
                    handleTwoSystemsMeasurement(x, y, z)
                } else {
                    handleAccelerometerOnlyMeasurement(x, y, z)
                }
            }
        }
    }

    fun stopMeasurement() {
        isMeasuring = false
        _angle.value = 0f
        sensorManagerHelper?.stopSensorListener()
    }

    fun getExportedData(context: Context): List<String> {
        return csvExporter.readCSV(context)
    }

    fun exportData(context: Context): Boolean {
        return try {
            csvExporter.exportToCSV(context)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun handleAccelerometerOnlyMeasurement(x: Float, y: Float, z: Float) {
        val calculatedAngle = Math.toDegrees(
            atan2(y.toDouble(), sqrt((x * x + z * z).toDouble()))
        ).toFloat()
        rawData.add(calculatedAngle)
        n++
        val filteredAngle = linearAcceleration(calculatedAngle, rawData, n)
        val timestamp = System.currentTimeMillis()
        linearAccelerationData.add(System.currentTimeMillis() to filteredAngle)
        csvExporter.recordData(timestamp, filteredAngle)
        _angle.value = 90 + filteredAngle
    }

    private fun handleTwoSystemsMeasurement(x: Float, y: Float, z: Float) {
        // Handle Accelerometer as usual
        handleAccelerometerOnlyMeasurement(x, y, z)

        // Process Gyroscope data
        sensorManagerHelper?.getGyroscopeData { gx, gy, gz ->
            val gyroscopeMagnitude = sqrt(gx * gx + gy * gy + gz * gz)
            currentGyroAngle += gyroscopeMagnitude * 0.01f // Integrate gyroscope data with a timestep of 0.01s

            // Combine accelerometer and gyroscope data
            val accelerometerAngle = linearAccelerationData.lastOrNull()?.second ?: 0f
            val combinedAngle = twoSystemsMeasurement(accelerometerAngle, currentGyroAngle)
            val timestamp = System.currentTimeMillis()

            // Update the twoSystemsMeasurementData list
            twoSystemsMeasurementData.add(System.currentTimeMillis() to combinedAngle)
            csvExporter.recordData(timestamp, combinedAngle)
            // Update the displayed angle
            _angle.value = combinedAngle
        }
    }

    fun linearAcceleration(currentRotation: Float, rawData: List<Float>, n: Int): Float {
        val alpha = 0.7f
        return if (n > 0 && n <= rawData.size) {
            alpha * currentRotation + (1 - alpha) * rawData[n - 1]
        } else {
            alpha * currentRotation
        }
    }

    fun twoSystemsMeasurement(linear: Float, gyroscope: Float): Float {
        val alpha = 0.7f
        return alpha * linear + (1 - alpha) * gyroscope
    }

}
