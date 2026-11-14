package com.example.data.local

import androidx.room.TypeConverter
import org.json.JSONArray

class VaultConverters {
    @TypeConverter
    fun fromStringList(list: List<String>): String {
        val array = JSONArray()
        list.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return try {
            val array = JSONArray(raw)
            buildList(array.length()) {
                for (i in 0 until array.length()) {
                    val value = array.optString(i, "").trim()
                    if (value.isNotEmpty()) add(value)
                }
            }
        } catch (_: Exception) {
            raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
}
