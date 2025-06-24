package com.google.ai.edge.gallery.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserPromptsRepository(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUserPromptOverride(override: UserPromptOverride) {
        val json = gson.toJson(override)
        sharedPreferences.edit().putString(override.id, json).apply()
    }

    fun getUserPromptOverride(id: String): UserPromptOverride? {
        val json = sharedPreferences.getString(id, null)
        return if (json != null) {
            val type = object : TypeToken<UserPromptOverride>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    fun getAllUserPromptOverrides(): Map<String, UserPromptOverride> {
        val allEntries = sharedPreferences.all
        val overrides = mutableMapOf<String, UserPromptOverride>()
        for ((key, value) in allEntries) {
            if (value is String) {
                try {
                    val type = object : TypeToken<UserPromptOverride>() {}.type
                    val override: UserPromptOverride = gson.fromJson(value, type)
                    // Ensure the key matches the ID in the object, primarily for sanity
                    if (override.id == key) {
                        overrides[key] = override
                    }
                } catch (e: Exception) {
                    // Log error or handle corrupted data
                    println("Error deserializing UserPromptOverride for key $key: ${e.message}")
                }
            }
        }
        return overrides
    }

    fun deleteUserPromptOverride(id: String) {
        sharedPreferences.edit().remove(id).apply()
    }

    companion object {
        private const val PREFS_NAME = "user_prompt_overrides_prefs"
    }
}
