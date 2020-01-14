package dev.jahir.frames.extensions

import androidx.preference.Preference

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