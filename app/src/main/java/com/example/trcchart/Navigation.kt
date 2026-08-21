package com.example.trcchart

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.trcchart.data.FeelingsRepository
import com.example.trcchart.ui.daily.DailyScreen
import com.example.trcchart.ui.feelings.FeelingsStep1Screen
import com.example.trcchart.ui.feelings.FeelingsStep2Screen
import com.example.trcchart.ui.feelings.FeelingsStep3Screen
import com.example.trcchart.ui.home.HomeScreen
import com.example.trcchart.ui.reports.ReportsScreen
import com.example.trcchart.ui.settings.SettingsScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(MainRoute)
    val context = LocalContext.current
    val repository = remember { FeelingsRepository(context) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<MainRoute> {
                HomeScreen(
                    repository = repository,
                    onNavigate = { key -> backStack.add(key) },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
            entry<FeelingsStep1Route> {
                FeelingsStep1Screen(
                    repository = repository,
                    onNext = { feeling, timestamp ->
                        backStack.add(FeelingsStep2Route(selectedFeeling = feeling, timestamp = timestamp))
                    },
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
            entry<FeelingsStep2Route> { route ->
                FeelingsStep2Screen(
                    repository = repository,
                    selectedFeeling = route.selectedFeeling,
                    timestamp = route.timestamp,
                    onNext = { reason, awareness, detailedFeelings ->
                        backStack.add(
                            FeelingsStep3Route(
                                selectedFeeling = route.selectedFeeling,
                                reason = reason,
                                awareness = awareness,
                                detailedFeelings = detailedFeelings,
                                timestamp = route.timestamp
                            )
                        )
                    },
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
            entry<FeelingsStep3Route> { route ->
                FeelingsStep3Screen(
                    repository = repository,
                    selectedFeeling = route.selectedFeeling,
                    reason = route.reason,
                    awareness = route.awareness,
                    detailedFeelings = route.detailedFeelings,
                    timestamp = route.timestamp,
                    onComplete = {
                        // Return to Home
                        while (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }
                    },
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
            entry<ReportsRoute> {
                ReportsScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
            entry<SettingsRoute> {
                SettingsScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
            entry<DailyRoute> {
                DailyScreen(
                    repository = repository,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.safeDrawingPadding()
                )
            }
        }
    )
}
