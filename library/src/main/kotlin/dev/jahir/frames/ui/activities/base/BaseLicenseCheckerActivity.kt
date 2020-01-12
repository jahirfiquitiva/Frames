package dev.jahir.frames.ui.activities.base

import dev.jahir.frames.utils.Prefs

abstract class BaseLicenseCheckerActivity<out P : Prefs> : BaseChangelogDialogActivity<P>() {

    var licenseCheckEnabled: Boolean = false
        private set

}