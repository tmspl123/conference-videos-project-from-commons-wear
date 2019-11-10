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

package com.commonsware.android.q.sharetargets

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

data class ShareTarget(
  val id: String,
  @StringRes val shortLabelRes: Int,
  @DrawableRes val iconRes: Int
)

private val SHARE_CATEGORIES =
  setOf("com.commonsware.android.q.sharetargets.CUSTOM_SHARE_TARGET")
private val TARGETS = listOf(
  ShareTarget("one", R.string.tag_one, R.drawable.ic_looks_one_black_24dp),
  ShareTarget("two", R.string.tag_two, R.drawable.ic_looks_two_black_24dp),
  ShareTarget("five", R.string.tag_five, R.drawable.ic_looks_5_black_24dp)
)

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (ShortcutManagerCompat.getDynamicShortcuts(this).size == 0) {
      val intent = Intent("$packageName.ACTION_WHATEVER")

      ShortcutManagerCompat.addDynamicShortcuts(this, TARGETS.map { tag ->
        ShortcutInfoCompat.Builder(this, tag.id)
          .setShortLabel(getString(tag.shortLabelRes))
          .setIcon(IconCompat.createWithResource(this, tag.iconRes))
          .setIntent(intent)
          .setLongLived(true)
          .setCategories(SHARE_CATEGORIES)
          .build()
      })

      Toast.makeText(this, "Share targets ready!", Toast.LENGTH_LONG).show()
    } else {
      Toast.makeText(this, "${intent.action} received!", Toast.LENGTH_LONG).show()
    }

    finish()
  }
}
