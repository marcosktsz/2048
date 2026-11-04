package com.marcosktsz.material2048

import android.graphics.Rect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.roundToInt

private val MIN_EXCLUSION_STRIP_WIDTH = 48.dp
private val MAX_EXCLUSION_HEIGHT = 200.dp

fun Modifier.gameBoardGestureExclusion(): Modifier = composed {
    val view = LocalView.current
    val density = LocalDensity.current
    DisposableEffect(view) {
        onDispose { ViewCompat.setSystemGestureExclusionRects(view, emptyList()) }
    }
    onGloballyPositioned { coordinates ->
        val bounds = coordinates.boundsInRoot()
        val maxHeightPx = with(density) { MAX_EXCLUSION_HEIGHT.toPx() }
        val centerY = (bounds.top + bounds.bottom) / 2f
        val halfHeight = minOf(bounds.height, maxHeightPx) / 2f
        val top = (centerY - halfHeight).roundToInt().coerceAtLeast(0)
        val bottom = (centerY + halfHeight).roundToInt()
        val gestureInsets = ViewCompat.getRootWindowInsets(view)
            ?.getInsets(WindowInsetsCompat.Type.systemGestures())
        val minStripWidthPx = with(density) { MIN_EXCLUSION_STRIP_WIDTH.toPx() }.roundToInt()
        val leftWidth = maxOf(gestureInsets?.left ?: 0, minStripWidthPx)
        val rightWidth = maxOf(gestureInsets?.right ?: 0, minStripWidthPx)
        ViewCompat.setSystemGestureExclusionRects(
            view,
            listOf(
                Rect(0, top, leftWidth, bottom),
                Rect(view.width - rightWidth, top, view.width, bottom),
            ),
        )
    }
}
