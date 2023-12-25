package com.example.mediastoreexifinterface

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageData(
    val uri: Uri?,
    val date: String?,
    val latitude: Double?,
    val longitude: Double?,
    val device: String?,
    val model: String?,
    val path: String?
): Parcelable