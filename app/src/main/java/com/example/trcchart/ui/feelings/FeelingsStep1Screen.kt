package com.example.trcchart.ui.feelings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trcchart.data.FeelingsRepository
import com.example.trcchart.theme.SaffronPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeelingsStep1Screen(
    repository: FeelingsRepository,
    onNext: (selectedFeeling: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val feelingsList by repository.feelings.collectAsState()
    var selectedFeeling by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newFeelingText by remember { mutableStateOf("") }

    LaunchedEffect(feelingsList) {
        if (selectedFeeling.isEmpty() && feelingsList.isNotEmpty()) {
            selectedFeeling = feelingsList.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Feeling", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Friend",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedCard(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedFeeling.isNotEmpty()) selectedFeeling else "Select a feeling",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown"
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.75f)
                        ) {
                            feelingsList.forEach { feeling ->
                                DropdownMenuItem(
                                    text = { Text(feeling, fontSize = 16.sp) },
                                    onClick = {
                                        selectedFeeling = feeling
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Plus button to add custom feeling directly
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = SaffronPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Feeling")
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedFeeling.isNotBlank()) {
                        onNext(selectedFeeling)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                enabled = selectedFeeling.isNotBlank()
            ) {
                Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Feeling") },
            text = {
                OutlinedTextField(
                    value = newFeelingText,
                    onValueChange = { newFeelingText = it },
                    label = { Text("Feeling Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFeelingText.isNotBlank()) {
                            val added = repository.addFeeling(newFeelingText)
                            if (added) {
                                selectedFeeling = newFeelingText.trim()
                            }
                            newFeelingText = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
