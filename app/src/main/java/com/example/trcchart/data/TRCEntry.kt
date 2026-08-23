package com.example.trcchart.data

import kotlinx.serialization.Serializable

@Serializable
data class TRCEntry(
    val id: String,
    val timestamp: Long,
    val feeling: String,
    val reason: String,
    val awareness: String,
    val feelingsDetail: String,
    val isGoodKarma: Boolean,
    val isBlame: Boolean,
    val isComplaint: Boolean,
    val isExcuse: Boolean,
    val isGossip: Boolean,
    val isSynced: Boolean = true
)
