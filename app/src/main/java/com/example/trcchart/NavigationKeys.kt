package com.example.trcchart

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object MainRoute : NavKey
@Serializable data object SettingsRoute : NavKey
@Serializable data object DailyRoute : NavKey
@Serializable data object ReportsRoute : NavKey

@Serializable data object FeelingsStep1Route : NavKey
@Serializable
data class FeelingsStep2Route(
    val selectedFeeling: String,
    val timestamp: Long
) : NavKey

@Serializable
data class FeelingsStep3Route(
    val selectedFeeling: String,
    val reason: String,
    val awareness: String,
    val detailedFeelings: String,
    val timestamp: Long
) : NavKey
