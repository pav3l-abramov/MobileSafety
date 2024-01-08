package com.example.mediastoreexifinterface

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import com.drew.metadata.exif.GpsDirectory
import com.example.mediastoreexifinterface.ui.theme.MediastoreExifInterfaceTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

import kotlinx.coroutines.launch
import java.io.File


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {


            MediastoreExifInterfaceTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home_screen") {
                        composable("home_screen") {
                            Greeting(navController=navController)// { navController.navigate("edit_screen") }
                        }
                        composable("edit_screen") {

                                val result=navController.previousBackStackEntry?.savedStateHandle?.get<ImageData>("img")


                            if (result != null) {
                                Log.d("check","${result.uri}")
                                MainContent2(navController=navController,img=result)
                            }// { navController.popBackStack() }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Greeting(modifier: Modifier = Modifier, navController: NavHostController) {
        val context = LocalContext.current
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
        var imagefilePath by remember {
            mutableStateOf<String?>(null)
        }
        val launcher = rememberLauncherForActivityResult(contract =ActivityResultContracts.GetContent()) { uri: Uri? ->imageUri = uri
            lifecycleScope.launch(Dispatchers.Main) {
                val result = async(Dispatchers.IO) {
                    val filePath= uri?.let { getRealPathFromURI(context, it) }
                    Log.d("filePath","$filePath")
                    val inputStream = uri?.let { contentResolver.openInputStream(it) }
                    val metadata = ImageMetadataReader.readMetadata(inputStream)
                    val exifDirectory = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java)
                    val model = exifDirectory?.getString(ExifIFD0Directory.TAG_MODEL)
                    val device = exifDirectory?.getString(ExifIFD0Directory.TAG_MAKE)
                    val date = exifDirectory?.getString(ExifIFD0Directory.TAG_DATETIME)
                    val gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory::class.java)
                    val latitude = gpsDirectory?.geoLocation?.latitude
                    val longitude = gpsDirectory?.geoLocation?.longitude

                    imagedate = date
                    imagelatitude = latitude
                    imagelongitude = longitude
                    imagedevice = device
                    imagemodel = model
                    imagefilePath=filePath
                    imagecheck = true
                }
            }
        }
        val img =
            ImageData(imageUri, imagedate, imagelatitude, imagelongitude, imagedevice, imagemodel,imagefilePath)

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

                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        key ="img",
                        value=img
                    )
                    navController.navigate("edit_screen")
