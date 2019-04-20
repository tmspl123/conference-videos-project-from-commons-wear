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

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*

private var Bundle.scenario: StorageScenario
  get() = getSerializable("scenario") as StorageScenario
  set(value) = putSerializable("scenario", value)

private var Bundle.createAsset: String
  get() = getString("createAsset", "")
  set(value) = putString("createAsset", value)

private var Bundle.mimeType: String
  get() = getString("mimeType", "")
  set(value) = putString("mimeType", value)

class StorageFragment : Fragment() {
  companion object {
    operator fun invoke(
      scenario: StorageScenario,
      createAsset: String,
      mimeType: String
    ) =
      StorageFragment().apply {
        arguments = Bundle().apply {
          this.scenario = scenario
          this.createAsset = createAsset
          this.mimeType = mimeType
        }
      }
  }

  private val motor: StorageMotor by viewModel { parametersOf(arguments!!.scenario) }
  private val topViewModel: TopViewModel by sharedViewModel()
  private var folderItem: MenuItem? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setHasOptionsMenu(true)
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.page_actions, menu)
    folderItem = menu.findItem(R.id.createFolder)
    folderItem?.isVisible = motor.states.value?.supportsDirectory ?: false

    return super.onCreateOptionsMenu(menu, inflater)
  }

  override fun onOptionsItemSelected(item: MenuItem) = when {
    item.itemId == R.id.create -> {
      motor.create(
        UUID.randomUUID().toString(),
        arguments!!.createAsset,
        arguments!!.mimeType
      )

      true
    }
    item.itemId == R.id.createFolder -> {
      motor.createDirectory(UUID.randomUUID().toString())
      true
    }
    else -> super.onOptionsItemSelected(item)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(R.layout.fragment_storage, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val adapter = Adapter(layoutInflater)
    val rv = view as RecyclerView
    val manager = LinearLayoutManager(activity)

    rv.layoutManager = manager
    rv.addItemDecoration(DividerItemDecoration(activity, manager.orientation))
    rv.adapter = adapter

    motor.states.observe(this) { state ->
      adapter.submitList(state.items)
      folderItem?.isVisible = state.supportsDirectory
    }

    topViewModel.refreshEvents.observe(this) { event ->
      event.handle { motor.refresh() }
    }
  }

  class Adapter(private val inflater: LayoutInflater) :
    ListAdapter<StorageItem, RowHolder>(StorageItemDiffer) {
    override fun onCreateViewHolder(
      parent: ViewGroup,
      viewType: Int
    ) = RowHolder(
      inflater.inflate(
        android.R.layout.simple_list_item_1,
        parent,
        false
      )
    )

    override fun onBindViewHolder(holder: RowHolder, position: Int) {
      holder.bind(getItem(position))
    }
  }

  class RowHolder(private val root: View) : RecyclerView.ViewHolder(root) {
    fun bind(item: StorageItem) {
      root.findViewById<TextView>(android.R.id.text1).text =
        root.context.getString(
          R.string.row_content,
          item.displayName,
          item.mimeType
        )
    }
  }

  object StorageItemDiffer : DiffUtil.ItemCallback<StorageItem>() {
    override fun areItemsTheSame(
      oldItem: StorageItem,
      newItem: StorageItem
    ) = oldItem === newItem

    override fun areContentsTheSame(
      oldItem: StorageItem,
      newItem: StorageItem
    ) = oldItem.displayName == newItem.displayName
  }
}