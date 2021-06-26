package com.stardust.auojs.inrt

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceScreen
import android.util.Log
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar



/**
 *
 */

class SettingsActivity : AppCompatActivity() {


    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        setContentView(R.layout.activity_settings)
        fragmentManager.beginTransaction().replace(R.id.fragment_setting, PreferenceFragment()).commit()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.text_settings)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class PreferenceFragment : android.preference.PreferenceFragment() {

        override fun onCreate(@Nullable savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preference)
        }

        override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen?, preference: Preference?): Boolean {

            Log.d("inrt", "onPreferenceTreeClick: ")
            val key = preference!!.key
            Log.d("inrt", "onPreferenceTreeClick: "+Pref.shouldEnableFloatingWindow());
            return super.onPreferenceTreeClick(preferenceScreen, preference)
        }
    }



}
