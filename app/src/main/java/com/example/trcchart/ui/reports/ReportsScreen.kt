package com.example.trcchart.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trcchart.data.AppLanguage
import com.example.trcchart.data.FeelingsRepository
import com.example.trcchart.data.LocalizedStrings
import com.example.trcchart.theme.SaffronPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    repository: FeelingsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by repository.entries.collectAsState()
    val currentLang by repository.language.collectAsState()
    val checkedIds by repository.checkedChecklistIds.collectAsState()
    val showSec1 by repository.showSection1.collectAsState()
    val showSec2 by repository.showSection2.collectAsState()
    val showSec3 by repository.showSection3.collectAsState()

    val strings = LocalizedStrings.get(currentLang)

    val dateOnlyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Default Date Range: Past 30 Days
    var endDateTimestamp by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis)
    }

    var startDateTimestamp by remember {
        mutableStateOf(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -30)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis)
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Filtered entries for date range
    val filteredEntries = remember(entries, startDateTimestamp, endDateTimestamp) {
        val startCal = Calendar.getInstance().apply {
            timeInMillis = startDateTimestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val endCal = Calendar.getInstance().apply {
            timeInMillis = endDateTimestamp
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        entries.filter { it.timestamp in startCal..endCal }
    }

    // Top feelings percentage calculation
    val topFeelingsBreakdown = remember(filteredEntries) {
        val total = filteredEntries.size
        if (total == 0) emptyList()
        else {
            filteredEntries.groupBy { it.feeling }
                .mapValues { (_, list) -> (list.size.toDouble() / total * 100) }
                .toList()
                .sortedByDescending { it.second }
        }
    }

    // Days count in date range
    val daysInRange = remember(startDateTimestamp, endDateTimestamp) {
        val diff = (endDateTimestamp - startDateTimestamp).coerceAtLeast(0)
        val days = (diff / (1000 * 60 * 60 * 24)).toInt() + 1
        days.coerceAtLeast(1)
    }

    // Section completion percentages based on total possible checkins (daysInRange * itemsCount)
    val section1Items = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 1 } }
    val section2Items = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 2 } }
    val section3Items = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 3 } }

    val sec1Pct = remember(checkedIds, daysInRange) {
        if (section1Items.isEmpty()) 0
        else {
            val checkedCount = section1Items.count { checkedIds.contains(it.id) }
            val totalPossible = section1Items.size * daysInRange
            ((checkedCount.toDouble() / totalPossible) * 100).toInt().coerceIn(0, 100)
        }
    }

    val sec2Pct = remember(checkedIds, daysInRange) {
        if (section2Items.isEmpty()) 0
        else {
            val checkedCount = section2Items.count { checkedIds.contains(it.id) }
            val totalPossible = section2Items.size * daysInRange
            ((checkedCount.toDouble() / totalPossible) * 100).toInt().coerceIn(0, 100)
        }
    }

    val sec3Pct = remember(checkedIds, daysInRange) {
        if (section3Items.isEmpty()) 0
        else {
            val checkedCount = section3Items.count { checkedIds.contains(it.id) }
            val totalPossible = section3Items.size * daysInRange
            ((checkedCount.toDouble() / totalPossible) * 100).toInt().coerceIn(0, 100)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.reportsTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SaffronPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date Range Selection Controls (Default 30 days)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Filter Date Range (Default: Past 30 Days)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedCard(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("From Date", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    text = dateOnlyFormat.format(Date(startDateTimestamp)),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        OutlinedCard(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("To Date", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    text = dateOnlyFormat.format(Date(endDateTimestamp)),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Top Feelings Breakdown by Percentage Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = strings.topFeelingsPercentage,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronPrimary
                    )

                    if (topFeelingsBreakdown.isEmpty()) {
                        Text(
                            text = "No feelings entries found in this date range.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    } else {
                        topFeelingsBreakdown.forEach { (feeling, percentage) ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = feeling,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f%%", percentage),
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronPrimary,
                                        fontSize = 14.sp
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { (percentage / 100.0).toFloat() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = SaffronPrimary,
                                    trackColor = SaffronPrimary.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            }

            // Checklist Section Completion Percentage Card
            if (showSec1 || showSec2 || showSec3) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "${strings.sectionCompletionPercentage} ($daysInRange days)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )

                        if (showSec1) {
                            PercentageProgressBar(
                                label = if (currentLang == AppLanguage.TAMIL) "1. அன்பு / LOVE (9 Items)" else "1. LOVE / அன்பு (9 Items)",
                                percentage = sec1Pct
                            )
                        }

                        if (showSec2) {
                            PercentageProgressBar(
                                label = if (currentLang == AppLanguage.TAMIL) "2. கணவன் மனைவி / HUSBAND & WIFE (6 Items)" else "2. HUSBAND & WIFE / கணவன் மனைவி (6 Items)",
                                percentage = sec2Pct
                            )
                        }

                        if (showSec3) {
                            PercentageProgressBar(
                                label = if (currentLang == AppLanguage.TAMIL) "3. மனப்பாங்கு / ATTITUDE & QUALITIES (5 Items)" else "3. ATTITUDE & QUALITIES / மனப்பாங்கு (5 Items)",
                                percentage = sec3Pct
                            )
                        }
                    }
                }
            }
        }
    }

    // Start Date Picker
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDateTimestamp)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { startDateTimestamp = it }
                        showStartDatePicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text(strings.cancel) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // End Date Picker
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDateTimestamp)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { endDateTimestamp = it }
                        showEndDatePicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text(strings.cancel) }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
private fun PercentageProgressBar(label: String, percentage: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Text(
                text = "$percentage%",
                fontWeight = FontWeight.Bold,
                color = SaffronPrimary,
                fontSize = 14.sp
            )
        }

        LinearProgressIndicator(
            progress = { percentage / 100.0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = SaffronPrimary,
            trackColor = SaffronPrimary.copy(alpha = 0.15f)
        )
    }
}
