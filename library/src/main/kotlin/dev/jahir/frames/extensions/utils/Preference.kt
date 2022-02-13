package dev.jahir.frames.extensions.utils

import androidx.preference.Preference
import androidx.preference.PreferenceGroup

fun Preference.setOnClickListener(onClick: () -> Unit = {}) {
    setOnPreferenceClickListener { onClick();true }
}

fun Preference.setOnCheckedChangeListener(onCheckedChange: (checked: Boolean) -> Unit = {}) {
    setOnPreferenceChangeListener { _, newValue ->
        onCheckedChange(
            (newValue as? @ParameterName(name = "checked") Boolean)
                ?: newValue.toString().equals("true", true)
        )
        true
    }
}

fun PreferenceGroup.removePreference(pref: Preference?) {
    pref ?: return
    this.removePreference(pref)
}
