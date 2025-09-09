// File: MainActivity.kt

package com.alex.eventtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alex.eventtracker.data.database.AppDatabase
import com.alex.eventtracker.data.repository.EventRepository
import com.alex.eventtracker.ui.screens.AddEditEventScreen
import com.alex.eventtracker.ui.screens.EventsViewModel
import com.alex.eventtracker.ui.screens.SettingsScreen
import com.alex.eventtracker.ui.screens.UpcomingEventsScreen
import com.alex.eventtracker.ui.theme.BirthdayAnniversaryTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and repository
        val database = AppDatabase.getDatabase(this)
        val repository = EventRepository(database.eventDao())
        val viewModel = EventsViewModel(repository)

        setContent {
            BirthdayAnniversaryTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BirthdayApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BirthdayApp(viewModel: EventsViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "upcoming_events"
    ) {
        composable("upcoming_events") {
            UpcomingEventsScreen(navController = navController, viewModel = viewModel)
        }
        composable("add_edit") {
            AddEditEventScreen(navController = navController, viewModel = viewModel)
        }
        composable("add_edit/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")?.toLong()
            AddEditEventScreen(navController = navController, viewModel = viewModel, eventId = eventId)
        }
        composable("settings") {
            SettingsScreen(navController = navController, viewModel = viewModel)
        }
    }
}