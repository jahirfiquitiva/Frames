package dev.jahir.frames.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.jahir.frames.R
import dev.jahir.frames.data.viewmodels.WallpapersDataViewModel
import dev.jahir.frames.ui.fragments.CollectionsFragment
import dev.jahir.frames.ui.fragments.FragmentState
import dev.jahir.frames.ui.fragments.WallpapersFragment
import dev.jahir.frames.utils.extensions.navigationBarColor
import dev.jahir.frames.utils.extensions.statusBarColor

private const val MAX_HISTORIC = 2

class BaseFramesActivity : AppCompatActivity() {

    private val bottomBar: BottomNavigationView? by lazy { findViewById<BottomNavigationView?>(R.id.bottom_bar) }

    private val wallpapersViewModel: WallpapersDataViewModel by lazy {
        ViewModelProviders.of(this).get(WallpapersDataViewModel::class.java)
    }
    private val wallpapersFragment: WallpapersFragment by lazy {
        WallpapersFragment.create(ArrayList(wallpapersViewModel.wallpapers))
    }
    private val collectionsFragment: CollectionsFragment by lazy {
        CollectionsFragment.create(ArrayList(wallpapersViewModel.collections))
    }

    private var currentFragment: Fragment? = null
    private val listState = mutableListOf<FragmentState>()
    private var currentTag: String = WallpapersFragment.TAG
    private var oldTag: String = WallpapersFragment.TAG
    private var currentMenuItemId: Int = R.id.wallpapers

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (savedInstanceState == null) loadFirstFragment()

        bottomBar?.setOnNavigationItemSelectedListener { menuItem ->
            if (currentMenuItemId != menuItem.itemId) {
                var fragment: Fragment? = null
                oldTag = currentTag
                currentMenuItemId = menuItem.itemId
                when (currentMenuItemId) {
                    R.id.wallpapers -> {
                        currentTag = WallpapersFragment.TAG
                        fragment = wallpapersFragment
                    }
                    R.id.collections -> {
                        currentTag = CollectionsFragment.TAG
                        fragment = collectionsFragment
                    }
                    /*
                    R.id.favorites -> {
                        currentTag = TAG_THIRD
                        fragment = NumberFragment.create("3")
                    }
                    */
                }
                loadFragment(fragment, currentTag)
                return@setOnNavigationItemSelectedListener true
            }
            false
        }

        wallpapersViewModel.observeWallpapers(this) { wallpapersFragment.updateItems(ArrayList(it)) }
        wallpapersViewModel.observeCollections(this) { collectionsFragment.updateItems(it) }
        wallpapersViewModel.loadData(this, "https://jahir.dev/frames/frames.json")

        /*
        val fab: FloatingActionButton? = findViewById(R.id.fab)
        fab?.setOnClickListener { changeNightMode(!isNightMode, true) }
        */
    }

    private fun recoverFragment() {
        val lastState = listState.last()
        listState.removeAt(listState.size - 1)

        currentTag = lastState.currentFragmentTag
        oldTag = lastState.previousFragmentTag

        val ft = supportFragmentManager.beginTransaction()

        val currentFragment = supportFragmentManager.findFragmentByTag(currentTag)
        val oldFragment = supportFragmentManager.findFragmentByTag(oldTag)

        if (currentFragment?.isVisible == true && oldFragment?.isHidden == true) {
            ft.hide(currentFragment).show(oldFragment)
        }

        ft.commit()

        val menu = bottomBar?.menu
        when (oldTag) {
            WallpapersFragment.TAG -> menu?.getItem(0)?.isChecked = true
            CollectionsFragment.TAG -> menu?.getItem(1)?.isChecked = true
            // TAG_THIRD -> menu?.getItem(2)?.isChecked = true
        }
    }

    private fun loadFirstFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        currentFragment = wallpapersFragment
        currentFragment?.let {
            transaction.add(R.id.fragments_container, it, WallpapersFragment.TAG)
        }
        transaction.commit()
    }

    private fun loadFragment(fragment: Fragment?, tag: String) {
        fragment ?: return
        if (currentFragment !== fragment) {
            val ft = supportFragmentManager.beginTransaction()
            if (fragment.isAdded) {
                currentFragment?.let { ft.hide(it).show(fragment) }
            } else {
                currentFragment?.let { ft.hide(it).add(R.id.fragments_container, fragment, tag) }
            }
            currentFragment = fragment
            ft.commit()
            addBackStack()
        }

    }

    private fun addBackStack() {
        when (listState.size) {
            MAX_HISTORIC -> {
                listState[1].previousFragmentTag = WallpapersFragment.TAG
                val firstState = listState[1]
                for (i in listState.indices) {
                    if (listState.indices.contains((i + 1))) {
                        listState[i] = listState[i + 1]
                    }
                }
                listState[0] = firstState
                listState[listState.lastIndex] = FragmentState(currentTag, oldTag)
            }
            else -> {
                listState.add(FragmentState(currentTag, oldTag))
            }
        }

    }

    override fun onResume() {
        super.onResume()
        statusBarColor = ContextCompat.getColor(this, R.color.primaryDark)
        navigationBarColor = ContextCompat.getColor(this, R.color.primaryDark)
    }

    override fun onBackPressed() {
        if (listState.size > 1) {
            recoverFragment()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wallpapersViewModel.destroy(this)
    }
}