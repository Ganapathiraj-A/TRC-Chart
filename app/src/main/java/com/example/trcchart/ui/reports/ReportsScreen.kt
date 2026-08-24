package com.example.trcchart.ui.reports

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.trcchart.data.AppLanguage
import com.example.trcchart.data.FeelingsRepository
import com.example.trcchart.data.LocalizedStrings
import com.example.trcchart.data.TRCEntry
import com.example.trcchart.theme.BadKarmaColor
import com.example.trcchart.theme.GoodKarmaColor
import com.example.trcchart.theme.SaffronPrimary
import com.example.trcchart.ui.daily.EditEntryDialog
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    repository: FeelingsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by repository.entries.collectAsState()
    val currentLang by repository.language.collectAsState()
    val availableFeelings by repository.feelings.collectAsState()
    val checklistLogs by repository.checklistLogs.collectAsState()
    val showMeditation by repository.showMeditation.collectAsState()
    val showCleaning by repository.showCleaning.collectAsState()
    val showSec1 by repository.showSection1.collectAsState()
    val showSec2 by repository.showSection2.collectAsState()
    val showSec3 by repository.showSection3.collectAsState()

    val strings = LocalizedStrings.get(currentLang)
    val context = LocalContext.current

    val dateOnlyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val fullDateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var editingEntry by remember { mutableStateOf<TRCEntry?>(null) }
    var deletingEntryId by remember { mutableStateOf<String?>(null) }
    var drilldownFeeling by remember { mutableStateOf<String?>(null) }

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

    // List of date strings in selected date range
    val datesInRange = remember(startDateTimestamp, endDateTimestamp) {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance().apply { timeInMillis = startDateTimestamp }
        val endCal = Calendar.getInstance().apply { timeInMillis = endDateTimestamp }
        while (!cal.after(endCal)) {
            list.add(dateOnlyFormat.format(cal.time))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
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
    val daysInRange = datesInRange.size.coerceAtLeast(1)

    // Section completion percentages
    val meditationItems = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 101 } }
    val cleaningItems = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 102 } }
    val section1Items = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 1 } }
    val section2Items = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 2 } }
    val section3Items = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 3 } }

    val medPct = remember(checklistLogs, datesInRange) {
        if (meditationItems.isEmpty() || datesInRange.isEmpty()) 0
        else {
            val checkedCount = datesInRange.sumOf { dateStr ->
                val dayChecked = checklistLogs[dateStr] ?: emptySet()
                meditationItems.count { dayChecked.contains(it.id) }
            }
            val totalPossible = meditationItems.size * datesInRange.size
            ((checkedCount.toDouble() / totalPossible) * 100).toInt().coerceIn(0, 100)
        }
    }

    val cleanPct = remember(checklistLogs, datesInRange) {
        if (cleaningItems.isEmpty() || datesInRange.isEmpty()) 0
        else {
            val checkedCount = datesInRange.sumOf { dateStr ->
                val dayChecked = checklistLogs[dateStr] ?: emptySet()
                cleaningItems.count { dayChecked.contains(it.id) }
            }
            val totalPossible = cleaningItems.size * datesInRange.size
            ((checkedCount.toDouble() / totalPossible) * 100).toInt().coerceIn(0, 100)
        }
    }

    val sec1Pct = remember(checklistLogs, datesInRange) {
        if (section1Items.isEmpty() || datesInRange.isEmpty()) 0
        else {
            val checkedCount = datesInRange.sumOf { dateStr ->
                val dayChecked = checklistLogs[dateStr] ?: emptySet()
                section1Items.count { dayChecked.contains(it.id) }
            }
            val totalPossible = section1Items.size * datesInRange.size
            ((checkedCount.toDouble() / totalPossible) * 100).toInt().coerceIn(0, 100)
        }
    }

    val sec2Pct = remember(checklistLogs, datesInRange) {
        if (section2Items.isEmpty() || datesInRange.isEmpty()) 0
        else {
            val checkedCount = datesInRange.sumOf { dateStr ->
                val dayChecked = checklistLogs[dateStr] ?: emptySet()
                section2Items.count { dayChecked.contains(it.id) }
            }
            val totalPossible = section2Items.size * datesInRange.size
            ((checkedCount.toDouble() / totalPossible) * 100).toInt().coerceIn(0, 100)
        }
    }

    val sec3Pct = remember(checklistLogs, datesInRange) {
        if (section3Items.isEmpty() || datesInRange.isEmpty()) 0
        else {
            val checkedCount = datesInRange.sumOf { dateStr ->
                val dayChecked = checklistLogs[dateStr] ?: emptySet()
                section3Items.count { dayChecked.contains(it.id) }
            }
            val totalPossible = section3Items.size * datesInRange.size
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

            // Tab Navigation: Summary vs Feelings History
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SaffronPrimary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = strings.summaryTab,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = "${strings.feelingsHistoryTab} (${filteredEntries.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                )
            }

            if (selectedTabIndex == 0) {
                // Tab 0: Summary View
                // Export to Excel (CSV) Button
                Button(
                    onClick = {
                        exportToExcelCSV(context, filteredEntries, fullDateFormat)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                    enabled = filteredEntries.isNotEmpty()
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Export Excel")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Selected Range to Excel (CSV)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                // Top Feelings Breakdown by Percentage Card with Total Entries Count
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.topFeelingsPercentage,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Surface(
                                color = SaffronPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${strings.totalFeelingsInRange}${filteredEntries.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SaffronPrimary,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (topFeelingsBreakdown.isEmpty()) {
                            Text(
                                text = strings.noFeelingsInRange,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        } else {
                            topFeelingsBreakdown.forEach { (feeling, percentage) ->
                                val count = filteredEntries.count { it.feeling == feeling }
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { drilldownFeeling = feeling }
                                        .padding(vertical = 4.dp, horizontal = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                        ) {
                                            Text(
                                                text = feeling,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = "Drill down",
                                                tint = SaffronPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = String.format(Locale.getDefault(), "%.1f%% (%d)", percentage, count),
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
                if (showMeditation || showCleaning || showSec1 || showSec2 || showSec3) {
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

                            if (showMeditation) {
                                PercentageProgressBar(
                                    label = if (currentLang == AppLanguage.TAMIL) "தியானம் / MEDITATION (2 Items)" else "MEDITATION / தியானம் (2 Items)",
                                    percentage = medPct
                                )
                            }

                            if (showCleaning) {
                                PercentageProgressBar(
                                    label = if (currentLang == AppLanguage.TAMIL) "சுத்திகரிப்பு / CLEANING PROCESS (2 Items)" else "CLEANING PROCESS / சுத்திகரிப்பு (2 Items)",
                                    percentage = cleanPct
                                )
                            }

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
            } else {
                // Tab 1: Feelings History (View entries one by one)
                if (filteredEntries.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.noFeelingsInRange,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    val sortedHistory = remember(filteredEntries) {
                        filteredEntries.sortedByDescending { it.timestamp }
                    }

                    sortedHistory.forEach { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.feeling,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronPrimary,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = if (entry.isGoodKarma) GoodKarmaColor else BadKarmaColor,
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = if (entry.isGoodKarma) strings.goodKarma else strings.badKarma,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // Edit Entry Icon Button
                                        IconButton(
                                            onClick = { editingEntry = entry },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Entry",
                                                tint = SaffronPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // Delete Entry Icon Button
                                        IconButton(
                                            onClick = { deletingEntryId = entry.id },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Entry",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = fullDateFormat.format(Date(entry.timestamp)),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                if (entry.reason.isNotBlank()) {
                                    Text(
                                        text = "${strings.reasonLabel}: ${entry.reason}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                if (entry.awareness.isNotBlank()) {
                                    Text(
                                        text = "${strings.awarenessLabel}: ${entry.awareness}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                if (entry.feelingsDetail.isNotBlank()) {
                                    Text(
                                        text = "${strings.feelingsDetailLabel}: ${entry.feelingsDetail}",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                val traps = mutableListOf<String>()
                                if (entry.isBlame) traps.add(strings.blame)
                                if (entry.isComplaint) traps.add(strings.complaint)
                                if (entry.isExcuse) traps.add(strings.excuse)
                                if (entry.isGossip) traps.add(strings.gossip)

                                if (traps.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${strings.observedTrapsPrefix}${traps.joinToString(", ")}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                                    )
                                }
                            }
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
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
                            val localCal = Calendar.getInstance().apply {
                                set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            startDateTimestamp = localCal.timeInMillis
                        }
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
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
                            val localCal = Calendar.getInstance().apply {
                                set(utcCal.get(Calendar.YEAR), utcCal.get(Calendar.MONTH), utcCal.get(Calendar.DAY_OF_MONTH), 23, 59, 59)
                                set(Calendar.MILLISECOND, 999)
                            }
                            endDateTimestamp = localCal.timeInMillis
                        }
                        showEndDatePicker = false
                    }
                ) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text(strings.cancel) }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // Edit Entry Dialog
    editingEntry?.let { entryToEdit ->
        EditEntryDialog(
            entry = entryToEdit,
            feelingsList = availableFeelings,
            strings = strings,
            onDismiss = { editingEntry = null },
            onSave = { updated ->
                repository.updateEntry(updated)
                editingEntry = null
            }
        )
    }

    // Delete Entry Confirmation Dialog
    deletingEntryId?.let { targetId ->
        AlertDialog(
            onDismissRequest = { deletingEntryId = null },
            title = { Text("Delete Entry / உணர்வை நீக்கு") },
            text = { Text("Are you sure you want to delete this recorded feeling entry?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteEntry(targetId)
                        deletingEntryId = null
                    }
                ) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEntryId = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Feeling Drilldown Dialog
    if (drilldownFeeling != null) {
        val targetFeeling = drilldownFeeling!!
        val feelingEntries = remember(filteredEntries, targetFeeling) {
            filteredEntries.filter { it.feeling == targetFeeling }.sortedByDescending { it.timestamp }
        }

        AlertDialog(
            onDismissRequest = { drilldownFeeling = null },
            title = {
                Column {
                    Text(
                        text = targetFeeling,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronPrimary
                    )
                    Text(
                        text = "${feelingEntries.size} ${if (feelingEntries.size == 1) "entry" else "entries"} in selected date range",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            text = {
                if (feelingEntries.isEmpty()) {
                    Text("No entries recorded for this feeling.", fontSize = 14.sp)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(feelingEntries) { entry ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = fullDateFormat.format(Date(entry.timestamp)),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )

                                        Surface(
                                            color = if (entry.isGoodKarma) GoodKarmaColor else BadKarmaColor,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (entry.isGoodKarma) strings.goodKarma else strings.badKarma,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (entry.reason.isNotBlank()) {
                                        Text(
                                            text = "Reason / காரணம்: ${entry.reason}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (entry.awareness.isNotBlank()) {
                                        Text(
                                            text = "Awareness / விழிப்புணர்வு: ${entry.awareness}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { drilldownFeeling = null }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = SaffronPrimary)
                }
            }
        )
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

// Function to generate Excel CSV file and share/export via Android Intent
private fun exportToExcelCSV(
    context: Context,
    entries: List<TRCEntry>,
    dateFormat: SimpleDateFormat
) {
    try {
        val csvHeader = "ID,Date Time,Feeling,Karma,Reason,Awareness,Detailed Feelings,Blame,Complaint,Excuse,Gossip\n"
        val sb = StringBuilder(csvHeader)

        entries.forEach { entry ->
            val dateStr = escapeCSV(dateFormat.format(Date(entry.timestamp)))
            val feelingStr = escapeCSV(entry.feeling)
            val karmaStr = if (entry.isGoodKarma) "Good Karma" else "Bad Karma"
            val reasonStr = escapeCSV(entry.reason)
            val awarenessStr = escapeCSV(entry.awareness)
            val detailStr = escapeCSV(entry.feelingsDetail)

            sb.append("${entry.id},$dateStr,$feelingStr,$karmaStr,$reasonStr,$awarenessStr,$detailStr,${entry.isBlame},${entry.isComplaint},${entry.isExcuse},${entry.isGossip}\n")
        }

        val file = File(context.cacheDir, "TRC_Chart_Export_${System.currentTimeMillis()}.csv")
        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Export TRC Chart Data to Excel"))
        Toast.makeText(context, "Export ready! Select Excel or file viewer app.", Toast.LENGTH_LONG).show()

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error exporting data: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun escapeCSV(value: String): String {
    val clean = value.replace("\n", " ").replace("\r", " ")
    return if (clean.contains(",") || clean.contains("\"")) {
        "\"" + clean.replace("\"", "\"\"") + "\""
    } else {
        clean
    }
}
