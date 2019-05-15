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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import org.koin.android.ext.android.inject

private const val CHANNEL_WHATEVER = "channel_whatever"
private const val FOREGROUND_ID = 1338

class ForegroundService : LifecycleService() {
  private val repo: LocationRepository by inject()

  override fun onCreate() {
    super.onCreate()

    val mgr = getSystemService(NotificationManager::class.java)!!

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
      mgr.getNotificationChannel(CHANNEL_WHATEVER) == null
    ) {
      mgr.createNotificationChannel(
        NotificationChannel(
          CHANNEL_WHATEVER,
          "Whatever",
          NotificationManager.IMPORTANCE_DEFAULT
        )
      )
    }

    startForeground(FOREGROUND_ID, buildForegroundNotification())

    repo.locations.observe(this, Observer {
      Log.d(
        "LocationForeground",
        "Latitude: ${it.latitude} Longitude: ${it.longitude}"
      )
    })
  }

  private fun buildForegroundNotification(): Notification {
    val pi = PendingIntent.getBroadcast(
      this,
      1337,
      Intent(this, StopServiceReceiver::class.java),
      0
    )
    val b = NotificationCompat.Builder(this, CHANNEL_WHATEVER)

    b.setOngoing(true)
      .setContentTitle(getString(R.string.app_name))
      .setContentText(getString(R.string.notif_text))
      .setSmallIcon(R.drawable.ic_notification)
      .setContentIntent(pi)

    return b.build()
  }
}

class StopServiceReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    context.stopService(Intent(context, ForegroundService::class.java))
  }
}