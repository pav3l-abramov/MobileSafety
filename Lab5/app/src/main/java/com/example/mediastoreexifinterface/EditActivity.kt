package com.example.mediastoreexifinterface

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.mediastoreexifinterface.ui.theme.MediastoreExifInterfaceTheme


class EditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediastoreExifInterfaceTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    // Calling the composable function
                    // to display element and its contents
                    MainContent2()

                }
            }
        }
    }
}

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent2() {
        val context = LocalContext.current
        val intent = (context as EditActivity).intent
        var imgURI = intent.getStringExtra("imageUri")
        var dateTime = intent.getStringExtra("DateTime")
        var latitude = intent.getStringExtra("Latitude")
        var longitude = intent.getStringExtra("Longitude")
        var device = intent.getStringExtra("Device")
        var model = intent.getStringExtra("Model")
        val dateTimeText = remember { mutableStateOf(dateTime?.let { TextFieldValue(it) }) }
        val latitudeText = remember { mutableStateOf(latitude?.let { TextFieldValue(it) }) }
        val longitudeText = remember { mutableStateOf(longitude?.let { TextFieldValue(it) }) }
        val deviceText = remember { mutableStateOf(device?.let { TextFieldValue(it) }) }
        val modelText = remember { mutableStateOf(model?.let { TextFieldValue(it) }) }
        Column(
            Modifier
                .fillMaxWidth()
                .absolutePadding(10.dp, 10.dp, 10.dp, 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            dateTimeText.value?.let {
                OutlinedTextField(
                    singleLine = true,
                    value = it,
                    onValueChange = {
                        dateTimeText.value = it
                    },

                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Enter dateTime")
                    },
                )
            }
            latitudeText.value?.let {
                OutlinedTextField(
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    value = it,
                    onValueChange = {
                        latitudeText.value = it
                    },

                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Enter latitude")
                    },
                )
            }
            longitudeText.value?.let {
                OutlinedTextField(
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    value = it,
                    onValueChange = {
                        longitudeText.value = it
                    },

                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Enter longitude")
                    },
                )
            }
            deviceText.value?.let {
                OutlinedTextField(
                    singleLine = true,
                    value = it,
                    onValueChange = {
                        deviceText.value = it
                    },

                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Enter device")
                    },
                )
            }
            modelText.value?.let {
                OutlinedTextField(
                    singleLine = true,
                    value = it,
                    onValueChange = {
                        modelText.value = it
                    },

                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(text = "Enter model")
                    },
                )
            }
            val mContext = LocalContext.current
            OutlinedButton(onClick = {

            }) {
                Text(text = "save change")
            }
        }
    }

