package com.example.trcchart.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.trcchart.data.TelemetryService
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
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
import com.example.trcchart.data.AppLanguage
import com.example.trcchart.data.FeelingsRepository
import com.example.trcchart.data.LocalizedStrings
import com.example.trcchart.theme.SaffronPrimary
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: FeelingsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val feelingsList by repository.feelings.collectAsState()
    val currentLang by repository.language.collectAsState()
    val userName by repository.userName.collectAsState()
    val userPhone by repository.userPhone.collectAsState()
    val showMeditation by repository.showMeditation.collectAsState()
    val showCleaning by repository.showCleaning.collectAsState()
    val showSec1 by repository.showSection1.collectAsState()
    val showSec2 by repository.showSection2.collectAsState()
    val showSec3 by repository.showSection3.collectAsState()

    val strings = LocalizedStrings.get(currentLang)
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var showProfileEditDialog by remember { mutableStateOf(false) }

    var editNameInput by remember(userName) { mutableStateOf(userName) }
    var editPhoneInput by remember(userPhone) { mutableStateOf(userPhone) }
    var inputText by remember { mutableStateOf("") }

    if (showProfileEditDialog) {
        AlertDialog(
            onDismissRequest = { showProfileEditDialog = false },
            title = { Text(strings.profileSectionTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = { Text(strings.nameLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editPhoneInput,
                        onValueChange = { editPhoneInput = it },
                        label = { Text(strings.phoneLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateUserProfile(editNameInput, editPhoneInput)
                        showProfileEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text(strings.save, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileEditDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Launcher for creating backup file in Google Drive / Storage
    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { saveBackupToUri(context, repository, it) }
    }

    // Launcher for opening backup file from Google Drive / Storage
    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { restoreBackupFromUri(context, repository, it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle, fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Card
            item {
                val userCountry by repository.userCountry.collectAsState()
                val userState by repository.userState.collectAsState()
                val userCity by repository.userCity.collectAsState()

                var nameInput by remember(userName) { mutableStateOf(userName) }
                var phoneInput by remember(userPhone) { mutableStateOf(userPhone) }
                var countryInput by remember(userCountry) { mutableStateOf(userCountry.ifBlank { "India" }) }
                var stateInput by remember(userState) { mutableStateOf(userState.ifBlank { "Tamil Nadu" }) }
                var cityInput by remember(userCity) { mutableStateOf(userCity) }

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
                        Text(
                            text = strings.profileSectionTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )

                        Text(
                            text = strings.profileSectionSub,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text(strings.nameLabel) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text(strings.phoneLabel) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Country Dropdown
                        val worldCountries = remember {
                            listOf(
                                "India", "Malaysia", "Singapore", "United States", "United Kingdom", "United Arab Emirates",
                                "Canada", "Australia", "Germany", "France", "Japan", "China", "Brazil", "South Africa",
                                "Saudi Arabia", "Sri Lanka", "Indonesia", "Thailand", "Vietnam", "Philippines",
                                "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Argentina", "Armenia", "Austria",
                                "Azerbaijan", "Bahrain", "Bangladesh", "Belarus", "Belgium", "Bhutan", "Bolivia", "Bosnia",
                                "Botswana", "Brunei", "Bulgaria", "Cambodia", "Cameroon", "Chile", "Colombia", "Costa Rica",
                                "Croatia", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Ecuador", "Egypt", "Estonia",
                                "Ethiopia", "Fiji", "Finland", "Georgia", "Ghana", "Greece", "Guatemala", "Honduras",
                                "Hong Kong", "Hungary", "Iceland", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica",
                                "Jordan", "Kazakhstan", "Kenya", "Kuwait", "Laos", "Latvia", "Lebanon", "Libya", "Lithuania",
                                "Luxembourg", "Madagascar", "Maldives", "Mali", "Malta", "Mauritius", "Mexico", "Moldova",
                                "Monaco", "Mongolia", "Morocco", "Myanmar", "Nepal", "Netherlands", "New Zealand", "Nicaragua",
                                "Nigeria", "Norway", "Oman", "Pakistan", "Palestine", "Panama", "Paraguay", "Peru", "Poland",
                                "Portugal", "Qatar", "Romania", "Russia", "Rwanda", "Senegal", "Serbia", "Seychelles", "Slovakia",
                                "Slovenia", "South Korea", "Spain", "Sudan", "Sweden", "Switzerland", "Taiwan", "Tanzania",
                                "Tunisia", "Turkey", "Uganda", "Ukraine", "Uruguay", "Uzbekistan", "Venezuela", "Yemen", "Zambia", "Zimbabwe"
                            ).sorted()
                        }

                        var countryExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = countryExpanded,
                            onExpandedChange = { countryExpanded = !countryExpanded }
                        ) {
                            OutlinedTextField(
                                value = countryInput,
                                onValueChange = {
                                    countryInput = it
                                    countryExpanded = true
                                },
                                label = { Text(strings.countryLabel) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryExpanded) },
                                shape = RoundedCornerShape(10.dp)
                            )

                            val filteredCountries = worldCountries.filter {
                                it.contains(countryInput, ignoreCase = true)
                            }

                            if (filteredCountries.isNotEmpty()) {
                                ExposedDropdownMenu(
                                    expanded = countryExpanded,
                                    onDismissRequest = { countryExpanded = false }
                                ) {
                                    filteredCountries.take(20).forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                countryInput = item
                                                countryExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // State Dropdown
                        val popularStatesMap = remember {
                            mapOf(
                                "India" to listOf("Tamil Nadu", "Kerala", "Karnataka", "Andhra Pradesh", "Telangana", "Maharashtra", "Delhi", "Gujarat", "West Bengal", "Punjab"),
                                "Malaysia" to listOf("Kuala Lumpur", "Selangor", "Johor", "Penang", "Perak", "Kedah", "Sabah", "Sarawak", "Melaka", "Pahang"),
                                "United States" to listOf("California", "Texas", "New York", "Florida", "Illinois", "Pennsylvania", "Ohio", "Georgia", "Washington"),
                                "United Kingdom" to listOf("England", "Scotland", "Wales", "Northern Ireland")
                            )
                        }

                        var stateExpanded by remember { mutableStateOf(false) }
                        val currentSuggestedStates = popularStatesMap[countryInput] ?: emptyList()

                        ExposedDropdownMenuBox(
                            expanded = stateExpanded,
                            onExpandedChange = { stateExpanded = !stateExpanded }
                        ) {
                            OutlinedTextField(
                                value = stateInput,
                                onValueChange = {
                                    stateInput = it
                                    stateExpanded = true
                                },
                                label = { Text(strings.stateLabel) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateExpanded) },
                                shape = RoundedCornerShape(10.dp)
                            )

                            val filteredStates = currentSuggestedStates.filter {
                                it.contains(stateInput, ignoreCase = true)
                            }

                            if (filteredStates.isNotEmpty()) {
                                ExposedDropdownMenu(
                                    expanded = stateExpanded,
                                    onDismissRequest = { stateExpanded = false }
                                ) {
                                    filteredStates.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                stateInput = item
                                                stateExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // City Dropdown
                        val popularCitiesMap = remember {
                            mapOf(
                                "Tamil Nadu" to listOf(
                                    "Ariyalur", "Chengalpattu", "Chennai", "Coimbatore", "Cuddalore", "Dharmapuri",
                                    "Dindigul", "Erode", "Kallakurichi", "Kanchipuram", "Kanyakumari (Nagercoil)", "Karur",
                                    "Krishnagiri", "Madurai", "Mayiladuthurai", "Nagapattinam", "Namakkal", "Nilgiris (Ooty)",
                                    "Perambalur", "Pudukkottai", "Ramanathapuram", "Ranipet", "Salem", "Sivaganga",
                                    "Tenkasi", "Thanjavur", "Theni", "Thoothukudi (Tuticorin)", "Tiruchirappalli (Trichy)",
                                    "Tirunelveli", "Tirupathur", "Tiruppur", "Tiruvallur", "Tiruvannamalai", "Tiruvarur",
                                    "Vellore", "Viluppuram", "Virudhunagar"
                                ),
                                "Kuala Lumpur" to listOf("Kuala Lumpur"),
                                "Selangor" to listOf("Petaling Jaya", "Shah Alam", "Subang Jaya", "Klang"),
                                "California" to listOf("Los Angeles", "San Francisco", "San Diego", "San Jose"),
                                "New York" to listOf("New York City", "Buffalo", "Rochester"),
                                "England" to listOf("London", "Manchester", "Birmingham", "Liverpool", "Leeds")
                            )
                        }

                        var cityExpanded by remember { mutableStateOf(false) }
                        val currentSuggestedCities = popularCitiesMap[stateInput] ?: emptyList()

                        ExposedDropdownMenuBox(
                            expanded = cityExpanded,
                            onExpandedChange = { cityExpanded = !cityExpanded }
                        ) {
                            OutlinedTextField(
                                value = cityInput,
                                onValueChange = {
                                    cityInput = it
                                    cityExpanded = true
                                },
                                label = { Text(strings.cityLabel) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                                shape = RoundedCornerShape(10.dp)
                            )

                            val filteredCities = currentSuggestedCities.filter {
                                it.contains(cityInput, ignoreCase = true)
                            }

                            if (filteredCities.isNotEmpty()) {
                                ExposedDropdownMenu(
                                    expanded = cityExpanded,
                                    onDismissRequest = { cityExpanded = false }
                                ) {
                                    filteredCities.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                cityInput = item
                                                cityExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                repository.updateUserProfile(
                                    nameInput.trim(),
                                    phoneInput.trim(),
                                    countryInput.trim(),
                                    stateInput.trim(),
                                    cityInput.trim()
                                )
                                android.widget.Toast.makeText(
                                    context,
                                    if (currentLang == com.example.trcchart.data.AppLanguage.TAMIL) "விவரங்கள் புதுப்பிக்கப்பட்டன" else "Profile Updated",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.save, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            // Section 1: Backup & Restore Card
            item {
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
                        Text(
                            text = "Backup & Restore / காப்புப்பிரதி",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SaffronPrimary
                        )
                        Text(
                            text = "Backup or restore your feelings, daily logs, and settings to/from your local phone storage.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                    createBackupLauncher.launch("TRC_Chart_Backup_$dateStr.json")
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = "Backup", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Backup", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    openBackupLauncher.launch(arrayOf("application/json", "*/*"))
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronPrimary)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = "Restore", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }

                        // Privacy Policy Note Banner inside Backup section
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = SaffronPrimary.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Privacy Policy",
                                    tint = SaffronPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = if (currentLang == AppLanguage.TAMIL) "தனியுரிமை குறிப்பு / Privacy Policy Note" else "Privacy Policy Note / தனியுரிமை குறிப்பு",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronPrimary
                                    )
                                    Text(
                                        text = if (currentLang == AppLanguage.TAMIL)
                                            "அனைத்து தரவுகளும் உங்கள் போனில் மட்டுமே சேமிக்கப்படும், மேகக்கணியில் (Cloud) சேமிக்கப்படாது. போனுக்கு வெளியே யாருக்கும் இது கிடைக்காது.\n(All data is stored locally on the phone and not stored in cloud. It won't be accessible to any one outside the phone.)"
                                        else
                                            "All data is stored locally on the phone and not stored in cloud. It won't be accessible to any one outside the phone.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section: Privacy & Cloud Control Toggle
            item {
                var isCloudEnabled by remember { mutableStateOf(TelemetryService.isCloudSyncEnabled()) }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isCloudEnabled) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                                contentDescription = "Privacy & Cloud Control",
                                tint = if (isCloudEnabled) SaffronPrimary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = strings.privacySectionTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCloudEnabled) SaffronPrimary else MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            text = strings.privacySectionSub,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = strings.cloudSyncLabel,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isCloudEnabled) strings.cloudSyncSubOn else strings.cloudSyncSubOff,
                                    fontSize = 12.sp,
                                    color = if (isCloudEnabled) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f) else MaterialTheme.colorScheme.error
                                )
                            }

                            Switch(
                                checked = isCloudEnabled,
                                onCheckedChange = { newState ->
                                    isCloudEnabled = newState
                                    TelemetryService.setCloudSyncEnabled(newState)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SaffronPrimary,
                                    checkedTrackColor = SaffronPrimary.copy(alpha = 0.3f),
                                    uncheckedThumbColor = MaterialTheme.colorScheme.error,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }
            }

            // Section: Admin Dashboard Link
            item {
                val adminDashboardUrl = "https://trc-chart-analytics.web.app"

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Admin Dashboard",
                                tint = SaffronPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Admin Analytics Dashboard / நிருவாகி பக்கம்",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary
                            )
                        }

                        Text(
                            text = "Access live telemetry stats, active audience reach, and user activity dashboard.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(adminDashboardUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Launch Admin Dashboard",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Admin Dashboard (Web)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }


            // Section 2: Language Toggle
            item {
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
                        Text(
                            text = strings.languageSectionTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.languageSectionSub,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        // Language Toggle Switch Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(25.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(25.dp)
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // English Option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(21.dp))
                                    .background(if (currentLang == AppLanguage.ENGLISH) SaffronPrimary else Color.Transparent)
                                    .clickable { repository.setLanguage(AppLanguage.ENGLISH) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "English",
                                    color = if (currentLang == AppLanguage.ENGLISH) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            // Tamil Option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(21.dp))
                                    .background(if (currentLang == AppLanguage.TAMIL) SaffronPrimary else Color.Transparent)
                                    .clickable { repository.setLanguage(AppLanguage.TAMIL) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "தமிழ் (Tamil)",
                                    color = if (currentLang == AppLanguage.TAMIL) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Daily Log Section Visibility Settings
            item {
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
                            text = "Daily Log Sections / தினசரி பகுதிகள்",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Hide sections that are not applicable to customize your daily log.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        HorizontalDivider()

                        // Meditation Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "MEDITATION / தியானம் (2 Items)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Switch(
                                checked = showMeditation,
                                onCheckedChange = { repository.setMeditationVisibility(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SaffronPrimary)
                            )
                        }

                        // Cleaning Process Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CLEANING PROCESS / சுத்திகரிப்பு (2 Items)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Switch(
                                checked = showCleaning,
                                onCheckedChange = { repository.setCleaningVisibility(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SaffronPrimary)
                            )
                        }

                        // Section 1 Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. LOVE / அன்பு (9 Items)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Switch(
                                checked = showSec1,
                                onCheckedChange = { repository.setSection1Visibility(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SaffronPrimary)
                            )
                        }

                        // Section 2 Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "2. HUSBAND & WIFE / கணவன் மனைவி (6 Items)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Switch(
                                checked = showSec2,
                                onCheckedChange = { repository.setSection2Visibility(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SaffronPrimary)
                            )
                        }

                        // Section 3 Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "3. ATTITUDE & QUALITIES / மனப்பாங்கு (5 Items)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Switch(
                                checked = showSec3,
                                onCheckedChange = { repository.setSection3Visibility(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = SaffronPrimary)
                            )
                        }
                    }
                }
            }

            // Section 4: Manage Feelings List Header
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = strings.feelingsSectionTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = strings.feelingsSectionSub,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // Section 4: Feelings Items
            itemsIndexed(feelingsList) { index, feeling ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = feeling,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f).padding(end = 4.dp)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { repository.moveFeelingUp(index) },
                                enabled = index > 0
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Move Up",
                                    tint = if (index > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }

                            IconButton(
                                onClick = { repository.moveFeelingDown(index) },
                                enabled = index < feelingsList.size - 1
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Move Down",
                                    tint = if (index < feelingsList.size - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }

                            IconButton(
                                onClick = {
                                    inputText = feeling
                                    showEditDialog = feeling
                                }
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = {
                                    showDeleteDialog = feeling
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Add Feeling Button at Bottom
            item {
                Button(
                    onClick = {
                        inputText = ""
                        showAddDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Feeling",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${strings.addNewFeelingTitle} / உணர்வைச் சேர்க்க",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Add Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(strings.addNewFeelingTitle) },
            text = {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text(strings.feelingNameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            repository.addFeeling(inputText)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(strings.add)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Edit Dialog
    showEditDialog?.let { targetFeeling ->
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text(strings.edit) },
            text = {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text(strings.feelingNameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            repository.editFeeling(targetFeeling, inputText)
                            showEditDialog = null
                        }
                    }
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Delete Dialog
    showDeleteDialog?.let { targetFeeling ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(strings.delete) },
            text = { Text("Are you sure you want to delete '$targetFeeling'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.removeFeeling(targetFeeling)
                        showDeleteDialog = null
                    }
                ) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

private fun saveBackupToUri(context: Context, repository: FeelingsRepository, uri: Uri) {
    try {
        val jsonStr = repository.exportBackupJson()
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(jsonStr.toByteArray(Charsets.UTF_8))
        }
        Toast.makeText(context, "Backup saved successfully to Google Drive / Storage!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to save backup: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun restoreBackupFromUri(context: Context, repository: FeelingsRepository, uri: Uri) {
    try {
        val jsonStr = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader(Charsets.UTF_8).readText()
        }
        if (jsonStr != null && repository.restoreBackupJson(jsonStr)) {
            Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Invalid backup file format.", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to restore backup: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
