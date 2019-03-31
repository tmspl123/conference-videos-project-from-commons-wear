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

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StorageMotor(
  private val ctxt: Context,
  private val source: IStorageSource
) : ViewModel() {
  data class ViewState(val items: List<StorageItem> = listOf())

  private val _states =
    MutableLiveData<ViewState>().apply { value = ViewState() }
  val states: LiveData<ViewState> = _states

  init {
    refresh()
  }

  fun create(name: String, createAsset: String, mimeType: String) {
    viewModelScope.launch(Dispatchers.Main) {
      source.create(name, mimeType) { out ->
        ctxt.assets.open(createAsset).use { it.copyTo(out) }
      }

      refreshImpl()
    }
  }

  fun refresh() {
    viewModelScope.launch(Dispatchers.Main) {
      refreshImpl()
    }
  }

  private suspend fun refreshImpl() {
    _states.value =
      _states.value!!.copy(items = source.listItems().sortedBy { it.displayName })
  }
}