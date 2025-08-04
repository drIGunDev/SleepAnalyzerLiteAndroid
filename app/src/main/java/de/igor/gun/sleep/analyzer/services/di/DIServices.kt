package de.igor.gun.sleep.analyzer.services.di

import android.app.Application
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiDefaultImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import de.igor.gun.sleep.analyzer.misc.MeasurementsChecker
import de.igor.gun.sleep.analyzer.services.sensors.PPGSource
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import de.igor.gun.sleep.analyzer.services.sensors.SensorDataSource
import de.igor.gun.sleep.analyzer.services.sensors.SensorScanner
import de.igor.gun.sleep.analyzer.services.sensors.polar.PPGSourceImpl
import de.igor.gun.sleep.analyzer.services.sensors.polar.PolarAPIImpl
import de.igor.gun.sleep.analyzer.services.sensors.polar.PolarDataSourceImpl
import de.igor.gun.sleep.analyzer.services.sensors.polar.PolarSensorScannerImpl
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object DIServices {

    @Provides
    @Singleton
    fun providePolarBleApi(application: Application): PolarBleApi = PolarBleApiDefaultImpl.defaultImplementation(
        application,
        REQUIRED_POLAR_FEATURES
    )

    @Provides
    @Singleton
    fun providePolarAPI(polarBleApi: PolarBleApi): SensorAPI = PolarAPIImpl(polarBleApi)

    @Provides
    @Singleton
    fun providePolarSensorScanner(polarBleApi: PolarBleApi): SensorScanner = PolarSensorScannerImpl(polarBleApi)

    @Provides
    @Singleton
    fun providePolarDataSource(sensorAPI: SensorAPI): SensorDataSource = PolarDataSourceImpl(sensorAPI)

    @Provides
    @Singleton
    fun provideMeasurementChecker() = MeasurementsChecker(quantizationIntervalInSec = RECORDING_QUANTIZATION_INTERVAL)

    private const val RECORDING_QUANTIZATION_INTERVAL = 1

    private val REQUIRED_POLAR_FEATURES = setOf(
        PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
        PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
        PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
        PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_H10_EXERCISE_RECORDING,
        PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_OFFLINE_RECORDING,
        PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
        PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_DEVICE_TIME_SETUP,
        PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO,
        PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_LED_ANIMATION
    )
}

@InstallIn(ViewModelComponent::class)
@Module
abstract class DISensorBinder {
    @Binds
    abstract fun bindPPGSource(source: PPGSourceImpl): PPGSource
}