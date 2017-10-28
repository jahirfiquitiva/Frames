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
package jahirfiquitiva.libs.frames.ui.fragments.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.Preference
import android.preference.PreferenceManager
import android.preference.PreferenceScreen
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import jahirfiquitiva.libs.kauextensions.extensions.bind

/**
 * A PreferenceFragment for the support library. Based on the platform's code with some removed
 * features and a basic ListView layout.
 
 * @author Christophe Beyls
 */
abstract class PreferenceFragment : Fragment() {
    
    private val FIRST_REQUEST_CODE = 100
    private val MSG_BIND_PREFERENCES = 1
    private val MSG_REQUEST_FOCUS = 2
    private val PREFERENCES_TAG = "android:preferences"
    private var HC_HORIZONTAL_PADDING = 0.8 //5.33
    
    private var mHavePrefs: Boolean = false
    private var mInitDone: Boolean = false
    private var mList: ListView? = null
    
    var preferenceManager: PreferenceManager? = null
        private set
    
    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_BIND_PREFERENCES -> bindPreferences()
                MSG_REQUEST_FOCUS -> mList!!.focusableViewAvailable(mList)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val c = PreferenceManager::class.java.getDeclaredConstructor(
                    Activity::class.java,
                    Int::class.javaPrimitiveType)
            c.isAccessible = true
            preferenceManager = c.newInstance(activity, FIRST_REQUEST_CODE)
        } catch (ignored: Exception) {
        }
        
    }
    
    override fun onCreateView(
            layoutInflater: LayoutInflater, viewGroup: ViewGroup?,
            savedInstanceState: Bundle?
                             ): View {
        val listView = ListView(activity)
        listView.id = android.R.id.list
        listView.dividerHeight = 0
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            HC_HORIZONTAL_PADDING = 5.33
        }
        val horizontalPadding = (HC_HORIZONTAL_PADDING * resources.displayMetrics.density).toInt()
        listView.setPadding(horizontalPadding, 0, horizontalPadding, 0)
        return listView
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (mHavePrefs) {
            bindPreferences()
        }
        mInitDone = true
        savedInstanceState?.let {
            val container = it.getBundle(PREFERENCES_TAG)
            container?.let {
                preferenceScreen?.restoreHierarchyState(it)
            }
        }
    }
    
    override fun onStop() {
        super.onStop()
        try {
            val m = PreferenceManager::class.java.getDeclaredMethod("dispatchActivityStop")
            m.isAccessible = true
            m.invoke(preferenceManager)
        } catch (ignored: Exception) {
        }
    }
    
    override fun onDestroyView() {
        mList = null
        mHandler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            val m = PreferenceManager::class.java.getDeclaredMethod("dispatchActivityDestroy")
            m.isAccessible = true
            m.invoke(preferenceManager)
        } catch (ignored: Exception) {
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        preferenceScreen?.let {
            val container = Bundle()
            it.saveHierarchyState(container)
            outState.putBundle(PREFERENCES_TAG, container)
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            val m = PreferenceManager::class.java.getDeclaredMethod(
                    "dispatchActivityResult",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    Intent::class.java)
            m.isAccessible = true
            m.invoke(preferenceManager, requestCode, resultCode, data)
        } catch (ignored: Exception) {
        }
    }
    
    private var preferenceScreen: PreferenceScreen?
        get() = try {
            val m = PreferenceManager::class.java.getDeclaredMethod("getPreferenceScreen")
            m.isAccessible = true
            m.invoke(preferenceManager) as PreferenceScreen
        } catch (e: Exception) {
            null
        }
        set(screen) = try {
            val m = PreferenceManager::class.java.getDeclaredMethod(
                    "setPreferences",
                    PreferenceScreen::class.java)
            m.isAccessible = true
            val result = m.invoke(preferenceManager, screen) as Boolean
            if (result && screen != null) {
                mHavePrefs = true
                if (mInitDone) postBindPreferences() else {
                }
            } else {
            }
        } catch (ignored: Exception) {
        }
    
    fun addPreferencesFromIntent(intent: Intent) {
        requirePreferenceManager()
        try {
            val m = PreferenceManager::class.java.getDeclaredMethod(
                    "inflateFromIntent",
                    Intent::class.java,
                    PreferenceScreen::class.java)
            m.isAccessible = true
            val screen = m.invoke(
                    preferenceManager, intent,
                    preferenceScreen) as PreferenceScreen
            preferenceScreen = screen
        } catch (ignored: Exception) {
        }
    }
    
    protected fun addPreferencesFromResource(resId: Int) {
        requirePreferenceManager()
        try {
            val m = PreferenceManager::class.java.getDeclaredMethod(
                    "inflateFromResource",
                    Context::class.java,
                    Int::class.javaPrimitiveType,
                    PreferenceScreen::class.java)
            m.isAccessible = true
            val screen = m.invoke(
                    preferenceManager, activity, resId,
                    preferenceScreen) as PreferenceScreen
            preferenceScreen = screen
        } catch (ignored: Exception) {
        }
    }
    
    protected fun findPreference(key: CharSequence): Preference? {
        preferenceManager?.let {
            return it.findPreference(key)
        }
        return null
    }
    
    private fun requirePreferenceManager() {
        if (preferenceManager == null) {
            throw RuntimeException("This should be called after super.onCreate.")
        }
    }
    
    private fun postBindPreferences() {
        if (!mHandler.hasMessages(MSG_BIND_PREFERENCES)) {
            mHandler.sendEmptyMessage(MSG_BIND_PREFERENCES)
        }
    }
    
    private fun bindPreferences() {
        preferenceScreen?.bind(listView)
    }
    
    private val listView: ListView?
        get() {
            ensureList()
            return mList
        }
    
    private fun ensureList() {
        if (mList != null) {
            return
        }
        val layout = view ?: throw IllegalStateException("Content view not yet created")
        val list: View by layout.bind(android.R.id.list)
        val listV = list as? ListView ?: throw RuntimeException(
                "Content has view with id attribute 'android.R.id.list' that is not a ListView class")
        mList = listV
        mHandler.sendEmptyMessage(MSG_REQUEST_FOCUS)
    }
}