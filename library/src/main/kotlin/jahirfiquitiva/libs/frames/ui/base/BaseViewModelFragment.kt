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

package jahirfiquitiva.libs.frames.ui.base

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jahirfiquitiva.libs.frames.ui.fragments.presenters.ViewModelFragmentPresenter

abstract class BaseViewModelFragment<in T>:BasicFragment<T>(), LifecycleObserver, ViewModelFragmentPresenter<T> {
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        initVM()
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun initVM() {
        initViewModel()
        registerObserver()
    }
    
    override fun onCreateView(inflater:LayoutInflater?, container:ViewGroup?,
                              savedInstanceState:Bundle?):View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (autoStartLoad()) loadDataFromViewModel()
        return view
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    abstract override fun unregisterObserver()
    
    abstract fun autoStartLoad():Boolean
}