package com.example.grocerymanager.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "grocery_manager_prefs",
)

@Singleton
open class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore: DataStore<Preferences> = context.userPreferencesDataStore

    open val preferences: Flow<UserPreferences> = dataStore.data.map { it.toUserPreferences() }

    open suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    open suspend fun setSetupComplete(complete: Boolean) {
        dataStore.edit { it[Keys.SETUP_COMPLETE] = complete }
    }

    open suspend fun setCurrencyCode(code: String) {
        dataStore.edit { it[Keys.CURRENCY_CODE] = code }
    }

    open suspend fun setUnitSystem(system: UnitSystem) {
        dataStore.edit { it[Keys.UNIT_SYSTEM] = system.name }
    }

    open suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    open suspend fun setAlertThreshold(value: Float) {
        dataStore.edit { it[Keys.ALERT_THRESHOLD] = value.coerceIn(0f, 1f) }
    }

    open suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

}

private object Keys {
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
    val CURRENCY_CODE = stringPreferencesKey("currency_code")
    val UNIT_SYSTEM = stringPreferencesKey("unit_system")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val ALERT_THRESHOLD = floatPreferencesKey("alert_threshold")
}

data class UserPreferences(
    val onboardingComplete: Boolean = false,
    val setupComplete: Boolean = false,
    val currencyCode: String = "USD",
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val themeMode: ThemeMode = ThemeMode.System,
    val alertThreshold: Float = 0.8f,
)

private fun Preferences.toUserPreferences(): UserPreferences = UserPreferences(
    onboardingComplete = this[Keys.ONBOARDING_COMPLETE] ?: false,
    setupComplete = this[Keys.SETUP_COMPLETE] ?: false,
    currencyCode = this[Keys.CURRENCY_CODE] ?: "USD",
    unitSystem = this[Keys.UNIT_SYSTEM]?.let { runCatching { UnitSystem.valueOf(it) }.getOrNull() } ?: UnitSystem.Metric,
    themeMode = this[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.System,
    alertThreshold = this[Keys.ALERT_THRESHOLD] ?: 0.8f,
)
