package com.example.timemanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.timemanager.ui.theme.DarkBorder
import com.example.timemanager.ui.theme.IndigoPrimary
import com.example.timemanager.util.JalaliCalendarHelper
import java.util.Calendar

@Composable
fun JalaliDatePickerDialog(
    initialYear: Int,
    initialMonth: Int, // 1-12
    initialDay: Int,   // 1-31
    onDismiss: () -> Unit,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(initialYear) }
    var selectedMonth by remember { mutableIntStateOf(initialMonth.coerceIn(1, 12)) }
    var selectedDay by remember {
        val maxDays = JalaliCalendarHelper.daysInJalaliMonth(initialYear, initialMonth)
        mutableIntStateOf(initialDay.coerceIn(1, maxDays))
    }

    val todayJalali = remember { JalaliCalendarHelper.getTodayJalali() }

    // Ensure day is valid when month or year changes
    val daysInCurrentMonth = remember(selectedYear, selectedMonth) {
        JalaliCalendarHelper.daysInJalaliMonth(selectedYear, selectedMonth)
    }
    if (selectedDay > daysInCurrentMonth) {
        selectedDay = daysInCurrentMonth
    }

    // Equivalent Gregorian date for display
    val gregorianEquivalent = remember(selectedYear, selectedMonth, selectedDay) {
        val (gy, gm, gd) = JalaliCalendarHelper.jalaliToGregorian(selectedYear, selectedMonth, selectedDay)
        val mName = JalaliCalendarHelper.GREGORIAN_MONTH_NAMES.getOrNull(gm - 1) ?: ""
        "$gd $mName $gy"
    }

    // Weekday alignment for 1st of this Jalali month
    // Iranian week starts on Saturday (شنبه)
    val firstDayOffset = remember(selectedYear, selectedMonth) {
        val (gy, gm, gd) = JalaliCalendarHelper.jalaliToGregorian(selectedYear, selectedMonth, 1)
        val cal = Calendar.getInstance().apply { set(gy, gm - 1, gd) }
        val dow = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon, ..., 7=Sat
        dow % 7 // Sat (7) -> 0, Sun (1) -> 1, ..., Fri (6) -> 6
    }

    val persianWeekdays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "انتخاب تاریخ شمسی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = {
                            selectedYear = todayJalali.year
                            selectedMonth = todayJalali.month
                            selectedDay = todayJalali.day
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = IndigoPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("امروز", fontSize = 12.sp, color = IndigoPrimary)
                    }
                }

                // Selection Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = IndigoPrimary.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "${selectedDay} ${JalaliCalendarHelper.JALALI_MONTH_NAMES_FA[selectedMonth - 1]} ${selectedYear}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IndigoPrimary
                        )
                        Text(
                            text = "معادل میلادی: $gregorianEquivalent",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Year Controller
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedYear -= 1 },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Year"
                        )
                    }

                    Text(
                        text = "سال $selectedYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { selectedYear += 1 },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Year"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Month Selector Chips (Horizontal scrollable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    JalaliCalendarHelper.JALALI_MONTH_NAMES_FA.forEachIndexed { index, mNameFa ->
                        val mNum = index + 1
                        val isSelected = mNum == selectedMonth
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMonth = mNum },
                            label = {
                                Text(
                                    text = mNameFa,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IndigoPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Persian Weekday Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    persianWeekdays.forEachIndexed { index, dayName ->
                        val isFriday = index == 6
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isFriday) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Days Grid
                val totalSlots = ((firstDayOffset + daysInCurrentMonth + 6) / 7) * 7
                val rows = totalSlots / 7

                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (c in 0 until 7) {
                            val slotIndex = r * 7 + c
                            val dayNum = slotIndex - firstDayOffset + 1
                            val isFriday = c == 6

                            if (dayNum in 1..daysInCurrentMonth) {
                                val isSelected = dayNum == selectedDay
                                val isToday = selectedYear == todayJalali.year &&
                                        selectedMonth == todayJalali.month &&
                                        dayNum == todayJalali.day

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> IndigoPrimary
                                                isToday -> IndigoPrimary.copy(alpha = 0.15f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isSelected || isToday) 1.5.dp else 0.dp,
                                            color = when {
                                                isSelected -> IndigoPrimary
                                                isToday -> IndigoPrimary.copy(alpha = 0.6f)
                                                else -> Color.Transparent
                                            },
                                            shape = CircleShape
                                        )
                                        .clickable { selectedDay = dayNum },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isFriday -> Color(0xFFE53935)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDateSelected(selectedYear, selectedMonth, selectedDay)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_jalali_date_btn")
            ) {
                Text("تایید (Confirm)", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("انصراف (Cancel)")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
