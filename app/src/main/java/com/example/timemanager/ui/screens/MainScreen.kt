package com.example.timemanager.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.timemanager.ui.components.EventComposerSheet
import com.example.timemanager.ui.theme.DarkBorder
import com.example.timemanager.ui.theme.IndigoPrimary
import com.example.timemanager.ui.theme.PinkAccent
import com.example.timemanager.ui.viewmodel.AppViewTab
import com.example.timemanager.ui.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: EventViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var eventToDeleteId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search events, notes…", fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp)
                                .testTag("search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(IndigoPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "TimeManager Pro",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Gregorian & Jalali Planner",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearchExpanded = !isSearchExpanded
                            if (!isSearchExpanded) {
                                viewModel.setSearchQuery("")
                            }
                        },
                        modifier = Modifier.testTag("toggle_search_btn")
                    ) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = if (isSearchExpanded) "Close search" else "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = uiState.activeTab == AppViewTab.LIST,
                    onClick = { viewModel.setActiveTab(AppViewTab.LIST) },
                    icon = {
                        Icon(imageVector = Icons.Default.FormatListBulleted, contentDescription = "List")
                    },
                    label = { Text("List", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = IndigoPrimary,
                        indicatorColor = IndigoPrimary
                    ),
                    modifier = Modifier.testTag("tab_list")
                )

                NavigationBarItem(
                    selected = uiState.activeTab == AppViewTab.MONTH,
                    onClick = { viewModel.setActiveTab(AppViewTab.MONTH) },
                    icon = {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Month")
                    },
                    label = { Text("Month", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = IndigoPrimary,
                        indicatorColor = IndigoPrimary
                    ),
                    modifier = Modifier.testTag("tab_month")
                )

                NavigationBarItem(
                    selected = uiState.activeTab == AppViewTab.COUNTDOWN,
                    onClick = { viewModel.setActiveTab(AppViewTab.COUNTDOWN) },
                    icon = {
                        Icon(imageVector = Icons.Default.HourglassTop, contentDescription = "Countdowns")
                    },
                    label = { Text("Countdown", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = IndigoPrimary,
                        indicatorColor = IndigoPrimary
                    ),
                    modifier = Modifier.testTag("tab_countdown")
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openComposer(null) },
                containerColor = IndigoPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_event")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState.activeTab) {
                AppViewTab.LIST -> {
                    EventListScreen(
                        events = uiState.filteredEvents,
                        stats = uiState.stats,
                        selectedFilter = uiState.selectedFilter,
                        searchQuery = uiState.searchQuery,
                        onFilterSelected = { viewModel.setFilter(it) },
                        onOpenComposer = { viewModel.openComposer(it) },
                        onDeleteEvent = { eventToDeleteId = it },
                        onTogglePin = { id, pinned -> viewModel.togglePin(id, pinned) },
                        onToggleChecklistItem = { event, id -> viewModel.toggleChecklistItem(event, id) },
                        onAddChecklistItem = { event, text -> viewModel.addChecklistItem(event, text) },
                        onRemoveChecklistItem = { event, id -> viewModel.removeChecklistItem(event, id) }
                    )
                }

                AppViewTab.MONTH -> {
                    CalendarMonthScreen(
                        allEvents = uiState.allEvents,
                        calendarSystem = uiState.calendarSystem,
                        selectedDateIso = uiState.selectedDate,
                        onDateSelected = { viewModel.setSelectedDate(it) },
                        onToggleCalendarSystem = { viewModel.toggleCalendarSystem() },
                        onOpenComposerWithDate = { date ->
                            viewModel.setSelectedDate(date)
                            viewModel.openComposer(null)
                        },
                        onOpenComposerForEdit = { viewModel.openComposer(it) },
                        onDeleteEvent = { eventToDeleteId = it },
                        onTogglePin = { id, pinned -> viewModel.togglePin(id, pinned) },
                        onToggleChecklistItem = { event, id -> viewModel.toggleChecklistItem(event, id) },
                        onAddChecklistItem = { event, text -> viewModel.addChecklistItem(event, text) },
                        onRemoveChecklistItem = { event, id -> viewModel.removeChecklistItem(event, id) }
                    )
                }

                AppViewTab.COUNTDOWN -> {
                    CountdownScreen(
                        allEvents = uiState.allEvents,
                        onEventClick = { viewModel.openComposer(it) }
                    )
                }
            }
        }
    }

    // Composer Sheet
    if (uiState.isComposerOpen) {
        EventComposerSheet(
            event = uiState.editingEvent,
            onDismiss = { viewModel.closeComposer() },
            onSave = { id, title, date, isAllDay, timeHm, category, repeat, repeatUntil, leadRepeat, note, isPinned, checklist ->
                viewModel.saveEvent(
                    id = id,
                    title = title,
                    date = date,
                    isAllDay = isAllDay,
                    timeHm = timeHm,
                    category = category,
                    repeat = repeat,
                    repeatUntil = repeatUntil,
                    leadRepeat = leadRepeat,
                    note = note,
                    isPinned = isPinned,
                    checklist = checklist,
                    reminders = emptyList()
                )
            }
        )
    }

    // Delete Confirmation Dialog
    eventToDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { eventToDeleteId = null },
            title = { Text("Delete Event?") },
            text = { Text("Are you sure you want to delete this event? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEvent(id)
                        eventToDeleteId = null
                    }
                ) {
                    Text("Delete", color = PinkAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToDeleteId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
