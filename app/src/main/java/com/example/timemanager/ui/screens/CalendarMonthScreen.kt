package com.example.timemanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timemanager.model.EventCategory
import com.example.timemanager.model.EventEntity
import com.example.timemanager.ui.components.EventCard
import com.example.timemanager.ui.theme.DarkBorder
import com.example.timemanager.ui.theme.IndigoPrimary
import com.example.timemanager.ui.viewmodel.CalendarSystem
import com.example.timemanager.util.JalaliCalendarHelper
import java.util.Calendar

@Composable
fun CalendarMonthScreen(
    allEvents: List<EventEntity>,
    calendarSystem: CalendarSystem,
    selectedDateIso: String,
    onDateSelected: (String) -> Unit,
    onToggleCalendarSystem: () -> Unit,
    onOpenComposerWithDate: (String) -> Unit,
    onOpenComposerForEdit: (EventEntity) -> Unit,
    onDeleteEvent: (Long) -> Unit,
    onTogglePin: (Long, Boolean) -> Unit,
    onToggleChecklistItem: (EventEntity, String) -> Unit,
    onAddChecklistItem: (EventEntity, String) -> Unit,
    onRemoveChecklistItem: (EventEntity, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayIso = remember { JalaliCalendarHelper.getTodayGregorian() }
    val todayJalali = remember { JalaliCalendarHelper.getTodayJalali() }

    // Navigation states for Gregorian
    val calInstance = remember { Calendar.getInstance() }
    var gregorianYear by remember { mutableIntStateOf(calInstance.get(Calendar.YEAR)) }
    var gregorianMonth by remember { mutableIntStateOf(calInstance.get(Calendar.MONTH) + 1) } // 1-12

    // Navigation states for Jalali
    var jalaliYear by remember { mutableIntStateOf(todayJalali.year) }
    var jalaliMonth by remember { mutableIntStateOf(todayJalali.month) } // 1-12

    // Keep navigation in sync when toggling systems
    var lastKnownSystem by remember { mutableStateOf(calendarSystem) }
    LaunchedEffect(calendarSystem) {
        if (calendarSystem != lastKnownSystem) {
            if (calendarSystem == CalendarSystem.JALALI) {
                // Switched to Jalali: compute Jalali equivalent of middle of current Gregorian view
                val j = JalaliCalendarHelper.gregorianToJalali(gregorianYear, gregorianMonth, 15)
                jalaliYear = j.year
                jalaliMonth = j.month
            } else {
                // Switched to Gregorian: compute Gregorian equivalent of middle of current Jalali view
                val (gy, gm, _) = JalaliCalendarHelper.jalaliToGregorian(jalaliYear, jalaliMonth, 15)
                gregorianYear = gy
                gregorianMonth = gm
            }
            lastKnownSystem = calendarSystem
        }
    }

    // Month titles & subtitles
    val monthTitle = if (calendarSystem == CalendarSystem.GREGORIAN) {
        "${JalaliCalendarHelper.GREGORIAN_MONTH_NAMES[gregorianMonth - 1]} $gregorianYear"
    } else {
        "${JalaliCalendarHelper.JALALI_MONTH_NAMES_FA[jalaliMonth - 1]} $jalaliYear"
    }

    val monthSubtitle = if (calendarSystem == CalendarSystem.GREGORIAN) {
        "Gregorian Calendar"
    } else {
        "${JalaliCalendarHelper.JALALI_MONTH_NAMES[jalaliMonth - 1]} • تقویم خورشیدی"
    }

    // Weekday headers
    val weekdays = if (calendarSystem == CalendarSystem.GREGORIAN) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    } else {
        listOf("شنبه", "۱شنبه", "۲شنبه", "۳شنبه", "۴شنبه", "۵شنبه", "جمعه")
    }

    // Days in current month
    val daysInCurrentMonth = remember(calendarSystem, gregorianYear, gregorianMonth, jalaliYear, jalaliMonth) {
        if (calendarSystem == CalendarSystem.GREGORIAN) {
            val c = Calendar.getInstance().apply {
                set(Calendar.YEAR, gregorianYear)
                set(Calendar.MONTH, gregorianMonth - 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            c.getActualMaximum(Calendar.DAY_OF_MONTH)
        } else {
            JalaliCalendarHelper.daysInJalaliMonth(jalaliYear, jalaliMonth)
        }
    }

    // First day offset
    // Gregorian: Sunday = 0, Monday = 1, ..., Saturday = 6
    // Jalali: Saturday = 0, Sunday = 1, ..., Friday = 6
    val firstDayOffset = remember(calendarSystem, gregorianYear, gregorianMonth, jalaliYear, jalaliMonth) {
        if (calendarSystem == CalendarSystem.GREGORIAN) {
            val c = Calendar.getInstance().apply {
                set(Calendar.YEAR, gregorianYear)
                set(Calendar.MONTH, gregorianMonth - 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            c.get(Calendar.DAY_OF_WEEK) - 1 // 1=Sun (0) ... 7=Sat (6)
        } else {
            val (gy, gm, gd) = JalaliCalendarHelper.jalaliToGregorian(jalaliYear, jalaliMonth, 1)
            val c = Calendar.getInstance().apply { set(gy, gm - 1, gd) }
            val dow = c.get(Calendar.DAY_OF_WEEK) // 1=Sun, ..., 7=Sat
            dow % 7 // Sat(7) -> 0, Sun(1) -> 1, ..., Fri(6) -> 6
        }
    }

    // Group events by ISO date
    val eventsByDate = remember(allEvents) {
        allEvents.groupBy { it.date }
    }

    val selectedDayEvents = remember(selectedDateIso, allEvents) {
        allEvents.filter { it.date == selectedDateIso }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("month_view_screen"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Month Switcher & System Toggle Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Month title and nav buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (calendarSystem == CalendarSystem.GREGORIAN) {
                                    if (gregorianMonth == 1) {
                                        gregorianMonth = 12
                                        gregorianYear -= 1
                                    } else {
                                        gregorianMonth -= 1
                                    }
                                } else {
                                    if (jalaliMonth == 1) {
                                        jalaliMonth = 12
                                        jalaliYear -= 1
                                    } else {
                                        jalaliMonth -= 1
                                    }
                                }
                            },
                            modifier = Modifier.testTag("prev_month_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous month"
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = monthTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = monthSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = IndigoPrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                if (calendarSystem == CalendarSystem.GREGORIAN) {
                                    if (gregorianMonth == 12) {
                                        gregorianMonth = 1
                                        gregorianYear += 1
                                    } else {
                                        gregorianMonth += 1
                                    }
                                } else {
                                    if (jalaliMonth == 12) {
                                        jalaliMonth = 1
                                        jalaliYear += 1
                                    } else {
                                        jalaliMonth += 1
                                    }
                                }
                            },
                            modifier = Modifier.testTag("next_month_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next month"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calendar Toggle Button
                    OutlinedButton(
                        onClick = onToggleCalendarSystem,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_calendar_system_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (calendarSystem == CalendarSystem.GREGORIAN) "تغییر به تقویم شمسی 🇮🇷 (Switch to Jalali)" else "Switch to Gregorian View 🌐 (میلادی)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weekday headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        weekdays.forEachIndexed { index, day ->
                            val isWeekend = if (calendarSystem == CalendarSystem.GREGORIAN) {
                                index == 0 || index == 6 // Sun, Sat
                            } else {
                                index == 6 // جمعه (Friday)
                            }
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isWeekend && calendarSystem == CalendarSystem.JALALI) {
                                    Color(0xFFE53935)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days Grid
                    val totalSlots = ((firstDayOffset + daysInCurrentMonth + 6) / 7) * 7
                    val rows = totalSlots / 7

                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (c in 0 until 7) {
                                val slotIndex = r * 7 + c
                                val dayNum = slotIndex - firstDayOffset + 1

                                if (dayNum in 1..daysInCurrentMonth) {
                                    // Calculate ISO date and labels
                                    val (dateString, primaryDayText, subDayText) = if (calendarSystem == CalendarSystem.GREGORIAN) {
                                        val iso = "%04d-%02d-%02d".format(gregorianYear, gregorianMonth, dayNum)
                                        val jSub = JalaliCalendarHelper.toJalaliString(iso).substringAfterLast("/")
                                        Triple(iso, dayNum.toString(), jSub)
                                    } else {
                                        val (cellGy, cellGm, cellGd) = JalaliCalendarHelper.jalaliToGregorian(jalaliYear, jalaliMonth, dayNum)
                                        val iso = "%04d-%02d-%02d".format(cellGy, cellGm, cellGd)
                                        Triple(iso, dayNum.toString(), cellGd.toString())
                                    }

                                    val isSelected = dateString == selectedDateIso
                                    val isToday = dateString == todayIso
                                    val isFriday = calendarSystem == CalendarSystem.JALALI && c == 6
                                    val dayEvents = eventsByDate[dateString] ?: emptyList()

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(0.9f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                when {
                                                    isSelected -> IndigoPrimary
                                                    isToday -> IndigoPrimary.copy(alpha = 0.2f)
                                                    isFriday -> Color(0xFFE53935).copy(alpha = 0.08f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isSelected || isToday) 1.5.dp else 0.5.dp,
                                                color = when {
                                                    isSelected -> IndigoPrimary
                                                    isToday -> IndigoPrimary.copy(alpha = 0.6f)
                                                    else -> DarkBorder.copy(alpha = 0.15f)
                                                },
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { onDateSelected(dateString) }
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = primaryDayText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                                color = when {
                                                    isSelected -> Color.White
                                                    isFriday -> Color(0xFFE53935)
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                fontSize = 13.sp
                                            )

                                            // Secondary calendar day indicator
                                            Text(
                                                text = subDayText,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                color = if (isSelected) {
                                                    Color.White.copy(alpha = 0.8f)
                                                } else {
                                                    IndigoPrimary.copy(alpha = 0.8f)
                                                }
                                            )

                                            // Event indicators (colored dots)
                                            if (dayEvents.isNotEmpty()) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier = Modifier.padding(top = 1.dp)
                                                ) {
                                                    dayEvents.take(3).forEach { evt ->
                                                        val cat = EventCategory.fromId(evt.category)
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    if (isSelected) Color.White else cat.accentColor
                                                                )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Events for selected day
        item {
            val selectedJalaliFull = remember(selectedDateIso) {
                val parts = selectedDateIso.split("-").mapNotNull { it.toIntOrNull() }
                if (parts.size == 3) {
                    val j = JalaliCalendarHelper.gregorianToJalali(parts[0], parts[1], parts[2])
                    "${j.day} ${j.monthNameFa()} ${j.year}"
                } else {
                    selectedDateIso
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (calendarSystem == CalendarSystem.JALALI) {
                            "رویدادهای $selectedJalaliFull"
                        } else {
                            "Events for $selectedDateIso"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (calendarSystem == CalendarSystem.JALALI) {
                            "میلادی: $selectedDateIso (${selectedDayEvents.size} رویداد)"
                        } else {
                            "Jalali: $selectedJalaliFull (${selectedDayEvents.size} events)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { onOpenComposerWithDate(selectedDateIso) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    modifier = Modifier.testTag("add_event_selected_date_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (calendarSystem == CalendarSystem.JALALI) "افزودن" else "Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (selectedDayEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (calendarSystem == CalendarSystem.JALALI) {
                                "هیچ رویدادی برای این روز ثبت نشده است"
                            } else {
                                "No events scheduled for this day"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(
                items = selectedDayEvents,
                key = { it.id }
            ) { event ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EventCard(
                        event = event,
                        onEditClick = { onOpenComposerForEdit(event) },
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
