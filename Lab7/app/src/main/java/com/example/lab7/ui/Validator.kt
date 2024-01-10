package com.example.lab7.ui

class Validator {
    fun validateStepsCount(steps: String): String {
        return if (steps.isNotBlank() && !steps.contains(" ") && steps.toLongOrNull() != null) ""
        else {
            "Некорректное количество шагов"
        }
    }
}