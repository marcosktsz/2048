package com.example.game2048

import kotlin.math.abs

internal class SwipeGestureTracker(
    private val startedAtMillis: Long,
    private val thresholdPx: Float = 28f,
    private val timeoutMillis: Long = 1_000L,
    private val commitThresholdPx: Float = thresholdPx,
) {
    var direction: Direction? = null
        private set

    var distance: Float = 0f
        private set

    var progressDelta: Float = 0f
        private set

    var isCancelled = false
        private set

    private var dragX = 0f
    private var dragY = 0f
    private var reverseDistance = 0f
    private var lastProgressAtMillis = startedAtMillis

    fun add(deltaX: Float, deltaY: Float, nowMillis: Long) {
        if (isCancelled) return

        dragX += deltaX
        dragY += deltaY
        if (direction == null) {
            if (deltaX != 0f || deltaY != 0f) lastProgressAtMillis = nowMillis
            if (maxOf(abs(dragX), abs(dragY)) > thresholdPx) {
                direction = if (abs(dragX) > abs(dragY)) {
                    if (dragX > 0f) Direction.RIGHT else Direction.LEFT
                } else {
                    if (dragY > 0f) Direction.DOWN else Direction.UP
                }
            }
        }

        direction?.let { lockedDirection ->
            progressDelta = when (lockedDirection) {
                Direction.LEFT -> -deltaX
                Direction.RIGHT -> deltaX
                Direction.UP -> -deltaY
                Direction.DOWN -> deltaY
            }
            if (progressDelta < 0f) {
                reverseDistance += -progressDelta
                if (reverseDistance >= thresholdPx) isCancelled = true
            } else if (progressDelta > 0f) {
                reverseDistance = 0f
                lastProgressAtMillis = nowMillis
            }

            distance = when (lockedDirection) {
                Direction.LEFT -> -dragX
                Direction.RIGHT -> dragX
                Direction.UP -> -dragY
                Direction.DOWN -> dragY
            }
        }
    }

    fun completedDirection(nowMillis: Long): Direction? {
        if (nowMillis - lastProgressAtMillis >= timeoutMillis) isCancelled = true
        return direction?.takeIf { !isCancelled && distance > commitThresholdPx }
    }
}
