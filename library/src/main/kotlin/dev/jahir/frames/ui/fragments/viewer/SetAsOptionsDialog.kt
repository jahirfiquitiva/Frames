package dev.jahir.frames.ui.fragments.viewer

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper
import dev.jahir.frames.extensions.mdDialog
import dev.jahir.frames.extensions.negativeButton
import dev.jahir.frames.extensions.positiveButton
import dev.jahir.frames.extensions.singleChoiceItems
import dev.jahir.frames.extensions.title

class SetAsOptionsDialog : DialogFragment() {

    private var selectedOption = 0
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
                activity?.let { ApplierDialog.show(it, selectedOption, wallpaper) }
                dismiss()
            }
            negativeButton(android.R.string.cancel) { dismiss() }
        }
    }

    override fun dismiss() {
        super.dismiss()
        wallpaper = null
    }

    companion object {
        private const val TAG = "SET_WALLPAPER_OPTION_DIALOG"
        fun create(wallpaper: Wallpaper? = null) =
            SetAsOptionsDialog().apply { this.wallpaper = wallpaper }

        fun show(activity: FragmentActivity, wallpaper: Wallpaper? = null) =
            create(wallpaper).show(activity.supportFragmentManager, TAG)
    }

}