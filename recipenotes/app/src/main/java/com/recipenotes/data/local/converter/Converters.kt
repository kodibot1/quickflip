package com.recipenotes.data.local.converter

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * Room Type Converters.
 *
 * Room only natively supports primitive types (String, Int, Long, etc.) for columns.
 * Type converters tell Room how to convert complex types to/from primitives for storage.
 *
 * Currently minimal since we store dates as ISO strings and timestamps as Longs,
 * but this is where you'd add converters for enums, lists, or custom types.
 */
class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? = dateString?.let { LocalDate.parse(it) }
}
