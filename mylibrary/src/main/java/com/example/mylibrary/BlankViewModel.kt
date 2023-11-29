package com.example.mylibrary

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BlankViewModel
@Inject constructor(
    private val blankRepository: BlankRepository
) : ViewModel() {
  fun getSomething(): String {
      return blankRepository.getSomething()
  }
}