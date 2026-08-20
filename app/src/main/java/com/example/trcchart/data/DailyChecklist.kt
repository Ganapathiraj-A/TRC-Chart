package com.example.trcchart.data

import kotlinx.serialization.Serializable

@Serializable
data class DailyChecklistItem(
    val id: String,
    val textTa: String,
    val textEn: String,
    val section: Int // 1: Anbu/Love, 2: Husband&Wife, 3: Quality/Attitude
)

@Serializable
data class DailyChecklistLog(
    val dateString: String, // YYYY-MM-DD
    val checkedItemIds: Set<String> = emptySet()
)
