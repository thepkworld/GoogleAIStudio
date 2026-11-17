package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.cineVaultDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "cinevault_preferences"
)

data class AppPreferencesState(
    val isDarkTheme: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val isGuestMode: Boolean = false,
    val lastSyncedUid: String? = null,
    val activeLocalAccountId: String? = null
)

class AppPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    constructor(context: Context) : this(context.applicationContext.cineVaultDataStore)

    val preferencesFlow: Flow<AppPreferencesState> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            AppPreferencesState(
                isDarkTheme = prefs[KEY_DARK_THEME] ?: true,
                hasCompletedOnboarding = prefs[KEY_ONBOARDING_DONE] ?: false,
                isGuestMode = prefs[KEY_GUEST_MODE] ?: false,
                lastSyncedUid = prefs[KEY_LAST_SYNCED_UID]?.ifBlank { null },
                activeLocalAccountId = prefs[KEY_ACTIVE_LOCAL_ACCOUNT_ID]?.ifBlank { null }
            )
        }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_DARK_THEME] = enabled
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean, isGuest: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_DONE] = completed
            prefs[KEY_GUEST_MODE] = isGuest
        }
    }

    suspend fun setGuestMode(isGuest: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_GUEST_MODE] = isGuest
        }
    }

    suspend fun setLastSyncedUid(uid: String?) {
        dataStore.edit { prefs ->
            if (uid.isNullOrBlank()) {
                prefs.remove(KEY_LAST_SYNCED_UID)
            } else {
                prefs[KEY_LAST_SYNCED_UID] = uid
            }
        }
    }

    suspend fun setActiveLocalAccountId(accountId: String?) {
        dataStore.edit { prefs ->
            if (accountId.isNullOrBlank()) {
                prefs.remove(KEY_ACTIVE_LOCAL_ACCOUNT_ID)
            } else {
                prefs[KEY_ACTIVE_LOCAL_ACCOUNT_ID] = accountId
                prefs[KEY_ONBOARDING_DONE] = true
                prefs[KEY_GUEST_MODE] = false
            }
        }
    }

    private companion object {
        val KEY_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val KEY_ONBOARDING_DONE = booleanPreferencesKey("has_completed_onboarding")
        val KEY_GUEST_MODE = booleanPreferencesKey("is_guest_mode")
        val KEY_LAST_SYNCED_UID = stringPreferencesKey("last_synced_uid")
        val KEY_ACTIVE_LOCAL_ACCOUNT_ID = stringPreferencesKey("active_local_account_id")
    }
}
