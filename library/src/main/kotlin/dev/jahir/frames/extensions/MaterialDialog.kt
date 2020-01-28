package dev.jahir.frames.extensions

import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.view.View
import android.widget.ListAdapter
import androidx.annotation.ArrayRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Context.mdDialog(options: MaterialAlertDialogBuilder.() -> MaterialAlertDialogBuilder): AlertDialog {
    return MaterialAlertDialogBuilder(this).options().create()
}

fun MaterialAlertDialogBuilder.title(@StringRes titleResId: Int) = setTitle(titleResId)
fun MaterialAlertDialogBuilder.title(title: String) = setTitle(title)

fun MaterialAlertDialogBuilder.message(@StringRes messageResId: Int) = setMessage(messageResId)
fun MaterialAlertDialogBuilder.message(message: String) = setMessage(message)

fun MaterialAlertDialogBuilder.positiveButton(@StringRes positiveButtonResId: Int, onClick: (DialogInterface) -> Unit = {}) =
    setPositiveButton(positiveButtonResId) { d, _ -> onClick(d) }

fun MaterialAlertDialogBuilder.positiveButton(
    positiveButtonText: String,
    onClick: (DialogInterface) -> Unit = {}
) = setPositiveButton(positiveButtonText) { d, _ -> onClick(d) }

fun MaterialAlertDialogBuilder.negativeButton(@StringRes negativeButtonResId: Int, onClick: (DialogInterface) -> Unit = {}) =
    setNegativeButton(negativeButtonResId) { d, _ -> onClick(d) }

fun MaterialAlertDialogBuilder.negativeButton(
    negativeButtonText: String,
    onClick: (DialogInterface) -> Unit = {}
) = setNegativeButton(negativeButtonText) { d, _ -> onClick(d) }

fun MaterialAlertDialogBuilder.neutralButton(@StringRes neutralButtonResId: Int, onClick: (DialogInterface) -> Unit = {}) =
    setNeutralButton(neutralButtonResId) { d, _ -> onClick(d) }

fun MaterialAlertDialogBuilder.neutralButton(
    neutralButtonText: String,
    onClick: (DialogInterface) -> Unit = {}
) = setNeutralButton(neutralButtonText) { d, _ -> onClick(d) }

fun MaterialAlertDialogBuilder.view(@LayoutRes layoutResId: Int) = setView(layoutResId)
fun MaterialAlertDialogBuilder.view(view: View?) = setView(view)
fun MaterialAlertDialogBuilder.cancelable(cancelable: Boolean) = setCancelable(cancelable)

fun MaterialAlertDialogBuilder.singleChoiceItems(
    @ArrayRes itemsResId: Int, checkedItem: Int = -1,
    onClick: (dialog: DialogInterface, which: Int) -> Unit = { _, _ -> }
) = setSingleChoiceItems(itemsResId, checkedItem) { d, w -> onClick(d, w) }

fun MaterialAlertDialogBuilder.singleChoiceItems(
    cursor: Cursor, checkedItem: Int = -1, labelColumn: String = "",
    onClick: (dialog: DialogInterface, which: Int) -> Unit = { _, _ -> }
) = setSingleChoiceItems(cursor, checkedItem, labelColumn) { d, w -> onClick(d, w) }

fun MaterialAlertDialogBuilder.singleChoiceItems(
    items: List<Any>, checkedItem: Int = -1,
    onClick: (dialog: DialogInterface, which: Int) -> Unit = { _, _ -> }
): MaterialAlertDialogBuilder {
    val itemsAsStrings = items.map { it.toString() }
    val actualItems = arrayOfNulls<CharSequence>(itemsAsStrings.size)
    ArrayList(itemsAsStrings).toArray(actualItems)
    return setSingleChoiceItems(actualItems, checkedItem) { d, w -> onClick(d, w) }
}

fun MaterialAlertDialogBuilder.singleChoiceItems(
    items: Array<CharSequence>, checkedItem: Int = -1,
    onClick: (dialog: DialogInterface, which: Int) -> Unit = { _, _ -> }
) = setSingleChoiceItems(items, checkedItem) { d, w -> onClick(d, w) }

fun MaterialAlertDialogBuilder.singleChoiceItems(
    adapter: ListAdapter, checkedItem: Int = -1,
    onClick: (dialog: DialogInterface, which: Int) -> Unit = { _, _ -> }
) = setSingleChoiceItems(adapter, checkedItem) { d, w -> onClick(d, w) }

fun MaterialAlertDialogBuilder.adapter(
    adapter: ListAdapter,
    onClick: (dialog: DialogInterface, which: Int) -> Unit = { _, _ -> }
) = setAdapter(adapter, onClick)