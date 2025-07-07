package com.example.myapplication.database

import androidx.room.TypeConverter
import java.util.Date

/**
 * Conversores para tipos de dados personalizados
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
