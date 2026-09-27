package com.example.game2048

internal object GameStateStorage {
    private const val VERSION = "1"
    private const val CELL_COUNT = 16

    fun encode(state: GameState): String = listOf(
        VERSION,
        state.score.toString(),
        state.won.toString(),
        state.gameOver.toString(),
        state.board.flatten().joinToString(","),
    ).joinToString("|")

    fun decode(encoded: String?): GameState? {
        val fields = encoded?.split('|') ?: return null
        if (fields.size != 5 || fields[0] != VERSION) return null

        val score = fields[1].toIntOrNull()?.takeIf { it >= 0 } ?: return null
        val won = fields[2].toBooleanStrictOrNull() ?: return null
        val gameOver = fields[3].toBooleanStrictOrNull() ?: return null
        val cells = fields[4].split(',').map { it.toIntOrNull() ?: return null }
        if (cells.size != CELL_COUNT || cells.any { it < 0 || (it != 0 && (it and (it - 1)) != 0) }) return null

        return GameState(
            board = cells.chunked(4),
            score = score,
            won = won,
            gameOver = gameOver,
        )
    }
}
