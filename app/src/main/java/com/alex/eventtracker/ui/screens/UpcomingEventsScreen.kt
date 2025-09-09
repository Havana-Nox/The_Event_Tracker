package com.alex.eventtracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.alex.eventtracker.data.model.Event
import com.alex.eventtracker.data.model.EventType
import com.alex.eventtracker.ui.theme.EventCardTheme
import com.alex.eventtracker.ui.theme.ThreeDaysOrange
import com.alex.eventtracker.ui.theme.TodayRed
import com.alex.eventtracker.ui.theme.TomorrowRed
import com.alex.eventtracker.ui.theme.TwoDaysOrange
import com.alex.eventtracker.utils.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingEventsScreen(
    navController: NavController,
    viewModel: EventsViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val events by viewModel.allEvents.collectAsState(initial = emptyList())

    // Sort events by days until next occurrence
    val sortedEvents = remember(events) {
        events.sortedBy { event ->
            DateUtils.calculateDaysUntilNextEvent(event)
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<Event?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Events") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_edit") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Event")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }



    ) { paddingValues ->
        Image(
            painter = painterResource(com.alex.eventtracker.R.drawable.confetti),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.30f), // very faint
            contentScale = ContentScale.Crop
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            state = rememberLazyListState(),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(
                items = sortedEvents,
                key = { event -> event.id }
            ) { event ->
                EventRow(
                    event = event,
                    onEdit = {
                        navController.navigate("add_edit/${event.id}")
                    },
                    onDelete = {
                        eventToDelete = event
                        showDeleteDialog = true
                    }
                )
            }
        }

        if (sortedEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(com.alex.eventtracker.R.drawable.cake),
                        contentDescription = "No events",
                        modifier = Modifier.fillMaxWidth(),
                        alpha = 0.3f
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "No upcoming events",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Delete confirmation dialog
        if (showDeleteDialog && eventToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Event") },
                text = { Text("Are you sure you want to delete ${eventToDelete?.name}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                eventToDelete?.let { event ->
                                    viewModel.deleteEvent(event)
                                    snackbarHostState.showSnackbar("Event deleted")
                                }
                                showDeleteDialog = false
                            }
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventRow(
    event: Event,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val daysUntil = DateUtils.calculateDaysUntilNextEvent(event)
    val age = DateUtils.calculateAge(event)


    var expanded by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        if (expanded) 6.dp else 1.dp, label = "elevation"
    )



    Card(
        shape = EventCardTheme.cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(0.98f)),
        elevation = CardDefaults.cardElevation(elevation),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), // very subtle
                shape = EventCardTheme.cardShape
            )
    ) {
        Column(
            Modifier
                .clickable { expanded = !expanded }
                .padding(16.dp)
                .background(
                    brush = SolidColor(MaterialTheme.colorScheme.surfaceContainer.copy(0.98f)),
                    shape = EventCardTheme.cardShape
                )
        ) {
            // ----- top line -----
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (event.type == EventType.BIRTHDAY) "Birthday" else "Anniversary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    (if (age != null) "$age  " else "") +
                            when (daysUntil) {
                                0L    -> "Today"
                                1L    -> "Tomorrow"
                                else  -> "In $daysUntil days"
                            },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (daysUntil) {
                        0L -> TodayRed
                        1L -> TomorrowRed
                        2L -> TwoDaysOrange
                        3L -> ThreeDaysOrange
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            // ----- actions -----
            AnimatedVisibility(visible = expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}