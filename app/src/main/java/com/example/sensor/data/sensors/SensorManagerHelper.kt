package com.example.sensor.data.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class SensorManagerHelper(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var accelerometerListener: SensorEventListener? = null
    private var gyroscopeListener: SensorEventListener? = null

    fun startAccelerometerListener(callback: (x: Float, y: Float, z: Float) -> Unit) {
        if (accelerometer == null) {
            Log.e("SensorCheck", "Accelerometer is not available on this device")
            return
        }

        accelerometerListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let { values ->
                    callback(values[0], values[1], values[2])
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        Log.d("SensorCheck", "Accelerometer listener started")
    }

    fun startGyroscopeListener(callback: (gx: Float, gy: Float, gz: Float) -> Unit) {

        if (gyroscope == null) {
            Log.e("SensorCheck", "Gyroscope is not available on this device")
            return
        }

        gyroscopeListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.let { values ->
                    callback(values[0], values[1], values[2])
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(gyroscopeListener, gyroscope, SensorManager.SENSOR_DELAY_UI)
        Log.d("SensorCheck", "Gyroscope listener started")
    }

    fun stopAccelerometerListener() {
        accelerometerListener?.let {
            sensorManager.unregisterListener(it)
            accelerometerListener = null
            Log.d("SensorCheck", "Accelerometer listener stopped")
        }
    }

    fun stopGyroscopeListener() {
        gyroscopeListener?.let {
            sensorManager.unregisterListener(it)
            gyroscopeListener = null
            Log.d("SensorCheck", "Gyroscope listener stopped")
        }
    }

    fun stopAllListeners() {
        stopAccelerometerListener()
        stopGyroscopeListener()
    }
}
