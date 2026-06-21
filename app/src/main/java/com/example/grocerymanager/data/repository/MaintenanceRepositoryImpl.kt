package com.example.grocerymanager.data.repository

import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.data.local.GroceryDatabase
import com.example.grocerymanager.domain.repository.MaintenanceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceRepositoryImpl @Inject constructor(
    private val database: GroceryDatabase,
    private val preferences: UserPreferencesRepository,
) : MaintenanceRepository {
    override suspend fun clearAll() {
        database.clearAllTables()
        preferences.clearAll()
    }
}
