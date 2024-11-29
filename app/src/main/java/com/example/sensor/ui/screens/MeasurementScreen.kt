package com.example.sensor.ui.screens

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sensor.utils.CVSExporter
import com.example.sensor.viewmodel.MeasurementViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.io.path.Path



@Composable
fun MeasurementScreen(
    viewModel: MeasurementViewModel = viewModel(),
    isTwoSystems: Boolean, // Pass the mode as a parameter
    onBackToHome: () -> Unit,
) {
    var timerValue by remember { mutableStateOf(10) } // Timer for countdown
    var isMeasuring by remember { mutableStateOf(false) } // Control for measurement
    var showExportButton by remember { mutableStateOf(false) } // Control for export button
    var exportedData by remember { mutableStateOf(emptyList<String>()) }
    var linearAccelerationDataToDisplay by remember { mutableStateOf(emptyList<Pair<Long, Float>>()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Initialize sensors and set the measurement mode
    LaunchedEffect(Unit) {
        viewModel.initialize(context)
        viewModel.setMode(isTwoSystems) // Set the mode based on input
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display current angle
        Text(text = "Current Angle: ${viewModel.angle.value}°")

        Spacer(modifier = Modifier.height(16.dp))

        // Display the timer during measurement
        if (isMeasuring) {
            Text(text = "Timer: $timerValue seconds")
        }

        Spacer(modifier = Modifier.height(16.dp))

        MeasurementGraph(data = viewModel.linearAccelerationData)

        Spacer(modifier = Modifier.height(16.dp))

        // Start/Stop measurement button
        if (isMeasuring) {
            Button(onClick = {
                isMeasuring = false
                viewModel.stopMeasurement()
                timerValue = 10 // Reset the timer
                showExportButton = true // Show export button
                linearAccelerationDataToDisplay = viewModel.linearAccelerationData
            }) {
                Text("Stop Measurement")
            }
        } else {
            Button(onClick = {
                isMeasuring = true
                viewModel.startMeasurement()
                showExportButton = false // Hide export button
                scope.launch {
                    // Start a coroutine to handle the timer
                    for (i in 10 downTo 0) {
                        timerValue = i
                        delay(1000L) // Wait for one second
                        if (!isMeasuring) break // Exit if the user stops measurement
                    }
                    if (isMeasuring) {
                        isMeasuring = false
                        viewModel.stopMeasurement()
                        timerValue = 10 // Reset timer automatically when it reaches 0
                        showExportButton = true // Show export button
                        linearAccelerationDataToDisplay = viewModel.linearAccelerationData
                    }
                }
            }) {
                Text("Start Measurement")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export data button, only visible when measurement is stopped
        if (showExportButton) {

            Button(onClick = {
                val success = viewModel.exportData(context)
                if (success) {
                    Toast.makeText(context, "Data exported successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to export data.", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Export Data")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        // Display linear acceleration data
        if (linearAccelerationDataToDisplay.isNotEmpty()) {
            Text(text = "Linear Acceleration Data:")
            linearAccelerationDataToDisplay.forEach { (timestamp, angle) ->
                Text(text = "Timestamp: $timestamp, Angle: $angle°")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // View exported data button
        Button(onClick = {
            exportedData = viewModel.getExportedData(context) // Read CSV data
        }) {
            Text("View Exported Data")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display exported data
        exportedData.forEach { line ->
            Text(text = line) // Show each line from the CSV file
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to navigate back to HomeScreen
        Button(onClick = onBackToHome) {
            Text("Back to Home")
        }
    }


}

@Composable
fun MeasurementGraph(data: List<Pair<Long, Float>>) {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .padding(16.dp)) {

        // Kontrollera om datan är tom
        if (data.isEmpty()) return@Canvas

        val path = Path() // Använd Compose Path
        val maxValue = data.maxOf { it.second }
        val minValue = data.minOf { it.second }
        val timeSpan = data.last().first - data.first().first
        val widthPerTime = size.width / timeSpan
        val heightPerValue = size.height / (maxValue - minValue)

        data.forEachIndexed { index, (time, value) ->
            val x = (time - data.first().first) * widthPerTime
            val y = size.height - (value - minValue) * heightPerValue

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // Rita Path med rätt färg och stroke
        drawPath(
            path = path,
            color = androidx.compose.ui.graphics.Color.Blue,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )
    }
}


