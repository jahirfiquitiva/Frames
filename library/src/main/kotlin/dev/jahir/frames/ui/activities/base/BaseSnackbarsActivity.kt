package dev.jahir.frames.ui.activities.base

import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import dev.jahir.frames.extensions.showSnackbar

abstract class BaseSnackbarsActivity : AppCompatActivity() {
    fun showSnackbar(
        text: CharSequence,
        duration: Int = Snackbar.LENGTH_SHORT,
        config: Snackbar.() -> Unit = {}
    ) {
        snackbarsRootView?.showSnackbar(text, duration, config)
    }

    fun showSnackbar(
        @StringRes text: Int,
        duration: Int = Snackbar.LENGTH_SHORT,
        config: Snackbar.() -> Unit = {}
    ) {
        showSnackbar(getString(text), duration, config)
    }

    open val snackbarsRootView: View? = window.decorView
}