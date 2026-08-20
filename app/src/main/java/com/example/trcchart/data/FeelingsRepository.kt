package com.example.trcchart.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FeelingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("trc_chart_prefs", Context.MODE_PRIVATE)

    private val defaultFeelings = listOf(
        "Anger",
        "Fear",
        "Sadness",
        "Joy",
        "Jealousy",
        "Anxiety",
        "Peace",
        "Confusion",
        "Guilt",
        "Pride"
    )

    private val _feelings = MutableStateFlow<List<String>>(emptyList())
    val feelings: StateFlow<List<String>> = _feelings.asStateFlow()

    private val _entries = MutableStateFlow<List<TRCEntry>>(emptyList())
    val entries: StateFlow<List<TRCEntry>> = _entries.asStateFlow()

    init {
        loadFeelings()
        loadEntries()
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
    }
}
