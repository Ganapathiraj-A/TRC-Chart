package com.example.trcchart.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class AppLanguage {
    ENGLISH,
    TAMIL
}

class FeelingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("trc_chart_prefs", Context.MODE_PRIVATE)

    private val defaultFeelings = listOf(
        "Anger / கோபம்",
        "Fear / பயம்",
        "Sadness / துக்கம்",
        "Joy / ஆனந்தம்",
        "Jealousy / பொறாமை",
        "Anxiety / கவலை",
        "Peace / அமைதி",
        "Confusion / குழப்பம்",
        "Guilt / குற்றவுணர்ச்சி",
        "Pride / அகந்தை"
    )

    private val _feelings = MutableStateFlow<List<String>>(emptyList())
    val feelings: StateFlow<List<String>> = _feelings.asStateFlow()

    private val _entries = MutableStateFlow<List<TRCEntry>>(emptyList())
    val entries: StateFlow<List<TRCEntry>> = _entries.asStateFlow()

    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    init {
        loadLanguage()
        loadFeelings()
        loadEntries()
    }

    private fun loadLanguage() {
        val langStr = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.name)
        _language.value = try {
            AppLanguage.valueOf(langStr ?: AppLanguage.ENGLISH.name)
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        prefs.edit().putString(KEY_LANGUAGE, lang.name).apply()
    }

    private fun loadFeelings() {
        val saved = prefs.getStringSet(KEY_FEELINGS, null)
        if (saved == null) {
            _feelings.value = defaultFeelings
            saveFeelingsToPrefs(defaultFeelings)
        } else {
            _feelings.value = saved.toList().sorted()
        }
    }

    private fun saveFeelingsToPrefs(list: List<String>) {
        prefs.edit().putStringSet(KEY_FEELINGS, list.toSet()).apply()
    }

    fun addFeeling(feeling: String): Boolean {
        val trimmed = feeling.trim()
        if (trimmed.isEmpty()) return false
        val current = _feelings.value.toMutableList()
        if (current.any { it.equals(trimmed, ignoreCase = true) }) return false
        current.add(trimmed)
        current.sort()
        _feelings.value = current
        saveFeelingsToPrefs(current)
        return true
    }

    fun editFeeling(oldFeeling: String, newFeeling: String): Boolean {
        val trimmed = newFeeling.trim()
        if (trimmed.isEmpty()) return false
        val current = _feelings.value.toMutableList()
        val index = current.indexOf(oldFeeling)
        if (index == -1) return false
        current[index] = trimmed
        current.sort()
        _feelings.value = current
        saveFeelingsToPrefs(current)
        return true
    }

    fun removeFeeling(feeling: String): Boolean {
        val current = _feelings.value.toMutableList()
        if (current.remove(feeling)) {
            _feelings.value = current
            saveFeelingsToPrefs(current)
            return true
        }
        return false
    }

    private fun loadEntries() {
        val jsonStr = prefs.getString(KEY_ENTRIES, null)
        if (jsonStr != null) {
            try {
                _entries.value = Json.decodeFromString<List<TRCEntry>>(jsonStr)
            } catch (e: Exception) {
                _entries.value = emptyList()
            }
        }
    }

    fun addEntry(entry: TRCEntry) {
        val current = _entries.value.toMutableList()
        current.add(0, entry) // latest first
        _entries.value = current
        try {
            val jsonStr = Json.encodeToString(current)
            prefs.edit().putString(KEY_ENTRIES, jsonStr).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val KEY_FEELINGS = "key_feelings_list"
        private const val KEY_ENTRIES = "key_trc_entries"
        private const val KEY_LANGUAGE = "key_app_language"
    }
}
