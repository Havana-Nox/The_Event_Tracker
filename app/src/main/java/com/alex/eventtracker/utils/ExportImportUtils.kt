package com.alex.eventtracker.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.alex.eventtracker.data.model.Event
import com.alex.eventtracker.data.model.EventType
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ExportImportUtils {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, LocalDateSerializer())
        .registerTypeAdapter(EventType::class.java, EventTypeSerializer())
        .setPrettyPrinting()
        .create()

    /**
     * Export events to: Documents/Events/Events_YYYY-MM-DD.json
     * probably should add a timestamp too, in case of multiple exports on the same day
     * who the hell would export more than once anyway?
     */
    suspend fun exportEventsToJson(context: Context, events: List<Event>): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                // Get Documents/Events/ directory
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val eventsDir = File(documentsDir, "Events")
                if (!eventsDir.exists()) {
                    eventsDir.mkdirs()
                }

                // Create filename with date: Events_2025-04-05.json
                val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val file = File(eventsDir, "Events_$date.json")

                // Convert events to JSON
                val json = gson.toJson(events)

                // Write to file
                FileOutputStream(file).use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(json)
                        writer.flush()
                    }
                }

                Result.success(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }

    /**
     * Imports events from a JSON file.
     * Does NOT delete existing events - version 77. It will work this time. I think.
     */
    suspend fun importEventsFromJson(context: Context, uri: Uri): Result<List<Event>> =
        withContext(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        reader.readText()
                    }
                } ?: return@withContext Result.failure(Exception("Could not open file input stream"))

                val type = object : TypeToken<List<Event>>() {}.type
                val events: List<Event> = gson.fromJson(jsonString, type)

                Result.success(events)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }

    // Serializer for LocalDate
    private class LocalDateSerializer : com.google.gson.JsonSerializer<LocalDate>, com.google.gson.JsonDeserializer<LocalDate> {
        private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        override fun serialize(src: LocalDate?, typeOfSrc: java.lang.reflect.Type?, context: com.google.gson.JsonSerializationContext?) =
            com.google.gson.JsonPrimitive(formatter.format(src))

        override fun deserialize(json: com.google.gson.JsonElement?, typeOfT: java.lang.reflect.Type?, context: com.google.gson.JsonDeserializationContext?) =
            LocalDate.parse(json?.asString, formatter)
    }

    // Serializer for EventType
    private class EventTypeSerializer : com.google.gson.JsonSerializer<EventType>, com.google.gson.JsonDeserializer<EventType> {
        override fun serialize(src: EventType?, typeOfSrc: java.lang.reflect.Type?, context: com.google.gson.JsonSerializationContext?) =
            com.google.gson.JsonPrimitive(src?.name)

        override fun deserialize(json: com.google.gson.JsonElement?, typeOfT: java.lang.reflect.Type?, context: com.google.gson.JsonDeserializationContext?) =
            EventType.valueOf(json?.asString ?: "BIRTHDAY")
    }
}