package com.example.timemanager

import android.app.Application
import com.example.timemanager.data.AppDatabase
import com.example.timemanager.data.EventRepository
import com.example.timemanager.data.JsonConverters
import com.example.timemanager.model.ChecklistItem
import com.example.timemanager.model.EventEntity
import com.example.timemanager.util.JalaliCalendarHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TimeManagerApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { EventRepository(database.eventDao()) }

    override fun onCreate() {
        super.onCreate()
        seedInitialDataIfEmpty()
    }

    private fun seedInitialDataIfEmpty() {
        CoroutineScope(Dispatchers.IO).launch {
            val existing = repository.allEvents.first()
            if (existing.isEmpty()) {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val cal = Calendar.getInstance()

                // Event 1: Today's Planning
                val todayStr = sdf.format(cal.time)
                val todayJalali = JalaliCalendarHelper.toJalaliString(todayStr)
                repository.insertEvent(
                    EventEntity(
                        title = "Q4 Product Strategy & Review",
                        date = todayStr,
                        jalaliDate = todayJalali,
                        isAllDay = false,
                        timeHm = "14:30",
                        category = "work",
                        repeat = "none",
                        note = "Review product roadmap, deliverables, and team metrics.",
                        isPinned = true,
                        checklistJson = JsonConverters.checklistToJson(
                            listOf(
                                ChecklistItem(text = "Review slide deck", isDone = true),
                                ChecklistItem(text = "Finalize milestone checklist", isDone = false),
                                ChecklistItem(text = "Share recording with stakeholders", isDone = false)
                            )
                        )
                    )
                )

                // Event 2: In 3 days - Birthday
                cal.add(Calendar.DAY_OF_YEAR, 3)
                val bdayStr = sdf.format(cal.time)
                val bdayJalali = JalaliCalendarHelper.toJalaliString(bdayStr)
                repository.insertEvent(
                    EventEntity(
                        title = "Sarah's Birthday Celebration 🎂",
                        date = bdayStr,
                        jalaliDate = bdayJalali,
                        isAllDay = true,
                        category = "birthday",
                        repeat = "yearly",
                        leadRepeat = "daily",
                        note = "Order favorite chocolate cake and gift card.",
                        isPinned = false,
                        checklistJson = JsonConverters.checklistToJson(
                            listOf(
                                ChecklistItem(text = "Buy gift & card", isDone = false),
                                ChecklistItem(text = "Confirm dinner reservation", isDone = false)
                            )
                        )
                    )
                )

                // Event 3: In 10 days - Travel
                cal.add(Calendar.DAY_OF_YEAR, 7)
                val travelStr = sdf.format(cal.time)
                val travelJalali = JalaliCalendarHelper.toJalaliString(travelStr)
                repository.insertEvent(
                    EventEntity(
                        title = "Flight to International Tech Summit ✈️",
                        date = travelStr,
                        jalaliDate = travelJalali,
                        isAllDay = false,
                        timeHm = "08:15",
                        category = "travel",
                        repeat = "none",
                        note = "Terminal 2, Flight TS-402. Don't forget passport and conference pass.",
                        isPinned = false,
                        checklistJson = JsonConverters.checklistToJson(
                            listOf(
                                ChecklistItem(text = "Check in online", isDone = false),
                                ChecklistItem(text = "Pack travel essentials", isDone = false)
                            )
                        )
                    )
                )
            }
        }
    }
}
