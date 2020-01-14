package dev.jahir.frames.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import dev.jahir.frames.R
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.gone
import dev.jahir.frames.ui.activities.base.BaseThemedActivity
import dev.jahir.frames.ui.fragments.SettingsFragment
import dev.jahir.frames.utils.Prefs

open class SettingsActivity : BaseThemedActivity<Prefs>() {

    override val prefs: Prefs by lazy { Prefs(this) }
    private val toolbar: Toolbar? by findView(R.id.toolbar)

    open fun getSettingsFragment(): SettingsFragment = SettingsFragment()

    private var preferenceDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
        findViewById<View?>(R.id.bottom_bar)?.gone()

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragments_container, getSettingsFragment(), SettingsFragment.TAG)
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) supportFinishAfterTransition()
        return super.onOptionsItemSelected(item)
    }

    fun showDialog(dialog: AlertDialog?) {
        preferenceDialog?.dismiss()
        preferenceDialog = dialog
        preferenceDialog?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceDialog?.dismiss()
    }
}