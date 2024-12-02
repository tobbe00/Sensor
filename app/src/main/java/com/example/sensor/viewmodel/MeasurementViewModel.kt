package com.example.sensor.viewmodel

import android.content.Context
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

    private val _angle = mutableStateOf(0f)
    val angle: State<Float> = _angle

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
        csvExporter.clearData()
        isMeasuring = true
        rawData.clear()
        linearAccelerationData.clear()
        twoSystemsMeasurementData.clear()
        currentGyroAngle = 0f

        sensorManagerHelper?.startSensorListener { x, y, z ->
            if (isMeasuring) {
                val sensorData = SensorData(x, y, z)
                if (isTwoSystemsMode) {
                    handleTwoSystemsMeasurement(sensorData)
                } else {
                    handleAccelerometerMeasurement(sensorData)
                }
            }
        }
    }

    fun stopMeasurement() {
        isMeasuring = false
        _angle.value = 0f
        sensorManagerHelper?.stopSensorListener()
    }

    private fun handleAccelerometerMeasurement(sensorData: SensorData) {
        val rawAngle = calculateAngle(sensorData)
        val filteredAngle = applyLinearAcceleration(90+rawAngle)
        val timestamp = System.currentTimeMillis()

        rawData.add(FilteredAngle(rawAngle, filteredAngle))
        linearAccelerationData.add(MeasurementData(timestamp, filteredAngle))
        csvExporter.recordData(timestamp, filteredAngle)

        _angle.value =filteredAngle
    }

    private fun handleTwoSystemsMeasurement(sensorData: SensorData) {
        handleAccelerometerMeasurement(sensorData)
        sensorManagerHelper?.getGyroscopeData { gx, gy, gz ->
            val gyroscopeData = GyroscopeData(
                gx = gx,
                gy = gy,
                gz = gz,
                magnitude = sqrt(gx * gx + gy * gy + gz * gz)
            )

            currentGyroAngle += gyroscopeData.magnitude * 0.01f
            val combinedAngle = applyTwoSystemsFusion(currentGyroAngle)
            val timestamp = System.currentTimeMillis()

            twoSystemsMeasurementData.add(MeasurementData(timestamp, combinedAngle))
            csvExporter.recordData(timestamp, combinedAngle)

            _angle.value = combinedAngle
        }
    }

    private fun calculateAngle(sensorData: SensorData): Float {
        return Math.toDegrees(
            atan2(sensorData.y.toDouble(), sqrt((sensorData.x * sensorData.x + sensorData.z * sensorData.z).toDouble()))
        ).toFloat()
    }

    private fun applyLinearAcceleration(rawAngle: Float): Float {
        val alpha = 0.7f
        val previousAngle = rawData.lastOrNull()?.filteredAngle ?: 0f
        return alpha * rawAngle + (1 - alpha) * previousAngle
    }

    private fun applyTwoSystemsFusion(gyroAngle: Float): Float {
        val alpha = 0.7f
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

    fun getExportedData(context: Context): List<String> {
        return csvExporter.readCSV(context)
    }
}
