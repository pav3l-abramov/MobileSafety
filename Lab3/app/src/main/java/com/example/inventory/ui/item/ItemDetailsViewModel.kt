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

package com.example.inventory.ui.item

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.Person
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.inventory.data.ItemsRepository
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.inventory.Contact
import com.example.inventory.Events
import com.example.inventory.MainActivity
import com.example.inventory.MyFragmentNavigation
import com.example.inventory.R
import com.example.inventory.data.Item
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.ArrayList
import kotlin.math.absoluteValue

/**
 * ViewModel to retrieve, update and delete an item from the [ItemsRepository]'s data source.
 */

class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository
) : ViewModel() {
    private val itemId: Int = checkNotNull(savedStateHandle[ItemDetailsDestination.itemIdArg])
    private var currentItem: Item? = null

    val emitter = Events.Emitter()

    val uiState: StateFlow<ItemDetailsUiState> =
        itemsRepository.getItemStream(itemId)
            .filterNotNull()
            .map {
                ItemDetailsUiState(outOfStock = it.quantity <= 0, itemDetails = it.toItemDetails())
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = ItemDetailsUiState()
            )

    fun reduceQuantityByOne() {
        viewModelScope.launch {
            val currentItem = uiState.value.itemDetails.toItem()
            if (currentItem.quantity > 0) {
                itemsRepository.updateItem(currentItem.copy(quantity = currentItem.quantity - 1))
            }
        }
    }

    fun shareItem(activityContext: Context) {
        val item = uiState.value.itemDetails.toItem()
        emitter.emitAndExecute(MyFragmentNavigation.ShareProduct(
            "Item: ${item.name}\n" +
                    "Price: ${item.price}\n" +
                    "Quantity: ${item.quantity}\n" +
                    "Supplier: ${item.supplierName}\n" +
                    "Email: ${item.supplierEmail}\n" +
                    "Phone: ${item.supplierPhone}\n"
        ))
    }

//    fun shareItem(activityContext: Context) {
//        val currentItem = uiState.value.itemDetails.toItem()
//        makeIntent(currentItem)
//        activityContext?.startActivity(makeIntent(currentItem))
//    }
//
//    fun makeIntent(item: Item): Intent {
//        val sendIntent: Intent = Intent().apply {
//            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_TEXT,
//                "Item: ${item.name}\n" +
//                     "Price: ${item.price}\n" +
//                     "Quantity: ${item.quantity}\n" +
//                     "Supplier: ${item.supplierName}\n" +
//                     "Email: ${item.supplierEmail}\n" +
//                     "Phone: ${item.supplierPhone}\n")
//            type = "text/plain"
//        }
//        return Intent.createChooser(sendIntent, null)
//    }

    suspend fun deleteItem() {
        itemsRepository.deleteItem(uiState.value.itemDetails.toItem())
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * UI state for ItemDetailsScreen
 */
data class ItemDetailsUiState(
    val outOfStock: Boolean = true,
    val itemDetails: ItemDetails = ItemDetails()
)
