package com.example.mylibrary

import javax.inject.Inject

class BlankRepository @Inject constructor() {
  fun getSomething(): String {
    return "ciao alen!"
  }
}