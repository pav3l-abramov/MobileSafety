package com.example.inventory.ui.setting

import android.app.Application
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.inventory.ui.item.itemEntry.ItemDetails
import java.io.File

class SettingViewModel(private val app: Application) : ViewModel() {
    private val masterKey = MasterKey.Builder(app)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    private val sharedPreferences = EncryptedSharedPreferences.create(
        app,
        "setting",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var settingUiState by mutableStateOf( SettingUiState(
        defaultSupplier = sharedPreferences.getString("defaultSupplier", "DefaultSupplier")!!,
        defaultEmail = sharedPreferences.getString("defaultEmail", "example@gmail.com")!!,
        defaultPhone = sharedPreferences.getString("defaultPhone", "88005553535")!!,
        defaultAdditionalNumber = sharedPreferences.getString("defaultAdditionalNumber", "555555")!!,
        useDefaultValues = sharedPreferences.getBoolean("useDefaultValues", false),
        hideSensitiveData = sharedPreferences.getBoolean("hideSensitiveData", false),
        allowSharingData = sharedPreferences.getBoolean("allowSharingData", false)))
        private set

    init {
        initializeSettings()
    }

    fun getStringPref(string: String): String {
        return sharedPreferences.getString(string, "???")!!
    }

    fun getBoolPref(string: String): Boolean {
        return sharedPreferences.getBoolean(string, false)!!
    }

    private fun initializeSettings() {
        if (sharedPreferences.contains("defaultSupplier")) return

        val editor = sharedPreferences.edit()
        editor.putString("defaultSupplier", "DefaultSupplier").apply()
        editor.putString("defaultEmail", "example@gmail.com").apply()
        editor.putString("defaultPhone", "88005553535").apply()
        editor.putString("defaultAdditionalNumber", "555555").apply()
        editor.putBoolean("useDefaultValues", false).apply()
        editor.putBoolean("hideSensitiveData", false).apply()
        editor.putBoolean("allowSharingData", false).apply()
        editor.apply()
    }

    fun updateSettingUiState(setting: SettingUiState) {
        settingUiState =
            SettingUiState(
                defaultSupplier = setting.defaultSupplier,
                defaultEmail = setting.defaultEmail,
                defaultPhone = setting.defaultPhone,
                defaultAdditionalNumber = setting.defaultAdditionalNumber,
                useDefaultValues = setting.useDefaultValues,
                hideSensitiveData = setting.hideSensitiveData,
                allowSharingData = setting.allowSharingData
            )
    }

    fun validateSupplier(uiState: SettingUiState = settingUiState): Boolean {
        return with(uiState) {
            defaultSupplier.isNotBlank()
        }
    }

    fun validateEmail(uiState: SettingUiState = settingUiState): Boolean {
        return with(uiState) {
            defaultEmail.isBlank() || Patterns.EMAIL_ADDRESS.matcher(defaultEmail).matches()
        }
    }

    fun validatePhone(uiState: SettingUiState = settingUiState): Boolean {
        return with(uiState) {
            defaultPhone.isBlank() || (Patterns.PHONE.matcher(defaultPhone).matches()
                    && defaultPhone.length >= 5)
        }
    }
    fun validateAdditionalNumber(uiState: SettingUiState = settingUiState): Boolean {
        return with(uiState) {
            defaultAdditionalNumber.isBlank() || (Patterns.PHONE.matcher(defaultAdditionalNumber).matches()
                    && defaultAdditionalNumber.length >= 5)
        }
    }

    fun saveSetting() {
        sharedPreferences.edit()
            .putString("defaultSupplier",settingUiState.defaultSupplier)
            .putString("defaultEmail",settingUiState.defaultEmail)
            .putString("defaultPhone",settingUiState.defaultPhone)
            .putString("defaultAdditionalNumber",settingUiState.defaultAdditionalNumber)
            .putBoolean("useDefaultValues", settingUiState.useDefaultValues)
            .putBoolean("hideSensitiveData", settingUiState.hideSensitiveData)
            .putBoolean("allowSharingData", settingUiState.allowSharingData).apply()
    }

    fun encryptFile(file: File) = EncryptedFile.Builder(
        app,
        file,
        masterKey,
        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
    ).build()
}

data class SettingUiState(
    val defaultSupplier: String = "",
    val defaultEmail: String = "",
    val defaultPhone: String = "",
    val defaultAdditionalNumber: String = "",
    val useDefaultValues: Boolean = false,
    val hideSensitiveData: Boolean = false,
    val allowSharingData: Boolean = false
)