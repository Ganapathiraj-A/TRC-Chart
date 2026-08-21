package com.example.trcchart.ui.daily

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trcchart.data.AppLanguage
import com.example.trcchart.data.FeelingsRepository
import com.example.trcchart.data.LocalizedStrings
import com.example.trcchart.theme.BadKarmaColor
import com.example.trcchart.theme.GoodKarmaColor
import com.example.trcchart.theme.SaffronPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen(
    repository: FeelingsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by repository.entries.collectAsState()
    val currentLang by repository.language.collectAsState()
    val checkedIds by repository.checkedChecklistIds.collectAsState()

    val showMeditation by repository.showMeditation.collectAsState()
    val showCleaning by repository.showCleaning.collectAsState()
    val showSec1 by repository.showSection1.collectAsState()
    val showSec2 by repository.showSection2.collectAsState()
    val showSec3 by repository.showSection3.collectAsState()

    val strings = LocalizedStrings.get(currentLang)

    var showSummaryDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Checklist, 1: Feelings Log

    // Selected Date Filter state
    var selectedDateTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val dateOnlyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    // Filter entries by selected date
    val selectedDateString = dateOnlyFormat.format(Date(selectedDateTimestamp))
    val filteredEntries = remember(entries, selectedDateString) {
        entries.filter { dateOnlyFormat.format(Date(it.timestamp)) == selectedDateString }
    }

    val meditationItems = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 101 } }
    val cleaningItems = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 102 } }
    val section1Items = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 1 } }
    val section2Items = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 2 } }
    val section3Items = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 3 } }

    val medCheckedCount = meditationItems.count { checkedIds.contains(it.id) }
    val cleanCheckedCount = cleaningItems.count { checkedIds.contains(it.id) }
    val sec1CheckedCount = section1Items.count { checkedIds.contains(it.id) }
    val sec2CheckedCount = section2Items.count { checkedIds.contains(it.id) }
    val sec3CheckedCount = section3Items.count { checkedIds.contains(it.id) }

    val visibleTotalItems = (if (showMeditation) meditationItems.size else 0) +
            (if (showCleaning) cleaningItems.size else 0) +
            (if (showSec1) section1Items.size else 0) +
            (if (showSec2) section2Items.size else 0) +
            (if (showSec3) section3Items.size else 0)

    val visibleCheckedCount = (if (showMeditation) medCheckedCount else 0) +
            (if (showCleaning) cleanCheckedCount else 0) +
            (if (showSec1) sec1CheckedCount else 0) +
            (if (showSec2) sec2CheckedCount else 0) +
            (if (showSec3) sec3CheckedCount else 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.dailyLogTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = { showSummaryDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.25f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = "Summary", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Summary", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
        ) {
            // Date Filter Header Card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            tint = SaffronPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = displayDateFormat.format(Date(selectedDateTimestamp)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = { showDatePickerDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Change Date", fontSize = 12.sp, color = SaffronPrimary)
                    }
                }
            }

            // Tab Header
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SaffronPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Daily Checklist ($visibleCheckedCount/$visibleTotalItems)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Feelings Log (${filteredEntries.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                // Checklist Sections
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Banner: God is within me
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SaffronPrimary.copy(alpha = 0.12f))
                        ) {
                            Text(
                                text = if (currentLang == AppLanguage.TAMIL)
                                    "இறைவன் என்னுள் இருக்கிறார் (ஒரு மன நேசத்திற்கு ஒரு யுகம்) God is within me (Every hourly)"
                                else
                                    "God is within me (Every hourly) / இறைவன் என்னுள் இருக்கிறார்",
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Section: Meditation / தியானம் (If Visible)
                    if (showMeditation) {
                        item {
                            SectionHeader(
                                title = if (currentLang == AppLanguage.TAMIL) "தியானம் / MEDITATION" else "MEDITATION / தியானம்",
                                progressText = "$medCheckedCount / ${meditationItems.size}"
                            )
                        }
                        items(meditationItems) { item ->
                            ChecklistRow(
                                text = if (currentLang == AppLanguage.TAMIL) item.textTa else item.textEn,
                                checked = checkedIds.contains(item.id),
                                onToggle = { repository.toggleChecklistItem(item.id) }
                            )
                        }
                    }

                    // Section: Cleaning Process / சுத்திகரிப்பு செயல்முறை (If Visible)
                    if (showCleaning) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            SectionHeader(
                                title = if (currentLang == AppLanguage.TAMIL) "சுத்திகரிப்பு செயல்முறை / CLEANING PROCESS" else "CLEANING PROCESS / சுத்திகரிப்பு செயல்முறை",
                                progressText = "$cleanCheckedCount / ${cleaningItems.size}"
                            )
                        }
                        items(cleaningItems) { item ->
                            ChecklistRow(
                                text = if (currentLang == AppLanguage.TAMIL) item.textTa else item.textEn,
                                checked = checkedIds.contains(item.id),
                                onToggle = { repository.toggleChecklistItem(item.id) }
                            )
                        }
                    }

                    // Section 1: Love / அன்பு (If Visible)
                    if (showSec1) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            SectionHeader(
                                title = if (currentLang == AppLanguage.TAMIL) "1. அன்பு / LOVE" else "1. LOVE / அன்பு",
                                progressText = "$sec1CheckedCount / ${section1Items.size}"
                            )
                        }
                        items(section1Items) { item ->
                            ChecklistRow(
                                text = if (currentLang == AppLanguage.TAMIL) item.textTa else item.textEn,
                                checked = checkedIds.contains(item.id),
                                onToggle = { repository.toggleChecklistItem(item.id) }
                            )
                        }
                    }

                    // Section 2: Husband & Wife / கணவன் மனைவி (If Visible)
                    if (showSec2) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            SectionHeader(
                                title = if (currentLang == AppLanguage.TAMIL) "2. கணவன் மனைவி / HUSBAND & WIFE" else "2. HUSBAND & WIFE / கணவன் மனைவி",
                                progressText = "$sec2CheckedCount / ${section2Items.size}"
                            )
                        }
                        items(section2Items) { item ->
                            ChecklistRow(
                                text = if (currentLang == AppLanguage.TAMIL) item.textTa else item.textEn,
                                checked = checkedIds.contains(item.id),
                                onToggle = { repository.toggleChecklistItem(item.id) }
                            )
                        }
                    }

                    // Section 3: Attitude & Qualities / மனப்பாங்கு (If Visible)
                    if (showSec3) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            SectionHeader(
                                title = if (currentLang == AppLanguage.TAMIL) "3. மனப்பாங்கு / ATTITUDE & QUALITIES" else "3. ATTITUDE & QUALITIES / மனப்பாங்கு",
                                progressText = "$sec3CheckedCount / ${section3Items.size}"
                            )
                        }
                        items(section3Items) { item ->
                            ChecklistRow(
                                text = if (currentLang == AppLanguage.TAMIL) item.textTa else item.textEn,
                                checked = checkedIds.contains(item.id),
                                onToggle = { repository.toggleChecklistItem(item.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else {
                // Feelings Entries Log Filtered by Selected Date
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    if (filteredEntries.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No feelings recorded for ${displayDateFormat.format(Date(selectedDateTimestamp))}.",
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredEntries) { entry ->
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
                                                color = SaffronPrimary
                                            )

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
                                        }

                                        Text(
                                            text = timeFormat.format(Date(entry.timestamp)),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        if (entry.reason.isNotBlank()) {
                                            Text(
                                                text = "${strings.reasonLabel}: ${entry.reason}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                        if (entry.awareness.isNotBlank()) {
                                            Text(
                                                text = "${strings.awarenessLabel}: ${entry.awareness}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }

                                        val traps = mutableListOf<String>()
                                        if (entry.isBlame) traps.add(strings.blame)
                                        if (entry.isComplaint) traps.add(strings.complaint)
                                        if (entry.isExcuse) traps.add(strings.excuse)
                                        if (entry.isGossip) traps.add(strings.gossip)

                                        if (traps.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "${strings.observedTrapsPrefix}${traps.joinToString(", ")}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Summary Dialog
    if (showSummaryDialog) {
        AlertDialog(
            onDismissRequest = { showSummaryDialog = false },
            title = {
                Text(
                    text = "Daily Progress Summary",
                    fontWeight = FontWeight.Bold,
                    color = SaffronPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Checklist Completion: $visibleCheckedCount / $visibleTotalItems items",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    HorizontalDivider()
                    if (showMeditation) Text("• MEDITATION: $medCheckedCount / 2 completed")
                    if (showCleaning) Text("• CLEANING PROCESS: $cleanCheckedCount / 2 completed")
                    if (showSec1) Text("• Section 1 (LOVE): $sec1CheckedCount / 9 completed")
                    if (showSec2) Text("• Section 2 (HUSBAND & WIFE): $sec2CheckedCount / 6 completed")
                    if (showSec3) Text("• Section 3 (ATTITUDE): $sec3CheckedCount / 5 completed")
                    HorizontalDivider()
                    Text("Feelings Recorded on ${displayDateFormat.format(Date(selectedDateTimestamp))}: ${filteredEntries.size}")
                    Text("• Good Karma: ${filteredEntries.count { it.isGoodKarma }}")
                    Text("• Bad Karma: ${filteredEntries.count { !it.isGoodKarma }}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showSummaryDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Date Picker Dialog for Daily Screen Review
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateTimestamp
        )

        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selected ->
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = selected
                            selectedDateTimestamp = cal.timeInMillis
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text(strings.cancel)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SectionHeader(title: String, progressText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Surface(
            color = SaffronPrimary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = progressText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SaffronPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ChecklistRow(
    text: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) SaffronPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (checked) SaffronPrimary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )

            Checkbox(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = SaffronPrimary)
            )
        }
    }
}
