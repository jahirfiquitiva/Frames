package dev.jahir.frames.utils

import android.content.Context
import android.content.res.XmlResourceParser
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import androidx.appcompat.app.AlertDialog
import dev.jahir.frames.R
import dev.jahir.frames.extensions.adapter
import dev.jahir.frames.extensions.findView
import dev.jahir.frames.extensions.hasContent
import dev.jahir.frames.extensions.inflate
import dev.jahir.frames.extensions.mdDialog
import dev.jahir.frames.extensions.positiveButton
import dev.jahir.frames.extensions.title
import org.xmlpull.v1.XmlPullParser

/**
 * Utils originally created by Allan Wang
 * Available at https://github.com/AllanWang/KAU
 * I have added them here (copy/pasted) because this lib doesn't really uses/needs all its features
 * at a 100%.
 * Anyway, full credits go to Allan, for these awesome extensions
 */

/**
 * Internals of the changelog dialog
 * Contains an mainAdapter for each item, as well as the tags to parse
 */

fun Context.buildChangelogDialog(
    @XmlRes xmlRes: Int = R.xml.changelog,
    @StringRes title: Int = R.string.changelog,
    @StringRes btnText: Int = android.R.string.ok
): AlertDialog? = buildChangelogDialog(xmlRes, getString(title), getString(btnText))

fun Context.buildChangelogDialog(
    @XmlRes xmlRes: Int,
    title: String,
    btnText: String
): AlertDialog? {
    return try {
        val items = parse(this, xmlRes)
        val adapter = MyChangelogAdapter(this)
        adapter.addAll(items)
        mdDialog {
            title(title)
            adapter(adapter)
            positiveButton(btnText) { it.dismiss() }
        }
    } catch (e: Exception) {
        null
    }
}

fun parse(context: Context, @XmlRes xmlRes: Int): List<Pair<String, ChangelogType>> {
    val items = mutableListOf<Pair<String, ChangelogType>>()
    val parser: XmlResourceParser? = try {
        context.resources.getXml(xmlRes)
    } catch (ignored: Exception) {
        null
    }
    var closed = false
    try {
        var eventType = parser?.eventType
        while (eventType != XmlPullParser.END_DOCUMENT && eventType != null) {
            if (eventType == XmlPullParser.START_TAG)
                ChangelogType.values.any { type -> parser?.let { type.add(it, items) } ?: false }
            eventType = parser?.next()
        }
    } catch (ignored: Exception) {
        closed = true
        try {
            parser?.close()
        } catch (ignored: Exception) {
        }
    } finally {
        if (!closed) parser?.close()
    }
    return items
}

class MyChangelogAdapter(context: Context) :
    ArrayAdapter<Pair<String, ChangelogType>>(context, R.layout.item_changelog_title) {

    override fun getViewTypeCount(): Int = 2

    override fun getItemViewType(position: Int): Int =
        getItem(position)?.second?.itemViewType ?: -1

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return when (getItemViewType(position)) {
            0, 1 -> {
                try {
                    val item = getItem(position)
                    val view = parent.inflate(item?.second?.layout ?: 0)
                    val text: TextView? by view.findView(R.id.kau_changelog_text)
                    text?.text = item?.first.orEmpty()
                    view
                } catch (e: Exception) {
                    super.getView(position, convertView, parent)
                }
            }
            else -> super.getView(position, convertView, parent)
        }
    }

    override fun isEnabled(position: Int): Boolean = false
    override fun areAllItemsEnabled(): Boolean = false
}

enum class ChangelogType(
    val tag: String,
    val attr: String,
    @LayoutRes val layout: Int,
    val itemViewType: Int
) {
    TITLE("version", "title", R.layout.item_changelog_title, 0),
    ITEM("item", "text", R.layout.item_changelog_content, 1);

    companion object {
        val values = values()
    }

    /**
     * Returns true if tag matches; false otherwise
     */
    fun add(parser: XmlResourceParser, list: MutableList<Pair<String, ChangelogType>>): Boolean {
        if (parser.name != tag) return false
        if (parser.getAttributeValue(null, attr).toString().hasContent())
            list.add(Pair(parser.getAttributeValue(null, attr), this))
        return true
    }
}