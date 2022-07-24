package dev.jahir.frames.ui.viewholders

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import com.afollestad.sectionedrecyclerview.SectionedViewHolder
import dev.jahir.frames.R
import dev.jahir.frames.extensions.context.string
import dev.jahir.frames.extensions.resources.hasContent
import dev.jahir.frames.extensions.views.context
import dev.jahir.frames.extensions.views.findView
import dev.jahir.frames.extensions.views.visibleIf

class SectionHeaderViewHolder(view: View) : SectionedViewHolder(view) {
    private val titleTextView: TextView? by view.findView(R.id.section_title)
    private val subtitleTextView: TextView? by view.findView(R.id.section_subtitle)
    private val divider: View? by view.findView(R.id.divider_layout)

    fun bind(@StringRes title: Int, @StringRes subtitle: Int, showDivider: Boolean = true) {
        bind(itemView.context.string(title), itemView.context.string(subtitle), showDivider)
    }

    fun bind(@StringRes title: Int, subtitle: String, showDivider: Boolean = true) {
        bind(itemView.context.string(title), subtitle, showDivider)
    }

    fun bind(title: String, @StringRes subtitle: Int, showDivider: Boolean = true) {
        bind(title, itemView.context.string(subtitle), showDivider)
    }

    fun bind(title: String, subtitle: String, showDivider: Boolean = true) {
        titleTextView?.text = title
        titleTextView?.visibleIf(title.hasContent())
        subtitleTextView?.text = HtmlCompat.fromHtml(subtitle, HtmlCompat.FROM_HTML_MODE_COMPACT)
        subtitleTextView?.visibleIf(subtitle.hasContent())
        divider?.visibleIf(showDivider)
    }

    fun bind(
        @StringRes title: Int,
        subtitle: ((ctx: Context) -> String)?,
        showDivider: Boolean = true
    ) {
        bind(itemView.context.string(title), subtitle, showDivider)
    }

    fun bind(title: String, subtitle: ((ctx: Context) -> String)?, showDivider: Boolean = true) {
        val subtitleText = subtitle?.invoke(context).orEmpty()
        bind(title, subtitle?.invoke(context).orEmpty(), showDivider)
        if (subtitleText.hasContent()) {
            subtitleTextView?.movementMethod = LinkMovementMethod.getInstance();
        }
    }
}
