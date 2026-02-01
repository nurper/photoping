package com.photoping.data.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "photoping")

class TokenStore(private val context: Context) : TokenStorage {
    private val tokenKey: Preferences.Key<String> = stringPreferencesKey("jwt_token")

    override val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[tokenKey]
    }

    override suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
        }
    }

    override suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(tokenKey)
        }
    }
}
