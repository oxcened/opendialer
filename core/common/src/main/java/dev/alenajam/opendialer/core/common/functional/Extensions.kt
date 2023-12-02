package dev.alenajam.opendialer.core.common.functional

import android.content.res.Resources.getSystem
import android.view.View
import androidx.annotation.IdRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun NavController.safeNavigate(directions: NavDirections) {
  currentDestination?.getAction(directions.actionId)?.let {
    navigate(directions)
  }
}

fun NavController.safeNavigate(@IdRes resId: Int) {
  currentDestination?.getAction(resId)?.let {
    navigate(resId)
  }
}

suspend fun View.awaitNextLayout() = suspendCancellableCoroutine<Unit> { cont ->
  // This lambda is invoked immediately, allowing us to create
  // a callback/listener

  val listener = object : View.OnLayoutChangeListener {
    override fun onLayoutChange(
      v: View?,
      left: Int,
      top: Int,
      right: Int,
      bottom: Int,
      oldLeft: Int,
      oldTop: Int,
      oldRight: Int,
      oldBottom: Int
    ) {
      // The next layout has happened!
      // First remove the listener to not leak the coroutine
      v?.removeOnLayoutChangeListener(this)
      // Finally resume the continuation, and
      // wake the coroutine up
      cont.resume(Unit)
    }
  }
  // If the coroutine is cancelled, remove the listener
  cont.invokeOnCancellation { removeOnLayoutChangeListener(listener) }
  // And finally add the listener to view
  addOnLayoutChangeListener(listener)

  // The coroutine will now be suspended. It will only be resumed
  // when calling cont.resume() in the listener above
}

val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()

fun <A, B> LiveData<A>.combineLatest(b: LiveData<B>): LiveData<Pair<A, B>> {
  return MediatorLiveData<Pair<A, B>>().apply {
    var lastA: A? = null
    var lastB: B? = null

    addSource(this@combineLatest) {
      if (it == null && value != null) value = null
      lastA = it
      if (lastA != null && lastB != null) value = lastA!! to lastB!!
    }

    addSource(b) {
      if (it == null && value != null) value = null
      lastB = it
      if (lastA != null && lastB != null) value = lastA!! to lastB!!
    }
  }
}