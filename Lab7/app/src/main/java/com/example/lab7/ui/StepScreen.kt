package com.example.lab7.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.navigation.NavHostController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stepscalendar.R
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import io.github.boguszpawlowski.composecalendar.selection.DynamicSelectionState
import io.github.boguszpawlowski.composecalendar.selection.SelectionMode
import kotlinx.coroutines.launch
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepScreen (viewModel: StepViewModel = viewModel(), navController: NavHostController){

    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(contentPadding)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current

            val healthConnectClient = HealthConnectClient.getOrCreate(context)

            val error by remember { viewModel.error }

            if (error != "") {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                viewModel.error.value = ""
            }

            val calendarState = rememberSelectableCalendarState()

            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                SelectableCalendar(calendarState = calendarState)

                SelectionControls(
                    selectionState = calendarState.selectionState,
                    viewModel,
                    healthConnectClient
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionControls(
    selectionState: DynamicSelectionState,
    vm: StepViewModel,
    healthConnectClient: HealthConnectClient
) {
    val stepsCount by remember { vm.stepsCount }

    val coroutineScope = rememberCoroutineScope()

    val startOfDay = selectionState.selection.firstOrNull()?.atStartOfDay(ZoneId.systemDefault())
        ?.withFixedOffsetZone()
    val endOfDay =
        selectionState.selection.lastOrNull()?.atStartOfDay(ZoneId.systemDefault())?.plusDays(1)
            ?.withFixedOffsetZone()
    coroutineScope.launch {
        if (startOfDay != null && endOfDay != null) {
            vm.getStepsCount(
                healthConnectClient,
                startOfDay,
                endOfDay
            )
        }
    }

    if (stepsCount.isNotBlank()) {
        Text(text = "Шаги: $stepsCount")
    }
    val newStepsData by remember { vm.newStepData }
    Row(

        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = newStepsData,
            onValueChange = {  vm.newStepData.value = it },
            label = {
                Text(
                    text = "Пройденные шаги",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )


    }
    Row(Modifier.fillMaxSize()) {
        Button(onClick = {
            coroutineScope.launch {
                if (startOfDay != null && endOfDay != null) {
                    vm.writeStepsRecord(
                        healthConnectClient,
                        startOfDay,
                        endOfDay
                    )
                }

            }
        }) {
            Text(text = "Запись")
        }



        Button(onClick = {
            coroutineScope.launch {
                if (startOfDay != null && endOfDay != null) {
                    vm.removeStepsRecord(
                        healthConnectClient,
                        startOfDay,
                        endOfDay,
                    )
                }

            }
        },
            colors = ButtonDefaults.buttonColors(containerColor  = Color.Red)
        ) {
            Text(text = "Удалить")
        }

    }


}