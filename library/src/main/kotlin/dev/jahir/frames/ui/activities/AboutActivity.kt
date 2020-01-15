package dev.jahir.frames.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.jahir.frames.R
import dev.jahir.frames.data.models.AboutItem
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.gone
import dev.jahir.frames.ui.activities.base.BaseThemedActivity
import dev.jahir.frames.ui.adapters.AboutAdapter
import dev.jahir.frames.utils.Prefs

class AboutActivity : BaseThemedActivity<Prefs>() {

    override val prefs: Prefs by lazy { Prefs(this) }
    private val toolbar: Toolbar? by findView(R.id.toolbar)
    private val recyclerView: RecyclerView? by findView(R.id.recycler_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)
        findViewById<View?>(R.id.bottom_bar)?.gone()

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

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

        val itemsNames = try {
            resources.getStringArray(R.array.credits_titles)
        } catch (e: Exception) {
            arrayOf<String>()
        }

        val itemsDescriptions = try {
            resources.getStringArray(R.array.credits_descriptions)
        } catch (e: Exception) {
            arrayOf<String>()
        }

        val itemsPhotos = try {
            resources.getStringArray(R.array.credits_photos)
        } catch (e: Exception) {
            arrayOf<String>()
        }

        val itemsButtonsTexts = try {
            resources.getStringArray(R.array.credits_buttons)
        } catch (e: Exception) {
            arrayOf<String>()
        }

        val itemsButtonsLinks = try {
            resources.getStringArray(R.array.credits_links)
        } catch (e: Exception) {
            arrayOf<String>()
        }

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

    private fun getInternalAboutItems(): ArrayList<AboutItem> {
        return arrayListOf(
            AboutItem(
                "Jahir Fiquitiva",
                getString(R.string.jahir_description),
                "https://jahir.dev/assets/images/me/me.jpg",
                arrayListOf(
                    "Website" to "https://jahir.dev",
                    "Twitter" to "https://twitter.com/jahirfiquitiva",
                    "GitHub" to "https://github.com/jahirfiquitiva"
                )
            ),
            AboutItem(
                "Eduardo Pratti",
                getString(R.string.eduardo_description),
                "https://pbs.twimg.com/profile_images/560688750247051264/seXz0Y25_400x400.jpeg",
                arrayListOf(
                    "Website" to "https://pratti.design/",
                    "Twitter" to "https://twitter.com/edpratti"
                )
            )
        )
    }
}