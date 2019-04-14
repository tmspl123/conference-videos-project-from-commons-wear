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

package com.commonsware.android.q.bubbleslice

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.slice.Slice
import androidx.slice.SliceProvider
import androidx.slice.builders.GridRowBuilder
import androidx.slice.builders.ListBuilder
import androidx.slice.builders.SliceAction
import androidx.slice.builders.ListBuilder.SMALL_IMAGE

private val FACES = intArrayOf(
  R.drawable.die_1,
  R.drawable.die_2,
  R.drawable.die_3,
  R.drawable.die_4,
  R.drawable.die_5,
  R.drawable.die_6
)

class SliceAndDiceProvider : SliceProvider() {

  override fun onCreateSliceProvider() = true

  override fun onBindSlice(sliceUri: Uri): Slice? {
    val ctxt = context ?: return null

    val builder = ListBuilder(ctxt, sliceUri, ListBuilder.INFINITY)
      .setAccentColor(ResourcesCompat.getColor(ctxt.resources, R.color.colorAccent, null))
      .addAction(buildRollAction(ctxt)).apply {
        setHeader(buildHeader(ctxt))
        addGridRow(buildGridRow(ctxt))
      }

    return builder.build()
  }

  private fun buildHeader(ctxt: Context) =
    ListBuilder.HeaderBuilder()
      .setTitle(ctxt.getString(R.string.header_title))
      .setPrimaryAction(buildRollAction(ctxt))

  private fun buildGridRow(ctxt: Context): GridRowBuilder {
    val row = GridRowBuilder()

    for (i in 0..4) {
      val die = FACES[(Math.random() * 6).toInt()]
      val bmp = BitmapFactory.decodeResource(ctxt.resources, die)

      row.addCell(
        GridRowBuilder.CellBuilder()
          .addImage(IconCompat.createWithBitmap(bmp), SMALL_IMAGE)
      )
    }

    return row
  }

  private fun buildRollAction(ctxt: Context): SliceAction {
    val bmp = BitmapFactory.decodeResource(
      ctxt.resources,
      R.drawable.refresh_18dp
    )

    return SliceAction.create(
      buildActionPI(ctxt),
      IconCompat.createWithBitmap(bmp),
      ListBuilder.ICON_IMAGE, ctxt.getString(R.string.roll)
    )
  }

  private fun buildActionPI(ctxt: Context): PendingIntent {
    val i = Intent(ctxt, SliceActionReceiver::class.java)

    return PendingIntent.getBroadcast(ctxt, 0, i, 0)
  }

  companion object {
    val ME: Uri = Uri.Builder()
      .scheme("content")
      .authority(BuildConfig.APPLICATION_ID + ".provider")
      .build()
  }
}
