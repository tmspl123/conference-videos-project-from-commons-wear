package com.commonsware.android.q.exif

import androidx.lifecycle.Observer

class Event<out T>(private val content: T) {
  private var hasBeenHandled = false

  fun handle(handler: (T) -> Unit) {
    if (!hasBeenHandled) {
      hasBeenHandled = true
      handler(content)
    }
  }
}

class EventObserver<T>(private val handler: (T) -> Unit) : Observer<Event<T>> {
  override fun onChanged(value: Event<T>?) {
    value?.handle(handler)
  }
}