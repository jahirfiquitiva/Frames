package dev.jahir.frames.utils.extensions

import android.content.Context
import androidx.recyclerview.widget.RecyclerView

val RecyclerView.ViewHolder.context: Context
    get() = itemView.context