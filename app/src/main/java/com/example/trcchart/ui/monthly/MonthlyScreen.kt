package com.example.trcchart.ui.monthly

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
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
import com.example.trcchart.data.FeelingsRepository
import com.example.trcchart.data.LocalizedStrings
import com.example.trcchart.data.TRCEntry
import com.example.trcchart.theme.BadKarmaColor
import com.example.trcchart.theme.GoodKarmaColor
import com.example.trcchart.theme.SaffronPrimary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyScreen(
    repository: FeelingsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by repository.entries.collectAsState()
    val currentLang by repository.language.collectAsState()
    val strings = LocalizedStrings.get(currentLang)
    val context = LocalContext.current

    // Month Selector state (Year-Month calendar)
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val monthValueFormat = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val dateOnlyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    var selectedMonthCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) })
    }

    var isCustomRangeMode by remember { mutableStateOf(false) }

    var startDateTimestamp by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis)
    }

    var endDateTimestamp by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis)
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Filtered entries calculation
    val filteredEntries = remember(entries, selectedMonthCalendar, isCustomRangeMode, startDateTimestamp, endDateTimestamp) {
        if (!isCustomRangeMode) {
            val targetMonth = monthValueFormat.format(selectedMonthCalendar.time)
            entries.filter { monthValueFormat.format(Date(it.timestamp)) == targetMonth }
        } else {
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
    }

    val totalCount = filteredEntries.size
    val goodKarmaCount = filteredEntries.count { it.isGoodKarma }
    val badKarmaCount = filteredEntries.count { !it.isGoodKarma }
    val blameCount = filteredEntries.count { it.isBlame }
    val complaintCount = filteredEntries.count { it.isComplaint }
    val excuseCount = filteredEntries.count { it.isExcuse }
    val gossipCount = filteredEntries.count { it.isGossip }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.monthlyTitle, fontWeight = FontWeight.Bold) },
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
            // Mode Toggle Switch: Month Selector vs Custom Range Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surface),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(3.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(if (!isCustomRangeMode) SaffronPrimary else Color.Transparent)
                        .clickable { isCustomRangeMode = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "By Month / மாதம்",
                        color = if (!isCustomRangeMode) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(3.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(if (isCustomRangeMode) SaffronPrimary else Color.Transparent)
                        .clickable { isCustomRangeMode = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Custom Range",
                        color = if (isCustomRangeMode) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Selector Controls Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isCustomRangeMode) {
                        // Month Selector Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Month",
                                    tint = SaffronPrimary
                                )
                                Text(
                                    text = monthFormat.format(selectedMonthCalendar.time),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val cal = selectedMonthCalendar.clone() as Calendar
                                        cal.add(Calendar.MONTH, -1)
                                        selectedMonthCalendar = cal
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("< Prev", fontSize = 12.sp, color = SaffronPrimary)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val cal = selectedMonthCalendar.clone() as Calendar
                                        cal.add(Calendar.MONTH, 1)
                                        selectedMonthCalendar = cal
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Next >", fontSize = 12.sp, color = SaffronPrimary)
                                }
                            }
                        }
                    } else {
                        // Date Range Selector Controls
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Select Date Range / தேதி வரம்பு",
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
                }
            }

            // Summary Statistics Card
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
                        text = "Summary Overview / சுருக்கம்",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Recorded: $totalCount", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("• Good Karma: $goodKarmaCount", color = GoodKarmaColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("• Bad Karma: $badKarmaCount", color = BadKarmaColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Column {
                            Text("Mind Traps:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("• Blame: $blameCount", fontSize = 12.sp)
                            Text("• Complaint: $complaintCount", fontSize = 12.sp)
                            Text("• Excuse: $excuseCount", fontSize = 12.sp)
                            Text("• Gossip: $gossipCount", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Export to Excel / CSV Button
            Button(
                onClick = {
                    exportToExcelCSV(context, filteredEntries, dateFormat)
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
                Text("Export Data to Excel (CSV)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // Filtered Entries List
            Text(
                text = "Entries Log (${filteredEntries.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No records found for selected period.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredEntries.forEach { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = entry.feeling,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronPrimary
                                    )

                                    Surface(
                                        color = if (entry.isGoodKarma) GoodKarmaColor else BadKarmaColor,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = if (entry.isGoodKarma) "Good Karma" else "Bad Karma",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = dateFormat.format(Date(entry.timestamp)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )

                                if (entry.reason.isNotBlank()) Text("Reason: ${entry.reason}", fontSize = 13.sp)
                                if (entry.awareness.isNotBlank()) Text("Awareness: ${entry.awareness}", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Start Date Picker Dialog
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
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // End Date Picker Dialog
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
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
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
