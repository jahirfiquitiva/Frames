package dev.jahir.frames.ui.activities.base

import androidx.appcompat.app.AppCompatActivity

abstract class BaseFinishResultActivity : AppCompatActivity() {
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