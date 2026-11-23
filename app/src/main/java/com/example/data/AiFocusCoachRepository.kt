package com.example.data

import android.util.Log
import com.example.model.AiPlanSuggestion
import com.example.model.AiSuggestedGoal
import com.example.model.GoalTier
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiFocusCoachRepo"

class AiFocusCoachRepository {

    private val coachModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    private val planSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "headline" to Schema.string(description = "A concise energizing headline under 8 words"),
                "strategyTip" to Schema.string(description = "One actionable 1-2 sentence focus strategy tip"),
                "suggestedGoals" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "title" to Schema.string(description = "Concise actionable goal title under 60 characters"),
                            "targetMinutes" to Schema.integer(description = "Recommended duration in minutes (15 to 180)"),
                            "category" to Schema.enumeration(
                                values = listOf("Deep Work", "Code", "Study", "Design", "Reading", "Fitness"),
                                description = "Work category"
                            ),
                            "tier" to Schema.enumeration(
                                values = listOf("ESSENTIAL", "OPTIONAL"),
                                description = "ESSENTIAL for core priorities, OPTIONAL for bonus tasks"
                            ),
                            "rationale" to Schema.string(description = "Brief 1-sentence reason why this block helps today")
                        )
                    ),
                    description = "Exactly 3 tailored daily focus blocks"
                )
            )
        )
    }

    private val plannerModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = planSchema
            }
        )
    }

    fun streamFocusBriefing(contextSummary: String): Flow<String> = flow {
        val prompt = """
            You are the AI Focus Coach inside Level Up, a bio-tech deep work logger & focus shield app.
            Based on the user's live focus telemetry below, write a sharp, motivating, zero-fluff 2-3 sentence briefing.
            Highlight their strongest lever right now and recommend the ideal next focus block length.
            Do not use markdown headers or emojis.
            
            Telemetry:
            $contextSummary
        """.trimIndent()

        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            coachModel.generateContentStream(prompt).collect { chunk ->
                val chunkText = chunk.text.orEmpty()
                if (chunkText.isNotEmpty()) {
                    accumulated.append(chunkText)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("AI Coach returned an empty briefing.")
        }
    }

    fun streamCoachResponse(
        userQuestion: String,
        contextSummary: String
    ): Flow<String> = flow {
        val prompt = """
            You are the Level Up AI Focus Coach. Give concise, practical, science-backed deep work advice.
            Keep your reply focused, structured with short bullet points when helpful, and under 140 words.
            
            User's current focus context:
            $contextSummary
            
            User request:
            $userQuestion
        """.trimIndent()

        val accumulated = StringBuilder()
        withTimeout(60_000L) {
            coachModel.generateContentStream(prompt).collect { chunk ->
                val chunkText = chunk.text.orEmpty()
                if (chunkText.isNotEmpty()) {
                    accumulated.append(chunkText)
                    emit(accumulated.toString())
                }
            }
        }
        if (accumulated.isBlank()) {
            throw IllegalStateException("AI Coach returned an empty reply.")
        }
    }

    suspend fun generateStructuredPlan(
        objective: String,
        contextSummary: String
    ): Result<AiPlanSuggestion> {
        return try {
            val prompt = """
                Break down this focus objective into 3 concrete time-blocked goals for today.
                Ensure at least 2 are ESSENTIAL and 1 is OPTIONAL bonus if appropriate.
                
                Current context:
                $contextSummary
                
                Objective:
                $objective
            """.trimIndent()

            val response = withTimeout(60_000L) {
                plannerModel.generateContent(prompt)
            }
            val jsonText = response.text
                ?: throw IllegalStateException("AI Planner returned empty JSON.")

            val json = JSONObject(jsonText)
            val headline = json.getString("headline")
            val strategyTip = json.getString("strategyTip")
            val goalsArray = json.getJSONArray("suggestedGoals")
            val goals = buildList {
                for (i in 0 until goalsArray.length()) {
                    val item = goalsArray.getJSONObject(i)
                    add(
                        AiSuggestedGoal(
                            title = item.getString("title").take(120),
                            targetMinutes = item.optInt("targetMinutes", 45).coerceIn(15, 180),
                            category = item.optString("category", "Deep Work"),
                            tier = GoalTier.fromRaw(item.optString("tier", "ESSENTIAL")),
                            rationale = item.optString("rationale", "")
                        )
                    )
                }
            }
            Result.success(
                AiPlanSuggestion(
                    headline = headline,
                    strategyTip = strategyTip,
                    suggestedGoals = goals
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI Planner timed out", e)
            Result.failure(Exception("AI planning timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI Planner failed", e)
            Result.failure(e)
        }
    }
}
