package com.example.sensor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.sensor.ui.screens.HomeScreen
import com.example.sensor.ui.screens.MeasurementScreen
import com.example.sensor.viewmodel.MeasurementViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("home") }
            val viewModel: MeasurementViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

            when (currentScreen) {
                "home" -> HomeScreen(onNavigateToMeasurementScreen = { currentScreen = "measure" })
                "measure" -> MeasurementScreen(viewModel = viewModel) {
                    currentScreen = "home" // Navigera tillbaka till HomeScreen
                }
            }
        }
    }
}
