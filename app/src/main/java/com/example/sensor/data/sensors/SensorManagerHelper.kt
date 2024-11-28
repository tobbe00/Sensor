package com.example.sensor.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorManagerHelper(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private var sensorEventListener: SensorEventListener? = null

    fun startSensorListener(callback: (x: Float, y: Float, z: Float) -> Unit) {
        sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let { values ->
                    callback(values[0], values[1], values[2])
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorEventListener?.let {
            sensorManager.registerListener(it, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun getGyroscopeData(callback: (gx: Float, gy: Float, gz: Float) -> Unit) {
        val gyroscopeListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let { values ->
                    callback(values[0], values[1], values[2])
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(gyroscopeListener, gyroscope, SensorManager.SENSOR_DELAY_UI)

        // Save gyroscopeListener to remove it later
        sensorEventListener = gyroscopeListener
    }

    fun stopSensorListener() {
        sensorEventListener?.let {
            sensorManager.unregisterListener(it) // Explicitly unregister SensorEventListener
            sensorEventListener = null
        }
    }
}