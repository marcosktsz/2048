package com.example.game2048

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

internal data class TilePaletteColors(val container: Color, val content: Color)

internal object TilePalette {
    const val MIN_TILE_CONTRAST = 3f
    const val MIN_TEXT_CONTRAST = 4.5f

    private val mutedLightTiles = listOf(
        Color(0xFF864F3E) to Color.White,
        Color(0xFF704F6B) to Color.White,
        Color(0xFF3D687E) to Color.White,
        Color(0xFF696F3C) to Color.White,
        Color(0xFF3B6B60) to Color.White,
        Color(0xFF75538B) to Color.White,
        Color(0xFF535D77) to Color.White,
        Color(0xFF93643D) to Color.White,
    )
    private val mutedDarkTiles = listOf(
        Color(0xFFD6AE9A) to Color(0xFF29221F),
        Color(0xFFC9A5C3) to Color(0xFF29212B),
        Color(0xFFA5BFCE) to Color(0xFF20282C),
        Color(0xFFC0C395) to Color(0xFF292A1E),
        Color(0xFF9BC3B8) to Color(0xFF1F2B28),
        Color(0xFFC4A9D4) to Color(0xFF29212E),
        Color(0xFFB3BACD) to Color(0xFF252733),
        Color(0xFFD5B084) to Color(0xFF2B251D),
    )

    fun colorsFor(value: Int, colors: ColorScheme): TilePaletteColors {
        val exponent = Integer.numberOfTrailingZeros(value)
        val (container, suggestedContent) = when {
            value == 0 -> colors.surfaceContainerHigh to colors.onSurfaceVariant
            exponent == 1 -> colors.primary to colors.onPrimary
            exponent == 2 -> colors.secondary to colors.onSecondary
            exponent == 3 -> colors.tertiary to colors.onTertiary
            colors.surface.luminance() > 0.5f -> mutedLightTiles[(exponent - 4) % mutedLightTiles.size]
            else -> mutedDarkTiles[(exponent - 4) % mutedDarkTiles.size]
        }

        val content = listOf(
            suggestedContent,
            colors.onSurface,
            colors.inverseOnSurface,
            Color.Black,
            Color.White,
        ).firstOrNull { contrastRatio(it, container) >= MIN_TEXT_CONTRAST }
            ?: listOf(colors.onSurface, colors.inverseOnSurface, Color.Black, Color.White)
                .maxBy { contrastRatio(it, container) }

        return TilePaletteColors(container, content)
    }

    fun contrastRatio(first: Color, second: Color): Float {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }

}
