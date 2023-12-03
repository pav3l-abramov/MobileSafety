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

package com.example.makeitso.screens.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.makeitso.R.drawable as AppIcon
import com.example.makeitso.R.string as AppText
import com.example.makeitso.common.composable.*
import com.example.makeitso.common.ext.card
import com.example.makeitso.common.ext.fieldModifier
import com.example.makeitso.common.ext.spacer
import coil.compose.AsyncImage

@ExperimentalMaterialApi
@Composable
fun SettingsScreen(
  restartApp: (String) -> Unit,
  openAndPopUp: (String, String) -> Unit,
  openScreen: (String) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: SettingsViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsState(
    initial = SettingsUiState(false)
  )
  val editUi by viewModel.editUiState

  Column(
    modifier = modifier.fillMaxWidth().fillMaxHeight().verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {


    if (uiState.isAnonymousAccount) {
      BasicToolbar(AppText.settings)

      Spacer(modifier = Modifier.spacer())
      RegularCardEditor(AppText.sign_in, AppIcon.ic_sign_in, "", Modifier.card()) {
        viewModel.onLoginClick(openScreen)
      }

      RegularCardEditor(AppText.create_account, AppIcon.ic_create_account, "", Modifier.card()) {
        viewModel.onSignUpClick(openScreen)
      }
    } else {
      if (!editUi.isEditable){
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          TopAppBar(
            title = { Text("Profile") },
            backgroundColor = toolbarColor(),
            actions = {
              IconButton(onClick = {viewModel.onEditChange(!editUi.isEditable)}){
                Icon(Icons.Filled.Edit, contentDescription = "Edit profile",tint= Color(0,0,0))
              }
            }
          )

          Spacer(modifier = Modifier.spacer())

          Card(
            modifier = Modifier.size(150.dp)
          ) {
            AsyncImage(
              modifier = Modifier.fillMaxSize(),
              model = uiState.picUrl,
              contentDescription = "profile_photo",
              contentScale = ContentScale.FillBounds
            )
          }

          OutlinedTextField(
            value = uiState.username,
            singleLine = true,
            modifier = Modifier.fieldModifier(),
            onValueChange = {},
            readOnly = true,
            label = {
              Text(text = "Username")
            },
            placeholder = {Text(text = "No username", fontStyle = FontStyle.Italic)}
          )

          OutlinedTextField(
            value = uiState.email,
            singleLine = true,
            modifier = Modifier.fieldModifier(),
            onValueChange = {},
            readOnly = true,
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") }
          )

          OutlinedTextField(
            value = uiState.providerInfo,
            singleLine = true,
            modifier = Modifier.fieldModifier(),
            onValueChange = {},
            readOnly = true,
            label = {
              Text(text = "Login")
            }
          )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          SignOutCard { viewModel.onSignOutClick(restartApp) }
          DeleteMyAccountCard { viewModel.onDeleteMyAccountClick(restartApp) }
        }
      }
      else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          TopAppBar(
            title = { Text("Profile Edit") },
            backgroundColor = toolbarColor()
          )

          Spacer(modifier = Modifier.spacer())

          Card(
            modifier = Modifier.size(150.dp)
          ) {
            AsyncImage(
              modifier = Modifier.fillMaxSize(),
              model = editUi.picUrl,
              contentDescription = "profile_photo",
              contentScale = ContentScale.FillBounds
            )
          }

          OutlinedTextField(
            value = editUi.username,
            singleLine = true,
            modifier = Modifier.fieldModifier(),
            placeholder = {Text(text = uiState.username, fontStyle = FontStyle.Italic)},
            onValueChange = {viewModel.onUsernameChange(it) },
            label = {
              Text(text = "Username")
            }
          )

          OutlinedTextField(
            value = editUi.email,
            singleLine = true,
            modifier = Modifier.fieldModifier(),
            onValueChange = {viewModel.onEmailChange(it)},
            placeholder = {Text(text = uiState.email, fontStyle = FontStyle.Italic)},
            label = {
              Text(text = "Email")
            },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email") }
          )

          OutlinedTextField(
            value = editUi.picUrl,
            singleLine = true,
            modifier = Modifier.fieldModifier(),
            placeholder = {Text(text = uiState.picUrl.toString(), fontStyle = FontStyle.Italic)},
            onValueChange = {viewModel.onPicUriChange(it) },
            label = {
              Text(text = "Avatar Url")
            },
          )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CancelCard({ viewModel.onCancelClick(openScreen) },
            {viewModel.onUsernameChange(uiState.username)},
            {viewModel.onEmailChange(uiState.email)},
            {viewModel.onPicUriChange(uiState.picUrl.toString())})
          ConfirmEditCard { viewModel.confirmChanges(openAndPopUp) }
        }
      }
    }
  }
}

