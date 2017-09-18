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
package jahirfiquitiva.libs.frames.ui.widgets

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.IdRes
import android.support.transition.ChangeBounds
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.support.v7.widget.AppCompatEditText
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import ca.allanwang.kau.kotlin.Debouncer2
import ca.allanwang.kau.kotlin.debounce
import ca.allanwang.kau.logging.KL
import ca.allanwang.kau.utils.addEndListener
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.circularHide
import ca.allanwang.kau.utils.circularReveal
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.hideKeyboard
import ca.allanwang.kau.utils.isVisible
import ca.allanwang.kau.utils.parentViewGroup
import ca.allanwang.kau.utils.setIcon
import ca.allanwang.kau.utils.setMarginTop
import ca.allanwang.kau.utils.showKeyboard
import ca.allanwang.kau.utils.string
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.tintCursor
import ca.allanwang.kau.utils.toDrawable
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.typeface.IIcon
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.frames.ui.widgets.SearchView.Configs
import jahirfiquitiva.libs.kauextensions.extensions.applyColorFilter
import jahirfiquitiva.libs.kauextensions.extensions.withAlpha
import jahirfiquitiva.libs.kauextensions.ui.views.CustomCardView
import org.jetbrains.anko.runOnUiThread

/**
 * Created by Allan Wang on 2017-06-23.
 *
 * A materialized SearchView with complete theming and customization
 * This view can be added programmatically and configured using the [Configs] DSL
 * It is preferred to add the view through an activity, but it can be attached to any ViewGroup
 * Beware of where specifically this is added, as its view or the keyboard may affect positioning
 *
 * Huge thanks to @lapism for his base
 * https://github.com/lapism/SearchView
 */
class SearchView:FrameLayout {
    
    constructor(context:Context):super(context)
    constructor(context:Context, attributeSet:AttributeSet):super(context, attributeSet)
    constructor(context:Context, attributeSet:AttributeSet, defStyleAttr:Int)
            :super(context, attributeSet, defStyleAttr)
    
    /**
     * Collection of all possible arguments when building the SearchView
     * Everything is made as opened as possible, so additional components may be found in the [SearchView]
     * However, these are the main config options
     */
    class Configs {
        /**
         * The foreground color accounts for all text colors and icon colors
         * Various alpha levels may be used for sub texts/dividers etc
         */
        var foregroundColor:Int = Color.parseColor("#de000000")
        /**
         * Namely the background for the card and recycler view
         */
        var backgroundColor:Int = Color.parseColor("#ffffff")
        /**
         * A color for icons
         */
        var iconsColor:Int = Color.parseColor("#8a000000")
        /**
         * Icon for the leftmost ImageView, which typically contains the hamburger menu/back arror
         */
        var navIcon:IIcon? = GoogleMaterial.Icon.gmd_arrow_back
        /**
         * Optional icon just to the left of the clear icon
         * This is not implemented by default, but can be used for anything, such as mic or redirects
         * Returns the extra imageview
         * Set the iicon as null to hide the extra icon
         */
        var extraIcon:Pair<IIcon, OnClickListener>? = null
        /**
         * Icon for the rightmost ImageView, which typically contains a close icon
         */
        var clearIcon:IIcon? = GoogleMaterial.Icon.gmd_clear
        /**
         * Duration for the circular reveal animation
         */
        var revealDuration:Long = 300L
        /**
         * Duration for the auto transition, which is namely used to resize the recycler view
         */
        var transitionDuration:Long = 100L
        /**
         * Defines whether the edit text and mainAdapter should clear themselves when the searchView is closed
         */
        var shouldClearOnClose:Boolean = false
        /**
         * Callback that will be called every time the searchView opens
         */
        var openListener:((searchView:SearchView) -> Unit)? = null
        /**
         * Callback that will be called every time the searchView closes
         */
        var closeListener:((searchView:SearchView) -> Unit)? = null
        /**
         * Draw a divider between the search bar and the suggestion items
         * The divider is colored based on the [foregroundColor]
         */
        var withDivider:Boolean = true
        /**
         * Hint string to be set in the searchView
         */
        var hintText:String? = null
        /**
         * Hint string res to be set in the searchView
         */
        var hintTextRes:Int = -1
        /**
         * StringRes for a "no results found" item
         * If [results] is ever set to an empty list, it will default to
         * a list with one item with this string
         *
         * For simplicity, kau contains [R.string.kau_no_results_found]
         * which you may use
         */
        var noResultsFound:Int = -1
        /**
         * Callback for when the query changes
         */
        var textCallback:(query:String, searchView:SearchView) -> Unit = { _, _ -> }
        /**
         * Callback for when the search action key is detected from the keyboard
         * Returns true if the searchbar should close afterwards, and false otherwise
         */
        var searchCallback:(query:String, searchView:SearchView) -> Boolean = { _, _ -> false }
        /**
         * Callback for when the search is closed
         */
        var closedCallback:(searchView:SearchView) -> Unit = {}
        /**
         * Debouncing interval between callbacks
         */
        var textDebounceInterval:Long = 0
        /**
         * If a [SearchItem]'s title contains the submitted query, make that portion bold
         * See [SearchItem.withHighlights]
         */
        var highlightQueryText:Boolean = true
        
