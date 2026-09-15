package com.example.timemanager.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timemanager.model.EventEntity
import com.example.timemanager.ui.components.EventCard
import com.example.timemanager.ui.components.FilterBar
import com.example.timemanager.ui.components.HeroPlannerCard
import com.example.timemanager.ui.theme.IndigoPrimary
import com.example.timemanager.ui.viewmodel.EventStats

@Composable
fun EventListScreen(
    events: List<EventEntity>,
    stats: EventStats,
    selectedFilter: String,
    searchQuery: String,
    onFilterSelected: (String) -> Unit,
    onOpenComposer: (EventEntity?) -> Unit,
    onDeleteEvent: (Long) -> Unit,
    onTogglePin: (Long, Boolean) -> Unit,
    onToggleChecklistItem: (EventEntity, String) -> Unit,
    onAddChecklistItem: (EventEntity, String) -> Unit,
    onRemoveChecklistItem: (EventEntity, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("event_list_screen"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero card (only show when not actively searching)
        if (searchQuery.isBlank()) {
            item {
                HeroPlannerCard(
                    stats = stats,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Category Filter Bar
        item {
            FilterBar(
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected
            )
        }

        // Empty state
        if (events.isEmpty()) {
            item {
                EmptyStateView(
                    selectedFilter = selectedFilter,
                    isSearching = searchQuery.isNotBlank(),
                    onAddClick = { onOpenComposer(null) }
                )
            }
        } else {
            items(
                items = events,
                key = { it.id }
            ) { event ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EventCard(
                        event = event,
                        onEditClick = { onOpenComposer(event) },
                        onDeleteClick = { onDeleteEvent(event.id) },
                        onTogglePin = { onTogglePin(event.id, event.isPinned) },
                        onToggleChecklistItem = { itemId -> onToggleChecklistItem(event, itemId) },
                        onAddChecklistItem = { text -> onAddChecklistItem(event, text) },
                        onRemoveChecklistItem = { itemId -> onRemoveChecklistItem(event, itemId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    selectedFilter: String,
    isSearching: Boolean,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(IndigoPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = IndigoPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSearching) "No matching events" else if (selectedFilter != "all") "No events in this category" else "No events yet!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isSearching) "Try a different search keyword or clear filters."
            else "Save your meetings, birthdays, tasks and appointments with smart Gregorian & Jalali dates.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
            modifier = Modifier.testTag("empty_add_btn")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.size(6.dp))
            Text("Add your first event", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            SuggestionChip(
                onClick = onAddClick,
                label = { Text("🎂 Birthdays", fontSize = 11.sp) },
                colors = SuggestionChipDefaults.suggestionChipColors()
            )
            Spacer(modifier = Modifier.size(6.dp))
            SuggestionChip(
                onClick = onAddClick,
                label = { Text("💼 Meetings", fontSize = 11.sp) },
                colors = SuggestionChipDefaults.suggestionChipColors()
            )
            Spacer(modifier = Modifier.size(6.dp))
            SuggestionChip(
                onClick = onAddClick,
                label = { Text("✈️ Travel", fontSize = 11.sp) },
                colors = SuggestionChipDefaults.suggestionChipColors()
            )
        }
    }
}
