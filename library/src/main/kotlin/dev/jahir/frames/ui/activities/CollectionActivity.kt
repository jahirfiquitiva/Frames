package dev.jahir.frames.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import dev.jahir.frames.R
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.extensions.gone
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.ui.activities.base.BaseChangelogDialogActivity
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.frames.utils.Prefs

open class CollectionActivity : BaseChangelogDialogActivity<Prefs>() {

    override val prefs: Prefs by lazy { Prefs(this) }

    open val wallpapersFragment: WallpapersFragment by lazy {
        WallpapersFragment.create(canModifyFavorites = canModifyFavorites())
    }

    private var collection: Collection? = null
    private var collectionName: String = ""
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

        collection = intent?.extras?.getParcelable(COLLECTION_KEY)
        collectionName = intent?.extras?.getString(COLLECTION_NAME_KEY, "") ?: ""
        if (!collectionName.hasContent()) {
            finish()
            return
        }

        supportActionBar?.title = collection?.displayName ?: collectionName

        wallpapersViewModel.observeCollections(this) {
            val rightCollection = try {
                it.getOrNull(it.indexOfFirst { coll ->
                    coll.name == collection?.name ?: collectionName
                })
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

    override fun getMenuRes(): Int = R.menu.toolbar_menu_simple

    override fun getSearchHint(itemId: Int): String = getString(R.string.search_wallpapers)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) supportFinishAfterTransition()
        return super.onOptionsItemSelected(item)
    }

    override fun internalDoSearch(filter: String, closed: Boolean) {
        super.internalDoSearch(filter, closed)
        wallpapersFragment.setRefreshEnabled(!filter.hasContent())
        wallpapersFragment.applyFilter(filter, closed)
    }

    override fun onFinish() {
        super.onFinish()
        setResult(
            if (favoritesModified) ViewerActivity.FAVORITES_MODIFIED_RESULT
            else ViewerActivity.FAVORITES_NOT_MODIFIED_RESULT,
            Intent().apply {
                putExtra(ViewerActivity.FAVORITES_MODIFIED, favoritesModified)
            }
        )
    }

    internal fun setFavoritesModified() {
        this.favoritesModified = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(COLLECTION_NAME_KEY, collectionName)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        collectionName = savedInstanceState.getString(COLLECTION_NAME_KEY, "") ?: ""
    }

    companion object {
        internal const val REQUEST_CODE = 11
        internal const val COLLECTION_KEY = "collection"
        internal const val COLLECTION_NAME_KEY = "collection_name"
    }
}