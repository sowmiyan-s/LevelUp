package com.example

import com.example.config.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppConfigTest {

    @Test
    fun testAppConfigConstants() {
        assertEquals("Level Up", AppConfig.APP_NAME)
        assertEquals("1.0.0", AppConfig.APP_VERSION)

        assertEquals("gemini-3.8-flash", AppConfig.AiCoach.MODEL_NAME)
        assertEquals(60_000L, AppConfig.AiCoach.TIMEOUT_MS)
        assertTrue(AppConfig.AiCoach.DEFAULT_CATEGORIES.contains("Deep Work"))

        assertEquals("users", AppConfig.Firestore.COLLECTION_USERS)
        assertEquals("goals", AppConfig.Firestore.COLLECTION_GOALS)
        assertEquals("sessions", AppConfig.Firestore.COLLECTION_SESSIONS)
        assertEquals("dayStatuses", AppConfig.Firestore.COLLECTION_DAY_STATUSES)

        assertEquals(25, AppConfig.Timer.DEFAULT_DURATION_MINUTES)
        assertEquals(5, AppConfig.Timer.MIN_DURATION_MINUTES)
        assertEquals(120, AppConfig.Timer.MAX_DURATION_MINUTES)
        assertEquals(listOf(15, 25, 45, 60, 90, 120), AppConfig.Timer.PRESET_DURATIONS_MINUTES)

        assertEquals(360L, AppConfig.UserProfile.DEFAULT_DAILY_TARGET_MINUTES)
        assertEquals(2L, AppConfig.UserProfile.DEFAULT_STREAK_FREEZES)
        assertTrue(AppConfig.UserProfile.DEFAULT_SHIELDED_APPS.contains("Instagram"))
    }
}
