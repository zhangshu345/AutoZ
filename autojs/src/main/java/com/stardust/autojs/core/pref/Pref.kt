package com.stardust.autojs.core.pref

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.stardust.app.GlobalAppContext

object Pref {







    private val preferences = PreferenceManager.getDefaultSharedPreferences(GlobalAppContext.get())

    val isSystemFloat: Boolean
        get() {
            return preferences.getBoolean("key_systemfloat", true)
        }

    val isStableModeEnabled: Boolean
        get() {
            return preferences.getBoolean("key_stable_mode", false)
        }

    val isGestureObservingEnabled: Boolean
        get() {
            return preferences.getBoolean("key_gesture_observing", false)
        }


}