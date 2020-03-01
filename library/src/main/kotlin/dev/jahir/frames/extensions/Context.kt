package dev.jahir.frames.extensions

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import dev.jahir.frames.R
import dev.jahir.frames.ui.activities.base.BaseThemedActivity
import dev.jahir.frames.utils.Prefs
import java.io.File

@Suppress("DEPRECATION")
fun Context.isNetworkAvailable(): Boolean {
    try {
        var connectivityManager: ConnectivityManager? = try {
            ContextCompat.getSystemService(this, ConnectivityManager::class.java)
        } catch (ignored: Exception) {
            null
        }
        if (connectivityManager == null)
            try {
                connectivityManager =
                    getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager?
            } catch (ignored: Exception) {
            }
        connectivityManager ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            capabilities?.let {
                it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                        || it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } ?: false
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo ?: return false
            val connected =
                activeNetworkInfo.isAvailable && activeNetworkInfo.isConnectedOrConnecting
            return connected || connectivityManager.allNetworkInfo.any { it.isConnectedOrConnecting }
        }
    } catch (ignored: Exception) {
        return false
    }
}

fun Context.resolveColor(@AttrRes attr: Int, fallback: Int = 0): Int {
    val a = theme.obtainStyledAttributes(intArrayOf(attr))
    try {
        return a.getColor(0, fallback)
    } finally {
        a.recycle()
    }
}

fun Context.getDrawable(name: String?): Drawable? {
    name ?: return null
    if (!name.hasContent()) return null
    return try {
        ContextCompat.getDrawable(this, resources.getIdentifier(name, "drawable", packageName))
    } catch (e: Exception) {
        null
    }
}

fun Context.toast(@StringRes res: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, res, duration).show()
}

fun Context.toast(content: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, content, duration).show()
}

@ColorInt
fun Context.getRightNavigationBarColor(): Int = if (prefs.shouldColorNavbar) {
    try {
        resolveColor(R.attr.colorSurface, ContextCompat.getColor(this, R.color.surface))
    } catch (e: Exception) {
        Color.parseColor("#000000")
    }
} else {
    Color.parseColor("#000000")
}

@Suppress("DEPRECATION")
val Context.currentVersionCode: Long
    get() = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, 0).longVersionCode
        } else {
            packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
        }
    } catch (e: Exception) {
        -1L
    }

fun Context.getAppName(defName: String = ""): String {
    var name: String = try {
        (packageManager?.getApplicationLabel(applicationInfo) ?: "").toString()
    } catch (e: Exception) {
        ""
    }
    if (name.hasContent()) return name

    val stringRes = applicationInfo?.labelRes ?: 0
    name = if (stringRes == 0) {
        applicationInfo?.nonLocalizedLabel?.toString() ?: ""
    } else {
        getString(stringRes)
    }

    if (name.hasContent()) return name
    if (defName.hasContent()) return defName

    val def = getString(R.string.app_name)
    return if (def.hasContent()) def else "Unknown"
}

val Context.isUpdate: Boolean
    get() {
        val prevVersion = prefs.lastVersion
        prefs.lastVersion = currentVersionCode
        return currentVersionCode > prevVersion
    }

fun Context.compliesWithMinTime(time: Long): Boolean =
    System.currentTimeMillis() - firstInstallTime > time

val Context.firstInstallTime: Long
    get() {
        return try {
            packageManager.getPackageInfo(packageName, 0).firstInstallTime
        } catch (e: Exception) {
            -1
        }
    }

fun Context.openLink(url: String?) {
    val link = url ?: return
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    if (browserIntent.resolveActivity(packageManager) != null)
        startActivity(browserIntent)
    else toast("Cannot find a browser")
}

val Context.dataCacheSize: String
    get() {
        var cache: Long = 0
        var extCache: Long = 0

        try {
            cacheDir?.listFiles()?.forEach {
                cache += if (it.isDirectory) it.dirSize else it.length()
            }
        } catch (ignored: Exception) {
        }

        try {
            externalCacheDir?.listFiles()?.forEach {
                extCache += if (it.isDirectory) it.dirSize else it.length()
            }
        } catch (ignored: Exception) {
        }

        val finalResult = ((cache + extCache) / 1024).toDouble()

        return if (finalResult > 1024) String.format("%.2f", finalResult / 1024) + " MB"
        else String.format("%.2f", finalResult) + " KB"
    }

fun Context.clearDataAndCache() {
    val appDir = File(cacheDir?.parent ?: return)
    appDir.let {
        if (it.exists()) {
            it.list()?.forEach { fl ->
                if (!fl.equals("lib", true)) File(appDir, fl).deleteEverything()
            }
        }
    }
    clearCache()
}

fun Context.clearCache() {
    try {
        cacheDir?.deleteEverything()
    } catch (ignored: Exception) {
    }
}

val Context.prefs: Prefs
    get() = (this as? BaseThemedActivity<*>)?.prefs ?: Prefs(this)

val Context.currentNightMode: Int
    get() = try {
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    } catch (e: Exception) {
        Configuration.UI_MODE_NIGHT_UNDEFINED
    }