package com.sebi.lifeos.lifeosapp.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette

@Composable
fun rememberAppDominantColor(
    packageName: String,
    fallback: Color
): Color {
    val context = LocalContext.current
    val pm = context.packageManager

    val state = produceState(initialValue = fallback, key1 = packageName) {
        val color = runCatching {
            val drawable = pm.getApplicationIcon(packageName)
            val bmp = drawableToBitmap(drawable)
            val palette = Palette.from(bmp).generate()

            val rgb =
                palette.dominantSwatch?.rgb
                    ?: palette.vibrantSwatch?.rgb
                    ?: palette.mutedSwatch?.rgb
                    ?: fallback.toArgb()

            Color(rgb)
        }.getOrElse { fallback }

        value = color
    }

    return state.value
}

private fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) {
        return drawable.bitmap
    }

    val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
    val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
