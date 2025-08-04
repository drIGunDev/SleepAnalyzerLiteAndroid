package de.igor.gun.sleep.analyzer.misc

import android.content.Context


class AppSettings(context: Context) {
    var deviceId: String by ItemPreference(context) { "" }
    var frameSizeHR: Int by ItemPreference(context) { 20 }
    var frameSizeACC: Int by ItemPreference(context) { 20 }
    var quantizationHR: Float by ItemPreference(context) { 0.8f }
    var quantizationACC: Float by ItemPreference(context) { 0.89f }
}

val AppSettings.isDeviceIdValid get() = deviceId.isNotEmpty()