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
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.documentfile.provider.DocumentFile
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.transaction
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val ASSET_TEXT = "sample.txt"
private const val ASSET_AUDIO = "sample.mp3"
private const val ASSET_IMAGE = "sample.png"
private const val ASSET_VIDEO = "sample.mp4"
private const val MIME_TYPE_TEXT = "text/plain"
private const val MIME_TYPE_AUDIO = "audio/mpeg"
private const val MIME_TYPE_IMAGE = "image/png"
private const val MIME_TYPE_VIDEO = "video/mp4"
private const val REQUEST_DOC = 1338
private const val REQUEST_TREE = 1339

class MainActivity : AbstractPermissionActivity() {
  override val desiredPermissions: Array<String> by lazy {
    resources.getStringArray(
      R.array.permissions
    )
  }
  private val viewModel: TopViewModel by viewModel()
  private val fragments = mapOf<Int, Fragment>()
  private val tag by lazy { getString(R.string.app_name) }

  override fun onReady(state: Bundle?) {
    setContentView(R.layout.activity_main)

    setSupportActionBar(toolbar)

    title =
      BuildConfig.APPLICATION_ID.removePrefix("com.commonsware.android.storage.")

    val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
    val navView: NavigationView = findViewById(R.id.nav_view)
    val toggle = ActionBarDrawerToggle(
      this,
      drawerLayout,
      toolbar,
      R.string.navigation_drawer_open,
      R.string.navigation_drawer_close
    )
    drawerLayout.addDrawerListener(toggle)
    toggle.syncState()

    navView.setNavigationItemSelectedListener { item ->
      navTo(item.itemId)
      drawer_layout.closeDrawer(GravityCompat.START)
      true
    }

    navTo(R.id.nav_internal_files)

    val msg =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Environment.isExternalStorageLegacy())
        "This app has legacy external storage"
      else "This app has Q-normal external storage"

    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
  }

  override fun onPermissionDenied() {
    Toast.makeText(this, R.string.no_permission, Toast.LENGTH_LONG).show()
    finish()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.top_actions, menu)

    return super.onCreateOptionsMenu(menu)
  }

  @TargetApi(Build.VERSION_CODES.Q)
  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.refresh -> viewModel.refresh()
      R.id.openDoc -> startActivityForResult(
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
          addCategory(Intent.CATEGORY_OPENABLE); type = "*/*"
        },
        REQUEST_DOC
      )
      R.id.openTree -> startActivityForResult(
        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE),
        REQUEST_TREE
      )
    }

    return super.onOptionsItemSelected(item)
  }

  @TargetApi(Build.VERSION_CODES.Q)
  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    if (resultCode == Activity.RESULT_OK) {
      when (requestCode) {
        REQUEST_DOC -> data?.data?.let { showDoc(it) }
        REQUEST_TREE -> data?.data?.let { showTree(it) }
      }
    }
  }

  private fun showDoc(uri: Uri) {
    Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show()

    Log.d(tag, "Uri = $uri")

    val docFile = DocumentFile.fromSingleUri(this, uri)

    docFile?.let {
      Log.d(tag, "Display name = ${docFile.name}")
      Log.d(tag, "Size = ${docFile.length()}")
      Log.d(tag, "MIME type = ${docFile.type}")
    }
  }

  private fun showTree(uri: Uri) {
    Toast.makeText(this, uri.toString(), Toast.LENGTH_LONG).show()

    val docFile = DocumentFile.fromTreeUri(this, uri)

    docFile?.let {
      Log.d(tag, "Display name = ${docFile.name}")
      Log.d(tag, "# of children = ${docFile.listFiles().size}")
    }
  }

  private fun navTo(itemId: Int) {
    supportFragmentManager.transaction {
      replace(
        R.id.frame,
        fragments.getOrElse(itemId) { createFragment(itemId) })
    }
  }

  private fun createFragment(itemId: Int) = when (itemId) {
    R.id.nav_internal_files -> StorageFragment(
      StorageScenario.INTERNAL_FILES,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    R.id.nav_external_files -> StorageFragment(
      StorageScenario.EXTERNAL_FILES,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    R.id.nav_external_cache -> StorageFragment(
      StorageScenario.EXTERNAL_CACHE,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    R.id.nav_external_root -> StorageFragment(
      StorageScenario.EXTERNAL_ROOT,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    R.id.nav_external_music -> StorageFragment(
      StorageScenario.EXTERNAL_MUSIC,
      ASSET_AUDIO,
      MIME_TYPE_AUDIO
    )
    R.id.nav_external_images -> StorageFragment(
      StorageScenario.EXTERNAL_IMAGES,
      ASSET_IMAGE,
      MIME_TYPE_IMAGE
    )
    R.id.nav_external_videos -> StorageFragment(
      StorageScenario.EXTERNAL_VIDEOS,
      ASSET_VIDEO,
      MIME_TYPE_VIDEO
    )
    R.id.nav_external_downloads -> StorageFragment(
      StorageScenario.EXTERNAL_DOWNLOADS,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    R.id.nav_external_documents -> StorageFragment(
      StorageScenario.EXTERNAL_DOCUMENTS,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    R.id.nav_external_alarms -> StorageFragment(
      StorageScenario.EXTERNAL_ALARMS,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    R.id.nav_external_dcim -> StorageFragment(
      StorageScenario.EXTERNAL_DCIM,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    R.id.nav_external_notifications -> StorageFragment(
      StorageScenario.EXTERNAL_NOTIFICATIONS,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    R.id.nav_external_podcasts -> StorageFragment(
      StorageScenario.EXTERNAL_PODCASTS,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    R.id.nav_external_ringtones -> StorageFragment(
      StorageScenario.EXTERNAL_RINGTONES,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    R.id.nav_media_music -> StorageFragment(
      StorageScenario.MEDIA_MUSIC,
      ASSET_AUDIO,
      MIME_TYPE_AUDIO
    )
    R.id.nav_media_images -> StorageFragment(
      StorageScenario.MEDIA_IMAGES,
      ASSET_IMAGE,
      MIME_TYPE_IMAGE
    )
    R.id.nav_media_videos -> StorageFragment(
      StorageScenario.MEDIA_VIDEOS,
      ASSET_VIDEO,
      MIME_TYPE_VIDEO
    )
    R.id.nav_media_files -> StorageFragment(
      StorageScenario.MEDIA_FILES,
      ASSET_TEXT,
      MIME_TYPE_TEXT
    )
    else -> throw IllegalArgumentException("Do not recognize $itemId for fragment")
  }
}
