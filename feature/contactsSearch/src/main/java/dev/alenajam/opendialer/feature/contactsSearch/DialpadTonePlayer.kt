package dev.alenajam.opendialer.feature.contactsSearch

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import dev.alenajam.opendialer.core.common.CommonUtils

internal class DialpadTonePlayer(
    private val context: Context,
) {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_DTMF, TONE_VOLUME)

    fun start(digit: Char) {
        if (!CommonUtils.isDmtfSettingEnabled(context)) return

        digit.toTone()?.let { tone ->
            toneGenerator.startTone(tone)
        }
    }

    fun stop() {
        toneGenerator.stopTone()
    }

    fun release() {
        toneGenerator.release()
    }

    private fun Char.toTone(): Int? = when (this) {
        '0' -> ToneGenerator.TONE_DTMF_0
        '1' -> ToneGenerator.TONE_DTMF_1
        '2' -> ToneGenerator.TONE_DTMF_2
        '3' -> ToneGenerator.TONE_DTMF_3
        '4' -> ToneGenerator.TONE_DTMF_4
        '5' -> ToneGenerator.TONE_DTMF_5
        '6' -> ToneGenerator.TONE_DTMF_6
        '7' -> ToneGenerator.TONE_DTMF_7
        '8' -> ToneGenerator.TONE_DTMF_8
        '9' -> ToneGenerator.TONE_DTMF_9
        '*' -> ToneGenerator.TONE_DTMF_S
        '#' -> ToneGenerator.TONE_DTMF_P
        else -> null
    }

    private companion object {
        const val TONE_VOLUME = 80
    }
}
