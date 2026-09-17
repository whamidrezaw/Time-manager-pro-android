package com.example.timemanager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.timemanager.data.EventRepository
import com.example.timemanager.data.JsonConverters
import com.example.timemanager.model.ChecklistItem
import com.example.timemanager.model.EventEntity
import com.example.timemanager.model.ReminderSpec
import com.example.timemanager.util.JalaliCalendarHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppViewTab {
    LIST,
    MONTH,
    COUNTDOWN
}

enum class CalendarSystem {
    GREGORIAN,
    JALALI
}

data class EventStats(
    val total: Int = 0,
    val upcoming: Int = 0,
    val today: Int = 0,
    val pinned: Int = 0
)

data class EventUiState(
    val allEvents: List<EventEntity> = emptyList(),
    val filteredEvents: List<EventEntity> = emptyList(),
    val selectedFilter: String = "all",
    val searchQuery: String = "",
    val activeTab: AppViewTab = AppViewTab.LIST,
    val calendarSystem: CalendarSystem = CalendarSystem.GREGORIAN,
    val selectedDate: String = JalaliCalendarHelper.getTodayGregorian(),
    val stats: EventStats = EventStats(),
    val editingEvent: EventEntity? = null,
    val isComposerOpen: Boolean = false
)

class EventViewModel(application: Application, private val repository: EventRepository) : AndroidViewModel(application) {

    private val _selectedFilter = MutableStateFlow("all")
    private val _searchQuery = MutableStateFlow("")
    private val _activeTab = MutableStateFlow(AppViewTab.LIST)
    private val _calendarSystem = MutableStateFlow(CalendarSystem.GREGORIAN)
    private val _selectedDate = MutableStateFlow(JalaliCalendarHelper.getTodayGregorian())
    private val _editingEvent = MutableStateFlow<EventEntity?>(null)
    private val _isComposerOpen = MutableStateFlow(false)

    private val _filterState = combine(
        repository.allEvents,
        _selectedFilter,
        _searchQuery
    ) { events, filter, query ->
        Triple(events, filter, query)
    }

    private val _navigationState = combine(
        _activeTab,
        _calendarSystem,
        _selectedDate
    ) { tab, calSys, date ->
        Triple(tab, calSys, date)
    }

    private val _dialogState = combine(
        _editingEvent,
        _isComposerOpen
    ) { editing, isOpen ->
        Pair(editing, isOpen)
    }

    val uiState: StateFlow<EventUiState> = combine(
        _filterState,
        _navigationState,
        _dialogState
    ) { filterState, navState, dialogState ->
        val (rawEvents, filter, query) = filterState
        val (tab, calSys, selDate) = navState
        val (editing, isComposerOpen) = dialogState

        var todayCount = 0
        var upcomingCount = 0
        var pinnedCount = 0

        for (e in rawEvents) {
            if (e.isPinned) pinnedCount++
            val cd = JalaliCalendarHelper.getCountdownText(e.date, e.timeHm, e.isAllDay)
            if (cd.diffDays == 0) todayCount++
            if (!cd.isPast) upcomingCount++
        }

        val stats = EventStats(
            total = rawEvents.size,
            upcoming = upcomingCount,
            today = todayCount,
            pinned = pinnedCount
        )

        val filtered = rawEvents.filter { event ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                event.title.contains(query, ignoreCase = true) ||
                        event.note.contains(query, ignoreCase = true) ||
                        event.category.contains(query, ignoreCase = true) ||
                        event.date.contains(query) ||
                        event.jalaliDate.contains(query)
            }

            val cd = JalaliCalendarHelper.getCountdownText(event.date, event.timeHm, event.isAllDay)

            val matchesFilter = when (filter) {
                "all" -> true
                "pinned" -> event.isPinned
                "past" -> cd.isPast && cd.diffDays != 0
                else -> event.category.equals(filter, ignoreCase = true)
            }

            matchesQuery && matchesFilter
        }

