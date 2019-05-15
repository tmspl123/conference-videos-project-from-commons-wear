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

package com.commonsware.android.conferencevideos

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.BuildCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import com.commonsware.android.conferencevideos.databinding.RowBinding
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AbstractPermissionActivity() {
  private val motor: MainMotor by viewModel()

  override val desiredPermissions = if (BuildCompat.isAtLeastQ()) arrayOf()
  else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

  override fun onPermissionDenied() {
    Toast.makeText(this, getString(R.string.msg_toast), Toast.LENGTH_LONG)
      .show()
    finish()
  }

  override fun onReady(state: Bundle?) {
    setContentView(R.layout.activity_main)

    val manager = LinearLayoutManager(this)

    videos.layoutManager = manager
    videos.addItemDecoration(DividerItemDecoration(this, manager.orientation))

    val adapter = VideoAdapter(layoutInflater) { video ->
      if (video.isDownloaded) {
        startActivity(
          Intent(
            Intent.ACTION_VIEW,
            video.uri
          ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
      } else {
        motor.download(video)
      }
    }

    videos.adapter = adapter

    motor.states.observe(this, Observer { adapter.submitList(it.videos) })
  }
}

class VideoAdapter(
  private val inflater: LayoutInflater,
  private val onClick: (VideoState) -> Unit
) :
  ListAdapter<VideoState, RowHolder>(VideoStateDiffer) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
    val binding = RowBinding.inflate(inflater, parent, false)

    return RowHolder(binding, onClick)
  }

  override fun onBindViewHolder(holder: RowHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

class RowHolder(
  private val binding: RowBinding,
  private val onClick: (VideoState) -> Unit
) :
  RecyclerView.ViewHolder(binding.root) {

  fun bind(state: VideoState) {
    binding.state = state
    binding.root.setOnClickListener { onClick(state) }
    binding.executePendingBindings()
  }
}

object VideoStateDiffer : DiffUtil.ItemCallback<VideoState>() {
  override fun areItemsTheSame(oldItem: VideoState, newItem: VideoState) =
    oldItem == newItem

  override fun areContentsTheSame(oldItem: VideoState, newItem: VideoState) =
    areItemsTheSame(oldItem, newItem)
}