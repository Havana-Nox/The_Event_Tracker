package com.alex.eventtracker.data.repository

import com.alex.eventtracker.data.dao.EventDao
import com.alex.eventtracker.data.model.Event
import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val eventDao: EventDao
) {
    fun getAllEvents(): Flow<List<Event>> = eventDao.getAllEvents()

    suspend fun insertEvent(event: Event) = eventDao.insertEvent(event)

    suspend fun updateEvent(event: Event) = eventDao.updateEvent(event)

    suspend fun deleteEvent(event: Event) = eventDao.deleteEvent(event)

    fun getEventById(id: Long): Flow<Event?> = eventDao.getEventById(id)

    suspend fun getEventsByIds(ids: List<Long>) = eventDao.getEventsByIds(ids)

    suspend fun deleteAllEvents() = eventDao.deleteAllEvents()
}