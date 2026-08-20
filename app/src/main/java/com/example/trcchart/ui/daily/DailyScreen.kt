package com.example.trcchart.ui.daily

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
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
    val strings = LocalizedStrings.get(currentLang)

    var showSummaryDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Checklist, 1: Feelings Log

    val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())

    val section1Items = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 1 } }
    val section2Items = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 2 } }
    val section3Items = remember(repository.checklistItems) { repository.checklistItems.filter { it.section == 3 } }

    val sec1CheckedCount = section1Items.count { checkedIds.contains(it.id) }
    val sec2CheckedCount = section2Items.count { checkedIds.contains(it.id) }
    val sec3CheckedCount = section3Items.count { checkedIds.contains(it.id) }
    val totalChecked = checkedIds.size

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
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = "Summary", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
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
            // Tab Header
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SaffronPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Daily Checklist ($totalChecked/20)", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Feelings Log (${entries.size})", fontWeight = FontWeight.Bold) }
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

                    // Section 1: Love / அன்பு
                    item {
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

                    // Section 2: Husband & Wife / கணவன் மனைவி
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
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

                    // Section 3: Attitude & Qualities / மனப்பாங்கு
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
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

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            } else {
                // Feelings Entries Log
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    if (entries.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.noDailyEntries,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(entries) { entry ->
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
                                            text = dateFormat.format(Date(entry.timestamp)),
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
                        text = "Checklist Completion: $totalChecked / 20 items",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Divider()
                    Text("• Section 1 (LOVE): $sec1CheckedCount / 9 completed")
                    Text("• Section 2 (HUSBAND & WIFE): $sec2CheckedCount / 6 completed")
                    Text("• Section 3 (ATTITUDE): $sec3CheckedCount / 5 completed")
                    Divider()
                    Text("Total Feelings Recorded Today: ${entries.size}")
                    Text("• Good Karma: ${entries.count { it.isGoodKarma }}")
                    Text("• Bad Karma: ${entries.count { !it.isGoodKarma }}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showSummaryDialog = false }) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
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
