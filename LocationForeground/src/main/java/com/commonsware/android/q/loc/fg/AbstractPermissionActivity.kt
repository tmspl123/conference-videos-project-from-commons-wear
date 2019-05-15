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

package com.commonsware.android.q.loc.fg

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val REQUEST_PERMISSION = 61125
private const val STATE_IN_PERMISSION = "inPermission"

abstract class AbstractPermissionActivity : AppCompatActivity() {
  protected abstract val desiredPermissions: Array<String>
  protected abstract fun onPermissionDenied()
  protected abstract fun onReady(state: Bundle?)

  private var isInPermission = false
  private var state: Bundle? = null

  final override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    this.state = savedInstanceState

    if (state != null) {
      isInPermission = state!!.getBoolean(STATE_IN_PERMISSION, false)
    }

    if (hasAllPermissions(desiredPermissions)) {
      onReady(state)
    } else if (!isInPermission) {
      isInPermission = true

      ActivityCompat
        .requestPermissions(
          this,
          netPermissions(desiredPermissions),
          REQUEST_PERMISSION
        )
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    isInPermission = false

    if (requestCode == REQUEST_PERMISSION) {
      if (hasAllPermissions(desiredPermissions)) {
        onReady(state)
      } else {
        onPermissionDenied()
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)

    outState.putBoolean(STATE_IN_PERMISSION, isInPermission)
  }

  private fun hasAllPermissions(perms: Array<String>) =
    perms.any { hasPermission(it) }

  protected fun hasPermission(perm: String) =
    ContextCompat.checkSelfPermission(
      this,
      perm
    ) == PackageManager.PERMISSION_GRANTED

  private fun netPermissions(wanted: Array<String>) =
    wanted.filter { !hasPermission(it) }.toTypedArray()
}
