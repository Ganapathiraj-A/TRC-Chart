package com.example.trcchart.ui.feelings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trcchart.data.FeelingsRepository
import com.example.trcchart.data.TRCEntry
import com.example.trcchart.theme.BadKarmaColor
import com.example.trcchart.theme.GoodKarmaColor
import com.example.trcchart.theme.SaffronPrimary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeelingsStep3Screen(
    repository: FeelingsRepository,
    selectedFeeling: String,
    reason: String,
    awareness: String,
    detailedFeelings: String,
    onComplete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isGoodKarma by remember { mutableStateOf(true) }
    var isBlame by remember { mutableStateOf(false) }
    var isComplaint by remember { mutableStateOf(false) }
    var isExcuse by remember { mutableStateOf(false) }
    var isGossip by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Karma & Reflection", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Feeling: $selectedFeeling",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SaffronPrimary
                        )
                        if (reason.isNotBlank()) Text("Reason: $reason", fontSize = 13.sp)
                        if (awareness.isNotBlank()) Text("Awareness: $awareness", fontSize = 13.sp)
                        if (detailedFeelings.isNotBlank()) Text("Detail: $detailedFeelings", fontSize = 13.sp)
                    }
                }

                // Karma Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Karma Toggle",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(27.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(27.dp)
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Good Karma Option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(if (isGoodKarma) GoodKarmaColor else Color.Transparent)
                                .clickable { isGoodKarma = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Good Karma",
                                color = if (isGoodKarma) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        // Bad Karma Option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(if (!isGoodKarma) BadKarmaColor else Color.Transparent)
                                .clickable { isGoodKarma = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Bad Karma",
                                color = if (!isGoodKarma) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                // Mind Traps Toggles
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Mind Traps / Patterns",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    ToggleRowItem(
                        label = "Blame",
                        checked = isBlame,
                        onCheckedChange = { isBlame = it }
                    )

                    ToggleRowItem(
                        label = "Complaint",
                        checked = isComplaint,
                        onCheckedChange = { isComplaint = it }
                    )

                    ToggleRowItem(
                        label = "Excuse",
                        checked = isExcuse,
                        onCheckedChange = { isExcuse = it }
                    )

                    ToggleRowItem(
                        label = "Gossip",
                        checked = isGossip,
                        onCheckedChange = { isGossip = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val entry = TRCEntry(
                        id = UUID.randomUUID().toString(),
                        timestamp = System.currentTimeMillis(),
                        feeling = selectedFeeling,
                        reason = reason,
                        awareness = awareness,
                        feelingsDetail = detailedFeelings,
                        isGoodKarma = isGoodKarma,
                        isBlame = isBlame,
                        isComplaint = isComplaint,
                        isExcuse = isExcuse,
                        isGossip = isGossip
                    )
                    repository.addEntry(entry)
                    onComplete()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text("Save Entry", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ToggleRowItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) },
        colors = CardDefaults.cardColors(
            containerColor = if (checked) SaffronPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = if (checked) ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(SaffronPrimary)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (checked) SaffronPrimary else MaterialTheme.colorScheme.onSurface
            )

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SaffronPrimary
                )
            )
        }
    }
}
