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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import androidx.databinding.DataBindingUtil
import com.commonsware.android.q.exif.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val REQUEST_OPEN = 1337
private const val REQUEST_PERM = 1338

class MainActivity : AppCompatActivity() {
  private val motor: MainMotor by viewModel()
  private lateinit var requireOriginalItem: MenuItem

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
      this,
      R.layout.activity_main
    )

    binding.lifecycleOwner = this
    binding.state = motor.states

    motor.countEvents.observe(this, EventObserver { count ->
      Toast.makeText(
        this@MainActivity,
        "$count images have geotags",
        Toast.LENGTH_LONG
      ).show()
    })
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.actions, menu)
    requireOriginalItem = menu.findItem(R.id.requireOriginal)

    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.openImage -> {
        startActivityForResult(
          Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/jpeg"),
          REQUEST_OPEN
        )
        return true
      }
      R.id.openMedia -> {
        requestMediaPerm()
        return true
      }
      R.id.requireOriginal -> {
        item.isChecked = !item.isChecked
        return true
      }
      R.id.countGeotags -> {
        motor.countGeotags()
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
    if (requestCode == REQUEST_OPEN) {
      if (resultCode == Activity.RESULT_OK) {
        data?.data?.let { motor.load(it) }
      }
    }
  }

  private fun requestMediaPerm() {
    val perms = if (BuildCompat.isAtLeastQ()) {
      arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_MEDIA_LOCATION
      )
    } else {
      arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    if (perms.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }) {
      motor.query("FreedomTower-Morning.jpg", requireOriginalItem.isChecked)
    } else {
      requestPermissions(perms, REQUEST_PERM)
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    if (requestCode == REQUEST_PERM) requestMediaPerm()
  }
}

