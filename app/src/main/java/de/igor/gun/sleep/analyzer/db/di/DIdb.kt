package de.igor.gun.sleep.analyzer.db.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.igor.gun.sleep.analyzer.db.AppDatabase
import de.igor.gun.sleep.analyzer.db.DBManager
import de.igor.gun.sleep.analyzer.db.SeriesRecorder
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object DIdb {
    @Provides
    @Singleton
    fun provideDataManager(appDatabase: AppDatabase) = DBManager(appDatabase)

    @Provides
    @Singleton
    fun provideDBRecordManager(dbManager: DBManager) = SeriesRecorder(dbManager)


    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context) = AppDatabase.createDB(context)
}