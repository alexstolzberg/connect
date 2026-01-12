package com.stolz.connect.di

import android.content.Context
import androidx.room.Room
import com.stolz.connect.data.local.ConnectDatabase
import com.stolz.connect.data.local.dao.CustomContactDao
import com.stolz.connect.data.local.dao.ScheduledConnectionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ConnectDatabase {
        return Room.databaseBuilder(
            context,
            ConnectDatabase::class.java,
            "connect_database"
        )
        .fallbackToDestructiveMigration() // For development - remove in production and add proper migrations
        .build()
    }
    
    @Provides
    fun provideScheduledConnectionDao(database: ConnectDatabase): ScheduledConnectionDao {
        return database.scheduledConnectionDao()
    }
    
    @Provides
    fun provideCustomContactDao(database: ConnectDatabase): CustomContactDao {
        return database.customContactDao()
    }
}
