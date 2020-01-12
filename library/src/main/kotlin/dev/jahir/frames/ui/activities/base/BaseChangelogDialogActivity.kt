package dev.jahir.frames.ui.activities.base

import androidx.appcompat.app.AlertDialog
import dev.jahir.frames.extensions.currentVersionCode
import dev.jahir.frames.extensions.prefs
import dev.jahir.frames.utils.buildChangelogDialog

abstract class BaseChangelogDialogActivity : BaseSearchableActivity() {

    private val changelogDialog: AlertDialog? by lazy { buildChangelogDialog() }

    internal fun showChangelog(force: Boolean = false) {
        val prevVersion = prefs.lastVersion
        prefs.lastVersion = currentVersionCode
        val isUpdate = currentVersionCode > prevVersion
        if (isUpdate || force) changelogDialog?.show()
    }

    override fun onDestroy() {
        changelogDialog?.dismiss()
        super.onDestroy()
    }
}