        /**
         * Sets config attributes to the given searchView
         */
        internal fun apply(searchView:SearchView) {
            with(searchView) {
                tintForeground(foregroundColor, iconsColor)
                tintBackground(backgroundColor)
                editText.hint = context.string(hintTextRes, hintText)
                textCallback.terminate()
                textCallback = debounce(textDebounceInterval, this@Configs.textCallback)
            }
        }
    }
    
    /**
     * Empties the list on the UI thread
     * The noResults item will not be added
     */
    internal fun clearResults() {
        textCallback.cancel()
        context.runOnUiThread { cardTransition() }
    }
    
    val configs = Configs()
    //views
    private val card:CustomCardView by bindView(R.id.kau_search_cardview)
    private val editText:AppCompatEditText by bindView(R.id.kau_search_edit_text)
    private val iconClear:ImageView by bindView(R.id.kau_search_clear)
    private var textCallback:Debouncer2<String, SearchView> =
            debounce(0) { query, _ ->
                KL.d("Search query $query found; set your own textCallback")
            }
    var menuItem:MenuItem? = null
    val isOpen:Boolean
        get() = parent != null && card.isVisible
    var hintText = ""
    
    /*
     * Ripple start points and search view offset
     * These are calculated every time the search view is opened,
     * and can be overridden with the open listener if necessary
     */
    var menuX:Int = -1             //starting x for circular reveal
    var menuY:Int = -1             //reference for cardview's marginTop
    var menuHalfHeight:Int = -1    //starting y for circular reveal (relative to the cardview)
    
