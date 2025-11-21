package com.example.taskmanagergpsapp_brc.Data;

import androidx.room.TypeConverter;

public class Converters {

    @TypeConverter
    public static String fromStatus(Status status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static Status toStatus(String value) {
        return value == null ? null : Status.valueOf(value);
    }
}
