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

package com.example.inventory.ui.item.itemEntry

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import com.example.inventory.data.MethodOfCreation
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.text.NumberFormat

/**
 * ViewModel to validate and insert items in the Room database.
 */
class ItemEntryViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {

    /**
     * Holds current item ui state
     */
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    /**
     * Updates the [itemUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState =
            ItemUiState(itemDetails = itemDetails, isEntryValid = validateInput(itemDetails) && validateEmail(itemDetails) && validatePhone(itemDetails))
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


    private fun validateAdditionalNumber(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            additionalNumber.isBlank() || (Patterns.PHONE.matcher(additionalNumber).matches()
                    && additionalNumber.length >= 5)
        }
    }


    suspend fun saveItem() {
        if (validateInput() && validateEmail() && validatePhone()&& validateQuantity() && validatePrice()) {
            itemsRepository.insertItem(itemUiState.itemDetails.toItem())
        }
    }
}

/**
 * Represents Ui State for an Item.
 */
data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false
)

@Serializable
data class ItemDetails(
    @Transient val id: Int = 0,
    val name: String = "",
    val price: String = "",
    val quantity: String = "",
    val supplierName: String = "",
    val supplierEmail: String = "",
    val supplierPhone: String = "",
    val additionalNumber: String = "",
    @Transient var methodOfCreation: MethodOfCreation = MethodOfCreation.MANUAL
)

/**
 * Extension function to convert [ItemDetails] to [Item]. If the value of [ItemDetails.price] is
 * not a valid [Double], then the price will be set to 0.0. Similarly if the value of
 * [ItemDetails.quantity] is not a valid [Int], then the quantity will be set to 0
 */
fun ItemDetails.toItem(): Item = Item(
    id = id,
    name = name,
    price = price.toDoubleOrNull() ?: 0.0,
    quantity = quantity.toIntOrNull() ?: 0,
    supplierName = supplierName,
    supplierEmail = supplierEmail,
    supplierPhone = supplierPhone,
    additionalNumber=additionalNumber,
    methodOfCreation = methodOfCreation
)

fun Item.formatedPrice(): String {
    return NumberFormat.getCurrencyInstance().format(price)
}

/**
 * Extension function to convert [Item] to [ItemUiState]
 */
fun Item.toItemUiState(isEntryValid: Boolean = false): ItemUiState = ItemUiState(
    itemDetails = this.toItemDetails(),
    isEntryValid = isEntryValid
)

/**
 * Extension function to convert [Item] to [ItemDetails]
 */
fun Item.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    name = name,
    price = price.toString(),
    quantity = quantity.toString(),
    supplierName = supplierName,
    supplierEmail = supplierEmail,
    supplierPhone = supplierPhone,
    additionalNumber=additionalNumber,
    methodOfCreation = methodOfCreation
)


