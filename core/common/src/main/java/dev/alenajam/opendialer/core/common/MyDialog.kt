package dev.alenajam.opendialer.core.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog

open class MyDialog : AlertDialog, View.OnClickListener {
    protected lateinit var dialogView: View
    private var listener: OnClickListener? = null
    protected lateinit var positiveButton: Button
    protected lateinit var negativeButton: Button

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, @StyleRes style: Int) : super(context, style) {
        init(context)
    }

    private fun init(context: Context) {
        dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_my, null)
        setView(dialogView)
        positiveButton = dialogView.findViewById(R.id.buttonPositive)
        negativeButton = dialogView.findViewById(R.id.buttonNegative)
    }

    override fun setTitle(title: CharSequence?) {
        val titleTextView = dialogView.findViewById<TextView>(R.id.title)
        titleTextView.text = title
    }

    override fun setTitle(@StringRes title: Int) {
        val titleTextView = dialogView.findViewById<TextView>(R.id.title)
        titleTextView.setText(title)
    }

    fun setContent(view: View) {
        val frameLayout = dialogView.findViewById<FrameLayout>(R.id.content)
        frameLayout.addView(view)
    }

    fun setPositiveButton(label: String) {
        positiveButton.visibility = View.VISIBLE
        positiveButton.text = label
        positiveButton.setOnClickListener(this)
    }

    fun setPositiveButton(@StringRes label: Int) {
        setPositiveButton(context.getString(label))
    }

    fun setNegativeButton(label: String) {
        negativeButton.visibility = View.VISIBLE
        negativeButton.text = label
        negativeButton.setOnClickListener(this)
    }

    fun setNegativeButton(@StringRes label: Int) {
        setNegativeButton(context.getString(label))
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun onClick(v: View) {
        val currentListener = listener ?: return
        when (v.id) {
            R.id.buttonPositive -> currentListener.onClick(this, BUTTON_POSITIVE)
            R.id.buttonNegative -> currentListener.onClick(this, BUTTON_NEGATIVE)
        }
    }

    fun interface OnClickListener {
        fun onClick(dialog: MyDialog, whichButton: Int)
    }
}
