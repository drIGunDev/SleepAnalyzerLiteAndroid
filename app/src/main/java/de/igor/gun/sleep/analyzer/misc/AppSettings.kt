package de.igor.gun.sleep.analyzer.misc

import android.content.Context


class AppSettings(context: Context) {
    var deviceId: String by ItemPreference(context) { "" }
    var frameSizeHR: Int by ItemPreference(context) { 192 }
    var frameSizeACC: Int by ItemPreference(context) { 117 }
    var quantizationHR: Float by ItemPreference(context) { 0.86f }
    var quantizationACC: Float by ItemPreference(context) { 0.76f }
}

val AppSettings.isDeviceIdValid get() = deviceId.isNotEmpty()