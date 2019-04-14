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
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.commonsware.android.q.typeinfo.databinding.RowBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
  private val vm: TypeInfoViewModel by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val rv = findViewById<RecyclerView>(R.id.types)
    val adapter = TypeAdapter(layoutInflater)

    rv.layoutManager = LinearLayoutManager(this)
    rv.adapter = adapter

    vm.types.observe(this) { types -> adapter.submitList(types) }
  }
}

class TypeAdapter(private val inflater: LayoutInflater) : ListAdapter<TypeRecord, RowHolder>(TypeRecordDiffer) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
    RowHolder(RowBinding.inflate(inflater, parent, false))

  override fun onBindViewHolder(holder: RowHolder, position: Int) {
    holder.bind(getItem(position))
  }
}

class RowHolder(private val binding: RowBinding) :
  RecyclerView.ViewHolder(binding.root) {
  fun bind(type: TypeRecord) {
    binding.setType(type)
    binding.icon.setImageIcon(type.icon)
  }
}

object TypeRecordDiffer : DiffUtil.ItemCallback<TypeRecord>() {
  override fun areItemsTheSame(oldItem: TypeRecord, newItem: TypeRecord) =
    oldItem.type == newItem.type

  override fun areContentsTheSame(oldItem: TypeRecord, newItem: TypeRecord) =
    areItemsTheSame(oldItem, newItem)
}