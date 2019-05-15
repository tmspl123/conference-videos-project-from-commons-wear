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
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.commonsware.android.q.loc.fg.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DateFormat

class MainActivity : AbstractPermissionActivity() {
  override val desiredPermissions =
    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
  private val motor: MainMotor by viewModel()

  override fun onPermissionDenied() {
    Toast.makeText(
      this,
      "Sorry, but we need permission to continue",
      Toast.LENGTH_LONG
    ).show()
  }

  override fun onReady(state: Bundle?) {
    val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

    binding.lifecycleOwner = this
    binding.state = motor.states
    motor.initRequest()
  }
}

data class MainViewState(
  val latitude: CharSequence,
  val longitude: CharSequence,
  val time: CharSequence
)

class MainMotor(private val repo: LocationRepository) : ViewModel() {
  val states: LiveData<MainViewState> = Transformations.map(repo.locations) {
    MainViewState(
      it.latitude.toString(),
      it.longitude.toString(),
      DateUtils.formatSameDayTime(
        it.time,
        System.currentTimeMillis(),
        DateFormat.SHORT,
        DateFormat.SHORT
      )
    )
  }

  fun initRequest() = repo.initRequest()
}