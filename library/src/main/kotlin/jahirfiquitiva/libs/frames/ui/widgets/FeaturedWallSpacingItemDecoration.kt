/*
 * Copyright (c) 2019. Jahir Fiquitiva
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

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import jahirfiquitiva.libs.kext.ui.decorations.GridSpacingItemDecoration

class FeaturedWallSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean = true
                                       ) :
    GridSpacingItemDecoration(spanCount, spacing, includeEdge) {
    
    override fun internalOffsetsSetup(outRect: Rect, view: View, parent: RecyclerView) {
        val position = parent.getChildAdapterPosition(view)
        if (position < 0) return
        if (position == 0) {
            outRect.set(spacing, spacing, spacing, spacing)
            return
        }
        val column = ((position - 1) % spanCount)
        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
            if (position in 2..(spanCount - 1)) outRect.top = spacing
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) outRect.top = spacing
        }
    }
}
