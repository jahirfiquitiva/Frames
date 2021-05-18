package dev.jahir.frames.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.data.models.Collection
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.views.gone
import dev.jahir.frames.ui.activities.base.BaseChangelogDialogActivity
import dev.jahir.frames.ui.fragments.WallpapersFragment

open class CollectionActivity : BaseChangelogDialogActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }

    open val wallpapersFragment: WallpapersFragment by lazy {
        WallpapersFragment.create(canModifyFavorites = canModifyFavorites())
    }

    private var collection: Collection? = null
    private var collectionName: String = ""
    private var favoritesModified: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragments)

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
        loadWallpapersData()

        replaceFragment(wallpapersFragment, WallpapersFragment.IN_COLLECTION_TAG)
    }

    override fun onResume() {
        super.onResume()
        bottomNavigation?.gone()
    }

    override fun getMenuRes(): Int = R.menu.toolbar_menu_simple

    override fun getSearchHint(itemId: Int): String = string(R.string.search_wallpapers)

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
        super.onSaveInstanceState(outState)
        outState.putString(COLLECTION_NAME_KEY, collectionName)
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
