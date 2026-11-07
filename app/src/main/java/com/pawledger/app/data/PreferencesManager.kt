package com.pawledger.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pawledger_prefs")

/**
 * DataStore 偏好设置。
 */
class PreferencesManager(private val context: Context) {

    private object Keys {
        val PERMISSION_GUIDE_DONE = booleanPreferencesKey("permission_guide_done")
        val REMINDER_OVERSPEND = booleanPreferencesKey("reminder_overspend")
        val REMINDER_GOAL = booleanPreferencesKey("reminder_goal")
        val REMINDER_BILL = booleanPreferencesKey("reminder_bill")
    }

    val permissionGuideDone: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.PERMISSION_GUIDE_DONE] ?: false }

    val reminderOverspend: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.REMINDER_OVERSPEND] ?: true }

    val reminderGoal: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.REMINDER_GOAL] ?: true }

    val reminderBill: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.REMINDER_BILL] ?: true }

    suspend fun setPermissionGuideDone(done: Boolean) {
        context.dataStore.edit { it[Keys.PERMISSION_GUIDE_DONE] = done }
    }

    suspend fun setReminderOverspend(on: Boolean) {
        context.dataStore.edit { it[Keys.REMINDER_OVERSPEND] = on }
    }

    suspend fun setReminderGoal(on: Boolean) {
        context.dataStore.edit { it[Keys.REMINDER_GOAL] = on }
    }

    suspend fun setReminderBill(on: Boolean) {
        context.dataStore.edit { it[Keys.REMINDER_BILL] = on }
    }
}
