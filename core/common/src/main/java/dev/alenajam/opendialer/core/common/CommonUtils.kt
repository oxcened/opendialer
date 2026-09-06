package dev.alenajam.opendialer.core.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.media.AudioManager
import android.net.Uri
import android.os.SystemClock
import android.provider.ContactsContract
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit

object CommonUtils {

    @JvmStatic
    @SuppressLint("DefaultLocale")
    fun getDurationTimeString(durationMilliseconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMilliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMilliseconds) -
                TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMilliseconds) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMilliseconds))

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    @JvmStatic
    @SuppressLint("DefaultLocale")
    fun getDurationTimeStringMinimal(durationMilliseconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMilliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMilliseconds) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(durationMilliseconds)
        )
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMilliseconds) - TimeUnit.MINUTES.toSeconds(
            TimeUnit.MILLISECONDS.toMinutes(durationMilliseconds)
        )

        val timeString = buildString {
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            if (seconds > 0) append("${seconds}s")
        }.trim()

        return timeString.ifEmpty { "0s" }
    }

    @JvmStatic
    fun textToBitmap(context: Context, messageText: String, textSize: Float, textColor: Int): Bitmap {
        val paint = Paint()
        val pixelTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            textSize, context.resources.displayMetrics
        ).toInt()
        paint.textSize = pixelTextSize.toFloat()
        paint.color = textColor
        paint.textAlign = Paint.Align.LEFT

        val baseline = -paint.ascent() + 10 // ascent() is negative
        val width = (paint.measureText(messageText) + 0.5f).toInt() // round
        val height = (baseline + paint.descent() + 0.5f).toInt()

        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(image)
        canvas.drawText(messageText, 0f, baseline, paint)

        return image
    }

    @JvmStatic
    fun getEditTextSelectedText(editText: EditText): String? {
        if (!editText.hasSelection()) return null

        val start = editText.selectionStart
        val end = editText.selectionEnd

        return if (start > end) {
            editText.text.subSequence(end, start).toString()
        } else {
            editText.text.subSequence(start, end).toString()
        }
    }

    @JvmStatic
    fun makeSms(context: Context, number: String?) {
        if (number.isNullOrEmpty()) return
        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", number, null))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @JvmStatic
    fun copyToClipobard(context: Context, text: String?) {
        if (text == null) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(null, text)
        clipboard?.let {
            it.setPrimaryClip(clip)
            Toast.makeText(context, context.getString(R.string.text_copied), Toast.LENGTH_SHORT).show()
        }
    }

    @JvmStatic
    fun showContactDetail(context: Context, contactId: Int) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId.toString())
        intent.data = uri
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @JvmStatic
    fun showProfileDetail(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = ContactsContract.Profile.CONTENT_URI
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    @JvmStatic
    fun shareContact(context: Context, contactId: Int) {
        val contactUri = Uri.withAppendedPath(
            ContactsContract.Contacts.CONTENT_VCARD_URI,
            contactId.toString(),
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = ContactsContract.Contacts.CONTENT_VCARD_TYPE
            putExtra(Intent.EXTRA_STREAM, contactUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    @JvmStatic
    fun setTheme(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    @JvmStatic
    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @JvmStatic
    fun convertDpToPixels(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    @JvmStatic
    fun addContactAsExisting(context: Context, number: String?) {
        if (number.isNullOrEmpty()) return
        val addExistingIntent = Intent(Intent.ACTION_INSERT_OR_EDIT)
        addExistingIntent.type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
        addExistingIntent.putExtra(ContactsContract.Intents.Insert.PHONE, number)
        addExistingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startContactActivity(context, addExistingIntent)
    }

    @JvmStatic
    fun createContact(context: Context, number: String?) {
        val createContactIntent = Intent(Intent.ACTION_INSERT)
        createContactIntent.type = ContactsContract.Contacts.CONTENT_TYPE
        number?.takeIf { it.isNotEmpty() }?.let {
            createContactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, it)
        }
        if (context !is Activity) createContactIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startContactActivity(context, createContactIntent)
    }

    private fun startContactActivity(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.contacts_app_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    @JvmStatic
    fun isDmtfSettingEnabled(context: Context): Boolean {
        return Settings.System.getInt(context.contentResolver, Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1
    }

    @JvmStatic
    fun isSoundEffectsEnabled(context: Context): Boolean {
        return Settings.System.getInt(context.contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1) == 1
    }

    @JvmStatic
    fun isRingerModeSilentOrVibrate(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager? ?: return false
        val ringerMode = audioManager.ringerMode
        return ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE
    }

    @JvmStatic
    fun getColorFromAttr(context: Context, attrInt: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrInt, typedValue, true)
        return ContextCompat.getColor(context, typedValue.resourceId)
    }

    @JvmStatic
    fun getCurrentTime(): Long {
        return SystemClock.elapsedRealtime()
    }
}
