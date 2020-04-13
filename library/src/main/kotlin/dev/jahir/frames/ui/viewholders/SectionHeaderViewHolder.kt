package dev.jahir.frames.ui.viewholders

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.views.findView
import dev.jahir.frames.extensions.views.visibleIf

class SectionHeaderViewHolder(view: View) : SectionedViewHolder(view) {
    private val titleTextView: TextView? by view.findView(R.id.section_title)
    private val subtitleTextView: TextView? by view.findView(R.id.section_subtitle)
    private val divider: View? by view.findView(R.id.divider)

    fun bind(@StringRes title: Int, @StringRes subtitle: Int, showDivider: Boolean = true) {
        val actualTitle = itemView.context.string(title)
        val actualSubtitle = itemView.context.string(subtitle)
        bind(actualTitle, actualSubtitle, showDivider)
    }

    fun bind(@StringRes title: Int, subtitle: String, showDivider: Boolean = true) {
        val actualTitle = itemView.context.string(title)
        bind(actualTitle, subtitle, showDivider)
    }

    fun bind(title: String, @StringRes subtitle: Int, showDivider: Boolean = true) {
        val actualSubtitle = itemView.context.string(subtitle)
        bind(title, actualSubtitle, showDivider)
    }

    fun bind(title: String, subtitle: String, showDivider: Boolean = true) {
        titleTextView?.text = title
        titleTextView?.visibleIf(title.hasContent())
        subtitleTextView?.text = subtitle
        subtitleTextView?.visibleIf(subtitle.hasContent())
        divider?.visibleIf(showDivider)
    }
}