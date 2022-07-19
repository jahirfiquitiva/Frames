package dev.jahir.frames.ui.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.data.Preferences
import dev.jahir.frames.data.models.AboutItem
import dev.jahir.frames.extensions.context.findView
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.context.stringArray
import dev.jahir.frames.extensions.views.tint
import dev.jahir.frames.ui.activities.base.BaseThemedActivity
import dev.jahir.frames.ui.adapters.AboutAdapter

open class AboutActivity : BaseThemedActivity<Preferences>() {

    override val preferences: Preferences by lazy { Preferences(this) }
    private val toolbar: Toolbar? by findView(R.id.toolbar)
    private val recyclerView: RecyclerView? by findView(R.id.recycler_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }
        toolbar?.tint()

        val adapter = AboutAdapter(getDesignerAboutItems(), getInternalAboutItems())
        recyclerView?.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView?.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) supportFinishAfterTransition()
        return super.onOptionsItemSelected(item)
    }

    private fun getDesignerAboutItems(): ArrayList<AboutItem> {
        val items = ArrayList<AboutItem>()

        val itemsNames = stringArray(R.array.credits_titles)
        val itemsDescriptions = stringArray(R.array.credits_descriptions)
        val itemsPhotos = stringArray(R.array.credits_photos)
        val itemsButtonsTexts = stringArray(R.array.credits_buttons)
        val itemsButtonsLinks = stringArray(R.array.credits_links)

        val namesCount = itemsNames.size
        val descriptionsCount = itemsDescriptions.size
        val photosCount = itemsPhotos.size
        val buttonsTextsCount = itemsButtonsTexts.size
        val buttonsLinksCount = itemsButtonsLinks.size

        if (namesCount == descriptionsCount && namesCount == photosCount &&
            namesCount == buttonsTextsCount && namesCount == buttonsLinksCount) {
            itemsNames.forEachIndexed { i, s ->
                val actualButtonsTexts = itemsButtonsTexts[i].split("|")
                val actualButtonsLinks = itemsButtonsLinks[i].split("|")
                val actualButtons = ArrayList<Pair<String, String>>()

                if (actualButtonsTexts.size == actualButtonsLinks.size) {
                    actualButtonsTexts.forEachIndexed { i2, s2 ->
                        actualButtons.add(Pair(s2, actualButtonsLinks[i2]))
                    }
                }

                items.add(AboutItem(s, itemsDescriptions[i], itemsPhotos[i], actualButtons))
            }
        }
        return items
    }

    private fun getInternalAboutItems() = getAdditionalInternalAboutItems().apply {
        add(
            AboutItem(
                "Jahir Fiquitiva",
                string(R.string.jahir_description),
                "https://jahir.dev/static/images/jahir/jahir.jpg",
                arrayListOf(
                    "Website" to "https://jahir.dev",
                    "GitHub" to "https://github.com/jahirfiquitiva"
                )
            )
        )
        if (shouldIncludeContributors()) {
            add(
                AboutItem(
                    "Eduardo Pratti",
                    string(R.string.eduardo_description),
                    "https://pbs.twimg.com/profile_images/560688750247051264/seXz0Y25_400x400.jpeg",
                    arrayListOf("Website" to "https://pratti.design/")
                )
            )
            add(
                AboutItem(
                    "Patryk Michalik",
                    string(R.string.patryk_description),
                    "https://raw.githubusercontent.com/patrykmichalik/brand/master/logo-on-indigo.png",
                    arrayListOf("Website" to "https://patrykmichalik.com")
                )
            )
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    open fun getAdditionalInternalAboutItems(): ArrayList<AboutItem> = arrayListOf()

    open fun shouldIncludeContributors(): Boolean = true
}
