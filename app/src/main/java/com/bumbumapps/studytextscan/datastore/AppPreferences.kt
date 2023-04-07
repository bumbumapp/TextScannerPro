package com.bumbumapps.studytextscan.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.bumbumapps.studytextscan.Constant.BOTH
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "prefs")

class AppPreferences(private val dataStore: DataStore<Preferences>) {



    val isHorizontal:Flow<String>
      get() = dataStore.data.map {
          it[isHorizontall]?:BOTH
      }


    suspend fun setViewNotHorizontal(string: String){
        dataStore.edit {
            it[isHorizontall]=string
        }
    }

    suspend fun incrementSupportCount() {
        dataStore.edit {
            var current = it[SCAN_COUNT] ?: 6
            current += 1
            it[SCAN_COUNT] = current
        }
    }

    private companion object {
        val SCAN_COUNT = intPreferencesKey(name = "scan_count")
        val isHorizontall= stringPreferencesKey(name="is_Horizontal")


    }
}