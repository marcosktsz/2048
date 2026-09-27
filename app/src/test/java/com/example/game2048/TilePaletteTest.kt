package com.example.game2048

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test

class TilePaletteTest {
    private val tileValues = listOf(0, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096)
    private val higherTileValues = listOf(16, 32, 64, 128, 256, 512, 1024, 2048)

    @Test
    fun allTilesMeetContrastTargetsInLightAndDarkThemes() {
        assertPaletteContrast(lightColorScheme())
        assertPaletteContrast(darkColorScheme())
    }

    @Test
    fun expressiveAccentsUseContrastingRolePairsOnSurface() {
        val palette = lightColorScheme(
            surface = Color(0xFFFCF9FF),
            primary = Color(0xFF455B8C),
            onPrimary = Color.White,
            secondary = Color(0xFF625B71),
            onSecondary = Color.White,
            tertiary = Color(0xFF77536F),
            onTertiary = Color.White,
        )

        assertPaletteContrast(palette)
    }

    @Test
    fun confirmationDialogTextRolesStayLegibleInLightAndDarkThemes() {
        listOf(lightColorScheme(), darkColorScheme()).forEach { colors ->
            val dialogSurface = colors.surfaceContainerHigh
            assertTrue(
                "Dialog title does not meet WCAG AA contrast",
                TilePalette.contrastRatio(colors.onSurface, dialogSurface) >= TilePalette.MIN_TEXT_CONTRAST,
            )
            assertTrue(
                "Dialog body does not meet WCAG AA contrast",
                TilePalette.contrastRatio(colors.onSurfaceVariant, dialogSurface) >= TilePalette.MIN_TEXT_CONTRAST,
            )
            val scoreCardSurface = colors.surfaceContainer
            assertTrue(
                "Score card value does not meet WCAG AA contrast",
                TilePalette.contrastRatio(colors.onSurface, scoreCardSurface) >= TilePalette.MIN_TEXT_CONTRAST,
            )
            assertTrue(
                "Score card label does not meet WCAG AA contrast",
                TilePalette.contrastRatio(colors.onSurfaceVariant, scoreCardSurface) >= TilePalette.MIN_TEXT_CONTRAST,
            )
        }
    }

    @Test
    fun headerTextRolesStayLegibleOnThePageSurface() {
        listOf(lightColorScheme(), darkColorScheme()).forEach { colors ->
            listOf(colors.surface, colors.surfaceContainerHigh).forEach { background ->
                assertTrue(
                    "Header title does not meet WCAG AA contrast",
                    TilePalette.contrastRatio(colors.onSurface, background) >= TilePalette.MIN_TEXT_CONTRAST,
                )
                assertTrue(
                    "Header subtitle does not meet WCAG AA contrast",
                    TilePalette.contrastRatio(colors.onSurfaceVariant, background) >= TilePalette.MIN_TEXT_CONTRAST,
                )
            }
        }
    }

    @Test
    fun higherTilesUseDistinctMutedColorsInLightAndDarkThemes() {
        listOf(lightColorScheme(), darkColorScheme()).forEach { colors ->
            val tiles = higherTileValues.map { TilePalette.colorsFor(it, colors).container }
            tiles.indices.forEach { index ->
                val tile = tiles[index]
                val channelSpread = maxOf(tile.red, tile.green, tile.blue) - minOf(tile.red, tile.green, tile.blue)
                assertTrue("Higher tile colors should stay muted", channelSpread <= 0.36f)
                if (index > 0) {
                    assertTrue(
                        "Adjacent higher tiles should be visually distinct",
                        colorDistance(tiles[index - 1], tile) >= 0.08f,
                    )
                }
            }
            assertTrue(
                "Tile 4 and tile 32 should not reuse the same color",
                TilePalette.colorsFor(4, colors).container != TilePalette.colorsFor(32, colors).container,
            )
        }
    }

    private fun assertPaletteContrast(colors: ColorScheme) {
        val board = colors.surface
        tileValues.forEach { value ->
            val tile = TilePalette.colorsFor(value, colors)
            if (value != 0) {
                assertTrue(
                    "Tile $value fill does not stand apart from the surface",
                    TilePalette.contrastRatio(tile.container, board) >= TilePalette.MIN_TILE_CONTRAST,
                )
                assertTrue(
                    "Tile $value text does not meet WCAG AA contrast",
                    TilePalette.contrastRatio(tile.content, tile.container) >= TilePalette.MIN_TEXT_CONTRAST,
                )
            }
        }
    }

    private fun colorDistance(first: Color, second: Color): Float = kotlin.math.sqrt(
        (first.red - second.red) * (first.red - second.red) +
            (first.green - second.green) * (first.green - second.green) +
            (first.blue - second.blue) * (first.blue - second.blue),
    )
}
