package com.uwi.btmap.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

class TTS(context: Context)
    : TextToSpeech.OnInitListener {

    private val TAG = "TTS"
    private val tts: TextToSpeech = TextToSpeech(context, this)

    override fun onInit(i: Int) {
        if (i == TextToSpeech.SUCCESS) {

            val localeUS = Locale.US

            val result = tts.setLanguage(localeUS)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.d(TAG, "onInit: Language not supported.")
            }

        } else {
            Log.d(TAG, "onInit: Failed to init.")
        }
    }

    fun play(message: String) {
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}