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

import androidx.compose.runtime.mutableStateOf
import com.example.makeitso.LOGIN_SCREEN
import com.example.makeitso.R
import com.example.makeitso.SETTINGS_SCREEN
import com.example.makeitso.SIGN_UP_SCREEN
import com.example.makeitso.SPLASH_SCREEN
import com.example.makeitso.common.ext.isValidEmail
import com.example.makeitso.common.snackbar.SnackbarManager
import com.example.makeitso.model.service.AccountService
import com.example.makeitso.model.service.LogService
import com.example.makeitso.model.service.StorageService
import com.example.makeitso.screens.MakeItSoViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.map

@HiltViewModel
class SettingsViewModel @Inject constructor(
  logService: LogService,
  private val accountService: AccountService,
  private val storageService: StorageService
) : MakeItSoViewModel(logService) {
  val uiState = accountService.currentUser.map{
    SettingsUiState(
      it.isAnonymous,
      it.username,
      it.email,
      it.picUrl,
      it.providerInfo
    )
  }

  var editUiState = mutableStateOf(accountService.getUserInfo())
    private set

  private val email
    get() = editUiState.value.email

  private val username
    get() = editUiState.value.username

  private val picUri
    get() = editUiState.value.picUrl

  fun onEditChange(newValue: Boolean) {
    editUiState.value = editUiState.value.copy(isEditable = newValue)
  }

  fun onEmailChange(newValue: String) {
    editUiState.value = editUiState.value.copy(email = newValue)
  }

  fun onUsernameChange(newValue: String) {
    editUiState.value = editUiState.value.copy(username = newValue)
  }

  fun onPicUriChange(newValue: String) {
    editUiState.value = editUiState.value.copy(picUrl = newValue)
  }

  fun onCancelClick(openScreen: (String) -> Unit) {
    editUiState.value = editUiState.value.copy(isEditable = false)
    openScreen(SETTINGS_SCREEN)
  }


  fun onLoginClick(openScreen: (String) -> Unit) = openScreen(LOGIN_SCREEN)

  fun onSignUpClick(openScreen: (String) -> Unit) = openScreen(SIGN_UP_SCREEN)

  fun onSignOutClick(restartApp: (String) -> Unit) {
    launchCatching {
      accountService.signOut()
      restartApp(SPLASH_SCREEN)
    }
  }
  fun confirmChanges(openScreen: (String, String) -> Unit) {
    if (!email.isValidEmail()) {
      SnackbarManager.showMessage(R.string.email_error)
      return
    }

    launchCatching {
      accountService.changeProfile(EditUiState(username = username, email = email, picUrl = picUri))
      editUiState.value = editUiState.value.copy(isEditable = false)
      accountService.getUserInfo()
      openScreen(SETTINGS_SCREEN,SETTINGS_SCREEN)
    }
  }

  fun onDeleteMyAccountClick(restartApp: (String) -> Unit) {
    launchCatching {
      accountService.deleteAccount()
      restartApp(SPLASH_SCREEN)
    }
  }
}
