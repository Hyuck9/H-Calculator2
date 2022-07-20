package me.hyuck9.calculator.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val PREFERENCE_NAME = "H-Calculator_Memory"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_NAME)

suspend fun Context.writeString(key: String, value: String) {
	dataStore.edit { pref ->
		pref[stringPreferencesKey(key)] = value
	}
}

fun Context.readString(key: String): Flow<String> =
	dataStore.data.map { pref ->
		pref[stringPreferencesKey(key)] ?: ""
	}