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

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import jahirfiquitiva.libs.frames.R

class CustomToolbar:Toolbar {
    constructor(context:Context):super(context)
    constructor(context:Context, attributeSet:AttributeSet):super(context, attributeSet)
    constructor(context:Context, attributeSet:AttributeSet, defStyleAttr:Int)
            :super(context, attributeSet, defStyleAttr)
    
    fun bindToActivity(activity:AppCompatActivity, showBackArrow:Boolean = true) {
        activity.setSupportActionBar(this)
        activity.supportActionBar?.let {
            with(it) {
                setHomeButtonEnabled(showBackArrow)
                setDisplayHomeAsUpEnabled(showBackArrow)
                setDisplayShowHomeEnabled(showBackArrow)
            }
        }
        if (showBackArrow) navigationIcon = ContextCompat.getDrawable(context, R.drawable.ic_back)
    }
}