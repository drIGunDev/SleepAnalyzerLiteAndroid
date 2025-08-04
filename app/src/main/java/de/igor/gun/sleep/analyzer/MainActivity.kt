package de.igor.gun.sleep.analyzer

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import de.igor.gun.sleep.analyzer.ui.navigation.RootGraphNavigation
import de.igor.gun.sleep.analyzer.ui.screens.splash.model.SplashViewModel
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.SensorViewModel
import de.igor.gun.sleep.analyzer.ui.screens.tracking.model.ServiceViewModel
import de.igor.gun.sleep.analyzer.ui.theme.SleepAnalyzerTheme


private fun String.splitAndGetLast(delimiter: Char = '.') = this.split(delimiter).last()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<SplashViewModel>()
    private val serviceViewModel by viewModels<ServiceViewModel>()
    private val sensorViewModel by viewModels<SensorViewModel>()

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        if (!ServiceViewModel.isServiceRunning(this)) {
            splashScreen.setKeepOnScreenCondition { viewModel.isLoading.value }
        }
        setContent {
            SleepAnalyzerTheme {
                RootGraphNavigation(
                    serviceViewModel = serviceViewModel,
                    sensorViewModel = sensorViewModel
                )
            }
        }

        if (!hasAllPermissions(applicationContext)) {
            checkPermissionAndStartService.launch(REQUESTED_PERMISSIONS)
        } else {
            serviceViewModel.bindServiceIfRunning(this)
            sensorViewModel.bindDefaultSensorIfServiceBound(applicationContext) {
                if (!it) {
                    sensorViewModel.automaticBindSensor(applicationContext)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceViewModel.unbindServiceIfRunning(this)
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        private val REQUESTED_PERMISSIONS = arrayOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.POST_NOTIFICATIONS,
            android.Manifest.permission.FOREGROUND_SERVICE,
            android.Manifest.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE,
        )

        @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
        private fun hasAllPermissions(context: Context) = REQUESTED_PERMISSIONS
            .all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val checkPermissionAndStartService =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (!granted) {
                val notGrantedPermissions = permissions
                    .filter { !(it.value || it.key.splitAndGetLast() == android.Manifest.permission.POST_NOTIFICATIONS.splitAndGetLast()) }
                    .map { it.key.split('.').last() }
                    .fold("") { permission, result -> "$permission\n$result" }
                if (notGrantedPermissions.isNotEmpty()) {
                    serviceViewModel.notGrantedPermissions.value = notGrantedPermissions
                }
            }
        }
}
