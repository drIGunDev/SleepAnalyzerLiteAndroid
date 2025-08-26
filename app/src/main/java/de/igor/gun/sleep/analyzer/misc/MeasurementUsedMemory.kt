package de.igor.gun.sleep.analyzer.misc

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Debug.getNativeHeapFreeSize
import android.os.Debug.getNativeHeapSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class MemoryRetrieveMethod {
    DEBUG, RUNTIME, ACTIVITY_SERVICE, DEBUG_RSS
}

@Composable
fun ShowUsedMemory(
    retrieveMethod: MemoryRetrieveMethod = MemoryRetrieveMethod.RUNTIME,
    delayIntervalMillis: Long = 1000L,
    startMeasuring: MutableState<Boolean> = rememberSaveable { mutableStateOf(true) }
) {
    @SuppressLint("DefaultLocale")
    fun Long.memoryToString(): String {
        return if (this < 1024) {
            "$this B."
        } else if (this < 1024 * 1024) {
            "${this / 1024} kB."
        } else if (this < 1024 * 1024 * 1024) {
            "${this/ (1024 * 1024)} mB."
        } else {
            "${String.format("%.3f", this.toDouble() / (1024 * 1024 * 1024))} gB."
        }
    }

    var usedMemory by rememberSaveable { mutableLongStateOf(0L) }
    var totalMemory by rememberSaveable { mutableLongStateOf(0L) }
    val context = LocalContext.current

    LaunchedEffect(startMeasuring.value) {
        if (!startMeasuring.value) return@LaunchedEffect
        while (startMeasuring.value) {
            delay(delayIntervalMillis)
            when (retrieveMethod) {
                MemoryRetrieveMethod.DEBUG -> {
                    totalMemory = getNativeHeapSize()
                    usedMemory = totalMemory - getNativeHeapFreeSize()
                }

                MemoryRetrieveMethod.RUNTIME -> {
                    val runtime = Runtime.getRuntime()
                    totalMemory = runtime.totalMemory()
                    usedMemory = totalMemory - runtime.freeMemory()
                }

                MemoryRetrieveMethod.ACTIVITY_SERVICE -> {
                    val (total, avail) = memoryInfoFromActivityManager(context)
                    totalMemory = total
                    usedMemory = totalMemory - avail
                }

                MemoryRetrieveMethod.DEBUG_RSS -> {
                    val (_, avail) = memoryInfoFromActivityManager(context)
                    totalMemory = avail
                    usedMemory = Debug.getPss() * 1024
                }
            }
        }
    }

    Text(text = "${usedMemory.memoryToString()} / ${totalMemory.memoryToString()}", fontSize = 12.sp, color = Color.Gray)
}

private fun memoryInfoFromActivityManager(context: Context): Pair<Long, Long> {
    val memoryInfo: ActivityManager.MemoryInfo = ActivityManager.MemoryInfo()
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    activityManager.getMemoryInfo(memoryInfo)
    return Pair(memoryInfo.totalMem, memoryInfo.availMem)
}

@Composable
@Preview(showBackground = true)
fun ShowUsedMemoryPreview() {
    ShowUsedMemory()
}