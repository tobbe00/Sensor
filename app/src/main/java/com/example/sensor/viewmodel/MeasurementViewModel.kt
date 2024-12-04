package com.example.sensor.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.sensor.data.models.FilteredAngle
import com.example.sensor.data.models.GyroscopeData
import com.example.sensor.data.models.MeasurementData
import com.example.sensor.data.models.SensorData
import com.example.sensor.data.sensors.SensorManagerHelper
import com.example.sensor.utils.CVSExporter
import java.sql.Timestamp
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.times
class MeasurementViewModel : ViewModel() {

    private val _linearAccelerationAngle = mutableStateOf(0f)
    val linearAccelerationAngle: State<Float> = _linearAccelerationAngle

    private val _gyroAngle = mutableStateOf(0f)
    val gyroAngle: State<Float> = _gyroAngle


    private val rawData = mutableListOf<FilteredAngle>()
    private val csvExporter = CVSExporter()
    private var sensorManagerHelper: SensorManagerHelper? = null

    private var isMeasuring = false
    var isTwoSystemsMode: Boolean = false // Use a boolean to toggle between modes
    private var currentGyroAngle = 0f

    val linearAccelerationData = mutableListOf<MeasurementData>()
    val twoSystemsMeasurementData = mutableListOf<MeasurementData>()

    fun initialize(context: Context) {
        sensorManagerHelper = SensorManagerHelper(context)
    }

    fun setMode(twoSystems: Boolean) {
        isTwoSystemsMode = twoSystems
    }

    fun startMeasurement() {
        if (sensorManagerHelper == null) return

        csvExporter.clearData()
        isMeasuring = true
        rawData.clear()
        linearAccelerationData.clear()
        twoSystemsMeasurementData.clear()
        currentGyroAngle = 0f

        if (isTwoSystemsMode) {
            Log.d("MeasurementViewModel", "Starting measurement with two systems mode")
            sensorManagerHelper?.startAccelerometerListener { x, y, z ->
                if (isMeasuring) handleAccelerometerMeasurement(SensorData(x, y, z))
            }
            sensorManagerHelper?.startGyroscopeListener { gx, gy, gz ->
                if (isMeasuring) handleGyroscopeMeasurement(gx, gy, gz)
            }
        } else {
            Log.d("MeasurementViewModel", "Starting measurement with accelerometer only")
            sensorManagerHelper?.startAccelerometerListener { x, y, z ->
                if (isMeasuring) handleAccelerometerMeasurement(SensorData(x, y, z))
            }
        }
    }

    fun stopMeasurement() {
        isMeasuring = false
        sensorManagerHelper?.stopAllListeners()
        Log.d("MeasurementViewModel", "Stopped all sensors")
    }

    private fun handleAccelerometerMeasurement(sensorData: SensorData) {
        val rawAngle = calculateAngle(sensorData)
        val filteredAngle = applyLinearAcceleration(90 + rawAngle)
        val timestamp = System.currentTimeMillis()

        _linearAccelerationAngle.value = filteredAngle
        rawData.add(FilteredAngle(rawAngle, filteredAngle))
        linearAccelerationData.add(MeasurementData(timestamp, filteredAngle))
        csvExporter.recordLinearData(timestamp, filteredAngle)

    }

    private fun handleGyroscopeMeasurement(gx: Float, gy: Float, gz: Float) {
        Log.d("MeasurementViewModel", "Received gyroscope data gx=$gx, gy=$gy, gz=$gz")
        val gyroscopeData = GyroscopeData(
            gx = gx,
            gy = gy,
            gz = gz,
            magnitude = sqrt(gx * gx + gy * gy + gz * gz)
        )

        currentGyroAngle += gyroscopeData.magnitude * 0.01f
        val combinedAngle = applyTwoSystemsFusion(currentGyroAngle)
        val timestamp = System.currentTimeMillis()
        _gyroAngle.value = combinedAngle
        twoSystemsMeasurementData.add(MeasurementData(timestamp, combinedAngle))
        csvExporter.recordGyroData(timestamp, combinedAngle)

        Log.d("MeasurementViewModel", "Added to twoSystemsMeasurementData: Timestamp=$timestamp, Angle=$combinedAngle")
    }

    private fun calculateAngle(sensorData: SensorData): Float {
        return Math.toDegrees(
            atan2(sensorData.y.toDouble(), sqrt((sensorData.x * sensorData.x + sensorData.z * sensorData.z).toDouble()))
        ).toFloat()
    }

    private fun applyLinearAcceleration(rawAngle: Float): Float {
        val alpha = 0.5f
        val previousAngle = rawData.lastOrNull()?.filteredAngle ?: 0f
        return alpha * rawAngle + (1 - alpha) * previousAngle
    }

    private fun applyTwoSystemsFusion(gyroAngle: Float): Float {
        val alpha = 0.5f
        val accelerometerAngle = linearAccelerationData.lastOrNull()?.angle ?: 0f
        return alpha * accelerometerAngle + (1 - alpha) * gyroAngle
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
}
