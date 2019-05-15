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

package com.commonsware.android.conferencevideos

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val FILENAMES = listOf(
  "AndroidSummit2016-MultiWindow.mp4",
  "DevFestFL2018-Room.mp4",
  "AndroidSummit2018-Slices.mp4",
  "droidconNYC2016-DragDrop.mp4"
)

data class VideoState(
  val filename: String,
  val uri: Uri?,
  val isDownloading: Boolean
) {
  val isDownloaded = uri != null
}

data class MainViewState(
  val videos: List<VideoState> = listOf()
)

class MainMotor(private val repo: VideoRepository) : ViewModel() {
  private val _states = MutableLiveData<MainViewState>().apply {
    value = MainViewState()
  }
  val states: LiveData<MainViewState> = _states

  init {
    viewModelScope.launch(Dispatchers.Main) {
      _states.value = MainViewState(FILENAMES.map { filename ->
        VideoState(filename, repo.getLocalUri(filename), false)
      })
    }
  }

  fun download(video: VideoState) {
    val interim = video.copy(isDownloading = true)

    _states.value = MainViewState(
      _states.value!!.videos.replace(video, interim)
    )

    viewModelScope.launch(Dispatchers.Main) {
      _states.value = MainViewState(
        _states.value!!.videos.replace(
          interim,
          video.copy(uri = repo.download(video.filename))
        )
      )
    }
  }
}

private fun <T> List<T>.replace(original: T, replacement: T): List<T> =
  map { item -> if (item == original) replacement else item }
