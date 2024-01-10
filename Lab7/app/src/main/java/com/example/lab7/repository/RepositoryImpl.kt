package com.example.lab7.repository

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.ZonedDateTime

class RepositoryImpl : Repository {
    override suspend fun getStepsCount(
        healthConnectClient: HealthConnectClient,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ): Long {
        try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(
                        startTime.toInstant(),
                        endTime.toInstant()
                    ),
                )
            )
            val stepCount = response[StepsRecord.COUNT_TOTAL]

            return stepCount ?: 0
        } catch (e: Exception) {
            return 0
        }
    }

    override suspend fun writeStepsRecord(
        healthConnectClient: HealthConnectClient,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime,
        count: Long
    ) {
        try {
            val stepsRecord = StepsRecord(
                count = count,
                startTime = startTime.toInstant(),
                endTime = endTime.toInstant(),
                startZoneOffset = startTime.offset,
                endZoneOffset = endTime.offset,
            )
            healthConnectClient.insertRecords(listOf(stepsRecord))
        } catch (_: Exception) {
        }
    }

    override suspend fun removeStepsRecord(
        healthConnectClient: HealthConnectClient,
        startTime: ZonedDateTime,
        endTime: ZonedDateTime
    ) {
        try {
            healthConnectClient.deleteRecords(
                StepsRecord::class, TimeRangeFilter.between(
                    startTime.toInstant(),
                    endTime.toInstant()
                )
            )
        } catch (_: Exception) {
        }
    }
}