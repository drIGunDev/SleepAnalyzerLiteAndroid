package de.igor.gun.sleep.analyzer.misc

import android.content.Context
import kotlin.reflect.KProperty


class ItemPreference<T>(context: Context, private val initializer: () -> T) {
    companion object {
        const val STORAGE_NANE = "app_settings"
    }

    private val preferences = context.getSharedPreferences(STORAGE_NANE, Context.MODE_PRIVATE)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        @Suppress("UNCHECKED_CAST")
        synchronized(this) {
            return preferences.all[property.name] as? T
                ?: let {
                    val value = initializer()
                    setValue(thisRef, property, value)
                    value
                }
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val editor = preferences.edit()
        synchronized(this) {
            when (value) {
                is String -> editor.putString(property.name, value as String)
                is Int -> editor.putInt(property.name, value as Int)
                is Long -> editor.putLong(property.name, value as Long)
                is Float -> editor.putFloat(property.name, value as Float)
                is Boolean -> editor.putBoolean(property.name, value as Boolean)
                else -> {}
            }
            editor.apply()
        }
    }
}