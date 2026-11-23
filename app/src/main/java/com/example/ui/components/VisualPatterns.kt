package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.WorkCategory
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Mint400

@DrawableRes
fun categoryIconRes(category: String): Int = when (WorkCategory.fromLabel(category)) {
    WorkCategory.DEEP_WORK -> R.drawable.ic_category_deep_work
    WorkCategory.CODE -> R.drawable.ic_category_code
    WorkCategory.STUDY -> R.drawable.ic_category_study
    WorkCategory.DESIGN -> R.drawable.ic_category_design
    WorkCategory.READING -> R.drawable.ic_category_reading
    WorkCategory.FITNESS -> R.drawable.ic_category_fitness
}

/**
 * Draws a subtle architectural bio-tech matrix of micro-dots and hairline accents
 * that stays crisp and unobtrusive behind modern Material 3 surfaces.
 */
fun Modifier.bioTechGridPattern(
    dotColor: Color = Mint400.copy(alpha = 0.14f),
    lineColor: Color = Emerald500.copy(alpha = 0.06f),
    spacing: Dp = 24.dp
): Modifier = this.drawBehind {
    val step = spacing.toPx()
    if (step <= 0f) return@drawBehind

    var x = step * 0.5f
    while (x < size.width) {
        drawLine(
            color = lineColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += step
    }

    var y = step * 0.5f
    while (y < size.height) {
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += step
    }

    x = step * 0.5f
    while (x < size.width) {
        y = step * 0.5f
        while (y < size.height) {
            drawCircle(
                color = dotColor,
                radius = 1.6f,
                center = Offset(x, y)
            )
            y += step
        }
        x += step
    }
}

/**
 * Draws clean, copyright-free concentric orbital contour arcs radiating from the top-end corner
 * for subtle bio-tech surface depth.
 */
fun Modifier.orbitalContourPattern(
    ringColor: Color = Mint400.copy(alpha = 0.12f),
    glowColor: Color = Emerald500.copy(alpha = 0.12f)
): Modifier = this.drawBehind {
    val anchor = Offset(size.width * 0.92f, size.height * 0.12f)
    val maxRadius = maxOf(size.width, size.height) * 0.72f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(glowColor, Color.Transparent),
            center = anchor,
            radius = maxRadius * 0.85f
        ),
        radius = maxRadius * 0.85f,
        center = anchor
    )

    val radii = listOf(0.28f, 0.48f, 0.68f, 0.90f)
    radii.forEachIndexed { idx, factor ->
        drawCircle(
            color = ringColor.copy(alpha = ringColor.alpha * (1f - idx * 0.18f)),
            radius = maxRadius * factor,
            center = anchor,
            style = Stroke(width = 1.25.dp.toPx())
        )
    }
}

@Composable
fun LevelUpBrandBadge(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Emerald500.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Mint400.copy(alpha = 0.45f)),
        modifier = modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.ic_levelup_brand),
                contentDescription = "Level Up emblem",
                tint = Mint400,
                modifier = Modifier.size(size * 0.58f)
            )
        }
    }
}

@Composable
fun IllustratedEmptyStateCard(
    @DrawableRes illustrationRes: Int,
    illustrationDescription: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionContent: (@Composable () -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                Image(
                    painter = painterResource(id = illustrationRes),
                    contentDescription = illustrationDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Emerald950.copy(alpha = 0.48f)
                                )
                            )
                        )
                )
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionContent != null) {
                Spacer(Modifier.height(14.dp))
                actionContent()
            }
        }
    }
}