        EventUiState(
            allEvents = rawEvents,
            filteredEvents = filtered,
            selectedFilter = filter,
            searchQuery = query,
            activeTab = tab,
            calendarSystem = calSys,
            selectedDate = selDate,
            stats = stats,
            editingEvent = editing,
            isComposerOpen = isComposerOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EventUiState()
    )

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setActiveTab(tab: AppViewTab) {
        _activeTab.value = tab
    }

    fun toggleCalendarSystem() {
        _calendarSystem.value = if (_calendarSystem.value == CalendarSystem.GREGORIAN) {
            CalendarSystem.JALALI
        } else {
            CalendarSystem.GREGORIAN
        }
    }

    fun setSelectedDate(dateIso: String) {
        _selectedDate.value = dateIso
    }

    fun openComposer(event: EventEntity? = null) {
        _editingEvent.value = event
        _isComposerOpen.value = true
    }

    fun closeComposer() {
        _isComposerOpen.value = false
        _editingEvent.value = null
    }

    fun saveEvent(
        id: Long = 0,
        title: String,
        date: String,
        isAllDay: Boolean,
        timeHm: String?,
        category: String,
        repeat: String,
        repeatUntil: String?,
        leadRepeat: String,
        note: String,
        isPinned: Boolean,
        checklist: List<ChecklistItem>,
        reminders: List<ReminderSpec>
    ) {
        viewModelScope.launch {
            val jalaliDate = JalaliCalendarHelper.toJalaliString(date)
            val event = EventEntity(
                id = id,
                title = title.trim(),
                date = date,
                jalaliDate = jalaliDate,
                isAllDay = isAllDay,
                timeHm = if (!isAllDay) timeHm else null,
                category = category,
                repeat = repeat,
                repeatUntil = repeatUntil,
                leadRepeat = leadRepeat,
                note = note.trim(),
                isPinned = isPinned,
                checklistJson = JsonConverters.checklistToJson(checklist),
                remindersJson = JsonConverters.remindersToJson(reminders)
            )


            if (id == 0L) {
                val newId = repository.insertEvent(event)
                com.example.timemanager.util.AlarmScheduler.schedule(getApplication(), event.copy(id = newId))
            } else {
                repository.updateEvent(event)
                com.example.timemanager.util.AlarmScheduler.schedule(getApplication(), event)
            }

            closeComposer()
        }
    }

    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            com.example.timemanager.util.AlarmScheduler.cancel(getApplication(), id)
            repository.deleteEvent(id)
        }
    }

    fun togglePin(id: Long, currentlyPinned: Boolean) {
        viewModelScope.launch {
            repository.setPinned(id, !currentlyPinned)
        }
    }

    fun toggleChecklistItem(event: EventEntity, itemId: String) {
        viewModelScope.launch {
            val currentList = JsonConverters.jsonToChecklist(event.checklistJson)
            val updated = currentList.map { item ->
                if (item.id == itemId) item.copy(isDone = !item.isDone) else item
            }
            repository.updateChecklist(event.id, updated)
        }
    }

    fun addChecklistItem(event: EventEntity, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val currentList = JsonConverters.jsonToChecklist(event.checklistJson)
            val updated = currentList + ChecklistItem(text = text.trim(), isDone = false)
            repository.updateChecklist(event.id, updated)
        }
    }

    fun removeChecklistItem(event: EventEntity, itemId: String) {
        viewModelScope.launch {
            val currentList = JsonConverters.jsonToChecklist(event.checklistJson)
            val updated = currentList.filterNot { it.id == itemId }
            repository.updateChecklist(event.id, updated)
        }
    }

    fun updateNote(event: EventEntity, note: String) {
        viewModelScope.launch {
            repository.updateNote(event.id, note.trim())
        }
    }
}

class EventViewModelFactory(private val application: Application, private val repository: EventRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