    init {
        View.inflate(context, R.layout.search_bar, this)
        iconClear.setSearchIcon(configs.clearIcon).setOnClickListener {
            editText.text.clear()
            revealClose()
        }
        tintForeground(configs.foregroundColor, configs.iconsColor)
        tintBackground(configs.backgroundColor)
        textCallback = debounce(0) { query, _ ->
            configs.textCallback(query, this@SearchView)
        }
        editText.setHintTextColor(configs.foregroundColor.withAlpha(0.5F))
        editText.hint = hintText
        editText.addTextChangedListener(object:TextWatcher {
            override fun afterTextChanged(s:Editable?) {}
            
            override fun beforeTextChanged(s:CharSequence, start:Int, count:Int, after:Int) {}
            
            override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {
                textCallback(s.toString().trim(), this@SearchView)
            }
        })
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!(configs.searchCallback(editText.text.toString(), this)))
                    editText.hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }
    }
    
    internal fun ImageView.setSearchIcon(iicon:IIcon?):ImageView {
        setIcon(iicon, sizeDp = 18, color = configs.iconsColor)
        return this
    }
    
    internal fun cardTransition(builder:TransitionSet.() -> Unit = {}) {
        TransitionManager.beginDelayedTransition(card,
                //we are only using change bounds, as the recyclerview items may be animated as well,
                //which causes a measure IllegalStateException
                                                 TransitionSet().addTransition(
                                                         ChangeBounds()).apply {
                                                     duration = configs.transitionDuration
                                                     builder()
                                                 })
    }
    
    /**
     * Update the base configurations and apply them to the searchView
     */
    fun applyConfigs(config:Configs.() -> Unit) {
        configs.config()
        configs.apply(this)
    }
    
    /**
     * Binds the SearchView to a menu item and handles everything internally
     * This is assuming that SearchView has already been added to a ViewGroup
     * If not, see the extension function [bindSearchView]
     */
    fun bind(menu:Menu, @IdRes id:Int, @ColorInt menuIconColor:Int = Color.WHITE,
             config:Configs.() -> Unit = {}):SearchView {
        applyConfigs(config)
        val menuItem = menu.findItem(id) ?: throw IllegalArgumentException(
                "Menu item with given id doesn't exist")
        if (menuItem.icon == null) menuItem.icon = GoogleMaterial.Icon.gmd_search.toDrawable(
                context, 18, menuIconColor)
        card.gone()
        menuItem.setOnMenuItemClickListener { revealOpen(); true }
        this.menuItem = menuItem
        return this
    }
    
    /**
     * Call to remove the searchView from the original menuItem,
     * with the option to replace the item click listener
     */
    fun unBind(replacementMenuItemClickListener:((item:MenuItem) -> Boolean)? = null) {
        parentViewGroup.removeView(this)
        menuItem?.setOnMenuItemClickListener(replacementMenuItemClickListener)
        menuItem = null
    }
    
    private fun configureCoords(item:MenuItem?) {
        item ?: return
        if (parent !is ViewGroup) return
        val view = parentViewGroup.findViewById<View>(item.itemId) ?: return
        val locations = IntArray(2)
        view.getLocationOnScreen(locations)
        menuX = (locations[0] + view.width / 2)
        menuHalfHeight = view.height / 2
        menuY = locations[1]
        card.viewTreeObserver.addOnPreDrawListener(object:ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw():Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                card.setMarginTop(menuY - card.height / 2)
                return true
            }
        })
    }
    
    /**
     * Handle a back press event
     * Returns true if back press is consumed, false otherwise
     */
    fun onBackPressed():Boolean {
        if (isOpen && menuItem != null) {
            revealClose()
            return true
        }
        return false
    }
    
    /**
     * Tint foreground attributes
     * This can be done publicly through [configs], which will also save the color
     */
    internal fun tintForeground(@ColorInt textColor:Int, @ColorInt iconsColor:Int) {
        iconClear.drawable.applyColorFilter(iconsColor)
        editText.tint(textColor)
        editText.tintCursor(textColor.withAlpha(0.5F))
        editText.setTextColor(ColorStateList.valueOf(textColor))
    }
    
    /**
     * Tint background attributes
     * This can be done publicly through [configs], which will also save the color
     */
    internal fun tintBackground(@ColorInt color:Int) {
        card.setCardBackgroundColor(color)
    }
    
    fun revealOpen() {
        if (parent == null || isOpen) return
        context.runOnUiThread {
            /**
             * The y component is relative to the cardView, but it hasn't been drawn yet so its own height is 0
             * We therefore use half the menuItem height, which is a close approximation to our intended value
             * The cardView matches the parent's width, so menuX is correct
             */
            configureCoords(menuItem)
            configs.openListener?.invoke(this@SearchView)
            editText.showKeyboard()
            card.circularReveal(menuX, menuHalfHeight, duration = configs.revealDuration) {
                cardTransition()
            }
        }
    }
    
    fun revealClose() {
        if (parent == null || !isOpen) return
        context.runOnUiThread {
            cardTransition {
                addEndListener {
                    card.circularHide(menuX, menuHalfHeight, duration = configs.revealDuration,
                                      onFinish = {
                                          configs.closeListener?.invoke(this@SearchView)
                                          if (configs.shouldClearOnClose) editText.text.clear()
                                      })
                    configs.closedCallback(this@SearchView)
                }
            }
            editText.hideKeyboard()
        }
    }
    
}

@DslMarker
annotation class KauSearch

/**
 * Helper function that binds to an activity's main view
 */
@KauSearch
fun Activity.bindSearchView(menu:Menu, @IdRes id:Int, @ColorInt menuIconColor:Int = Color.WHITE,
                            config:SearchView.Configs.() -> Unit = {}):SearchView
        = findViewById<ViewGroup>(android.R.id.content).bindSearchView(menu, id, menuIconColor,
                                                                       config)

/**
 * Bind searchView to a menu item; call this in [Activity.onCreateOptionsMenu]
 * Be wary that if you may reinflate the menu many times (eg through [Activity.invalidateOptionsMenu]),
 * it may be worthwhile to hold a reference to the searchview and only bind it if it hasn't been bound before
 */
@KauSearch
fun ViewGroup.bindSearchView(menu:Menu, @IdRes id:Int, @ColorInt menuIconColor:Int = Color.WHITE,
                             config:SearchView.Configs.() -> Unit = {}):SearchView {
    val searchView = SearchView(context)
    searchView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                                       FrameLayout.LayoutParams.MATCH_PARENT)
    addView(searchView)
    searchView.bind(menu, id, menuIconColor, config)
    return searchView
}