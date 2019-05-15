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

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.lang.UnsupportedOperationException

data class StorageItem(
  val displayName: String,
  val mimeType: String?
)

interface IStorageSource {
  suspend fun listItems(): List<StorageItem>
  suspend fun create(
    name: String,
    mimeType: String,
    creator: (OutputStream) -> Unit
  )
  val supportsDirectory: Boolean
  suspend fun createDirectory(name: String)
}

class ContentSource(
  private val ctxt: Context,
  private val root: DocumentFile
) :
  IStorageSource {
  companion object {
    fun on(ctxt: Context, dir: File): ContentSource {
      dir.mkdirs()

      return ContentSource(ctxt, DocumentFile.fromFile(dir))
    }

    fun onPublic(ctxt: Context, loc: String): ContentSource {
      val dir = Environment.getExternalStoragePublicDirectory(loc)

      dir.mkdirs()

      return ContentSource(ctxt, DocumentFile.fromFile(dir))
    }
  }

  override val supportsDirectory = true

  override suspend fun listItems(): List<StorageItem> =
    withContext(Dispatchers.IO) {
      root.listFiles()
        .map {
          val type = if (it.isDirectory) "directory" else it.type

          StorageItem(it.name.orEmpty(), type)
        }
    }

  override suspend fun create(
    name: String,
    mimeType: String,
    creator: (OutputStream) -> Unit
  ) {
    withContext(Dispatchers.IO) {
      val child = root.createFile(mimeType, name)

      child?.let {
        val uri = child.uri

        ctxt.contentResolver.openOutputStream(uri).use {
          creator(
            it!!
          )
        }

        if (uri.scheme == ContentResolver.SCHEME_FILE) {
          MediaScannerConnection.scanFile(
            ctxt,
            arrayOf(uri.path),
            arrayOf(mimeType),
            null
          )
        }
      }
    }
  }

  override suspend fun createDirectory(name: String) {
    withContext(Dispatchers.IO) {
      root.createDirectory(name)
    }
  }
}

class MediaSource(
  private val ctxt: Context,
  private val root: Uri,
  private val resolver: ContentResolver
) : IStorageSource {
  override val supportsDirectory = false

  override suspend fun listItems(): List<StorageItem> {
    return withContext(Dispatchers.IO) {
      val cursor = resolver.query(root, null, null, null, null)

      cursor?.let {
        cursor.use {
          val nameColumn =
            cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
          val typeColumn =
            cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)

          generateSequence { if (cursor.moveToNext()) cursor else null }
            .map {
              StorageItem(
                it.getString(nameColumn) ?: "Name not provided",
                it.getString(typeColumn) ?: "unknown"
              )
            }
            .toList()
        }
      } ?: listOf()
    }
  }

  override suspend fun create(
    name: String,
    mimeType: String,
    creator: (OutputStream) -> Unit
  ) {
    withContext(Dispatchers.IO) {
      val resolver = ctxt.contentResolver
      val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
      }

      val uri = resolver.insert(root, contentValues)!!

      ctxt.contentResolver.openOutputStream(uri).use {
        creator(
          it!!
        )
      }
    }
  }

  override suspend fun createDirectory(name: String) {
    throw UnsupportedOperationException("how did you get here?")
  }
}