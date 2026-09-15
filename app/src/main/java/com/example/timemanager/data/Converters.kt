package com.example.timemanager.data

import com.example.timemanager.model.ChecklistItem
import com.example.timemanager.model.ReminderSpec
import org.json.JSONArray
import org.json.JSONObject

object JsonConverters {

    fun checklistToJson(list: List<ChecklistItem>): String {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("text", item.text)
                put("isDone", item.isDone)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToChecklist(json: String?): List<ChecklistItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<ChecklistItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ChecklistItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        text = obj.optString("text", ""),
                        isDone = obj.optBoolean("isDone", false)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }

    fun remindersToJson(list: List<ReminderSpec>): String {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("mode", item.mode)
                put("daysBefore", item.daysBefore)
                put("hour", item.hour)
                put("minute", item.minute)
                put("offsetMinutes", item.offsetMinutes)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToReminders(json: String?): List<ReminderSpec> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<ReminderSpec>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ReminderSpec(
                        mode = obj.optString("mode", "absolute"),
                        daysBefore = obj.optInt("daysBefore", 0),
                        hour = obj.optInt("hour", 9),
                        minute = obj.optInt("minute", 0),
                        offsetMinutes = obj.optInt("offsetMinutes", 0)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }
}
