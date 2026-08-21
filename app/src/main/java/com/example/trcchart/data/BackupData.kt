package com.example.trcchart.data

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val feelings: List<String>,
    val entries: List<TRCEntry>,
    val checkedChecklistIds: List<String> = emptyList(),
    val checklistLogs: List<DailyChecklistLog> = emptyList(),
    val showSection1: Boolean = true,
    val showSection2: Boolean = true,
    val showSection3: Boolean = true,
    val language: String = AppLanguage.ENGLISH.name
)

