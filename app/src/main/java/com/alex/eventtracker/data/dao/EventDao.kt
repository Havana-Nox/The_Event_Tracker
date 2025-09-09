package com.alex.eventtracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alex.eventtracker.data.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY name")
    fun getAllEvents(): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()

    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventById(id: Long): Flow<Event?>  // Changed from suspend Event? to Flow<Event?>

    @Query("SELECT * FROM events WHERE id IN (:ids)")
    suspend fun getEventsByIds(ids: List<Long>): List<Event>
}