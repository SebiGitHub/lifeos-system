package com.sebi.lifeos.lifeosapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sebi.lifeos.lifeosapp.usage.AppMetaMapper
import com.sebi.lifeos.lifeosapp.util.TimeFmt
import kotlin.math.max

@Composable
fun AppUsageRow(
    packageName: String,
    label: String,
    totalMs: Long,
    maxMs: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pm = context.packageManager

    val displayLabel = remember(packageName, label) {
        val looksLikePkg = label.contains(".") && label.any { it.isLetter() }
        val resolved = if (looksLikePkg) AppMetaMapper.resolveLabel(pm, packageName) else label
        resolved.ifBlank { label }
    }

    val denom = max(1L, maxMs)
    val progress = (totalMs.toFloat() / denom.toFloat()).coerceIn(0f, 1f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppIcon(packageName = packageName)

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = displayLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            GradientProgressBar(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.width(4.dp))

        Text(
            text = TimeFmt.msToHuman(totalMs),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * ✅ Overload “simple” (misma estructura que usa tu TodayScreen en el ZIP):
 * AppUsageRow(packageName=..., totalMs=...)
 */
@Composable
fun AppUsageRow(
    packageName: String,
    totalMs: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pm = context.packageManager

    val displayLabel = remember(packageName) {
        AppMetaMapper.resolveLabel(pm, packageName).ifBlank { packageName }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppIcon(packageName = packageName)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.width(4.dp))

        Text(
            text = TimeFmt.msToHuman(totalMs),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
