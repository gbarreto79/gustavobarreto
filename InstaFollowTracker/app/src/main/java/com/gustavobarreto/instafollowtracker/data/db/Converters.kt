package com.gustavobarreto.instafollowtracker.data.db

import androidx.room.TypeConverter
import com.gustavobarreto.instafollowtracker.data.model.ListType

class Converters {
    @TypeConverter
    fun fromListType(value: ListType): String = value.name

    @TypeConverter
    fun toListType(value: String): ListType = ListType.valueOf(value)
}
