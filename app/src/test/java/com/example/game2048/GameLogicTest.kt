package com.example.game2048

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
    fun threeEqualTilesMergeTowardTheSwipedEdgeOnlyOnce() {
        val leftState = GameState(
            board = listOf(
                listOf(2, 2, 2, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
            ),
        )
        val rightState = leftState.copy(board = listOf(
            listOf(0, 2, 2, 2),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 0),
        ))
        val upState = GameState(
            board = listOf(
                listOf(0, 0, 0, 2),
                listOf(0, 0, 0, 2),
                listOf(0, 0, 0, 2),
                listOf(0, 0, 0, 0),
            ),
        )
        val downState = upState.copy(board = listOf(
            listOf(0, 0, 0, 0),
            listOf(0, 0, 0, 2),
            listOf(0, 0, 0, 2),
            listOf(0, 0, 0, 2),
        ))

        val movedLeft = GameLogic.move(leftState, Direction.LEFT)
        val movedRight = GameLogic.move(rightState, Direction.RIGHT)
        val movedUp = GameLogic.move(upState, Direction.UP)
        val movedDown = GameLogic.move(downState, Direction.DOWN)

        assertEquals(4, movedLeft.score)
        assertEquals(listOf(4, 2), movedLeft.board[0].take(2))
        assertEquals(4, movedRight.score)
        assertEquals(listOf(2, 4), movedRight.board[0].takeLast(2))
        assertEquals(4, movedUp.score)
        assertEquals(listOf(4, 2), listOf(movedUp.board[0][3], movedUp.board[1][3]))
        assertEquals(4, movedDown.score)
        assertEquals(listOf(2, 4), listOf(movedDown.board[2][3], movedDown.board[3][3]))
    }

    @Test
    fun leftSwipeOnRowThreeMergesColumnsZeroAndOne() {
        val state = GameState(
            board = listOf(
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(2, 2, 2, 0),
            ),
        )
        val moved = GameLogic.move(state, Direction.LEFT)
        val motion = GameLogic.motion(state, moved, Direction.LEFT, id = 1)

        assertEquals(4, moved.score)
        assertEquals(listOf(4, 2), moved.board[3].take(2))
        assertEquals(listOf(MergeMotion(4, BoardPosition(3, 0), true)), motion.merges)
        assertEquals(
            listOf(TileMotion(2, BoardPosition(3, 1), BoardPosition(3, 0), TileMotionKind.MERGE_SOURCE)),
            motion.tiles.filter { it.kind == TileMotionKind.MERGE_SOURCE },
        )
        assertEquals(
            listOf(TileMotion(2, BoardPosition(3, 2), BoardPosition(3, 1), TileMotionKind.SLIDE)),
            motion.tiles.filter { it.kind == TileMotionKind.SLIDE },
        )
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
        assertFalse(GameLogic.canMove(state.board, Direction.LEFT))
        assertTrue(GameLogic.canMove(state.board, Direction.RIGHT))
    }

    @Test
    fun reversingSwipeCancelsItsPendingMove() {
        val swipe = SwipeGestureTracker(startedAtMillis = 0L)

        swipe.add(0f, -80f, nowMillis = 100L)
        assertEquals(Direction.UP, swipe.completedDirection(nowMillis = 100L))

        swipe.add(0f, 40f, nowMillis = 200L)

        assertTrue(swipe.isCancelled)
        assertNull(swipe.completedDirection(nowMillis = 200L))
    }

    @Test
    fun smallSwipeReversalDoesNotCancelMove() {
        val swipe = SwipeGestureTracker(startedAtMillis = 0L)

        swipe.add(0f, -60f, nowMillis = 100L)
        swipe.add(0f, 10f, nowMillis = 200L)

        assertFalse(swipe.isCancelled)
        assertEquals(Direction.UP, swipe.completedDirection(nowMillis = 200L))
    }

    @Test
    fun swipeMustTravelPastItsConfiguredThreshold() {
        val swipe = SwipeGestureTracker(startedAtMillis = 0L, thresholdPx = 100f)

        swipe.add(0f, -99f, nowMillis = 100L)
        assertNull(swipe.completedDirection(nowMillis = 100L))

        swipe.add(0f, -2f, nowMillis = 200L)

        assertEquals(Direction.UP, swipe.completedDirection(nowMillis = 200L))
    }

    @Test
    fun directionLocksBeforeTheLongerMoveCommitDistance() {
        val swipe = SwipeGestureTracker(startedAtMillis = 0L, commitThresholdPx = 100f)

        swipe.add(0f, -40f, nowMillis = 100L)

        assertEquals(Direction.UP, swipe.direction)
        assertNull(swipe.completedDirection(nowMillis = 100L))

        swipe.add(0f, -61f, nowMillis = 200L)

        assertEquals(Direction.UP, swipe.completedDirection(nowMillis = 200L))
    }

    @Test
    fun swipeHeldTooLongBeforeReleaseIsCancelled() {
        val swipe = SwipeGestureTracker(startedAtMillis = 0L)
        swipe.add(0f, -60f, nowMillis = 100L)

        assertNull(swipe.completedDirection(nowMillis = 1_100L))
        assertTrue(swipe.isCancelled)
    }

    @Test
    fun swipeContinuesAfterLongPauseWhenMovementResumes() {
        val swipe = SwipeGestureTracker(startedAtMillis = 0L)
        swipe.add(0f, -60f, nowMillis = 100L)
        swipe.add(0f, -10f, nowMillis = 2_000L)

        assertFalse(swipe.isCancelled)
        assertEquals(Direction.UP, swipe.completedDirection(nowMillis = 2_000L))
    }

    @Test
    fun motionMapsSlidesAndMergeSourcesToTheirDestinations() {
        val state = GameState(
            board = listOf(
                listOf(0, 2, 2, 0),
                listOf(0, 0, 4, 0),
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
        )

        assertEquals(
            listOf(TileMotion(4, BoardPosition(1, 2), BoardPosition(1, 0), TileMotionKind.SLIDE)),
            motion.tiles.filter { it.kind == TileMotionKind.SLIDE },
        )
        assertEquals(
            listOf(
                TileMotion(2, BoardPosition(0, 1), BoardPosition(0, 0), TileMotionKind.MERGE_SOURCE),
                TileMotion(2, BoardPosition(0, 2), BoardPosition(0, 0), TileMotionKind.MERGE_SOURCE),
            ),
            motion.tiles.filter { it.kind == TileMotionKind.MERGE_SOURCE },
        )
        assertEquals(listOf(MergeMotion(4, BoardPosition(0, 0), false)), motion.merges)
        assertEquals(1, motion.tiles.count { it.kind == TileMotionKind.SPAWN })
        val spawnedTile = motion.tiles.single { it.kind == TileMotionKind.SPAWN }
        val spawnedAt = spawnedTile.to
        assertTrue(spawnedTile.value == 2 || spawnedTile.value == 4)
        assertEquals(spawnedTile.value, moved.board[spawnedAt.row][spawnedAt.column])
        assertTrue(spawnedAt !in setOf(BoardPosition(0, 0), BoardPosition(1, 0)))
        assertEquals(3, moved.board.flatten().count { it != 0 })

        val movingBoard = GameLogic.animationBoard(moved.board, motion, BoardAnimationPhase.MOVING)
        val spawningBoard = GameLogic.animationBoard(moved.board, motion, BoardAnimationPhase.SPAWNING)
        assertEquals(0, movingBoard[0][0])
        assertEquals(0, movingBoard[spawnedAt.row][spawnedAt.column])
        assertEquals(4, spawningBoard[0][0])
        assertEquals(4, spawningBoard[1][0])
        assertEquals(0, spawningBoard[spawnedAt.row][spawnedAt.column])
    }

    @Test
    fun stationaryMergeTargetStaysVisibleWhileTheOtherTileMovesIntoIt() {
        val state = GameState(
            board = listOf(
                listOf(2, 2, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
                listOf(0, 0, 0, 0),
            ),
        )
        val moved = GameLogic.move(state, Direction.LEFT)
        val motion = GameLogic.motion(state, moved, Direction.LEFT, id = 1)

        assertEquals(
            listOf(TileMotion(2, BoardPosition(0, 1), BoardPosition(0, 0), TileMotionKind.MERGE_SOURCE)),
            motion.tiles.filter { it.kind == TileMotionKind.MERGE_SOURCE },
        )
        assertEquals(listOf(MergeMotion(4, BoardPosition(0, 0), true)), motion.merges)
        assertEquals(2, GameLogic.animationBoard(moved.board, motion, BoardAnimationPhase.MOVING)[0][0])
        assertEquals(4, GameLogic.animationBoard(moved.board, motion, BoardAnimationPhase.SPAWNING)[0][0])
    }
}
