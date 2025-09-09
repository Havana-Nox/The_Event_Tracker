package com.alex.eventtracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alex.eventtracker.utils.ExportImportUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: EventsViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val events by viewModel.allEvents.collectAsState(initial = emptyList())

    var showExportSuccess by remember { mutableStateOf(false) }
    var exportPath by remember { mutableStateOf("") }
    var showImportConfirm by remember { mutableStateOf(false) }
    var importUri by remember { mutableStateOf<Uri?>(null) }
    var showImportResult by remember { mutableStateOf(false) }
    var importResultMessage by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            importUri = it
            showImportConfirm = true
        }
    }

    // Auto-hide export success
    LaunchedEffect(showExportSuccess) {
        if (showExportSuccess) {
            delay(3000)
            showExportSuccess = false
        }
    }

    // Auto-hide import result
    LaunchedEffect(showImportResult) {
        if (showImportResult) {
            delay(3000)
            showImportResult = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Data Management Section
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    scope.launch {
                        val result = ExportImportUtils.exportEventsToJson(context, events)
                        result.onSuccess { path ->
                            exportPath = path
                            showExportSuccess = true
                        }.onFailure { e ->
                            importResultMessage = "Export failed: ${e.message}"
                            showImportResult = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Text("Export Data")
            }

            Button(
                onClick = {
                    filePickerLauncher.launch(arrayOf("application/json"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Text("Import Data")
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // About Section
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Birthday & Anniversary Tracker v1.73",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Track important dates and never forget a birthday or anniversary again.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Export Success Dialog
        if (showExportSuccess) {
            AlertDialog(
                onDismissRequest = { showExportSuccess = false },
                title = { Text("Export Successful") },
                text = { Text("Data exported to:\n$exportPath") },
                confirmButton = {
                    Button(onClick = { showExportSuccess = false }) {
                        Text("OK")
                    }
                }
            )
        }

        // Import Confirm Dialog
        if (showImportConfirm && importUri != null) {
            AlertDialog(
                onDismissRequest = { showImportConfirm = false },
                title = { Text("Confirm Import") },
                text = { Text("This will import all data. Are you sure?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val result = ExportImportUtils.importEventsFromJson(context, importUri!!)
                                result.onSuccess { importedEvents ->
                                    importedEvents.forEach { newEvent ->
                                        val alreadyExists = events.any { existing ->
                                            existing.name == newEvent.name && existing.date == newEvent.date
                                        }
                                        if (!alreadyExists) {
                                            viewModel.insertEvent(newEvent)
                                        }
                                    }

                                    importResultMessage = "Import successful: ${importedEvents.size} events imported"
                                    showImportResult = true
                                }.onFailure { e ->
                                    importResultMessage = "Import failed: ${e.message}"
                                    showImportResult = true
                                }

                                showImportConfirm = false
                                importUri = null
                            }
                        }
                    ) {
                        Text("Import")
                    }
                },
                dismissButton = {
                    Button(onClick = {
                        showImportConfirm = false
                        importUri = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Import Result Dialog
        if (showImportResult) {
            AlertDialog(
                onDismissRequest = { showImportResult = false },
                title = { Text("Import Result") },
                text = { Text(importResultMessage) },
                confirmButton = {
                    Button(onClick = { showImportResult = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}