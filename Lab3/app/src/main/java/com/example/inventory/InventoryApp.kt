/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.inventory

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.material.icons.Icons.Filled
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.inventory.R.string
import com.example.inventory.ui.AppViewModelProvider
import com.example.inventory.ui.item.ItemDetailsViewModel
import com.example.inventory.ui.navigation.InventoryNavHost
import com.example.inventory.ui.settings.SettingsViewModel

/**
 * Top level composable that represents screens for the application.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InventoryApp(navController: NavHostController = rememberNavController(),
                 settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    InventoryNavHost(navController = navController, settingsViewModel = settingsViewModel)
}

/**
 * App bar to display title and conditionally display the back navigation.
 */
@Composable
fun InventoryTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    showShareButton: Boolean = false,
    canShare: Boolean = false,
    settings: Boolean = false,
    uploadFile: Boolean = false,
    navigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {},
    viewModel: ItemDetailsViewModel? = null,
    intentResultLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null
) {
    val context = LocalContext.current

    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Filled.ArrowBack,
                        contentDescription = stringResource(string.back_button)
                    )
                }
            }
        },
        actions = {
            if (showShareButton) {
                IconButton(onClick = {
                    if(canShare) {
                        viewModel!!.shareItem(context)
                    } else {
                        Toast.makeText(context, "This feature is disabled in the settings", Toast.LENGTH_LONG).show()
                    }
                                     },
                    modifier = Modifier) {
                    Icon(
                        imageVector = Filled.Share,
                        contentDescription = stringResource(string.back_button),
                        tint = if (canShare) Color.Unspecified else Color.LightGray
                    )
                }
            }
            if (settings) {
                IconButton(onClick = navigateToSettings,
                    modifier = Modifier) {
                    Icon(
                        imageVector = Filled.Settings,
                        contentDescription = stringResource(string.back_button)
                    )
                }
            }
            if (uploadFile) {
                IconButton(onClick = { intentResultLauncher?.launch(openFileIntent()) },
                    modifier = Modifier) {
                    Icon(
                        imageVector = Filled.ExitToApp,
                        contentDescription = stringResource(string.back_button)
                    )
                }
            }
        }
    )
}

private fun openFileIntent() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
    addCategory(Intent.CATEGORY_OPENABLE)
    type = "application/json"
}
