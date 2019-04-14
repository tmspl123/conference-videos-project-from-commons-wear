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

import android.annotation.TargetApi
import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val ASSET_TEXT = "sample.txt"
private const val ASSET_AUDIO = "sample.mp3"
private const val ASSET_IMAGE = "sample.png"
private const val ASSET_VIDEO = "sample.mp4"
private const val REQUEST_ROLE = 1337

class MainActivity : AbstractPermissionActivity() {
  override val desiredPermissions: Array<String> by lazy {
    resources.getStringArray(
      R.array.permissions
    )
  }
  private val roleManager: RoleManager
      by lazy { getSystemService(RoleManager::class.java) }
  private val viewModel: TopViewModel by viewModel()
  private var menuRequestRole: MenuItem? = null

  override fun onReady(state: Bundle?) {
    setContentView(R.layout.activity_main)

    pager.adapter = Pages(this, supportFragmentManager)
    setSupportActionBar(toolbar)
  }

  override fun onPermissionDenied() {
    Toast.makeText(this, R.string.no_permission, Toast.LENGTH_LONG).show()
    finish()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.top_actions, menu)

    menuRequestRole = menu.findItem(R.id.requestRole)
    menuRequestRole?.isEnabled = allowRoleRequest()

    return super.onCreateOptionsMenu(menu)
  }

  @TargetApi(Build.VERSION_CODES.Q)
  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when {
      item.itemId == R.id.requestRole -> {
        startActivityForResult(
          roleManager.createRequestRoleIntent(BuildConfig.ROLE),
          REQUEST_ROLE
        )
        return true
      }
      item.itemId == R.id.refresh -> viewModel.refresh()
    }

    return super.onOptionsItemSelected(item)
  }

  @TargetApi(Build.VERSION_CODES.Q)
  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    if (requestCode == REQUEST_ROLE && resultCode == Activity.RESULT_OK) {
      viewModel.refresh()
      menuRequestRole?.isEnabled = allowRoleRequest()
    }
  }

  @TargetApi(Build.VERSION_CODES.Q)
  private fun allowRoleRequest() = resources.getBoolean(R.bool.isQ)
      && BuildConfig.ROLE != null
      && roleManager.isRoleAvailable(BuildConfig.ROLE)
      && !roleManager.isRoleHeld(BuildConfig.ROLE)
}

private val TITLES = listOf(
  R.string.title_internal_files,
  R.string.title_external_files,
  R.string.title_external_cache,
  R.string.title_external_music,
  R.string.title_external_images,
  R.string.title_external_videos,
  R.string.title_external_downloads,
  R.string.title_external_documents,
  R.string.title_media_music,
  R.string.title_media_images,
  R.string.title_media_videos,
  R.string.title_media_files
)

private const val MIME_TYPE_TEXT = "text/plain"
private const val MIME_TYPE_AUDIO = "audio/mpeg"
private const val MIME_TYPE_IMAGE = "image/png"
private const val MIME_TYPE_VIDEO = "video/mp4"

private class Pages(private val ctxt: Context, fm: FragmentManager) :
  FragmentPagerAdapter(fm) {

  override fun getCount() = 12

  override fun getItem(position: Int) = when (position) {
    0 -> StorageFragment(
      StorageScenario.INTERNAL_FILES,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    1 -> StorageFragment(
      StorageScenario.EXTERNAL_FILES,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    2 -> StorageFragment(
      StorageScenario.EXTERNAL_CACHE,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    3 -> StorageFragment(
      StorageScenario.EXTERNAL_MUSIC,
      ASSET_AUDIO,
      MIME_TYPE_AUDIO
    )
    4 -> StorageFragment(
      StorageScenario.EXTERNAL_IMAGES,
      ASSET_IMAGE,
      MIME_TYPE_IMAGE
    )
    5 -> StorageFragment(
      StorageScenario.EXTERNAL_VIDEOS,
      ASSET_VIDEO,
      MIME_TYPE_VIDEO
    )
    6 -> StorageFragment(
      StorageScenario.EXTERNAL_DOWNLOADS,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    7 -> StorageFragment(
      StorageScenario.EXTERNAL_DOCUMENTS,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    8 -> StorageFragment(
      StorageScenario.MEDIA_MUSIC,
      ASSET_AUDIO,
      MIME_TYPE_AUDIO
    )
    9 -> StorageFragment(
      StorageScenario.MEDIA_IMAGES,
      ASSET_IMAGE,
      MIME_TYPE_IMAGE
    )
    10 -> StorageFragment(
      StorageScenario.MEDIA_VIDEOS,
      ASSET_VIDEO,
      MIME_TYPE_VIDEO
    )
    else -> StorageFragment(
      StorageScenario.MEDIA_FILES,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
  }

  override fun getPageTitle(position: Int): String =
    ctxt.getString(TITLES[position])
}
