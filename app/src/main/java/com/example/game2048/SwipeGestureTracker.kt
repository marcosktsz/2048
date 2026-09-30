package com.example.game2048

import kotlin.math.abs

internal class SwipeGestureTracker(
    private val startedAtMillis: Long,
    private val reverseCancelThresholdPx: Float = 28f,
    private val timeoutMillis: Long = 200L,
    private val heldCommitThresholdPx: Float = 28f,
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
    private var hasBeenHeld = false
    private var heldDistance = 0f

    fun add(deltaX: Float, deltaY: Float, nowMillis: Long) {
        if (isCancelled) return
        if (nowMillis - lastProgressAtMillis >= timeoutMillis) hasBeenHeld = true

        dragX += deltaX
        dragY += deltaY
        if (direction == null) {
            if (deltaX != 0f || deltaY != 0f) lastProgressAtMillis = nowMillis
            if (dragX != 0f || dragY != 0f) {
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
                if (reverseDistance >= reverseCancelThresholdPx) isCancelled = true
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
            if (hasBeenHeld) heldDistance += progressDelta
        }
    }

    fun completedDirection(nowMillis: Long): Direction? {
        if (nowMillis - lastProgressAtMillis >= timeoutMillis) hasBeenHeld = true
        val commitDistance = if (hasBeenHeld) heldDistance else distance
        val requiredDistance = if (hasBeenHeld) heldCommitThresholdPx else 0f
        return direction?.takeIf { !isCancelled && commitDistance > requiredDistance }
    }
}
