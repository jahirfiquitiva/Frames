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

package jahirfiquitiva.libs.frames.fragments.base

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jahirfiquitiva.libs.frames.fragments.presenters.FramesFragmentPresenter
import jahirfiquitiva.libs.kauextensions.extensions.inflate

abstract class BaseViewModelFragment<in T>:LifecycleFragment(), LifecycleObserver, FramesFragmentPresenter<T> {

    internal lateinit var content:View

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun onStart() {
        initViewModel()
        super.onStart()
        registerObserver()
        loadDataFromViewModel()
    }

    override fun onCreateView(inflater:LayoutInflater?, container:ViewGroup?,
                              savedInstanceState:Bundle?):View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val contentView = container?.inflate(getContentLayout())
        contentView?.let {
            content = it
            initUI(content)
        }
        return contentView
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    abstract override fun unregisterObserver()
}