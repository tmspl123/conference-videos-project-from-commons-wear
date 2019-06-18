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

package com.commonsware.android.q.typeinfo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val MIME_TYPES = listOf(
  "audio/aac",
  "application/x-abiword",
  "application/x-freearc",
  "video/x-msvideo",
  "application/vnd.amazon.ebook",
  "application/octet-stream",
  "image/bmp",
  "application/x-bzip",
  "application/x-bzip2",
  "application/x-csh",
  "text/css",
  "text/csv",
  "application/msword",
  "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  "application/vnd.ms-fontobject",
  "application/epub+zip",
  "image/gif",
  "text/html",
  "image/vnd.microsoft.icon",
  "text/calendar",
  "application/java-archive",
  "image/jpeg",
  "text/javascript",
  "application/json",
  "application/ld+json",
  "text/javascript",
  "audio/mpeg",
  "video/mpeg",
  "application/vnd.apple.installer+xml",
  "application/vnd.oasis.opendocument.presentation",
  "application/vnd.oasis.opendocument.spreadsheet",
  "application/vnd.oasis.opendocument.text",
  "audio/ogg",
  "video/ogg",
  "application/ogg",
  "font/otf",
  "image/png",
  "application/pdf",
  "application/vnd.ms-powerpoint",
  "application/vnd.openxmlformats-officedocument.presentationml.presentation",
  "application/x-rar-compressed",
  "application/rtf",
  "application/x-sh",
  "image/svg+xml",
  "application/x-shockwave-flash",
  "application/x-tar",
  "image/tiff",
  "font/ttf",
  "text/plain",
  "application/vnd.visio",
  "audio/wav",
  "audio/webm",
  "video/webm",
  "image/webp",
  "font/woff",
  "font/woff2",
  "application/xhtml+xml",
  "application/vnd.ms-excel",
  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
  "application/xml",
  "application/zip",
  "video/3gpp",
  "video/3gpp2",
  "application/x-7z-compressed",
  "rutabaga/marshmallow"
)

enum class ThemeMode {
  LIGHT, DARK, SYSTEM
}

private const val PREF_THEME_MODE = "themeMode"

class MainMotor(context: Context) : ViewModel() {
  private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
  private val _states = MutableLiveData<MainViewState>()
  val states: LiveData<MainViewState> = _states

  init {
    viewModelScope.launch(Dispatchers.Main) {
      _states.postValue(
        MainViewState(
          mapTypes(context),
          loadInitialThemeMode()
        )
      )
    }
  }

  fun setThemeMode(mode: ThemeMode) {
    prefs.edit().putString(PREF_THEME_MODE, mode.name).apply()
    _states.value?.let { _states.value = it.copy(themeMode = mode) }
  }

  private suspend fun loadInitialThemeMode() = withContext(Dispatchers.IO) {
    prefs.getString(PREF_THEME_MODE, null)?.let { ThemeMode.valueOf(it) }
      ?: ThemeMode.SYSTEM
  }

  private suspend fun mapTypes(context: Context) =
    withContext(Dispatchers.Default) {
      val resolver = context.contentResolver

      MIME_TYPES
        .map { RowState(it, resolver.getTypeInfo(it)) }
        .sortedBy { it.description.toString() }
    }
}