/*
 * One4Home Wallpaper Bridge helper, v1.0.2
 * https://one4home.app/developers/wallpaper-bridge
 *
 * Copied unchanged from the integration kit except for the package name, so it can be
 * compared with (and updated from) the upstream kit.
 *
 * MIT License
 *
 * Copyright (c) 2026 One4Studio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.jahir.frames.data.one4home

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Copyable AndroidX helper. Connect with StartActivityForResult, then notify after a successful Home apply. */
object One4HomeWallpaperBridge {
    const val HOME_PACKAGE = "com.one4studio.one4home"
    fun connectionIntent(homePackage: String = HOME_PACKAGE): Intent = Intent().setClassName(
        homePackage, "com.one4studio.one4home.ConnectWallpaperActivity")

    /** Call when expiring a shared cache file, after allowing time for asynchronous delivery. */
    fun revoke(context: Context, file: File, homePackage: String = HOME_PACKAGE) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.one4home.bridge", file)
        if (android.os.Build.VERSION.SDK_INT >= 26) context.revokeUriPermission(homePackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        else context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    /** The exact file applied to FLAG_SYSTEM; wallpaperId is WallpaperManager.setStream's returned ID. */
    fun notifyApplied(context: Context, image: File, wallpaperId: Int, depth: File? = null,
        mimeType: String = "image/jpeg", homePackage: String = HOME_PACKAGE) {
        require(wallpaperId > 0) { "A successful system wallpaper apply is required" }
        require(mimeType.startsWith("image/"))
        val authority = "${context.packageName}.one4home.bridge"
        val uri = FileProvider.getUriForFile(context, authority, image)
        val depthUri = depth?.let { FileProvider.getUriForFile(context, authority, it) }
        val intent = Intent("com.one4studio.one4home.IMPORT_STATIC_WALLPAPER_SOURCE")
            .setClassName(homePackage, "com.one4studio.one4home.WallpaperSourceImportReceiver")
            .setDataAndType(uri, mimeType)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .putExtra("com.one4studio.one4home.extra.WALLPAPER_ID", wallpaperId)
            .putExtra("com.one4studio.one4home.extra.REQUEST_DEPTH", false)
        intent.clipData = ClipData.newRawUri("Applied wallpaper", uri).apply {
            depthUri?.let { addItem(ClipData.Item(it)) }
        }
        depthUri?.let { intent.putExtra("com.one4studio.one4home.extra.DEPTH_SOURCE", it.toString()) }
        // Manifest receivers do not receive Activity-lifetime URI grants reliably on all Android versions.
        context.grantUriPermission(homePackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        depthUri?.let { context.grantUriPermission(homePackage, it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        context.sendBroadcast(intent)
    }
}
