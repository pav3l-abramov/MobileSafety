package com.example.lab7

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import com.example.lab7.ui.Navigation
import com.example.lab7.ui.theme.Lab7Theme

class MainActivity : ComponentActivity(){
    private val PERMISSIONS =
        setOf(
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getWritePermission(StepsRecord::class)
        )
    private lateinit var requestPermissions: ActivityResultLauncher<Set<String>>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val requestPermissionActivityContract =
            PermissionController.createRequestPermissionResultContract()

        requestPermissions =
            registerForActivityResult(requestPermissionActivityContract) { granted ->
                if (granted.containsAll(PERMISSIONS)) {
                    //todo
                } else {
                    Toast.makeText(this, "Нет разрешения на чтение/запись шагов", Toast.LENGTH_LONG)
                        .show()
                    finish()
                }
            }
        setContent {
            Lab7Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val providerPackageName = "com.google.android.apps.healthdata"
                    val context = LocalContext.current
                    when (HealthConnectClient.getSdkStatus(context, providerPackageName)) {
                        HealthConnectClient.SDK_UNAVAILABLE -> {
                            Toast.makeText(LocalContext.current, "SDK_UNV", Toast.LENGTH_LONG)
                                .show()
                        }
                        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                            // Optionally redirect to package installer to find a provider, for example:
                            val uriString =
                                "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW).apply {
                                    setPackage("com.android.vending")
                                    data = Uri.parse(uriString)
                                    putExtra("overlay", true)
                                    putExtra("callerId", context.packageName)
                                }
                            )
                        }

                        HealthConnectClient.SDK_AVAILABLE -> {
                            val healthConnectClient = HealthConnectClient.getOrCreate(context)
                            LaunchedEffect(Unit) {
                                checkPermissionsAndRun(healthConnectClient)
                            }
                            Navigation()


                        }
                    }

                }
            }
        }
    }

    suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()

        if (granted.containsAll(PERMISSIONS)) {
        } else {
            requestPermissions.launch(PERMISSIONS)
        }
    }
}

