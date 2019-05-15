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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class LocationRepository(private val context: Context) {
  private val _locations = MutableLiveData<Location>()
  val locations: LiveData<Location> = _locations
  private var locationsRequested = false

  init {
    initRequest()
  }

  fun initRequest() {
    if (!locationsRequested) {
      val mgr = context.getSystemService(LocationManager::class.java)

      if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
      ) {
        locationsRequested = true
        mgr.requestLocationUpdates(
          LocationManager.GPS_PROVIDER,
          0,
          0.0f,
          object : LocationListener {
            override fun onLocationChanged(location: Location) {
              _locations.postValue(location)
            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
              // unused
            }

            override fun onProviderEnabled(p0: String?) {
              // unused
            }

            override fun onProviderDisabled(p0: String?) {
              // unused
            }
          })
      }
    }
  }
}