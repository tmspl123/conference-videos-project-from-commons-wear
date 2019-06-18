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

import android.content.ContentResolver
import android.graphics.drawable.Icon

data class RowState(
  val type: String,
  val label: CharSequence,
  val description: CharSequence,
  val icon: Icon
) {
  constructor(type: String, typeInfo: ContentResolver.MimeTypeInfo) : this(
    type,
    typeInfo.label,
    typeInfo.contentDescription,
    typeInfo.icon
  )
}
