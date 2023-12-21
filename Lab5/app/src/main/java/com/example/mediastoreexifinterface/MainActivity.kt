package com.example.mediastoreexifinterface

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.load
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.GpsDirectory
import com.drew.metadata.jpeg.JpegDirectory
import com.example.mediastoreexifinterface.ui.theme.MediastoreExifInterfaceTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {

            MediastoreExifInterfaceTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Greeting(modifier: Modifier = Modifier) {
        val mContext = LocalContext.current
        var imageUri by remember {
            mutableStateOf<Uri?>(null)
        }
        var imagedate by remember {
            mutableStateOf<String?>(null)
        }
        var imagelatitude by remember {
            mutableStateOf<Double?>(null)
        }
        var imagelongitude by remember {
            mutableStateOf<Double?>(null)
        }
        var imagedevice by remember {
            mutableStateOf<String?>(null)
        }
        var imagemodel by remember {
            mutableStateOf<String?>(null)
        }
        var imagecheck by remember {
            mutableStateOf<Boolean?>(null)
        }
        val launcher = rememberLauncherForActivityResult(
            contract =
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            imageUri = uri
            lifecycleScope.launch(Dispatchers.Main) {
                val result = async(Dispatchers.IO) {
                    val inputStream = uri?.let { contentResolver.openInputStream(it) }
                    val metadata = ImageMetadataReader.readMetadata(inputStream)
//                    for (directory in metadata.directories) {
//                        for (tag in directory.tags) {
                    //     Log.i("here!!!", "$imageUri")
//                        }
//                    }
                    val exifDirectory =
                        metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
                    val model = exifDirectory?.getString(ExifIFD0Directory.TAG_MODEL)
                    val device = exifDirectory?.getString(ExifIFD0Directory.TAG_MAKE)
                    val date = exifDirectory?.getString(ExifIFD0Directory.TAG_DATETIME)

                    val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
                    val latitude = gpsDirectory?.geoLocation?.latitude
                    val longitude = gpsDirectory?.geoLocation?.longitude

                    ImageData(date, latitude, longitude, device, model)
                    imagedate = date
                    imagelatitude = latitude
                    imagelongitude = longitude
                    imagedevice = device
                    imagemodel = model
                    imagecheck = true
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .absolutePadding(10.dp, 10.dp, 10.dp, 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Main", modifier = modifier
            )
            Button(onClick = {
                launcher.launch("image/*")
            }) {
                Text(text = "pick image")
            }
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier
                    .padding(4.dp)
                    .width(200.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
            Row(modifier = modifier) {

                imagedate?.let { Text(text = "DateTime: " + it, fontWeight = FontWeight.Bold) }
            }
            Row(modifier = modifier) {

                imagelatitude?.let { Text(text = "Latitude: " + it, fontWeight = FontWeight.Bold) }
            }
            Row(modifier = modifier) {

                imagelongitude?.let {
                    Text(
                        text = "Longitude: " + it,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(modifier = modifier) {

                imagedevice?.let { Text(text = "Device: " + it, fontWeight = FontWeight.Bold) }
            }
            Row(modifier = modifier) {

                imagemodel?.let { Text(text = "Model: " + it, fontWeight = FontWeight.Bold) }
            }
            if (imagecheck != null) {
                Button(onClick = {
                    val intent = (Intent(mContext, EditActivity::class.java))
                    intent.putExtra("imageUri", imageUri.toString())
                    intent.putExtra("DateTime", imagedate)
                    intent.putExtra("Latitude", imagelatitude.toString())
                    intent.putExtra("Longitude", imagelongitude.toString())
                    intent.putExtra("Device", imagedevice)
                    intent.putExtra("Model", imagemodel)
                    mContext.startActivity(intent)
                }) {
                    Text(text = "edit tags")
                }
            }
        }
    }
}