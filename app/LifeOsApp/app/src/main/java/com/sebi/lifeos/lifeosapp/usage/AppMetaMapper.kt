package com.sebi.lifeos.lifeosapp.usage

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

object AppMetaMapper {

    fun resolveLabel(pm: PackageManager, packageName: String): String {
        return try {
            val app = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(app).toString()
        } catch (_: Throwable) {
            packageName
        }
    }

    /**
     * Categorías limitadas a: Estudio, Juegos, Música, Entretenimiento, Otros.
     */
    fun resolveCategory(pm: PackageManager, packageName: String): String {
        return try {
            val app = pm.getApplicationInfo(packageName, 0)
            when (app.category) {
                ApplicationInfo.CATEGORY_GAME -> "Juegos"
                ApplicationInfo.CATEGORY_AUDIO -> "Música"
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Estudio"
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE,
                ApplicationInfo.CATEGORY_SOCIAL,
                ApplicationInfo.CATEGORY_NEWS -> "Entretenimiento"
                else -> "Otros"
            }
        } catch (_: Throwable) {
            "Otros"
        }
    }
}
