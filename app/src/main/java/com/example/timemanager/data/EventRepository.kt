package com.example.timemanager.data

import com.example.timemanager.model.ChecklistItem
import com.example.timemanager.model.EventEntity
import kotlinx.coroutines.flow.Flow

class EventRepository(private val eventDao: EventDao) {

    val allEvents: Flow<List<EventEntity>> = eventDao.getAllEvents()

    fun getEventById(id: Long): Flow<EventEntity?> = eventDao.getEventById(id)

    suspend fun insertEvent(event: EventEntity): Long = eventDao.insertEvent(event)

    suspend fun updateEvent(event: EventEntity) = eventDao.updateEvent(event)

    suspend fun deleteEvent(id: Long) = eventDao.deleteEventById(id)

    suspend fun setPinned(id: Long, pinned: Boolean) = eventDao.setPinned(id, pinned)

    suspend fun updateChecklist(id: Long, items: List<ChecklistItem>) {
        val json = JsonConverters.checklistToJson(items)
        eventDao.updateChecklist(id, json)
    }

    suspend fun updateNote(id: Long, note: String) = eventDao.updateNote(id, note)
}
