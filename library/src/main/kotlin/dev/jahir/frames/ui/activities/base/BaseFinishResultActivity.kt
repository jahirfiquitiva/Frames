package dev.jahir.frames.ui.activities.base

abstract class BaseFinishResultActivity : BaseSnackbarsActivity() {
    override fun finish() {
        onFinish()
        super.finish()
    }

    override fun finishAfterTransition() {
        onFinish()
        super.finishAfterTransition()
    }

    override fun onBackPressed() {
        supportFinishAfterTransition()
    }

    open fun onFinish() {}
}