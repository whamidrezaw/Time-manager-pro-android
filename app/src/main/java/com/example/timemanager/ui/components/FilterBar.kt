package com.example.timemanager.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timemanager.ui.theme.IndigoPrimary

data class FilterOption(val id: String, val label: String, val emoji: String)

val FILTER_OPTIONS = listOf(
    FilterOption("all", "All", "🌐"),
    FilterOption("pinned", "Pinned", "📌"),
    FilterOption("birthday", "Birthday", "🎂"),
    FilterOption("work", "Work", "💼"),
    FilterOption("health", "Health", "❤️"),
    FilterOption("family", "Family", "👨‍👩‍👧"),
    FilterOption("travel", "Travel", "✈️"),
    FilterOption("finance", "Finance", "💰"),
    FilterOption("study", "Study", "📚"),
    FilterOption("past", "Past", "🗄️")
)

@Composable
fun FilterBar(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FILTER_OPTIONS.forEach { opt ->
            val isSelected = opt.id.equals(selectedFilter, ignoreCase = true)
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(opt.id) },
                label = {
                    Text(
                        text = "${opt.emoji} ${opt.label}",
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IndigoPrimary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    selectedBorderColor = IndigoPrimary
                ),
                modifier = Modifier.testTag("filter_${opt.id}")
            )
        }
    }
}
