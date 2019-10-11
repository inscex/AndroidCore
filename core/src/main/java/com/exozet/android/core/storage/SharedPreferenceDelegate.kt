package com.exozet.android.core.storage

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Parcelable
import android.preference.PreferenceManager
import com.exozet.android.core.extensions.lazyFast
import com.exozet.android.core.extensions.safeContext
import com.exozet.android.core.gson.fromJson
import com.exozet.android.core.gson.toJson
import com.github.florent37.application.provider.application
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


private class SharedPreferenceDelegate<T>(
    private val context: Context,
    private val defaultValue: T,
    private val getter: SharedPreferences.(String, T) -> T,
    private val setter: Editor.(String, T) -> Editor,
    private val key: String
) : ReadWriteProperty<Any, T> {

    private val safeContext: Context by lazyFast { context.safeContext() }

    private val sharedPreferences: SharedPreferences by lazyFast {
        PreferenceManager.getDefaultSharedPreferences(safeContext)
    }

    override fun getValue(thisRef: Any, property: KProperty<*>) =
        sharedPreferences
            .getter(key, defaultValue)

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) =
        sharedPreferences
            .edit()
            .setter(key, value)
            .apply()
}

@Suppress("UNCHECKED_CAST")
fun <T> sharedPreference(key: String, defaultValue: T): ReadWriteProperty<Any, T> =
    when (defaultValue) {
        is Parcelable -> SharedPreferenceDelegate(application!!, defaultValue, SharedPreferences::getParcelable, Editor::putParcelable, key)
        is Boolean -> SharedPreferenceDelegate(application!!, defaultValue, SharedPreferences::getBoolean, Editor::putBoolean, key)
        is Int -> SharedPreferenceDelegate(application!!, defaultValue, SharedPreferences::getInt, Editor::putInt, key)
        is Long -> SharedPreferenceDelegate(application!!, defaultValue, SharedPreferences::getLong, Editor::putLong, key)
        is Float -> SharedPreferenceDelegate(application!!, defaultValue, SharedPreferences::getFloat, Editor::putFloat, key)
        is String -> SharedPreferenceDelegate(application!!, defaultValue, SharedPreferences::getString, Editor::putString, key)
        else -> throw IllegalArgumentException()
    } as ReadWriteProperty<Any, T>


inline fun <reified T : Parcelable> SharedPreferences.getParcelable(key: String, defValue: T): T = getString(key, defValue.toJson())!!.fromJson()

inline fun <reified T : Parcelable> Editor.putParcelable(key: String, value: T): Editor = putString(key, value.toJson())