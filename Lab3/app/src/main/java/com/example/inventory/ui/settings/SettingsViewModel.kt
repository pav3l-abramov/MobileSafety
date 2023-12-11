package com.example.inventory.ui.settings

import android.app.Application
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.inventory.ui.item.ItemDetails
import java.io.File

class SettingsViewModel(private val app: Application) : ViewModel() {
    private val masterKey = MasterKey.Builder(app)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    private val sharedPreferences = EncryptedSharedPreferences.create(
        app,
        "settings",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var settingsUiState by mutableStateOf( SettingsUiState(
        defaultSupplier = sharedPreferences.getString("defaultSupplier", "No name")!!,
        defaultEmail = sharedPreferences.getString("defaultEmail", "example@ex.com")!!,
        defaultPhone = sharedPreferences.getString("defaultPhone", "+71234567890")!!,
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
        editor.putString("defaultSupplier", "Дефолтный поставщик").apply()
        editor.putString("defaultEmail", "mail@mail.com").apply()
        editor.putString("defaultPhone", "+79045011111").apply()
        editor.putBoolean("useDefaultValues", false).apply()
        editor.putBoolean("hideSensitiveData", false).apply()
        editor.putBoolean("allowSharingData", false).apply()
        editor.apply()
    }

    fun updateSettingsUiState(settings: SettingsUiState) {
        settingsUiState =
            SettingsUiState(
                defaultSupplier = settings.defaultSupplier,
                defaultEmail = settings.defaultEmail,
                defaultPhone = settings.defaultPhone,
                useDefaultValues = settings.useDefaultValues,
                hideSensitiveData = settings.hideSensitiveData,
                allowSharingData = settings.allowSharingData
            )
    }

    fun validateSupplier(uiState: SettingsUiState = settingsUiState): Boolean {
        return with(uiState) {
            defaultSupplier.isNotBlank()
        }
    }

    fun validateEmail(uiState: SettingsUiState = settingsUiState): Boolean {
        return with(uiState) {
            defaultEmail.isBlank() || Patterns.EMAIL_ADDRESS.matcher(defaultEmail).matches()
        }
    }

    fun validatePhone(uiState: SettingsUiState = settingsUiState): Boolean {
        return with(uiState) {
            defaultPhone.isBlank() || (Patterns.PHONE.matcher(defaultPhone).matches()
                    && defaultPhone.startsWith("+7") && defaultPhone.length == 12)
        }
    }

    fun saveSettings() {
        sharedPreferences.edit()
        .putString("defaultSupplier",settingsUiState.defaultSupplier)
        .putString("defaultEmail",settingsUiState.defaultEmail)
        .putString("defaultPhone",settingsUiState.defaultPhone)
        .putBoolean("useDefaultValues", settingsUiState.useDefaultValues)
        .putBoolean("hideSensitiveData", settingsUiState.hideSensitiveData)
        .putBoolean("allowSharingData", settingsUiState.allowSharingData).apply()
    }

    fun encryptFile(file: File) = EncryptedFile.Builder(
        app,
        file,
        masterKey,
        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
    ).build()
}

data class SettingsUiState(
    val defaultSupplier: String = "",
    val defaultEmail: String = "",
    val defaultPhone: String = "",
    val useDefaultValues: Boolean = false,
    val hideSensitiveData: Boolean = false,
    val allowSharingData: Boolean = false
)