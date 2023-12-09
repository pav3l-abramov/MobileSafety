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
package com.example.inventory

import android.content.ClipData
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.FileProvider
import com.example.inventory.ui.theme.InventoryTheme
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
    // Tag to log to console
    private val tag = "DShare.MainActivity"

    // Domain authority for our app FileProvider
    private val fileProviderAuthority = "com.example.inventory.fileprovider"

    // Cache directory to store images
    // This is the same path specified in the @xml/file_paths and accessed from the AndroidManifest
    private val imageCacheDir = "images"

    // Name of the file to use for the thumbnail image
    private val imageFile = "image.png"

    private lateinit var sharingShortcutsManager: SharingShortcutsManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val itemDetailsViewModel: ItemDetailsViewModel by viewModels()
        //itemDetailsViewModel.emitter.observe(this, navigationEventsObserver)

        setContent {
            InventoryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InventoryApp()
                }
            }
        }
        sharingShortcutsManager = SharingShortcutsManager().also {
            it.pushDirectShareTargets(this)
        }
    }

    fun share(textToShare: String) {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, textToShare)

        // (Optional) If you want a preview title, set it with Intent.EXTRA_TITLE
        sharingIntent.putExtra(Intent.EXTRA_TITLE, "Share")

        // (Optional) if you want a preview thumbnail, create a content URI and add it
        // The system only supports content URIs
        val thumbnail = getClipDataThumbnail()
        thumbnail?.let {
            sharingIntent.clipData = it
            sharingIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        startActivity(Intent.createChooser(sharingIntent, null))
    }

    private fun getClipDataThumbnail(): ClipData? {
        return try {
            val contentUri = saveThumbnailImage()
            ClipData.newUri(contentResolver, null, contentUri)
        } catch (e: FileNotFoundException) {
            Log.e(tag, e.localizedMessage ?: "getClipDataThumbnail FileNotFoundException")
            null
        } catch (e: IOException) {
            Log.e(tag, e.localizedMessage ?: "getClipDataThumbnail IOException")
            null
        }
    }

    @Throws(IOException::class)
    private fun saveThumbnailImage(): Uri {
        val bm = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        val cachePath = File(applicationContext.cacheDir, imageCacheDir)
        cachePath.mkdirs()
        val stream = FileOutputStream("$cachePath/$imageFile")
        //bm.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        val imagePath = File(cacheDir, imageCacheDir)
        val newFile = File(imagePath, imageFile)
        return FileProvider.getUriForFile(this, fileProviderAuthority, newFile)
    }
}