package com.kim.minemind

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from the XML file
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
