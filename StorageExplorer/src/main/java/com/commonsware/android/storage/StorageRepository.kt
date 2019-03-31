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

package com.commonsware.android.storage

import android.content.Context
import android.os.Environment
import android.provider.MediaStore

enum class StorageScenario {
  INTERNAL_FILES,
  EXTERNAL_FILES,
  EXTERNAL_CACHE,
  EXTERNAL_MUSIC,
  EXTERNAL_IMAGES,
  EXTERNAL_VIDEOS,
  EXTERNAL_DOWNLOADS,
  EXTERNAL_DOCUMENTS,
  MEDIA_MUSIC,
  MEDIA_IMAGES,
  MEDIA_VIDEOS,
  MEDIA_FILES
}

class StorageRepository(val ctxt: Context) {
  fun getSource(scenario: StorageScenario): IStorageSource = when (scenario) {
    StorageScenario.INTERNAL_FILES -> ContentSource.on(ctxt, ctxt.filesDir)
    StorageScenario.EXTERNAL_FILES -> ContentSource.on(
      ctxt,
      ctxt.getExternalFilesDir(
        null
      )!!
    )
    StorageScenario.EXTERNAL_CACHE -> ContentSource.on(
      ctxt,
      ctxt.externalCacheDir!!
    )
    StorageScenario.EXTERNAL_MUSIC -> ContentSource.onPublic(
      ctxt,
      Environment.DIRECTORY_MUSIC
    )
    StorageScenario.EXTERNAL_IMAGES -> ContentSource.onPublic(
      ctxt,
      Environment.DIRECTORY_PICTURES
    )
    StorageScenario.EXTERNAL_VIDEOS -> ContentSource.onPublic(
      ctxt,
      Environment.DIRECTORY_MOVIES
    )
    StorageScenario.EXTERNAL_DOWNLOADS -> ContentSource.onPublic(
      ctxt,
      Environment.DIRECTORY_DOWNLOADS
    )
    StorageScenario.EXTERNAL_DOCUMENTS -> ContentSource.onPublic(
      ctxt,
      Environment.DIRECTORY_DOCUMENTS
    )
    StorageScenario.MEDIA_MUSIC -> MediaSource(
      ctxt,
      MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
      ctxt.contentResolver
    )
    StorageScenario.MEDIA_IMAGES -> MediaSource(
      ctxt,
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
      ctxt.contentResolver
    )
    StorageScenario.MEDIA_VIDEOS -> MediaSource(
      ctxt,
      MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
      ctxt.contentResolver
    )
    StorageScenario.MEDIA_FILES -> MediaSource(
      ctxt,
      MediaStore.Files.getContentUri("external"),
      ctxt.contentResolver
    )
  }
}