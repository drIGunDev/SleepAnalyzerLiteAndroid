package de.igor.gun.sleep.analyzer.repositories.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.igor.gun.sleep.analyzer.db.DBManager
import de.igor.gun.sleep.analyzer.db.SeriesRecorder
import de.igor.gun.sleep.analyzer.hypnogram.computation.HypnogramComputation
import de.igor.gun.sleep.analyzer.misc.AppParameters
import de.igor.gun.sleep.analyzer.misc.MeasurementsChecker
import de.igor.gun.sleep.analyzer.repositories.DataRepository
import de.igor.gun.sleep.analyzer.repositories.MeasurementsRecorder
import de.igor.gun.sleep.analyzer.repositories.tools.ChartBuilder
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import de.igor.gun.sleep.analyzer.services.sensors.SensorDataSource
import javax.inject.Qualifier
import javax.inject.Singleton


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HCProvider

@InstallIn(SingletonComponent::class)
@Module
object DIdb {

    @Provides
    fun provideChartBuilder() = ChartBuilder(ChartBuilder.Screen.default())

    @Provides
    @Singleton
    fun provideModelParameters(@ApplicationContext context: Context): AppParameters {
        val appParameters = AppParameters(context)
        appParameters.initFromPreferences()
        return appParameters
    }

    @Provides
    @Singleton
    fun provideRecorder(
        dbManager: DBManager,
        seriesRecorder: SeriesRecorder,
        dataRepository: DataRepository,
        sensorAPI: SensorAPI,
        dataSource: SensorDataSource,
        measurementsChecker: MeasurementsChecker,
        chartBuilder: ChartBuilder,
        appParameters: AppParameters
    ) = MeasurementsRecorder(
        dbManager,
        seriesRecorder,
        dataRepository,
        sensorAPI,
        dataSource,
        measurementsChecker,
        chartBuilder,
        appParameters = appParameters
    )

    @HCProvider
    @Provides
    fun provideComputation() = HypnogramComputation()
}