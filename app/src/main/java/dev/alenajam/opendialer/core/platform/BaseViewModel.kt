package dev.alenajam.opendialer.core.platform

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.alenajam.opendialer.core.common.exception.Failure

abstract class BaseViewModel : ViewModel() {
  abstract val TAG: String?

  var failure: MutableLiveData<Pair<Failure, String?>> = MutableLiveData()

  fun handleFailure(failure: Failure) {
    this.failure.postValue(Pair(failure, null))
  }

  fun handleFailure(failure: Failure, key: String) {
    this.failure.postValue(Pair(failure, key))
  }

  val loadings = ObservableField<Set<String>>(setOf())

  fun showLoading(key: String) {
    loadings.get()?.toMutableSet()?.let {
      it.add(key)
      loadings.set(it)
      Log.i(TAG, "<$key> add, current value: $it")
    }
  }

  fun hideLoading(key: String) {
    loadings.get()?.toMutableSet()?.let {
      val removed = it.remove(key)
      loadings.set(it)
      if (removed) {
        Log.i(TAG, "<$key> remove, current value: $it")
      } else {
        Log.i(TAG, "<$key> attempted remove but was not found, current value: $it")
      }
    }
  }
}