package com.example.grocerymanager.domain.repository

/**
 * Cross-cutting "wipe everything" action that needs to touch every Room
 * table and the DataStore preferences file. Lives behind an interface so
 * the feature layer never imports a Room class.
 */
interface MaintenanceRepository {
    /** Drop every table and clear the DataStore. */
    suspend fun clearAll()
}
