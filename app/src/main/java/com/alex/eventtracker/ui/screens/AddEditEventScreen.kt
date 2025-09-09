// File: AddEditEventScreen.kt

package com.alex.eventtracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alex.eventtracker.data.model.Event
import com.alex.eventtracker.data.model.EventType
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEventScreen(
    navController: NavController,
    viewModel: EventsViewModel,  // ← Passed in, not created here
    eventId: Long? = null
) {
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("") }
    var selectedDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var eventType by rememberSaveable { mutableStateOf(EventType.BIRTHDAY) }
    var yearKnown by rememberSaveable { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Load event if editing
    val event by viewModel.getEventById(eventId ?: -1).collectAsState(initial = null)

    // Populate fields if editing
    LaunchedEffect(event) {
        event?.let { nonNullEvent ->
            name = nonNullEvent.name
            selectedDate = nonNullEvent.date
            eventType = nonNullEvent.type
            yearKnown = nonNullEvent.yearKnown
        }
    }

    // Reset form when switching from edit to add
    LaunchedEffect(eventId) {
        if (eventId == null) {
            name = ""
            selectedDate = null
            eventType = EventType.BIRTHDAY
            yearKnown = true
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (eventId == null) "Add Event" else "Edit Event") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Date picker
            Text(
                text = "Date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate?.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")) ?: "Select a date",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        Button(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                            showDatePicker = false
                        }) {
                            Text("OK")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Event type
            Text(
                text = "Event Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = eventType == EventType.BIRTHDAY,
                    onClick = { eventType = EventType.BIRTHDAY }
                )
                Text(
                    text = "Birthday",
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                RadioButton(
                    selected = eventType == EventType.ANNIVERSARY,
                    onClick = { eventType = EventType.ANNIVERSARY }
                )
                Text(
                    text = "Anniversary",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Year known checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = yearKnown,
                    onCheckedChange = { yearKnown = it }
                )
                Text(
                    text = "Year known",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            Button(
                onClick = {
                    if (name.isNotBlank() && selectedDate != null) {
                        scope.launch {
                            val eventToSave = event?.copy(
                                name = name,
                                date = selectedDate!!,
                                type = eventType,
                                yearKnown = yearKnown
                            ) ?: Event(
                                name = name,
                                date = selectedDate!!,
                                type = eventType,
                                yearKnown = yearKnown
                            )

                            if (eventId == null) {
                                viewModel.insertEvent(eventToSave)
                            } else {
                                viewModel.updateEvent(eventToSave)
                            }

                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && selectedDate != null
            ) {
                Text("Save")
            }
        }
    }
}