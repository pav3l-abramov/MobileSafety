package com.example.lab7.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
    navController = navController,
    startDestination = StepScreenRoutes.Step.name
    ) {

        composable(StepScreenRoutes.Step.name) {
            StepScreen(navController = navController)
        }
    }
}

enum class StepScreenRoutes {
    Step,
}