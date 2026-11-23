package com.example.config

/**
 * Centralized Application Configuration for Level Up.
 * Defines global constants, AI model settings, Firestore collection references,
 * timer defaults, user profile defaults, and authentication metadata.
 */
object AppConfig {
    // App Information
    const val APP_NAME = "Level Up"
    const val APP_VERSION = "1.0.0"

    // AI Focus Coach Configuration (Firebase AI / Gemini)
    object AiCoach {
        const val MODEL_NAME = "gemini-3.8-flash"
        const val TIMEOUT_MS = 60_000L
        const val MAX_RESPONSE_WORDS = 140

        val DEFAULT_CATEGORIES = listOf(
            "Deep Work",
            "Code",
            "Study",
            "Design",
            "Reading",
            "Fitness"
        )

        const val BRIEFING_SYSTEM_PROMPT = """
            You are the AI Focus Coach inside Level Up, a bio-tech deep work logger & focus shield app.
            Based on the user's live focus telemetry below, write a sharp, motivating, zero-fluff 2-3 sentence briefing.
            Highlight their strongest lever right now and recommend the ideal next focus block length.
            Do not use markdown headers or emojis.
        """

        const val COACH_CHAT_SYSTEM_PROMPT = """
            You are the Level Up AI Focus Coach. Give concise, practical, science-backed deep work advice.
            Keep your reply focused, structured with short bullet points when helpful, and under 140 words.
        """
    }

    // Firestore Database Collections
    object Firestore {
        const val COLLECTION_USERS = "users"
        const val COLLECTION_GOALS = "goals"
        const val COLLECTION_SESSIONS = "sessions"
        const val COLLECTION_DAY_STATUSES = "dayStatuses"
    }

    // Focus Dial & Timer Configuration
    object Timer {
        const val DEFAULT_DURATION_MINUTES = 25
        const val MIN_DURATION_MINUTES = 5
        const val MAX_DURATION_MINUTES = 120
        val PRESET_DURATIONS_MINUTES = listOf(15, 25, 45, 60, 90, 120)
    }

    // User Profile & Shield Defaults
    object UserProfile {
        const val DEFAULT_DAILY_TARGET_MINUTES = 360L // 6 hours
        const val DEFAULT_STREAK_FREEZES = 2L

        val DEFAULT_SHIELDED_APPS = listOf(
            "Instagram",
            "X / Twitter",
            "YouTube",
            "TikTok"
        )

        val AVAILABLE_DISTRACTION_APPS = listOf(
            "Instagram",
            "X / Twitter",
            "YouTube",
            "TikTok",
            "Reddit",
            "Discord",
            "Twitch",
            "Netflix"
        )
    }

    // Authentication & Firebase App Check Configuration
    object Auth {
        const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
        const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"
        const val FIRESTORE_DATABASE_ID_RES = "firestore_database_id"
        const val DEFAULT_WEB_CLIENT_ID_RES = "default_web_client_id"
    }
}
