package com.example.timemanager.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EventCategory(
    val id: String,
    val displayName: String,
    val emoji: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    GENERAL("general", "General", "🌐", Icons.Default.Event, Color(0xFF5B6CF8)),
    BIRTHDAY("birthday", "Birthday", "🎂", Icons.Default.Cake, Color(0xFFFF5B8A)),
    WORK("work", "Work", "💼", Icons.Default.Work, Color(0xFF00C9FF)),
    HEALTH("health", "Health", "❤️", Icons.Default.Favorite, Color(0xFFFF4565)),
    FAMILY("family", "Family", "👨‍👩‍👧", Icons.Default.FamilyRestroom, Color(0xFFFFAF38)),
    TRAVEL("travel", "Travel", "✈️", Icons.Default.Flight, Color(0xFF38EF7D)),
    FINANCE("finance", "Finance", "💰", Icons.Default.Paid, Color(0xFF00E599)),
    STUDY("study", "Study", "📚", Icons.Default.MenuBook, Color(0xFFA855F7)),
    OTHER("other", "Other", "📌", Icons.Default.Event, Color(0xFF8B95A5));

    companion object {
        fun fromId(id: String): EventCategory {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GENERAL
        }
    }
}

enum class RepeatType(val id: String, val label: String) {
    NONE("none", "One-time"),
    DAILY("daily", "Daily"),
    WEEKLY("weekly", "Weekly"),
    MONTHLY("monthly", "Monthly"),
    YEARLY("yearly", "Yearly");

    companion object {
        fun fromId(id: String): RepeatType {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}

enum class LeadRepeat(val id: String, val label: String) {
    NONE("none", "No nudges"),
    DAILY("daily", "Daily nudges until event"),
    WEEKLY("weekly", "Weekly nudges until event"),
    MONTHLY("monthly", "Monthly nudges until event");

    companion object {
        fun fromId(id: String): LeadRepeat {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}

data class ChecklistItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isDone: Boolean = false
)

data class ReminderSpec(
    val mode: String = "absolute", // "absolute", "relative", "lead"
    val daysBefore: Int = 0,
    val hour: Int = 9,
    val minute: Int = 0,
    val offsetMinutes: Int = 0
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: String, // Gregorian "YYYY-MM-DD"
    val jalaliDate: String, // "YYYY/MM/DD"
    val isAllDay: Boolean = true,
    val timeHm: String? = null, // "HH:mm"
    val category: String = "general",
    val repeat: String = "none",
    val repeatUntil: String? = null,
    val leadRepeat: String = "none",
    val note: String = "",
    val isPinned: Boolean = false,
    val checklistJson: String = "[]",
    val remindersJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis()
)
