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

package com.commonsware.android.q.auth.check

import android.app.Activity
import android.app.KeyguardManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import kotlinx.android.synthetic.main.activity_main.*

private const val REQUEST_CODE = 1337

class MainActivity : AppCompatActivity() {
  private lateinit var on: Drawable
  private lateinit var off: Drawable
  private val signal = CancellationSignal()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    off = DrawableCompat.wrap(
      VectorDrawableCompat.create(
        resources,
        R.drawable.ic_fingerprint_black_24dp, null
      )!!
    )
    off.setTint(resources.getColor(android.R.color.black, null))

    on = DrawableCompat.wrap(
      VectorDrawableCompat.create(
        resources,
        R.drawable.ic_fingerprint_black_24dp, null
      )!!
    )
    on.setTint(resources.getColor(R.color.colorPrimary, null))

    val prompt = BiometricPrompt.Builder(this)
      .setTitle("This is the title")
      .setDescription("This is the description")
      .setSubtitle("This is the subtitle")
      .apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          setDeviceCredentialAllowed(true)
        } else {
          setNegativeButton(
            "Ick!",
            mainExecutor,
            DialogInterface.OnClickListener { _, _ ->
              fingerprint.setImageDrawable(off)
            })
        }
      }
      .build()

    fingerprint.apply {
      setImageDrawable(off)
      setOnClickListener {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
          fingerprint.setImageDrawable(on)
          prompt.authenticate(signal, mainExecutor, authCallback)
        } else {
          Toast.makeText(
            this@MainActivity,
            R.string.msg_not_available,
            Toast.LENGTH_LONG
          ).show()
        }
      }
    }
  }

  override fun onActivityResult(
    requestCode: Int, resultCode: Int,
    data: Intent?
  ) {
    if (requestCode == REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        Toast.makeText(this, "Authenticated!", Toast.LENGTH_SHORT).show()
      } else {
        Toast.makeText(this, "WE ARE UNDER ATTACK!", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun fallback() {
    val i = getSystemService(KeyguardManager::class.java)
      .createConfirmDeviceCredentialIntent(
        "title",
        "description"
      )

    if (i == null) {
      Toast.makeText(
        this@MainActivity,
        "No authentication required!",
        Toast.LENGTH_SHORT
      )
        .show()
    } else {
      startActivityForResult(i, REQUEST_CODE)
    }
  }

  private val authCallback = object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationError(
      errorCode: Int,
      errString: CharSequence
    ) {
      fingerprint.setImageDrawable(off)

      if (errorCode == BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS) {
        fallback()
      } else {
        Toast.makeText(this@MainActivity, errString, Toast.LENGTH_LONG).show()
      }
    }

    override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
      // unused
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
      Toast.makeText(
        this@MainActivity,
        R.string.msg_authenticated,
        Toast.LENGTH_LONG
      ).show()
      fingerprint.setImageDrawable(off)
    }

    override fun onAuthenticationFailed() {
      fallback()
    }
  }
}
