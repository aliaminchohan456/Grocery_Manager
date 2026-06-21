package com.example.grocerymanager.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GroceryDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        GroceryDatabase::class.java,
    )

    private val dbName = "grocery_manager.db"

    @Test
    fun `v1 to v2 migration adds indices and FK on shopping_list while preserving rows`() {
        // Create a v1 database manually.
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                "INSERT INTO categories (name, iconName, colorHex, isDefault, sortOrder, createdAt, updatedAt) " +
                    "VALUES ('Dairy', 'Egg', '#000', 1, 1, 0, 0)",
            )
            execSQL(
                "INSERT INTO shopping_list (name, quantity, unit, categoryId, isPurchased, purchasedAt, createdAt, updatedAt) " +
                    "VALUES ('Milk', 2.0, 'L', 1, 0, NULL, 0, 0)",
            )
            close()
        }

        // Run the migration.
        val db = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)

        // 1. Original row preserved.
        val cursor = db.query("SELECT name, categoryId FROM shopping_list")
        cursor.use {
            assertThat(it.moveToFirst()).isTrue()
            assertThat(it.getString(0)).isEqualTo("Milk")
            assertThat(it.getLong(1)).isEqualTo(1L)
        }

        // 2. New indices exist.
        val indexRows = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='shopping_list'")
        val indexNames = mutableListOf<String>()
        indexRows.use { c -> while (c.moveToNext()) indexNames += c.getString(0) }
        assertThat(indexNames).contains("index_shopping_list_isPurchased")
        assertThat(indexNames).contains("index_shopping_list_categoryId")

        // 3. Foreign key is present in the table definition.
        val schema = db.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='shopping_list'")
        val ddl = schema.use { c -> if (c.moveToFirst()) c.getString(0) ?: "" else "" }
        assertThat(ddl).contains("FOREIGN KEY")
        assertThat(ddl).contains("REFERENCES `categories`(`id`)")
    }

    @Test
    fun `v1 to v2 migration preserves isPurchased across the temp table copy`() {
        // Reproduces a real-world risk: the temp-table rebuild must not
        // drop the isPurchased column. We seed both flavours of rows and
        // assert they round-trip.
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                "INSERT INTO categories (name, iconName, colorHex, isDefault, sortOrder, createdAt, updatedAt) " +
                    "VALUES ('Dairy', 'Egg', '#000', 1, 1, 0, 0)",
            )
            execSQL(
                "INSERT INTO shopping_list (name, quantity, unit, categoryId, isPurchased, purchasedAt, createdAt, updatedAt) " +
                    "VALUES ('Milk', 2.0, 'L', 1, 0, NULL, 0, 0)",
            )
            execSQL(
                "INSERT INTO shopping_list (name, quantity, unit, categoryId, isPurchased, purchasedAt, createdAt, updatedAt) " +
                    "VALUES ('Cheese', 1.0, 'pcs', 1, 1, 1700000000000, 0, 0)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)
        val cursor = db.query("SELECT name, isPurchased, purchasedAt FROM shopping_list ORDER BY name")
        cursor.use {
            assertThat(it.moveToFirst()).isTrue()
            assertThat(it.getString(0)).isEqualTo("Cheese")
            assertThat(it.getInt(1)).isEqualTo(1)
            assertThat(it.getLong(2)).isEqualTo(1700000000000L)
            assertThat(it.moveToNext()).isTrue()
            assertThat(it.getString(0)).isEqualTo("Milk")
            assertThat(it.getInt(1)).isEqualTo(0)
            assertThat(it.getLong(2)).isEqualTo(0L)
        }
    }

    @Test
    fun `v2 to v3 migration adds iconName column and seeds default items`() {
        helper.createDatabase(dbName, 2).apply {
            execSQL(
                "INSERT INTO categories (name, iconName, colorHex, isDefault, sortOrder, createdAt, updatedAt) " +
                    "VALUES ('Dairy & Eggs', 'Egg', '#2E7D32', 1, 1, 0, 0)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(dbName, 3, true, MIGRATION_2_3)

        // iconName column exists.
        val columns = db.query("PRAGMA table_info(grocery_items)")
        val columnNames = mutableListOf<String>()
        columns.use { c -> while (c.moveToNext()) columnNames += c.getString(1) }
        assertThat(columnNames).contains("iconName")

        // Default seed for the 'Dairy & Eggs' category is present.
        val cursor = db.query(
            "SELECT name, iconName, defaultCategoryId FROM grocery_items WHERE defaultCategoryId = " +
                "(SELECT id FROM categories WHERE name = 'Dairy & Eggs')",
        )
        val itemNames = mutableListOf<String>()
        cursor.use { c -> while (c.moveToNext()) itemNames += c.getString(0) }
        assertThat(itemNames).contains("Milk")
        assertThat(itemNames).contains("Eggs")
    }
}
