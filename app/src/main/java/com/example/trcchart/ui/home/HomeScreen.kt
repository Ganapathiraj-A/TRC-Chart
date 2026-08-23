package com.example.trcchart.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.trcchart.*
import com.example.trcchart.data.FeelingsRepository
import com.example.trcchart.data.LocalizedStrings
import com.example.trcchart.theme.SaffronDark
import com.example.trcchart.theme.SaffronPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: FeelingsRepository,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang by repository.language.collectAsState()
    val userName by repository.userName.collectAsState()
    val userPhone by repository.userPhone.collectAsState()
    val strings = LocalizedStrings.get(currentLang)

    var showProfileDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(userName.isBlank()) }
    var inputName by androidx.compose.runtime.remember(userName) { androidx.compose.runtime.mutableStateOf(userName) }
    var inputPhone by androidx.compose.runtime.remember(userPhone) { androidx.compose.runtime.mutableStateOf(userPhone) }

    val welcomeTitle = if (userName.isNotBlank()) {
        if (currentLang == com.example.trcchart.data.AppLanguage.TAMIL) {
            "$userName அவர்களே நல்வரவு"
        } else {
            "Welcome $userName"
        }
    } else {
        strings.welcomeBannerTitle
    }

    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = {
                if (userName.isNotBlank()) showProfileDialog = false
            },
            title = { Text(strings.profileSectionTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(strings.enterDetailsPrompt, fontSize = 13.sp)

                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text(strings.nameLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputPhone,
                        onValueChange = { inputPhone = it },
                        label = { Text(strings.phoneLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            repository.updateUserProfile(inputName.trim(), inputPhone.trim())
                            showProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text(strings.save, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = if (userName.isNotBlank()) {
                {
                    TextButton(onClick = { showProfileDialog = false }) {
                        Text(strings.cancel)
                    }
                }
            } else null
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = strings.appTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                        Text(
                            text = strings.appSubtitle,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SaffronPrimary
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Card banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(SaffronPrimary, SaffronDark)
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = welcomeTitle,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (strings.welcomeBannerSubtitle.isNotBlank()) {
                            Text(
                                text = strings.welcomeBannerSubtitle,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            Text(
                text = strings.mainMenuTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Grid Options - 4 Main Cards: Feelings, Daily, Reports, Settings
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HomeCardOption(
                        title = strings.feelingsOption,
                        subtitle = strings.feelingsOptionSub,
                        icon = Icons.Default.Favorite,
                        badgeColor = Color(0xFFEF4444),
                        onClick = { onNavigate(FeelingsStep1Route) },
                        modifier = Modifier.weight(1f)
                    )
                    HomeCardOption(
                        title = strings.dailyOption,
                        subtitle = strings.dailyOptionSub,
                        icon = Icons.Default.DateRange,
                        badgeColor = Color(0xFF3B82F6),
                        onClick = { onNavigate(DailyRoute) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HomeCardOption(
                        title = strings.reportsOption,
                        subtitle = strings.reportsOptionSub,
                        icon = Icons.Default.BarChart,
                        badgeColor = Color(0xFFF59E0B),
                        onClick = { onNavigate(ReportsRoute) },
                        modifier = Modifier.weight(1f)
                    )
                    HomeCardOption(
                        title = strings.settingsOption,
                        subtitle = strings.settingsOptionSub,
                        icon = Icons.Default.Settings,
                        badgeColor = Color(0xFF8B5CF6),
                        onClick = { onNavigate(SettingsRoute) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // APK Update Link Footer with Copy Icon
            val context = androidx.compose.ui.platform.LocalContext.current
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            val apkDownloadUrl = "https://github.com/Ganapathiraj-A/TRC-Chart/releases/download/latest/TRC_Chart.apk"

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "TRC Chart App v2.7-location-profile",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Update Button
                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(apkDownloadUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.CloudDownload,
                            contentDescription = "Download Update",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Download Latest Update",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Copy Link Icon Button
                    IconButton(
                        onClick = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(apkDownloadUrl))
                            android.widget.Toast.makeText(context, "APK Link copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SaffronPrimary.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ContentCopy,
                            contentDescription = "Copy APK Link",
                            tint = SaffronPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeCardOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(135.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = badgeColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
