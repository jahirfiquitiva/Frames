package dev.jahir.frames.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.R
import dev.jahir.frames.data.models.AboutItem
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.views.findView
import dev.jahir.frames.extensions.views.loadFramesPic
import dev.jahir.frames.extensions.views.visibleIf
import dev.jahir.frames.ui.widgets.AboutButtonsLayout

class AboutViewHolder(view: View) : SectionedViewHolder(view) {

    private val photoImageView: AppCompatImageView? by view.findView(R.id.photo)
    private val nameTextView: TextView? by view.findView(R.id.name)
    private val descriptionTextView: TextView? by view.findView(R.id.description)
    private val buttonsView: AboutButtonsLayout? by view.findView(R.id.buttons)

    fun bind(aboutItem: AboutItem?) {
        aboutItem ?: return
        nameTextView?.text = aboutItem.name
        nameTextView?.visibleIf(aboutItem.name.hasContent())
        descriptionTextView?.text = aboutItem.description
        descriptionTextView?.visibleIf(aboutItem.description.orEmpty().hasContent())
        aboutItem.links.forEach { buttonsView?.addButton(it.first, it.second) }
        buttonsView?.visibleIf(aboutItem.links.isNotEmpty())
        photoImageView?.loadFramesPic(aboutItem.photoUrl.orEmpty(), cropAsCircle = true)
    }
}