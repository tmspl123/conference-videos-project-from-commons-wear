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

package com.commonsware.q.appinstaller

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders

private const val REQUEST_SAF = 1337

class MainActivity : AppCompatActivity() {
  private lateinit var motor: MainMotor

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    motor = ViewModelProviders.of(this)[MainMotor::class.java]
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.actions, menu)

    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.open -> {
        startActivityForResult(
          Intent(Intent.ACTION_OPEN_DOCUMENT)
            .setType("application/vnd.android.package-archive")
            .addCategory(Intent.CATEGORY_OPENABLE), REQUEST_SAF
        )
        return true
      }
    }

    return super.onOptionsItemSelected(item)
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    if (requestCode == REQUEST_SAF) {
      if (resultCode == Activity.RESULT_OK) {
        data?.data?.let { motor.install(it) }
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data)
    }
  }
}
