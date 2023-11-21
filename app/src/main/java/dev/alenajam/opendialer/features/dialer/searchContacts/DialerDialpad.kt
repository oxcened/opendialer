package dev.alenajam.opendialer.features.dialer.searchContacts

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import dev.alenajam.opendialer.R

class DialerDialpad(context: Context, attributeSet: AttributeSet) :
  ConstraintLayout(context, attributeSet) {
  init {
    LayoutInflater.from(context).inflate(R.layout.keypad, this, true)
  }
}