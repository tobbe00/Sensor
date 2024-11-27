package com.example.sensor.data.processor

class AngleProcessor {
    private var previousEWMA = 0f

    fun calculateAngleUsingEWMA(input: Float, alpha: Float = 0.1f): Float {
        val output = alpha * input + (1 - alpha) * previousEWMA
        previousEWMA = output
        return output
    }

    fun calculateAngleUsingFusion(linearInput: Float, gyroInput: Float, alpha: Float = 0.98f): Float {
        return alpha * linearInput + (1 - alpha) * gyroInput
    }
}
