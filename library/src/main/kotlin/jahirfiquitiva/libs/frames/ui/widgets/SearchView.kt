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
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.transition.ChangeBounds
import android.support.transition.TransitionManager
import android.support.transition.TransitionSet
import android.support.v4.content.ContextCompat
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
import ca.allanwang.kau.utils.addEndListener
import ca.allanwang.kau.utils.bindView
import ca.allanwang.kau.utils.circularHide
import ca.allanwang.kau.utils.circularReveal
import ca.allanwang.kau.utils.gone
import ca.allanwang.kau.utils.hideKeyboard
import ca.allanwang.kau.utils.isVisible
import ca.allanwang.kau.utils.parentViewGroup
import ca.allanwang.kau.utils.setMarginTop
import ca.allanwang.kau.utils.showKeyboard
import ca.allanwang.kau.utils.tint
import ca.allanwang.kau.utils.tintCursor
import jahirfiquitiva.libs.frames.R
import jahirfiquitiva.libs.kauextensions.extensions.activeIconsColor
import jahirfiquitiva.libs.kauextensions.extensions.applyColorFilter
import jahirfiquitiva.libs.kauextensions.extensions.cardBackgroundColor
import jahirfiquitiva.libs.kauextensions.extensions.getDrawable
import jahirfiquitiva.libs.kauextensions.extensions.getPrimaryTextColorFor
import jahirfiquitiva.libs.kauextensions.extensions.withAlpha
import jahirfiquitiva.libs.kauextensions.ui.views.CustomCardView
import org.jetbrains.anko.runOnUiThread

/**
 * Created by Allan Wang on 2017-06-23.
 *
 * A materialized SearchView with complete theming and customization
 * This view can be added programmatically
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
    
    interface SearchListener {
        fun onQueryChanged(query:String)
        fun onQuerySubmit(query:String)
        fun onSearchOpened(searchView:SearchView)
        fun onSearchClosed(searchView:SearchView)
    }
    
    /**
     * Empties the list on the UI thread
     * The noResults item will not be added
     */
    internal fun clearResults() {
        context.runOnUiThread { cardTransition() }
    }
    
    private val card:CustomCardView by bindView(R.id.kau_search_cardview)
    private val editText:AppCompatEditText by bindView(R.id.kau_search_edit_text)
    private val iconClear:ImageView by bindView(R.id.kau_search_clear)
    
    var menuItem:MenuItem? = null
    val isOpen:Boolean
        get() = parent != null && card.isVisible
    var hintText = ""
        set(value) {
            field = value
            editText.hint = value
        }
    var listener:SearchListener? = null
    var shouldClearOnClose = true
    
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
        iconClear.setSearchIcon("ic_close".getDrawable(context)
                                        .applyColorFilter(context.activeIconsColor))
                .setOnClickListener {
                    editText.text.clear()
                    revealClose()
                }
        tintForeground(context.getPrimaryTextColorFor(context.cardBackgroundColor),
                       context.activeIconsColor)
        tintBackground(context.cardBackgroundColor)
        
        editText.setHintTextColor(
                context.getPrimaryTextColorFor(context.cardBackgroundColor).withAlpha(0.5F))
        editText.hint = hintText
        editText.addTextChangedListener(object:TextWatcher {
            override fun afterTextChanged(s:Editable?) {}
            
            override fun beforeTextChanged(s:CharSequence, start:Int, count:Int, after:Int) {}
            
            override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {
                listener?.onQueryChanged(s.toString().trim())
            }
        })
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                listener?.onQuerySubmit(editText.text.toString().trim())
                editText.hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }
    }
    
    internal fun ImageView.setSearchIcon(@DrawableRes icon:Int):ImageView {
        setImageDrawable(ContextCompat.getDrawable(context, icon))
        return this
    }
    
    internal fun ImageView.setSearchIcon(icon:Drawable):ImageView {
        setImageDrawable(icon)
        return this
    }
    
    internal fun cardTransition(builder:TransitionSet.() -> Unit = {}) {
        TransitionManager.beginDelayedTransition(card,
                //we are only using change bounds, as the recyclerview items may be animated as well,
                //which causes a measure IllegalStateException
                                                 TransitionSet().addTransition(
                                                         ChangeBounds()).apply {
                                                     duration = 100L
                                                     builder()
                                                 })
    }
    
    /**
     * Binds the SearchView to a menu item and handles everything internally
     * This is assuming that SearchView has already been added to a ViewGroup
     * If not, see the extension function [bindSearchView]
     */
    fun bind(menu:Menu, @IdRes id:Int, withExtra:Boolean):SearchView {
        val menuItem = menu.findItem(id) ?: throw IllegalArgumentException(
                "Menu item with given id doesn't exist")
        card.gone()
        configureCoords(menuItem, withExtra)
        menuItem.setOnMenuItemClickListener { revealOpen(withExtra); true }
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
    
    private fun configureCoords(item:MenuItem?, withExtra:Boolean) {
        item ?: return
        if (parent !is ViewGroup) return
        val view = parentViewGroup.findViewById<View>(item.itemId) ?: return
        val locations = IntArray(2)
        view.getLocationOnScreen(locations)
        menuX = (locations[0] + view.width / 2)
        menuHalfHeight = view.height / 2
        menuY = locations[1] + (if (withExtra) menuHalfHeight else 0)
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
        editText.setHintTextColor(textColor.withAlpha(0.5F))
    }
    
    /**
     * Tint background attributes
     * This can be done publicly through [configs], which will also save the color
     */
    internal fun tintBackground(@ColorInt color:Int) {
        card.setCardBackgroundColor(color)
    }
    
    fun revealOpen(withExtra:Boolean = false) {
        if (parent == null || isOpen) return
        context.runOnUiThread {
            /**
             * The y component is relative to the cardView, but it hasn't been drawn yet so its own height is 0
             * We therefore use half the menuItem height, which is a close approximation to our intended value
             * The cardView matches the parent's width, so menuX is correct
             */
            // configureCoords(menuItem, withExtra)
            listener?.onSearchOpened(this@SearchView)
            editText.showKeyboard()
            card.circularReveal(menuX, menuHalfHeight, duration = 350L) {
                cardTransition()
            }
        }
    }
    
    fun revealClose() {
        if (parent == null || !isOpen) return
        context.runOnUiThread {
            cardTransition {
                addEndListener {
                    card.circularHide(menuX, menuHalfHeight, duration = 350L,
                                      onFinish = {
                                          listener?.onSearchClosed(this@SearchView)
                                          if (shouldClearOnClose)
                                              editText.text.clear()
                                      })
                }
            }
            editText.hideKeyboard()
        }
    }
    
}

/**
 * Helper function that binds to an activity's main view
 */
fun Activity.bindSearchView(menu:Menu, @IdRes id:Int, withExtra:Boolean = false):SearchView
        = findViewById<ViewGroup>(android.R.id.content).bindSearchView(menu, id, withExtra)

/**
 * Bind searchView to a menu item; call this in [Activity.onCreateOptionsMenu]
 * Be wary that if you may reinflate the menu many times (eg through [Activity.invalidateOptionsMenu]),
 * it may be worthwhile to hold a reference to the searchview and only bind it if it hasn't been bound before
 */
fun ViewGroup.bindSearchView(menu:Menu, @IdRes id:Int, withExtra:Boolean = false):SearchView {
    val searchView = SearchView(context)
    searchView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                                       FrameLayout.LayoutParams.MATCH_PARENT)
    addView(searchView)
    searchView.bind(menu, id, withExtra)
    return searchView
}