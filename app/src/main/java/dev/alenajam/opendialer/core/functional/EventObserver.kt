package dev.alenajam.opendialer.core.functional

import androidx.lifecycle.Observer
import dev.alenajam.opendialer.core.common.functional.Event

/**
 * An [Observer] for [Event]s, simplifying the pattern of checking if the [Event]'s content has
 * already been handled.
 *
 * [onEventUnhandledContent] is *only* called if the [Event]'s contents has not been handled.
 */
class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
  override fun onChanged(value: Event<T>) {
    value.getContentIfNotHandled()?.let { onEventUnhandledContent(it) }
  }
}