package dev.jahir.frames.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.Toolbar
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.data.viewmodels.WallpapersDataViewModel
import dev.jahir.frames.extensions.context.findView
import dev.jahir.frames.extensions.context.isNetworkAvailable
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.fragments.mdDialog
import dev.jahir.frames.extensions.fragments.message
import dev.jahir.frames.extensions.fragments.negativeButton
import dev.jahir.frames.extensions.fragments.positiveButton
import dev.jahir.frames.extensions.fragments.title
import dev.jahir.frames.extensions.utils.lazyViewModel
import dev.jahir.frames.extensions.views.gone
import dev.jahir.frames.extensions.views.goneIf
import dev.jahir.frames.extensions.views.tint
import dev.jahir.frames.muzei.FramesArtProvider
import dev.jahir.frames.ui.activities.base.BaseThemedActivity

open class MuzeiSettingsActivity : BaseThemedActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }

    private var dialog: AlertDialog? = null
    private var selectedCollections = ""

    private val collsSummaryText: TextView? by findView(R.id.choose_collections_summary)
    private val checkBox: AppCompatCheckBox? by findView(R.id.wifi_checkbox)

    open val viewModel: WallpapersDataViewModel by lazyViewModel()

    @SuppressLint("WrongViewCast", "MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_muzei_settings)
        viewModel.loadData(
            string(R.string.json_url),
            loadCollections = true, loadFavorites = false, force = false
        )

        selectedCollections = preferences.muzeiCollections
        checkBox?.isChecked = preferences.refreshMuzeiOnWiFiOnly

        val toolbar: Toolbar? by findView(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.muzei_settings)
        toolbar?.tint()

        findViewById<View?>(R.id.other_divider)?.goneIf(!shouldShowCollections())

        findViewById<LinearLayout>(R.id.wifi_only).setOnClickListener {
            checkBox?.toggle()
            saveChanges()
        }
        collsSummaryText?.text = getString(R.string.choose_collections_summary, selectedCollections)

        if (shouldShowCollections()) {
            findViewById<View?>(R.id.choose_collections)?.setOnClickListener {
                if (isNetworkAvailable()) {
                    if (viewModel.collections.isNotEmpty())
                        showChooseCollectionsDialog()
                } else {
                    showNotConnectedDialog()
                }
            }
        } else {
            findViewById<View?>(R.id.choose_collections)?.gone()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) doFinish()
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() = doFinish()

    private fun saveChanges() {
        preferences.refreshMuzeiOnWiFiOnly = checkBox?.isChecked ?: false
        preferences.muzeiCollections = selectedCollections
    }

    private fun showNotConnectedDialog() {
        destroyDialog()
        dialog = mdDialog {
            title(R.string.muzei_not_connected_title)
            message(R.string.muzei_not_connected_content)
            positiveButton(android.R.string.ok)
        }
        dialog?.show()
    }

    private fun showChooseCollectionsDialog() {
        destroyDialog()
        val collections = ArrayList<Collection>()
        collections.addAll(viewModel.collections.distinct())
        collections.add(0, Collection("Favorites"))
        collections.distinct()
        val eachSelectedCollection = selectedCollections.split(",").map { it.trim() }.distinct()
        val mappedCollections = ArrayList(collections.map {
            Pair(it, eachSelectedCollection.any { co -> co.equals(it.displayName, true) })
        })

        dialog = mdDialog {
            title(R.string.choose_collections_title)
            setMultiChoiceItems(
                mappedCollections.map { it.first.displayName }.toTypedArray(),
                mappedCollections.map { it.second }.toBooleanArray()
            ) { _, i, checked ->
                mappedCollections[i] = Pair(mappedCollections[i].first, checked)
            }
            positiveButton(android.R.string.ok) { d ->
                selectedCollections = mappedCollections.filter { it.second }
                    .joinToString(", ") { it.first.displayName }
                collsSummaryText?.text = getString(
                    R.string.choose_collections_summary, selectedCollections
                )
                saveChanges()
                d.dismiss()
            }
            negativeButton(android.R.string.cancel)
        }
        dialog?.show()
    }

    private fun doFinish() {
        destroyDialog()
        saveChanges()
        try {
            viewModel.destroy(this)
        } catch (ignored: Exception) {
        }
        try {
            startService(getProviderIntent()?.apply {
                putExtra("restart", true)
            })
        } catch (e: Exception) {
        }
        supportFinishAfterTransition()
    }

    private fun destroyDialog() {
        dialog?.dismiss()
        dialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        doFinish()
    }

    open fun shouldShowCollections(): Boolean = true

    open fun getProviderIntent(): Intent? = Intent(this, FramesArtProvider::class.java)
}