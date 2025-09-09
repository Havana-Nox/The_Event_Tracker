package com.alex.eventtracker.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alex.eventtracker.data.model.Event
import com.alex.eventtracker.data.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class EventsViewModel(
    private val repository: EventRepository
) : ViewModel() {

    val allEvents: Flow<List<Event>> = repository.getAllEvents()

    fun insertEvent(event: Event) {
        viewModelScope.launch {
            repository.insertEvent(event)
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun getEventById(id: Long): Flow<Event?> = repository.getEventById(id)

}