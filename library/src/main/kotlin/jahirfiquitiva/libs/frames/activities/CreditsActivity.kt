/*
 * Copyright (c) 2017. Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike
 * 4.0 International License. You may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jahirfiquitiva.libs.frames.activities

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.Toolbar
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.adapters.CreditsAdapter
import jahirfiquitiva.libs.frames.holders.Credit
import jahirfiquitiva.libs.kauextensions.activities.ThemedActivity
import jahirfiquitiva.libs.kauextensions.extensions.getActiveIconsColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.getSecondaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.isInHorizontalMode
import jahirfiquitiva.libs.kauextensions.extensions.primaryColor
import jahirfiquitiva.libs.kauextensions.extensions.tint
import jahirfiquitiva.libs.kauextensions.ui.views.EmptyViewRecyclerView

class CreditsActivity:ThemedActivity() {

    override fun lightTheme():Int = R.style.LightTheme
    override fun darkTheme():Int = R.style.DarkTheme
    override fun transparentTheme():Int = R.style.ClearTheme
    override fun amoledTheme():Int = R.style.AmoledTheme

    private lateinit var rv:EmptyViewRecyclerView
    private lateinit var fastScroll:RecyclerFastScroller

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.tint(getPrimaryTextColorFor(primaryColor, 0.6F),
                     getSecondaryTextColorFor(primaryColor, 0.6F),
                     getActiveIconsColorFor(primaryColor, 0.6F))

        setSupportActionBar(toolbar)

        rv = findViewById(R.id.list_rv)
        rv.state = EmptyViewRecyclerView.State.LOADING
        val layoutManager = GridLayoutManager(this, if (isInHorizontalMode) 2 else 1,
                                              GridLayoutManager.VERTICAL, false)
        rv.layoutManager = layoutManager
        val adapter = CreditsAdapter(buildCreditsList())
        adapter.setLayoutManager(layoutManager)
        rv.adapter = adapter
        rv.state = EmptyViewRecyclerView.State.NORMAL

        fastScroll = findViewById(R.id.fast_scroller)
        fastScroll.attachRecyclerView(rv)
    }

    private fun buildCreditsList():ArrayList<Credit> {
        val list = ArrayList<Credit>()

        val titles = resources.getStringArray(R.array.credits_titles)
        val descriptions = resources.getStringArray(R.array.credits_descriptions)
        val photos = resources.getStringArray(R.array.credits_photos)
        val buttons = resources.getStringArray(R.array.credits_buttons)
        val links = resources.getStringArray(R.array.credits_links)

        if (descriptions.size == titles.size && photos.size == titles.size) {
            (0 until titles.size).mapTo(list) {
                Credit(Credit.Type.CREATOR, photos[it], titles[it],
                       descriptions[it], buttons[it].split("|"), links[it].split("|"))
            }
        }

        list.add(Credit(Credit.Type.DASHBOARD, JAHIR_PHOTO_URL, "Jahir Fiquitiva",
                        resources.getString(R.string.dashboard_copyright), JAHIR_BUTTONS.split("|"),
                        JAHIR_LINKS.split("|")))

        list.add(Credit(Credit.Type.DASHBOARD, SHERRY_PHOTO_URL, "Sherry Sabatine",
                        resources.getString(R.string.sherry_description),
                        SHERRY_BUTTONS.split("|"), SHERRY_LINKS.split("|")))

        list.add(Credit(Credit.Type.DEV_CONTRIBUTION, MAX_PHOTO_URL, "Maximilian Keppeler",
                        link = "https://plus.google.com/+MaxKeppeler"))

        list.add(Credit(Credit.Type.UI_CONTRIBUTION, PATRYK_PHOTO_URL, "Patryk Goworowski",
                        link = "https://plus.google.com/+PatrykGoworowski"))

        list.add(Credit(Credit.Type.UI_CONTRIBUTION, LUMIQ_PHOTO_URL, "Lumiq Creative",
                        link = "https://plus.google.com/+LumiqCreative"))

        return list
    }
}

const val JAHIR_PHOTO_URL = "https://github.com/jahirfiquitiva/Website-Resources/raw/master/myself/me-square-white.png"
const val JAHIR_BUTTONS = "Website|Google+|Play Store"
const val JAHIR_LINKS = "https://www.jahirfiquitiva.me/|https://www.google.com/+JahirFiquitivaR|https://play.google.com/store/apps/dev?id=7438639276314720952"
const val SHERRY_PHOTO_URL = "https://pbs.twimg.com/profile_images/853258651326459904/yogDkP9p.jpg"
const val SHERRY_BUTTONS = "Website|Google+"
const val SHERRY_LINKS = "http://photography-by-sherry.com/home|https://plus.google.com/+SherrySabatine"

const val MAX_PHOTO_URL = "https://lh3.googleusercontent.com/yvcLR6mThBOpHYo6iIG9SlyEHmmVgO1LaPIv_Eu9unSGqt99fnaBVLtR1rom16c_t98tz_sxGeo8Ba5MPCI=w1107-h623-rw-no"
const val LUMIQ_PHOTO_URL = "https://lh3.googleusercontent.com/AEM9NXPSVn77YGo4SIQIeMyyTb7BWkwp96XcJlnYfHZU1fFxDZ2cvXlJSzu-3Nb-rj7Sl4x-0QMG8m_3Rg=w1107-h623-rw-no"
const val PATRYK_PHOTO_URL = "https://lh3.googleusercontent.com/EpfG2M4si7jn_lk01ure5CGDPF07Aw3YPA88NMvoG1txfGIPc-feN2LdrBby_5W8VPJNCBNGjzCtOYclHck=w1107-h623-rw-no"