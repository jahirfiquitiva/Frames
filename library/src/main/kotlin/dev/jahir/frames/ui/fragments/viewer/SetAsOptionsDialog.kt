package dev.jahir.frames.ui.fragments.viewer

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.fragments.mdDialog
import dev.jahir.frames.extensions.fragments.negativeButton
import dev.jahir.frames.extensions.fragments.positiveButton
import dev.jahir.frames.extensions.fragments.singleChoiceItems
import dev.jahir.frames.extensions.fragments.title
import dev.jahir.frames.ui.activities.ViewerActivity

class SetAsOptionsDialog : DialogFragment() {

    private var selectedOption = -1
    private var wallpaper: Wallpaper? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return requireContext().mdDialog {
            title(R.string.apply_to)
            singleChoiceItems(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) R.array.set_wallpaper_options
                else R.array.set_wallpaper_options_pre_nougat, selectedOption
            ) { _, option ->
                this@SetAsOptionsDialog.selectedOption =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) option else option + 2
            }
            positiveButton(android.R.string.ok) {
                if (selectedOption >= 0)
                    (activity as? ViewerActivity)?.showApplierDialog(wallpaper, selectedOption)
                dismiss()
            }
            negativeButton(android.R.string.cancel) { dismiss() }
        }
    }

    override fun dismiss() {
        wallpaper = null
        selectedOption = -1
        try {
            super.dismiss()
        } catch (e: Exception) {
        }
    }

    companion object {
        internal const val TAG = "SET_WALLPAPER_OPTIONS_DIALOG"
        fun create(wallpaper: Wallpaper? = null) =
            SetAsOptionsDialog().apply { this.wallpaper = wallpaper }
    }
}