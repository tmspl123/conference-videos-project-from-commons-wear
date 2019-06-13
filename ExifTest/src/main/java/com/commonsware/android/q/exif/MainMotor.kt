/*
  Copyright (c) 2019 CommonsWare, LLC

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.

  Covered in detail in the book _Elements of Android Q

  https://commonsware.com/AndroidQ
*/

package com.commonsware.android.q.exif

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class MainViewState(
  val latitude: String,
  val longitude: String,
  val image: Uri
)

class MainMotor(private val context: Context) : ViewModel() {
  private val _states = MutableLiveData<MainViewState>()
  val states: LiveData<MainViewState> = _states
  private val _countEvents = MutableLiveData<Event<Int>>()
  val countEvents: LiveData<Event<Int>> = _countEvents
  private val collection =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Images.Media.getContentUri(
      MediaStore.VOLUME_EXTERNAL
    ) else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

  init {
    viewModelScope.launch(Dispatchers.Main) { loadGeotags(ensureImage()) }
  }

  fun load(image: Uri) {
    viewModelScope.launch(Dispatchers.Main) { loadGeotags(image) }
  }

  fun query(name: String, requireOriginal: Boolean) {
    viewModelScope.launch(Dispatchers.Main) {
      queryForImage(
        name,
        requireOriginal
      )
    }
  }

  fun countGeotags() {
    viewModelScope.launch(Dispatchers.Main) { queryForGeotags() }
  }

  private suspend fun ensureImage() = withContext(Dispatchers.IO) {
    val target = File(
      Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
      "FreedomTower-Morning.jpg"
    )

    if (!target.exists()) {
      context.assets.open("FreedomTower-Morning.jpg").use { src ->
        FileOutputStream(target).use { dst ->
          src.copyTo(dst)
        }
      }

      MediaScannerConnection.scanFile(
        context,
        arrayOf(target.absolutePath),
        arrayOf("image/jpeg"),
        null
      )
    }

    Uri.fromFile(target)
  }

  private suspend fun loadGeotags(image: Uri) {
    withContext(Dispatchers.IO) {
      context.contentResolver.openInputStream(image)?.use { src ->
        val exif = ExifInterface(src)
        val location = exif.latLong

        _states.postValue(
          MainViewState(
            location?.get(0)?.toString() ?: "<none>",
            location?.get(1)?.toString() ?: "<none>",
            image
          )
        )
      }
    }
  }

  private suspend fun queryForImage(name: String, requireOriginal: Boolean) =
    withContext(Dispatchers.IO) {
      context.contentResolver.query(
        collection,
        arrayOf(MediaStore.Files.FileColumns._ID),
        "${MediaStore.MediaColumns.DISPLAY_NAME} = ?",
        arrayOf(name),
        null
      )?.let { cursor ->
        if (cursor.count > 0) {
          cursor.moveToFirst()

          val idColumn =
            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
          val uri = Uri.withAppendedPath(collection, cursor.getString(idColumn))
            .let {
              if (requireOriginal) {
                MediaStore.setRequireOriginal(it)
              } else it
            }

          loadGeotags(uri)
        }
      }
    }

  private suspend fun queryForGeotags() {
    withContext(Dispatchers.IO) {
      val projection = arrayOf(
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.Images.Media.LATITUDE,
        MediaStore.Images.Media.LONGITUDE
      )

      context.contentResolver.query(collection, projection, null, null, null)
        ?.let { cursor ->
          val latColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE)
          val lonColumn =
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE)
          var count = 0

          while (cursor.moveToNext()) {
            val name = cursor.getString(0)

            if (cursor.getDouble(latColumn) != 0.0 ||
              cursor.getDouble(lonColumn) != 0.0
            ) {
              count += 1
            }
          }

          _countEvents.postValue(Event(count))
        }
    }
  }
}