package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Full-bleed scenic alpine twilight & mirror lake wallpaper designed specifically
 * as an iOS-style glassmorphic backdrop.
 */
@Composable
fun ScenicMountainLakeWallpaper(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val horizonY = h * 0.64f

            // 1. Twilight Sky Gradient (Deep Starlit Indigo -> Aurora Violet -> Sunset Rose/Amber)
            drawRect(
                brush = Brush.verticalGradient(
                    0.0f to Color(0xFF070B1A),
                    0.22f to Color(0xFF14183E),
                    0.42f to Color(0xFF2B1E58),
                    0.55f to Color(0xFF5D2A6C),
                    0.64f to Color(0xFF9E426E),
                    1.0f to Color(0xFF0D1528)
                ),
                size = size
            )

            // 2. Soft Aurora Borealis Ribbons in the Upper Sky
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x4400F0FF),
                        Color(0x1800FF87),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.22f, h * 0.18f),
                    radius = w * 0.75f
                ),
                center = Offset(w * 0.22f, h * 0.18f),
                radius = w * 0.75f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x44FF2E93),
                        Color(0x22A855F7),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.82f, h * 0.26f),
                    radius = w * 0.7f
                ),
                center = Offset(w * 0.82f, h * 0.26f),
                radius = w * 0.7f
            )

            // 3. Crisp Starfield in Upper Twilight Sky
            val starPositions = listOf(
                Offset(0.12f, 0.07f) to 2.2f,
                Offset(0.28f, 0.05f) to 1.6f,
                Offset(0.46f, 0.09f) to 2.5f,
                Offset(0.67f, 0.06f) to 1.8f,
                Offset(0.88f, 0.08f) to 2.4f,
                Offset(0.19f, 0.14f) to 1.5f,
                Offset(0.38f, 0.13f) to 2.0f,
                Offset(0.58f, 0.15f) to 1.7f,
                Offset(0.76f, 0.12f) to 2.6f,
                Offset(0.92f, 0.16f) to 1.6f,
                Offset(0.08f, 0.21f) to 1.9f,
                Offset(0.51f, 0.22f) to 2.1f,
                Offset(0.83f, 0.23f) to 1.8f
            )
            starPositions.forEach { (pos, radiusDp) ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.75f),
                    radius = radiusDp.dp.toPx(),
                    center = Offset(w * pos.x, h * pos.y)
                )
            }

            // 4. Warm Sunset / Alpine Horizon Glow Orb Behind the Peaks
            val sunCenter = Offset(w * 0.54f, horizonY - h * 0.11f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x88FFB300),
                        Color(0x55FF2E93),
                        Color(0x18A855F7),
                        Color.Transparent
                    ),
                    center = sunCenter,
                    radius = w * 0.65f
                ),
                center = sunCenter,
                radius = w * 0.65f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xEEFFF3D1),
                        Color(0xAAFF8A65),
                        Color.Transparent
                    ),
                    center = sunCenter,
                    radius = w * 0.16f
                ),
                center = sunCenter,
                radius = w * 0.16f
            )

            // 5. Distant Mountain Range (Soft Lavender-Blue Silhouette)
            val distantPeaks = Path().apply {
                moveTo(0f, horizonY)
                lineTo(0f, horizonY - h * 0.14f)
                lineTo(w * 0.16f, horizonY - h * 0.23f)
                lineTo(w * 0.31f, horizonY - h * 0.16f)
                lineTo(w * 0.49f, horizonY - h * 0.28f)
                lineTo(w * 0.66f, horizonY - h * 0.18f)
                lineTo(w * 0.84f, horizonY - h * 0.26f)
                lineTo(w, horizonY - h * 0.17f)
                lineTo(w, horizonY)
                close()
            }
            drawPath(
                path = distantPeaks,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3E2F6B),
                        Color(0xFF282249)
                    ),
                    startY = horizonY - h * 0.28f,
                    endY = horizonY
                )
            )

            // 6. Midground Majestic Alpine Range
            val midPeaks = Path().apply {
                moveTo(0f, horizonY)
                lineTo(0f, horizonY - h * 0.10f)
                lineTo(w * 0.22f, horizonY - h * 0.21f)
                lineTo(w * 0.39f, horizonY - h * 0.11f)
                lineTo(w * 0.62f, horizonY - h * 0.22f)
                lineTo(w * 0.79f, horizonY - h * 0.12f)
                lineTo(w * 0.94f, horizonY - h * 0.19f)
                lineTo(w, horizonY - h * 0.14f)
                lineTo(w, horizonY)
                close()
            }
            drawPath(
                path = midPeaks,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF27244E),
                        Color(0xFF171A3A)
                    ),
                    startY = horizonY - h * 0.22f,
                    endY = horizonY
                )
            )

            // 7. Foreground Rugged Mountain Ridges Framing the Lake
            val forePeaks = Path().apply {
                moveTo(0f, horizonY)
                lineTo(0f, horizonY - h * 0.08f)
                lineTo(w * 0.12f, horizonY - h * 0.14f)
                lineTo(w * 0.29f, horizonY - h * 0.06f)
                lineTo(w * 0.45f, horizonY - h * 0.12f)
                lineTo(w * 0.71f, horizonY - h * 0.05f)
                lineTo(w * 0.89f, horizonY - h * 0.13f)
                lineTo(w, horizonY - h * 0.07f)
                lineTo(w, horizonY)
                close()
            }
            drawPath(
                path = forePeaks,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF191D3A),
                        Color(0xFF0E1326)
                    ),
                    startY = horizonY - h * 0.14f,
                    endY = horizonY
                )
            )

            // 8. Mirror Alpine Lake Below the Horizon
            drawRect(
                brush = Brush.verticalGradient(
                    0.0f to Color(0xFF4A2856),
                    0.18f to Color(0xFF26234C),
                    0.50f to Color(0xFF141D3B),
                    0.82f to Color(0xFF0B1328),
                    1.0f to Color(0xFF070C1A)
                ),
                topLeft = Offset(0f, horizonY),
                size = Size(w, h - horizonY)
            )

            // 9. Sunset & Aurora Shimmering Reflections on the Lake Water
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x44FF8A65),
                        Color(0x28FF2E93),
                        Color(0x1400F0FF),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.54f, horizonY + h * 0.08f),
                    radius = w * 0.55f
                ),
                center = Offset(w * 0.54f, horizonY + h * 0.08f),
                radius = w * 0.55f
            )

            // Subtle horizontal water reflection ripples
            val rippleOffsets = listOf(0.02f, 0.05f, 0.09f, 0.14f, 0.20f, 0.27f)
            rippleOffsets.forEachIndexed { idx, relY ->
                val y = horizonY + h * relY
                val rippleWidth = w * (0.45f - idx * 0.05f)
                val startX = w * 0.54f - rippleWidth / 2f
                drawLine(
                    color = Color.White.copy(alpha = 0.14f - idx * 0.018f),
                    start = Offset(startX, y),
                    end = Offset(startX + rippleWidth, y),
                    strokeWidth = 1.5.dp.toPx()
                )
            }

            // Horizon mist glow line
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color(0x66FF9E80),
                        Color(0x8800F0FF),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, horizonY),
                end = Offset(w, horizonY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

/**
 * Reusable iPhone-style translucent frosted glass card with a specular glass rim.
 */
@Composable
fun IosGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    accentGlowColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val borderBrush = if (accentGlowColor != null) {
        Brush.verticalGradient(
            listOf(
                accentGlowColor.copy(alpha = 0.65f),
                Color.White.copy(alpha = 0.22f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.38f),
                Color.White.copy(alpha = 0.12f)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x42FFFFFF),
                        Color(0x1CFFFFFF)
                    )
                )
            )
            .background(Color(0x4D0F172A))
            .border(BorderStroke(1.dp, borderBrush), shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        content = content
    )
}

