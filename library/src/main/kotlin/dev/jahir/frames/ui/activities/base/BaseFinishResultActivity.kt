package dev.jahir.frames.ui.activities.base

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity

abstract class BaseFinishResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            onSafeBackPressed()
        }
    }

    override fun finish() {
        onFinish()
        super.finish()
    }

    override fun finishAfterTransition() {
        onFinish()
        super.finishAfterTransition()
    }

    open fun onSafeBackPressed() {
        supportFinishAfterTransition()
    }

    open fun onFinish() {}
}
