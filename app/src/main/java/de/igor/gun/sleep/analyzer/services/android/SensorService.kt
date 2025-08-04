package de.igor.gun.sleep.analyzer.services.android

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import de.igor.gun.sleep.analyzer.services.sensors.SensorAPI
import javax.inject.Inject


@AndroidEntryPoint
class SensorService : Service() {
    enum class Actions {
        START, STOP
    }

    @Inject
    lateinit var sensorAPI: SensorAPI

    override fun onCreate() {
        super.onCreate()
        sensorAPI.setApiCallback()
    }

    override fun onBind(p0: Intent?) = binder

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action.let {
            when (it) {
                Actions.START.name -> startAsForegroundService()
                Actions.STOP.name -> stopService()
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    inner class ServiceBinder : Binder() {
        fun getService(): SensorService = this@SensorService
    }

    private val binder = ServiceBinder()

    private fun stopService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun startAsForegroundService() {
        NotificationsHelper.createNotificationChannel(this)
        ServiceCompat.startForeground(
            this,
            1,
            NotificationsHelper.buildNotification(this),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )
    }
}