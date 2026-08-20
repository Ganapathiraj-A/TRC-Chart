package com.example.trcchart.ui.monthly

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun MonthlyScreen(
    repository: FeelingsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by repository.entries.collectAsState()
    val currentLang by repository.language.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    val totalEntries = entries.size
    val goodKarmaCount = entries.count { it.isGoodKarma }
    val badKarmaCount = entries.count { !it.isGoodKarma }

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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = strings.monthlyOverview,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("${strings.totalRecordedFeelings}$totalEntries", fontSize = 15.sp)
                    Text("${strings.goodKarmaCount}$goodKarmaCount", fontSize = 15.sp)
                    Text("${strings.badKarmaCount}$badKarmaCount", fontSize = 15.sp)
                }
            }

            Text(
                text = strings.monthlyFutureNote,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}
