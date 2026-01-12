package com.stolz.connect.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stolz.connect.data.local.dao.CustomContactDao
import com.stolz.connect.data.local.dao.ScheduledConnectionDao
import com.stolz.connect.data.local.entity.CustomContactEntity
import com.stolz.connect.data.local.entity.ScheduledConnectionEntity

@Database(
    entities = [ScheduledConnectionEntity::class, CustomContactEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class ConnectDatabase : RoomDatabase() {
    abstract fun scheduledConnectionDao(): ScheduledConnectionDao
    abstract fun customContactDao(): CustomContactDao
}
