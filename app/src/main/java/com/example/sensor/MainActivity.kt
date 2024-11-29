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
            var isTwoSystems by remember { mutableStateOf(false) } // Track the mode
            val viewModel: MeasurementViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

            when (currentScreen) {
                "home" -> HomeScreen(
                    onNavigateToMeasurementScreen = { mode ->
                        isTwoSystems = mode // Set the selected mode
                        currentScreen = "measure" // Navigate to the measurement screen
                    }
                )
                "measure" -> MeasurementScreen(
                    viewModel = viewModel,
                    isTwoSystems = isTwoSystems, // Pass the mode to the screen
                    onBackToHome = { currentScreen = "home" } // Navigate back to hom

                )
            }
        }
    }
}
