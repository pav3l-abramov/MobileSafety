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

package com.example.makeitso.screens.login

import android.content.IntentSender
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.makeitso.R.string as AppText
import com.example.makeitso.common.composable.*
import com.example.makeitso.common.ext.basicButton
import com.example.makeitso.common.ext.fieldModifier
import com.example.makeitso.common.ext.textButton
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
  restartApp: (String) -> Unit,
  modifier: Modifier = Modifier,
  oneTapClient: SignInClient,
  signInRequest: BeginSignInRequest,
  viewModel: LoginViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState
  val coroutineScope = rememberCoroutineScope()

  BasicToolbar(AppText.login_details)

  Column(
    modifier = modifier.fillMaxWidth().fillMaxHeight().verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    EmailField(uiState.email, viewModel::onEmailChange, Modifier.fieldModifier())
    PasswordField(uiState.password, viewModel::onPasswordChange, Modifier.fieldModifier())

    BasicButton(AppText.sign_in, Modifier.basicButton()) { viewModel.onSignInClick(restartApp) }
    val launcher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.StartIntentSenderForResult()){

      coroutineScope.launch {
        viewModel.googleSignIn(it.data ?: return@launch, oneTapClient, restartApp)
      }
    }

    BasicButton(AppText.sign_in_google, Modifier.basicButton()) {
      oneTapClient.beginSignIn(signInRequest).addOnSuccessListener {
        try{
          launcher.launch(IntentSenderRequest.Builder(it.pendingIntent.intentSender).build())
        }catch (e: IntentSender.SendIntentException) {
          Log.d("fail", "Couldn't start One Tap UI: ${e.localizedMessage}")
        }
      }.addOnFailureListener { e ->
        // No saved credentials found. Launch the One Tap sign-up flow, or
        // do nothing and continue presenting the signed-out UI.
        Log.d("failure", e.localizedMessage)
      }
    }
    BasicTextButton(AppText.forgot_password, Modifier.textButton()) {
      viewModel.onForgotPasswordClick()
    }
  }
}
