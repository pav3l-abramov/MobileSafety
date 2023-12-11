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

package com.example.inventory.ui.item.editItem

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ItemsRepository
import com.example.inventory.ui.item.itemEntry.ItemDetails
import com.example.inventory.ui.item.itemEntry.ItemUiState
import com.example.inventory.ui.item.itemEntry.toItem
import com.example.inventory.ui.item.itemEntry.toItemUiState
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve and update an item from the [ItemsRepository]'s data source.
 */

/**
 * ViewModel to retrieve and update an item from the [ItemsRepository]'s data source.
 */
class ItemEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository
) : ViewModel() {
    /**
     * Holds current item ui state
     */
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    private val itemId: Int = checkNotNull(savedStateHandle[ItemEditDestination.itemIdArg])

    init {
        viewModelScope.launch {
            itemUiState = itemsRepository.getItemStream(itemId)
                .filterNotNull()
                .first()
                .toItemUiState(true)
        }
    }

    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank() && supplierName.isNotBlank()
        }
    }

    private fun validateEmail(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            supplierEmail.isBlank() || Patterns.EMAIL_ADDRESS.matcher(supplierEmail).matches()
        }
    }

    private fun validatePhone(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            supplierPhone.isBlank() || (Patterns.PHONE.matcher(supplierPhone).matches()
                    && supplierPhone.length >= 5)
        }
    }

    private fun validateQuantity(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            quantity.isNotBlank() && quantity.all { it in '0'..'9' }
        }
    }

    private fun validatePrice(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            price.isNotBlank() && price.toDoubleOrNull() != null
        }
    }

    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState =
            ItemUiState(itemDetails = itemDetails, isEntryValid = validateInput(itemDetails) && validateEmail(itemDetails) && validatePhone(itemDetails))
    }

    suspend fun updateItem() {
        if (validateInput(itemUiState.itemDetails) && validateQuantity(itemUiState.itemDetails)
            && validatePrice(itemUiState.itemDetails) && validateEmail(itemUiState.itemDetails)
            && validatePhone(itemUiState.itemDetails)) {
            itemsRepository.updateItem(itemUiState.itemDetails.toItem())
        }
    }
}