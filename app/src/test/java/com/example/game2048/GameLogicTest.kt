package com.example.game2048

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GameLogicTest {
    @Test
    fun matchingTilesMergeOnceAndAwardTheirValue() {
        val state = GameState(
            board = listOf(
                listOf(2, 2, 2, 2),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
            ),
        )

        val moved = GameLogic.move(state, Direction.LEFT)

        assertEquals(8, moved.score)
        assertTrue(moved.board[0].count { it == 4 } >= 2)
    }

    @Test
    fun moveWithoutAnyShiftReturnsOriginalState() {
        val state = GameState(
            board = listOf(
                listOf(4, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
            ),
        )

        assertSame(state, GameLogic.move(state, Direction.LEFT))
    }

    @Test
    fun motionMapsSlidesAndMergeSourcesToTheirDestinations() {
        val state = GameState(
            board = listOf(
                listOf(0, 2, 2, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
            ),
        )
        val moved = GameLogic.move(state, Direction.LEFT)

        val motion = GameLogic.motion(
            state,
            moved,
            Direction.LEFT,
            id = 1,
            initialDragX = 24f,
            initialDragY = -8f,
        )

        assertEquals(24f, motion.initialDragX)
        assertEquals(-8f, motion.initialDragY)
        assertEquals(
            listOf(
                TileMotion(2, BoardPosition(0, 1), BoardPosition(0, 0), TileMotionKind.MERGE_SOURCE),
                TileMotion(2, BoardPosition(0, 2), BoardPosition(0, 0), TileMotionKind.MERGE_SOURCE),
            ),
            motion.tiles.filter { it.kind == TileMotionKind.MERGE_SOURCE },
        )
        assertEquals(listOf(MergeMotion(4, BoardPosition(0, 0))), motion.merges)
        assertEquals(1, motion.tiles.count { it.kind == TileMotionKind.SPAWN })

        val movingBoard = GameLogic.animationBoard(moved.board, motion, spawning = false)
        val spawningBoard = GameLogic.animationBoard(moved.board, motion, spawning = true)
        val spawnedAt = motion.tiles.single { it.kind == TileMotionKind.SPAWN }.to
        assertEquals(0, movingBoard[0][0])
        assertEquals(4, spawningBoard[0][0])
        assertEquals(0, spawningBoard[spawnedAt.row][spawnedAt.column])
    }
}
