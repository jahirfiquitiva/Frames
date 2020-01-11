package dev.jahir.frames.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import dev.jahir.frames.R
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.gone
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.ui.activities.base.BaseFavoritesConnectedActivity
import dev.jahir.frames.ui.fragments.CollectionsFragment
import dev.jahir.frames.ui.fragments.WallpapersFragment

class CollectionActivity : BaseFavoritesConnectedActivity() {

    private val toolbar: Toolbar? by findView(R.id.toolbar)

    private val wallpapersFragment: WallpapersFragment by lazy { WallpapersFragment.create() }

    private var favoritesModified: Boolean = false

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

        val collection = intent?.extras?.getString(CollectionsFragment.COLLECTION_EXTRA, "")
        if (collection == null || !collection.hasContent()) {
            finish()
            return
        }

        supportActionBar?.title = collection

        wallpapersViewModel.observeCollections(this) {
            val rightCollection = try {
                it.getOrNull(it.indexOfFirst { coll -> coll.name == collection })
            } catch (e: Exception) {
                null
            }
            wallpapersFragment.updateItems(ArrayList(rightCollection?.wallpapers.orEmpty()))
        }
        wallpapersViewModel.loadData(this)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragments_container, wallpapersFragment, WallpapersFragment.TAG)
        transaction.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) supportFinishAfterTransition()
        return super.onOptionsItemSelected(item)
    }

    override fun onPermissionsAccepted(permissions: Array<out String>) {
        // Do nothing
    }

    override fun finish() {
        setResult(
            if (favoritesModified) ViewerActivity.FAVORITES_MODIFIED_RESULT
            else ViewerActivity.FAVORITES_NOT_MODIFIED_RESULT
        )
        super.finish()
    }

    internal fun setFavoritesModified() {
        this.favoritesModified = true
    }

    companion object {
        internal const val REQUEST_CODE = 11
    }
}