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

package com.commonsware.android.q.sharesomething

import android.app.Activity
import android.content.ClipData
import android.content.ContentResolver
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

private const val REQUEST_SIMPLE = 1337
private const val REQUEST_IMAGE = 1338

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    shareTextSimple.setOnClickListener {
      share {
        it.setType("text/plain")
          .putExtra(Intent.EXTRA_TEXT, "This is the shared text")
      }
    }

    shareContentSimple.setOnClickListener {
      startActivityForResult(
        Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*"),
        REQUEST_SIMPLE
      )
    }

    shareTextResource.setOnClickListener {
      share {
        it.setType("text/plain")
          .putExtra(Intent.EXTRA_TEXT, "This is the shared text")
          .apply {
            clipData = ClipData.newUri(
              contentResolver,
              "This is a label",
              resources.buildUri(R.drawable.ic_child_care_black_24dp)
            )
          }
      }
    }

    shareTextContent.setOnClickListener {
      startActivityForResult(
        Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*"),
        REQUEST_SIMPLE
      )
    }
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    when (requestCode) {
      REQUEST_SIMPLE -> if (resultCode == Activity.RESULT_OK) {
        data?.data?.let { uri ->
          share {
            it.setType(contentResolver.getType(uri))
              .putExtra(Intent.EXTRA_STREAM, uri)
          }
        }
      }
      REQUEST_IMAGE -> if (resultCode == Activity.RESULT_OK) {
        data?.data?.let { uri ->
          share {
            it.setType("text/plain")
              .putExtra(Intent.EXTRA_TEXT, "This is the shared text")
              .apply {
                clipData =
                  ClipData.newUri(contentResolver, "This is a label", uri)
              }
          }
        }
      }
    }
  }

  private fun share(configurator: (Intent) -> Unit) {
    val sendIntent = Intent(Intent.ACTION_SEND)
      .putExtra(Intent.EXTRA_TITLE, "This is the preview title")
      .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    configurator(sendIntent)

    startActivity(
      Intent.createChooser(
        sendIntent,
        null
      )
    )
  }
}

private fun Resources.buildUri(resourceId: Int) = Uri.Builder()
  .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
  .authority(this.getResourcePackageName(resourceId))
  .appendPath(this.getResourceTypeName(resourceId))
  .appendPath(this.getResourceEntryName(resourceId))
  .build()