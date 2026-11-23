package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val iconBitmap: ImageBitmap? = null
)

object InstalledAppsHelper {

    fun getInstalledApps(context: Context): List<InstalledAppItem> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            pm.queryIntentActivities(mainIntent, 0)
        } catch (_: Exception) {
            emptyList()
        }

        val myPackage = context.packageName
        val apps = mutableListOf<InstalledAppItem>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == myPackage) continue // Don't block self

            val name = resolveInfo.loadLabel(pm)?.toString() ?: pkg
            val iconDrawable = try {
                resolveInfo.loadIcon(pm)
            } catch (_: Exception) {
                null
            }

            val bitmap = iconDrawable?.let { drawableToImageBitmap(it) }
            apps.add(InstalledAppItem(packageName = pkg, appName = name, iconBitmap = bitmap))
        }

        return apps.distinctBy { it.packageName }.sortedBy { it.appName.lowercase() }
    }

    fun getAppInfo(context: Context, packageName: String): InstalledAppItem {
        val pm = context.packageManager
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val name = pm.getApplicationLabel(appInfo).toString()
            val icon = pm.getApplicationIcon(appInfo)
            InstalledAppItem(packageName = packageName, appName = name, iconBitmap = drawableToImageBitmap(icon))
        } catch (_: Exception) {
            // Fallback for custom or uninstalled package names (e.g. "Instagram", "com.instagram.android")
            val prettyName = packageName.substringAfterLast(".").replaceFirstChar { it.uppercase() }
            InstalledAppItem(packageName = packageName, appName = prettyName.ifBlank { packageName }, iconBitmap = null)
        }
    }

    private fun drawableToImageBitmap(drawable: Drawable): ImageBitmap? {
        return try {
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                return drawable.bitmap.asImageBitmap()
            }
            val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(128) else 96
            val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(128) else 96
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
}
