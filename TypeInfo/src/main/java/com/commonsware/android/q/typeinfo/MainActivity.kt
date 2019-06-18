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

package com.commonsware.android.q.typeinfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.commonsware.android.q.typeinfo.databinding.RowBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
  private val motor: MainMotor by viewModel()
  private lateinit var light: MenuItem
  private lateinit var dark: MenuItem
  private lateinit var system: MenuItem
  private lateinit var currentThemeMode: ThemeMode

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val rv = findViewById<RecyclerView>(R.id.types)
    val adapter = TypeAdapter(layoutInflater)

    rv.layoutManager = LinearLayoutManager(this)
    rv.adapter = adapter

    motor.states.observe(this) { state ->
      adapter.submitList(state.types)

      currentThemeMode = state.themeMode

      if (::light.isInitialized) {
        updateThemeMode(currentThemeMode)
      }
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.themes, menu)
    light = menu.findItem(R.id.light)
    dark = menu.findItem(R.id.dark)
    system = menu.findItem(R.id.system)

    if (::currentThemeMode.isInitialized) updateThemeMode(currentThemeMode)

    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item) {
      light -> {
        light.isChecked = true
        motor.setThemeMode(ThemeMode.LIGHT)
        return true
      }
      dark -> {
        dark.isChecked = true
        motor.setThemeMode(ThemeMode.DARK)
        return true
      }
      system -> {
        system.isChecked = true
        motor.setThemeMode(ThemeMode.SYSTEM)
        return true
      }
    }

    return super.onOptionsItemSelected(item)
  }

  private fun updateThemeMode(themeMode: ThemeMode) {
    when (themeMode) {
      ThemeMode.LIGHT -> {
        light.isChecked = true
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
      }
      ThemeMode.DARK -> {
        dark.isChecked = true
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
      }
      ThemeMode.SYSTEM -> {
        system.isChecked = true

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
      }
    }
  }
}

class TypeAdapter(private val inflater: LayoutInflater) :
  ListAdapter<RowState, RowHolder>(TypeRecordDiffer) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    RowHolder(RowBinding.inflate(inflater, parent, false))

  override fun onBindViewHolder(holder: RowHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

class RowHolder(private val binding: RowBinding) :
  RecyclerView.ViewHolder(binding.root) {
  fun bind(type: RowState) {
    binding.setType(type)
    binding.icon.setImageIcon(type.icon)
  }
}

object TypeRecordDiffer : DiffUtil.ItemCallback<RowState>() {
  override fun areItemsTheSame(oldItem: RowState, newItem: RowState) =
    oldItem.type == newItem.type

  override fun areContentsTheSame(oldItem: RowState, newItem: RowState) =
    areItemsTheSame(oldItem, newItem)
}