@ExperimentalMaterialApi
@Composable
private fun ConfirmEditCard(backToSettings: () -> Unit) {
  var showWarningDialog by remember { mutableStateOf(false) }

  RegularCardEditor(AppText.confirm_changes, AppIcon.ic_check, "", Modifier.card()) {
    showWarningDialog = true
  }

  if (showWarningDialog) {
    AlertDialog(
      title = { Text("Do you confirm changes?") },
      text = { Text("You will return to profile overview") },
      dismissButton = { DialogCancelButton(AppText.cancel) { showWarningDialog = false } },
      confirmButton = {
        DialogConfirmButton(AppText.stop_changes) {
          backToSettings()
          showWarningDialog = false
        }
      },
      onDismissRequest = { showWarningDialog = false }
    )
  }
}


@ExperimentalMaterialApi
@Composable
private fun CancelCard(backToSettings: () -> Unit, resetUsername: () -> Unit, resetEmail: () -> Unit, resetPicUri: () -> Unit) {
  var showWarningDialog by remember { mutableStateOf(false) }

  RegularCardEditor(AppText.return_to_profile, AppIcon.ic_exit, "", Modifier.card()) {
    showWarningDialog = true
  }

  if (showWarningDialog) {
    AlertDialog(
      title = { Text("Stop doing changes?") },
      text = { Text("You will return to profile overview") },
      dismissButton = { DialogCancelButton(AppText.cancel) { showWarningDialog = false } },
      confirmButton = {
        DialogConfirmButton(AppText.stop_changes) {
          backToSettings()
          resetUsername()
          resetEmail()
          resetPicUri()
          showWarningDialog = false
        }
      },
      onDismissRequest = { showWarningDialog = false }
    )
  }
}

@Composable
private fun toolbarColor(darkTheme: Boolean = isSystemInDarkTheme()): Color {
  return if (darkTheme) MaterialTheme.colors.secondary else MaterialTheme.colors.primaryVariant
}


@ExperimentalMaterialApi
@Composable
private fun SignOutCard(signOut: () -> Unit) {
  var showWarningDialog by remember { mutableStateOf(false) }

  RegularCardEditor(AppText.sign_out, AppIcon.ic_exit, "", Modifier.card()) {
    showWarningDialog = true
  }

  if (showWarningDialog) {
    AlertDialog(
      title = { Text(stringResource(AppText.sign_out_title)) },
      text = { Text(stringResource(AppText.sign_out_description)) },
      dismissButton = { DialogCancelButton(AppText.cancel) { showWarningDialog = false } },
      confirmButton = {
        DialogConfirmButton(AppText.sign_out) {
          signOut()
          showWarningDialog = false
        }
      },
      onDismissRequest = { showWarningDialog = false }
    )
  }
}

@ExperimentalMaterialApi
@Composable
private fun DeleteMyAccountCard(deleteMyAccount: () -> Unit) {
  var showWarningDialog by remember { mutableStateOf(false) }

  DangerousCardEditor(
    AppText.delete_my_account,
    AppIcon.ic_delete_my_account,
    "",
    Modifier.card()
  ) {
    showWarningDialog = true
  }

  if (showWarningDialog) {
    AlertDialog(
      title = { Text(stringResource(AppText.delete_account_title)) },
      text = { Text(stringResource(AppText.delete_account_description)) },
      dismissButton = { DialogCancelButton(AppText.cancel) { showWarningDialog = false } },
      confirmButton = {
        DialogConfirmButton(AppText.delete_my_account) {
          deleteMyAccount()
          showWarningDialog = false
        }
      },
      onDismissRequest = { showWarningDialog = false }
    )
  }
}
