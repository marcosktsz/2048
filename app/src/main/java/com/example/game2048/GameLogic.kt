package com.example.game2048

import kotlin.random.Random

internal enum class Direction { UP, DOWN, LEFT, RIGHT }

internal data class GameState(
    val board: List<List<Int>>,
    val score: Int = 0,
    val won: Boolean = false,
    val gameOver: Boolean = false,
)

internal data class BoardPosition(val row: Int, val column: Int)

internal enum class TileMotionKind { SLIDE, MERGE_SOURCE, SPAWN }
internal enum class BoardAnimationPhase { MOVING, MERGING, SPAWNING }

internal data class TileMotion(
    val value: Int,
    val from: BoardPosition,
    val to: BoardPosition,
    val kind: TileMotionKind,
)

internal data class MergeMotion(val value: Int, val position: BoardPosition)

internal data class BoardMotion(
    val id: Int,
    val tiles: List<TileMotion>,
    val merges: List<MergeMotion>,
    val direction: Direction,
)

internal object GameLogic {
    private const val SIZE = 4
    private data class SlidePlan(
        val tiles: List<TileMotion>,
        val merges: List<MergeMotion>,
        val occupied: Set<BoardPosition>,
    )

    fun newGame(): GameState {
        var state = GameState(List(SIZE) { List(SIZE) { 0 } })
        state = addRandomTile(state)
        return addRandomTile(state)
    }

    fun move(state: GameState, direction: Direction): GameState {
        val board = state.board.map { it.toMutableList() }
        var gained = 0

        for (lineIndex in 0 until SIZE) {
            val positions = (0 until SIZE).map { offset ->
                when (direction) {
                    Direction.LEFT -> lineIndex to offset
                    Direction.RIGHT -> lineIndex to SIZE - 1 - offset
                    Direction.UP -> offset to lineIndex
                    Direction.DOWN -> SIZE - 1 - offset to lineIndex
                }
            }
            val values = positions.map { (row, column) -> board[row][column] }.filter { it != 0 }
            val merged = mutableListOf<Int>()
            var index = 0
            while (index < values.size) {
                if (index + 1 < values.size && values[index] == values[index + 1]) {
                    val combined = values[index] * 2
                    merged += combined
                    gained += combined
                    index += 2
                } else {
                    merged += values[index]
                    index++
                }
            }
            positions.forEachIndexed { offset, (row, column) ->
                board[row][column] = merged.getOrElse(offset) { 0 }
            }
        }

        val movedBoard = board.map { it.toList() }
        if (movedBoard == state.board) return state

        val moved = state.copy(
            board = movedBoard,
            score = state.score + gained,
            won = state.won || movedBoard.flatten().any { it >= 2048 },
        )
        return addRandomTile(moved).let { it.copy(gameOver = !hasMoves(it.board)) }
    }

    fun motion(
        before: GameState,
        after: GameState,
        direction: Direction,
        id: Int,
    ): BoardMotion {
        val plan = slidePlan(before.board, direction)
        val tiles = plan.tiles.toMutableList()

        for (row in 0 until SIZE) {
            for (column in 0 until SIZE) {
                val position = BoardPosition(row, column)
                val value = after.board[row][column]
                if (value != 0 && position !in plan.occupied) {
                    tiles += TileMotion(value, position, position, TileMotionKind.SPAWN)
                }
            }
        }

        return BoardMotion(id, tiles, plan.merges, direction)
    }

    private fun slidePlan(board: List<List<Int>>, direction: Direction): SlidePlan {
        val tiles = mutableListOf<TileMotion>()
        val merges = mutableListOf<MergeMotion>()
        val occupied = mutableSetOf<BoardPosition>()

        for (lineIndex in 0 until SIZE) {
            val positions = (0 until SIZE).map { offset ->
                when (direction) {
                    Direction.LEFT -> BoardPosition(lineIndex, offset)
                    Direction.RIGHT -> BoardPosition(lineIndex, SIZE - 1 - offset)
                    Direction.UP -> BoardPosition(offset, lineIndex)
                    Direction.DOWN -> BoardPosition(SIZE - 1 - offset, lineIndex)
                }
            }
            val entries = positions.mapNotNull { position ->
                board[position.row][position.column]
                    .takeIf { it != 0 }
                    ?.let { it to position }
            }
            var readIndex = 0
            var writeIndex = 0
            while (readIndex < entries.size) {
                val target = positions[writeIndex]
                val current = entries[readIndex]
                if (readIndex + 1 < entries.size && current.first == entries[readIndex + 1].first) {
                    val next = entries[readIndex + 1]
                    tiles += TileMotion(current.first, current.second, target, TileMotionKind.MERGE_SOURCE)
                    tiles += TileMotion(next.first, next.second, target, TileMotionKind.MERGE_SOURCE)
                    merges += MergeMotion(current.first * 2, target)
                    readIndex += 2
                } else {
                    if (current.second != target) {
                        tiles += TileMotion(current.first, current.second, target, TileMotionKind.SLIDE)
                    }
                    readIndex++
                }
                occupied += target
                writeIndex++
            }
        }

        return SlidePlan(tiles, merges, occupied)
    }

    fun animationBoard(board: List<List<Int>>, motion: BoardMotion, phase: BoardAnimationPhase): List<List<Int>> {
        val spawnTargets = motion.tiles.filter { it.kind == TileMotionKind.SPAWN }.map { it.to }
        val hiddenTargets = when (phase) {
            BoardAnimationPhase.MOVING -> motion.tiles
                .filter { it.kind != TileMotionKind.SPAWN }
                .map { it.to } + motion.merges.map { it.position } + spawnTargets
            BoardAnimationPhase.MERGING -> motion.merges.map { it.position } + spawnTargets
            BoardAnimationPhase.SPAWNING -> spawnTargets
        }
        val hidden = hiddenTargets.toSet()
        return board.mapIndexed { rowIndex, row ->
            row.mapIndexed { columnIndex, value ->
                if (BoardPosition(rowIndex, columnIndex) in hidden) 0 else value
            }
        }
    }

    private fun addRandomTile(state: GameState): GameState {
        val empty = state.board.flatMapIndexed { row, values ->
            values.mapIndexedNotNull { column, value -> if (value == 0) row to column else null }
        }
        if (empty.isEmpty()) return state
        val (row, column) = empty.random()
        val board = state.board.map { it.toMutableList() }
        board[row][column] = if (Random.nextFloat() < 0.9f) 2 else 4
        return state.copy(board = board.map { it.toList() })
    }

    private fun hasMoves(board: List<List<Int>>): Boolean {
        if (board.any { row -> row.any { it == 0 } }) return true
        for (row in 0 until SIZE) {
            for (column in 0 until SIZE) {
                if (row + 1 < SIZE && board[row][column] == board[row + 1][column]) return true
                if (column + 1 < SIZE && board[row][column] == board[row][column + 1]) return true
            }
        }
        return false
    }
}