/**
 * iPhone-style glowing squircle icon badge where each icon has its own distinct radial color glow.
 */
@Composable
fun GlowingGlassIcon(
    icon: ImageVector,
    contentDescription: String?,
    glowColor: Color,
    isSelected: Boolean = true,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    val squircleShape = RoundedCornerShape(size * 0.34f)
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                // Distinct colored radial halo glow around each icon
                val haloRadius = this.size.minDimension * (if (isSelected) 0.98f else 0.78f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = if (isSelected) 0.72f else 0.44f),
                            glowColor.copy(alpha = if (isSelected) 0.28f else 0.14f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = haloRadius
                    ),
                    radius = haloRadius,
                    center = center
                )
            }
            .clip(squircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isSelected) {
                        listOf(
                            glowColor.copy(alpha = 0.48f),
                            glowColor.copy(alpha = 0.22f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.20f),
                            glowColor.copy(alpha = 0.12f)
                        )
                    }
                )
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isSelected) 0.85f else 0.45f),
                        glowColor.copy(alpha = if (isSelected) 0.90f else 0.55f)
                    )
                ),
                shape = squircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) Color.White else glowColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Compact frosted glass pill button with a colored icon glow.
 */
@Composable
fun GlassActionPill(
    label: String,
    icon: ImageVector,
    glowColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (enabled) 0.22f else 0.10f),
                        glowColor.copy(alpha = if (enabled) 0.16f else 0.05f)
                    )
                )
            )
            .background(Color(0x440F172A))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.50f),
                        glowColor.copy(alpha = 0.55f)
                    )
                ),
                shape = shape
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.65f),
                                Color.Transparent
                            ),
                            radius = size.minDimension * 0.85f
                        ),
                        radius = size.minDimension * 0.85f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
