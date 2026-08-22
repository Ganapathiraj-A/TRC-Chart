package com.example.trcchart.ui.feelings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trcchart.data.FeelingsRepository
import com.example.trcchart.data.LocalizedStrings
import com.example.trcchart.theme.SaffronPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeelingsStep2Screen(
    repository: FeelingsRepository,
    selectedFeeling: String,
    timestamp: Long,
    onNext: (reason: String, awareness: String, detailedFeelings: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang by repository.language.collectAsState()
    val entries by repository.entries.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    var reasonText by remember { mutableStateOf("") }
    var awarenessText by remember { mutableStateOf("") }
    var detailedFeelingsText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.awarenessLabel, fontWeight = FontWeight.Bold) },
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    color = SaffronPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.selectedFeelingPrefix}$selectedFeeling",
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (entries.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val previousEntry = entries.maxByOrNull { it.timestamp }
                                    previousEntry?.let {
                                        reasonText = it.reason
                                        awarenessText = it.awareness
                                        detailedFeelingsText = it.feelingsDetail
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronPrimary)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.usePreviousButton, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }



                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text(strings.reasonLabel) },
                    placeholder = { Text(strings.reasonPlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = awarenessText,
                    onValueChange = { awarenessText = it },
                    label = { Text(strings.awarenessLabel) },
                    placeholder = { Text(strings.awarenessPlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = detailedFeelingsText,
                    onValueChange = { detailedFeelingsText = it },
                    label = { Text(strings.feelingsDetailLabel) },
                    placeholder = { Text(strings.feelingsDetailPlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onNext(reasonText.trim(), awarenessText.trim(), detailedFeelingsText.trim())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text(strings.nextButton, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
