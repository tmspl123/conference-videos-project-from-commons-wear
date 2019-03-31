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

package com.commonsware.android.q.attention

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters


private const val NOTIF_ID = 1337
private const val CHANNEL_WHATEVER = "whatever"

class StartActivityWorker(
  private val appContext: Context,
  workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
  override fun doWork(): Result {
    appContext.startActivity(
      Intent(
        appContext,
        MainActivity::class.java
      ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )

    return Result.success()
  }
}

class ShowNotificationWorker(
  private val appContext: Context,
  workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
  override fun doWork(): Result {
    val pi = PendingIntent.getActivity(
      appContext,
      0,
      Intent(appContext, MainActivity::class.java),
      PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = NotificationCompat.Builder(appContext, CHANNEL_WHATEVER)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle("Um, hi!")
      .setAutoCancel(true)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setFullScreenIntent(pi, true)

    val mgr = appContext.getSystemService(NotificationManager::class.java)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
      && mgr.getNotificationChannel(CHANNEL_WHATEVER) == null
    ) {
      mgr.createNotificationChannel(
        NotificationChannel(
          CHANNEL_WHATEVER,
          "Whatever",
          NotificationManager.IMPORTANCE_HIGH
        )
      )
    }

    mgr.notify(NOTIF_ID, builder.build())

    return Result.success()
  }
}