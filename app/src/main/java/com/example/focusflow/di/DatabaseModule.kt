package com.example.focusflow.di

import android.content.Context
import androidx.room.Room
import com.example.focusflow.data.local.AppDatabase
import com.example.focusflow.data.local.RutinaDao
import com.example.focusflow.data.local.TareaDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "focusflow_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideTareaDao(database: AppDatabase): TareaDao {
        return database.tareaDao()
    }

    @Provides
    @Singleton
    fun provideRutinaDao(database: AppDatabase): RutinaDao {
        return database.rutinaDao()
    }
}
