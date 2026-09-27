package com.example.game2048

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameStateStorageTest {
    @Test
    fun roundTripPreservesBoardAndGameStatus() {
        val state = GameState(
            board = listOf(
                listOf(2, 4, 8, 16),
                listOf(32, 64, 128, 256),
                listOf(512, 1024, 2048, 0),
                listOf(0, 0, 0, 0),
            ),
            score = 3580,
            won = true,
            gameOver = false,
        )

        assertEquals(state, GameStateStorage.decode(GameStateStorage.encode(state)))
    }

    @Test
    fun invalidOrUnsupportedStateIsRejected() {
        assertNull(GameStateStorage.decode(null))
        assertNull(GameStateStorage.decode("not-a-game"))
        assertNull(GameStateStorage.decode("1|10|false|false|3,0"))
        assertNull(GameStateStorage.decode("1|10|false|false|3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"))
    }
}
