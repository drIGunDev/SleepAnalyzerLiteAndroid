package de.igor.gun.sleep.analyzer.ui.screens.tracking.model

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.igor.gun.sleep.analyzer.services.android.SensorService
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class ServiceViewModel @Inject constructor() : ViewModel() {
    enum class ServiceState {
        STOPPED,
        RUNNING,
    }

    val state: MutableState<ServiceState> = mutableStateOf(ServiceState.STOPPED)

    val notGrantedPermissions = mutableStateOf<String?>(null)

    fun startService(context: Context) {
        triggerForegroundService(context, SensorService.Actions.START.name)
        bindServiceIfRunning(context)
    }

    fun stopService(context: Context) {
        if (isServiceRunning(context)) {
            triggerForegroundService(context, SensorService.Actions.STOP.name)
            unbindServiceIfRunning(context)
        }
        state.value = ServiceState.STOPPED
    }

    private fun triggerForegroundService(context: Context, action: String) {
        Intent(context, SensorService::class.java).apply {
            this.action = action
            context.startForegroundService(this)
        }
    }

    fun bindServiceIfRunning(context: Context) {
        if (isServiceRunning(context)) {
            Intent(context, SensorService::class.java).also {
                context.bindService(it, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    fun unbindServiceIfRunning(context: Context) {
        if (isServiceRunning(context)) {
            context.unbindService(connection)
        }
    }

    companion object {
        @Suppress("DEPRECATION")
        fun isServiceRunning(context: Context): Boolean {
            val manager = ContextCompat.getSystemService(context, ActivityManager::class.java) ?: return false
            return manager.getRunningServices(Integer.MAX_VALUE).any { serviceInfo ->
                Timber.w("serviceInfo.service.className: ${serviceInfo.service.className}")
                serviceInfo.service.className == SensorService::class.java.name
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            state.value = ServiceState.RUNNING
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            state.value = ServiceState.STOPPED
        }
    }
}