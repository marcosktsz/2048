package com.example.game2048

import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsScreenTest {
    @Test
    fun unknownThemePreferenceFallsBackToSystem() {
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromPreference(null))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromPreference("unexpected"))
    }

    @Test
    fun themePreferenceRestoresExplicitChoice() {
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.fromPreference("light"))
        assertEquals(AppThemeMode.DARK, AppThemeMode.fromPreference("dark"))
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.fromPreference("system"))
    }
}
