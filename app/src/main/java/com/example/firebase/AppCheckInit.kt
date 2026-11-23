package com.example.firebase

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.example.config.AppConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"

fun configureAppCheck(context: Context, intent: Intent?) {
    // 1. Ensure FirebaseApp is reliably initialized before any Firebase operations
    ensureFirebaseApp(context)

    // 2. Configure App Check with complete isolation from core app lifecycle
    try {
        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(AppConfig.Auth.DEBUG_TOKEN_EXTRA)

            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                // Synchronous commit() is required before installing DebugAppCheckProviderFactory.
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(TAG, "App Check debug secret configured successfully.")
            } else {
                Log.w(TAG, "No debug token found in Intent extra.")
            }

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val resId = context.resources.getIdentifier(
                AppConfig.Auth.RECAPTCHA_SITE_KEY_RES,
                "string",
                context.packageName
            )
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
                } else {
                    Log.e(TAG, "recaptcha_site_key resource string is blank.")
                }
            } else {
                Log.e(TAG, "recaptcha_site_key string resource not found in package.")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

private fun ensureFirebaseApp(context: Context) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            val options = FirebaseOptions.fromResource(context) ?: run {
                val databaseId = try {
                    context.getString(R.string.firestore_database_id)
                } catch (_: Exception) {
                    "ai-studio-android-levelup"
                }
                val projectId = databaseId.substringBefore('-').ifBlank { "ai-studio-android-levelup" }
                FirebaseOptions.Builder()
                    .setApplicationId(context.packageName)
                    .setApiKey("AIzaSyDummyKeyForFallbackInitialization00")
                    .setProjectId(projectId)
                    .setDatabaseUrl("https://$projectId.firebaseio.com")
                    .build()
            }
            FirebaseApp.initializeApp(context, options)
            Log.d(TAG, "FirebaseApp initialized successfully.")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error ensuring FirebaseApp is initialized from resource", e)
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(context.packageName)
                    .setApiKey("AIzaSyDummyKeyForFallbackInitialization00")
                    .setProjectId("ai-studio-android-levelup")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "FirebaseApp initialized via fallback options.")
            }
        } catch (fatal: Exception) {
            Log.e(TAG, "Fatal: unable to initialize FirebaseApp", fatal)
        }
    }
}
