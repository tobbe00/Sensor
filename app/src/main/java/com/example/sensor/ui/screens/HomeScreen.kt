package com.example.sensor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.unit.dp


@Composable
fun HomeScreen(
    onNavigateToMeasurementScreen: (Boolean) -> Unit // Boolean to represent mode
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Button for single system (accelerometer only)
            Button(
                onClick = {
                    onNavigateToMeasurementScreen(false) // Pass 'false' for single system mode
                }
            ) {
                Text(text = "Measure with Internal Accelerometer")
            }

            // Button for two systems (accelerometer + gyroscope)
            Button(
                onClick = {
                    onNavigateToMeasurementScreen(true) // Pass 'true' for two systems mode
                }
            ) {
                Text(text = "Measure with 2 Systems (Accelerometer + Gyroscope)")
            }
        }
    }
}