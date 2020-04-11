package dev.jahir.frames.extensions.resources

import android.content.res.XmlResourceParser

fun XmlResourceParser.nextOrNull(): Int? =
    try {
        next()
    } catch (e: Exception) {
        null
    }

fun XmlResourceParser.getAttributeValue(attributeName: String): String? =
    try {
        getAttributeValue(null, attributeName)
    } catch (e: Exception) {
        null
    }