//                    val intent = (Intent(mContext, EditActivity::class.java))
//                    mContext.startActivity(intent)
                }) {
                    Text(text = "edit tags")
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainContent2(modifier: Modifier = Modifier,navController: NavHostController,img:ImageData) {
        val context = LocalContext.current

        //Log.d("getRealPathFromURI","${img.uri?.let { getImageFilePath(context, it) }}")
    var imgURI = img.uri
    var dateTime = img.date
    var latitude = img.latitude.toString()
    var longitude = img.longitude.toString()
    var device = img.device
    var model = img.model
    val dateTimeText = remember { mutableStateOf(dateTime?.let { TextFieldValue(it) }) }
    val latitudeText = remember { mutableStateOf(latitude.let { TextFieldValue(it) }) }
    val longitudeText = remember { mutableStateOf(longitude.let { TextFieldValue(it) }) }
    val deviceText = remember { mutableStateOf(device?.let { TextFieldValue(it) }) }
    val modelText = remember { mutableStateOf(model?.let { TextFieldValue(it) }) }
        val fileRealPath = getRealPathFromURI(context, img.uri) ?: ""
        //val fileRealPathFromUri = convertUriToImageAbsolutePath(img.uri,fileRealPath)
       // val fileRealPath= img.uri?.let { getAbsolutePath(context!!, it) } ?: ""
        Column(
            Modifier
                .fillMaxWidth()
                .absolutePadding(10.dp, 10.dp, 10.dp, 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(text = "fileRealPath: $fileRealPath \n")
//        Text(text = "path: $fileRealPath \n")
//            Text(text = "fileRealPathFromUri: $fileRealPathFromUri")


        dateTimeText.value?.let {
            OutlinedTextField(
                singleLine = true,
                value = it,
                onValueChange = {
                    dateTimeText.value = it
                },

                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Enter dateTime")
                },
            )
        }
        latitudeText.value?.let {
            OutlinedTextField(
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                value = it,
                onValueChange = {
                    latitudeText.value = it
                },

                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Enter latitude")
                },
            )
        }
        longitudeText.value?.let {
            OutlinedTextField(
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                value = it,
                onValueChange = {
                    longitudeText.value = it
                },

                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Enter longitude")
                },
            )
        }
        deviceText.value?.let {
            OutlinedTextField(
                singleLine = true,
                value = it,
                onValueChange = {
                    deviceText.value = it
                },

                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Enter device")
                },
            )
        }
        modelText.value?.let {
            OutlinedTextField(
                singleLine = true,
                value = it,
                onValueChange = {
                    modelText.value = it
                },

                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Enter model")
                },
            )
        }
            AsyncImage(
                model = img.uri,
                contentDescription = null,
                modifier = Modifier
                    .padding(4.dp)
                    .width(200.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )
            OutlinedButton(onClick = {
//                val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
//                val imageFile=  File("$storageDir/Camera/$fileRealPath")
                //val imageFile: File = File("DCIM/Camera/$fileRealPath") // Replace with your image path
                //Log.d("imageFile","$imageFile")
//               val exif = ExifInterface(img.uri.toString())
//                exif.setAttribute(ExifInterface.TAG_DATETIME, "2002:01:22 00:00:00")
                img.path.run {
                    ExifInterface(File(this)).run {
                        Log.d("123131","123131312")
                        setAttribute(ExifInterface.TAG_DATETIME,dateTimeText.value.toString())
                        setAttribute(ExifInterface.TAG_GPS_LATITUDE, latitudeText.value.toString())
                        setAttribute(ExifInterface.TAG_GPS_LONGITUDE, longitudeText.value.toString())
                        setAttribute(ExifInterface.TAG_MODEL, modelText.value.toString())
                        setAttribute(ExifInterface.TAG_MAKE, deviceText.value.toString())
                        saveAttributes()
                    }
                }
                navController.popBackStack()

                //onClick()
                //System.exit(0)
            }) {
                Text(text = "save change")
            }
        }
    }
    fun getFilePath(context: Context, uri: Uri?): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.MediaColumns.DISPLAY_NAME
        )
        try {
            if (uri == null) return null
            cursor = context.contentResolver.query(uri, projection, null, null,
                null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    fun getAbsolutePathFromUri(context: Context, imageUri: Uri): String? {
        var absolutePath: String? = null

        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = context.contentResolver.query(imageUri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                absolutePath = it.getString(columnIndex)
            }
        }

        return absolutePath
    }

    fun getExifInterfaceAbsolutePath(context: Context, imageUri: Uri): String? {
        // Получаем абсолютный путь к файлу из Uri
        val absolutePath = getAbsolutePathFromUri(context, imageUri)

        if (absolutePath != null) {
            // Создаем экземпляр ExifInterface, передавая ему абсолютный путь к файлу
            val exifInterface = ExifInterface(absolutePath)

            // Теперь можно работать с объектом ExifInterface, вызывая его методы для получения дополнительной информации
            // Например, чтобы получить ориентацию изображения, можно вызвать:
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

            // Другие возможные атрибуты можно найти в документации к классу ExifInterface
        }

        return absolutePath
    }

    fun getRealPathFromURI_API19(context: Context, uri: Uri?): String? {
        var filePath = ""
        val wholeID = DocumentsContract.getDocumentId(uri)
        val id = wholeID.split(":").toTypedArray()[1]
        val column = arrayOf(MediaStore.Images.Media.DATA)

        // where id is equal to
        val sel = MediaStore.Images.Media._ID + "=?"
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            column, sel, arrayOf(id), null
        )
        val columnIndex: Int = cursor!!.getColumnIndex(column[0])
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex)
        }
        cursor.close()
        return filePath
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri?): String? {
        var cursor: Cursor? = null
        return try {
            val proj =
                arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
            cursor!!.moveToFirst()
            val column_index = cursor.getColumnIndex(proj[0])

            cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }


}
