/*
Copyright 2022 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.example.makeitso

import android.Manifest
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Surface
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.makeitso.common.composable.PermissionDialog
import com.example.makeitso.common.composable.RationaleDialog
import com.example.makeitso.common.snackbar.SnackbarManager
import com.example.makeitso.screens.edit_task.EditTaskScreen
import com.example.makeitso.screens.login.LoginScreen
import com.example.makeitso.screens.settings.SettingsScreen
import com.example.makeitso.screens.sign_up.SignUpScreen
import com.example.makeitso.screens.splash.SplashScreen
import com.example.makeitso.screens.tasks.TasksScreen
import com.example.makeitso.theme.MakeItSoTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope

// MakeItSoActivity starts the first composable, which uses material cards that are still experimental.
// TODO: Update material dependency and experimental annotations once the API stabilizes.
@AndroidEntryPoint
@ExperimentalMaterialApi
class MakeItSoActivity : AppCompatActivity() {
  private lateinit var oneTapClient: SignInClient
  private lateinit var signInRequest: BeginSignInRequest
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    oneTapClient = Identity.getSignInClient(this)
    signInRequest = BeginSignInRequest.builder()
      .setGoogleIdTokenRequestOptions(
        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
          .setSupported(true)
          // Your server's client ID, not your Android client ID.
          .setServerClientId(getString(R.string.web_client_id))
          // Only show accounts previously used to sign in.
          .setFilterByAuthorizedAccounts(false)
          .build()
      )
      .build()

    setContent {

      MakeItSoTheme {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          RequestNotificationPermissionDialog()
        }

        Surface(color = MaterialTheme.colors.background) {
          val appState = rememberAppState()

          Scaffold(
            snackbarHost = {
              SnackbarHost(
                hostState = it,
                modifier = Modifier.padding(8.dp),
                snackbar = { snackbarData ->
                  Snackbar(snackbarData, contentColor = MaterialTheme.colors.onPrimary)
                }
              )
            },
            scaffoldState = appState.scaffoldState
          ) { innerPaddingModifier ->
            NavHost(
              navController = appState.navController,
              startDestination = SPLASH_SCREEN,
              modifier = Modifier.padding(innerPaddingModifier)
            ) {
              makeItSoGraph(appState)
            }
          }
        }
      }
    }
  }


  @ExperimentalMaterialApi
  fun NavGraphBuilder.makeItSoGraph(appState: MakeItSoAppState) {
    composable(SPLASH_SCREEN) {
      SplashScreen(openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) })
    }

    composable(SETTINGS_SCREEN) {
      SettingsScreen(
        restartApp = { route -> appState.clearAndNavigate(route) },
        openScreen = { route -> appState.navigate(route) },
        openAndPopUp = {route, popUp -> appState.navigateAndPopUp(route, popUp)}
      )
    }

    composable(LOGIN_SCREEN) {
      LoginScreen(restartApp = { route -> appState.clearAndNavigate(route) },
        oneTapClient = oneTapClient, signInRequest = signInRequest)


    }

    composable(SIGN_UP_SCREEN) {
      SignUpScreen(openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) })
    }

    composable(TASKS_SCREEN) { TasksScreen(openScreen = { route -> appState.navigate(route) }) }

    composable(
      route = "$EDIT_TASK_SCREEN$TASK_ID_ARG",
      arguments = listOf(navArgument(TASK_ID) {
        nullable = true
        defaultValue = null
      })
    ) {
      EditTaskScreen(
        popUpScreen = { appState.popUp() }
      )
    }

  }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun RequestNotificationPermissionDialog() {
      val permissionState = rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)

      if (!permissionState.status.isGranted) {
        if (permissionState.status.shouldShowRationale) RationaleDialog()
        else PermissionDialog { permissionState.launchPermissionRequest() }
      }
    }

    @Composable
    fun rememberAppState(
      scaffoldState: ScaffoldState = rememberScaffoldState(),
      navController: NavHostController = rememberNavController(),
      snackbarManager: SnackbarManager = SnackbarManager,
      resources: Resources = resources(),
      coroutineScope: CoroutineScope = rememberCoroutineScope()
    ) =
      remember(scaffoldState, navController, snackbarManager, resources, coroutineScope) {
        MakeItSoAppState(scaffoldState, navController, snackbarManager, resources, coroutineScope)
      }
  @Composable
  @ReadOnlyComposable
  fun resources(): Resources {
    LocalConfiguration.current
    return LocalContext.current.resources
  }



  }

