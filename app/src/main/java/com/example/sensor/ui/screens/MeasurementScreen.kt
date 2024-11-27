package com.example.sensor.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sensor.utils.CVSExporter
import com.example.sensor.viewmodel.MeasurementViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MeasurementScreen(viewModel: MeasurementViewModel = viewModel(), onBackToHome: () -> Unit) {
    var timerValue by remember { mutableStateOf(10) } // Timer som räknar ner
    var isMeasuring by remember { mutableStateOf(false) } // Kontroll för mätning
    var showExportButton by remember { mutableStateOf(false) } // Kontroll för export-knappen
    var exportedData by remember { mutableStateOf(emptyList<String>()) }
    var linearAccelerationDataToDisplay by remember { mutableStateOf(emptyList<Pair<Long, Float>>()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Visa aktuell vinkel
        Text(text = "Current Angle: ${viewModel.angle.value}°")

        Spacer(modifier = Modifier.height(16.dp))

        // Visa timern när mätningen är aktiv
        if (isMeasuring) {
            Text(text = "Timer: $timerValue seconds")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isMeasuring) {
            Button(onClick = {
                isMeasuring = false
                viewModel.stopMeasurement()
                timerValue = 10 // Återställ timer
                showExportButton = true // Visa export-knappen
                linearAccelerationDataToDisplay = viewModel.linearAccelerationData
            }) {
                Text("Stop Measurement")
            }
        } else {
            Button(onClick = {

                isMeasuring = true
                viewModel.startMeasurement()
                showExportButton = false // Dölj export-knappen
                scope.launch {
                    // Starta en coroutine för att hantera timern
                    for (i in 10 downTo 0) {
                        timerValue = i
                        delay(1000L) // Vänta en sekund
                        if (!isMeasuring) break // Avsluta om användaren trycker på Stop
                    }
                    if (isMeasuring) {
                        isMeasuring = false
                        viewModel.stopMeasurement()
                        timerValue = 10 // Återställ timer automatiskt när den når 0
                        showExportButton = true // Visa export-knappen
                        linearAccelerationDataToDisplay = viewModel.linearAccelerationData
                    }
                }
            }) {
                Text("Start Measurement")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visa export-knappen endast när mätningen är stoppad
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

        if (linearAccelerationDataToDisplay.isNotEmpty()) {
            Text(text = "Linear Acceleration Data:")
            linearAccelerationDataToDisplay.forEach { (timestamp, angle) ->
                Text(text = "Timestamp: $timestamp, Angle: $angle°")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            exportedData = viewModel.getExportedData(context) // Läs CSV-data
        }) {
            Text("View Exported Data")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    exportedData.forEach { line ->
        Text(text = line) // Visa varje rad i CSV-filen
    }
        Spacer(modifier = Modifier.height(16.dp))

        // Knapp för att navigera tillbaka till HomeScreen
        Button(onClick = onBackToHome) {
            Text("Back to Home")
        }
    }

