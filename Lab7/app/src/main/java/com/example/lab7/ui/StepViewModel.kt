package com.example.lab7.ui

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.AndroidViewModel
import com.example.lab7.repository.Repository
import com.example.lab7.repository.RepositoryImpl
import java.time.ZonedDateTime

class StepViewModel(app: Application) : AndroidViewModel(app) {

    var stepsCount = mutableStateOf("")
    var newStepData = mutableStateOf("")
    var error = mutableStateOf("")

    private val healthConnectRepository: Repository = RepositoryImpl()
    private val stepsValidatorUseCase = Validator()

    suspend fun getStepsCount(
        healthConnectClient: HealthConnectClient,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ) {
        stepsCount.value =
            healthConnectRepository.getStepsCount(healthConnectClient, startTime, endTime)
                .toString()
    }

    suspend fun writeStepsRecord(
        healthConnectClient: HealthConnectClient,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ) {
        error.value = stepsValidatorUseCase.validateStepsCount(newStepData.value)
        if (error.value != "") return
        healthConnectRepository.writeStepsRecord(
            healthConnectClient,
            startTime,
            endTime,
            newStepData.value.toLongOrNull() ?: 0
        )
        stepsCount.value =
            healthConnectRepository.getStepsCount(healthConnectClient, startTime, endTime)
                .toString()
    }

    suspend fun removeStepsRecord(
        healthConnectClient: HealthConnectClient,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime,
    ) {
        healthConnectRepository.removeStepsRecord(healthConnectClient, startTime, endTime)
        stepsCount.value =
            healthConnectRepository.getStepsCount(healthConnectClient, startTime, endTime)
                .toString()
    }

}