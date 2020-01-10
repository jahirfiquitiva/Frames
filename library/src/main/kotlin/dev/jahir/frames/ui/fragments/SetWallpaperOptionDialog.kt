package dev.jahir.frames.ui.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Wallpaper

class SetWallpaperOptionDialog : DialogFragment() {

    private var selectedOption = 0
    private var wallpaper: Wallpaper? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.apply_to)
            .setSingleChoiceItems(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) R.array.set_wallpaper_options
                else R.array.set_wallpaper_options_pre_nougat, selectedOption
            ) { _, option ->
                this.selectedOption =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) option else option + 2
            }
            .setPositiveButton(android.R.string.ok) { _, _ ->
                activity?.let { WallpaperApplierDialog.show(it, selectedOption, wallpaper) }
                dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .create()
    }

    override fun dismiss() {
        super.dismiss()
        wallpaper = null
    }

    companion object {
        private const val TAG = "SET_WALLPAPER_OPTION_DIALOG"
        fun create(wallpaper: Wallpaper? = null) =
            SetWallpaperOptionDialog().apply { this.wallpaper = wallpaper }

        fun show(activity: FragmentActivity, wallpaper: Wallpaper? = null) =
            create(wallpaper).show(activity.supportFragmentManager, TAG)
    }

}