package com.example.grocerymanager.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2 migration. Preserves all user data.
 *
 * Changes:
 *  1. Add an index on `shopping_list(isPurchased)`.
 *  2. Rebuild `shopping_list` with:
 *       - an index on `categoryId`
 *       - a `FOREIGN KEY (categoryId) REFERENCES categories(id) ON DELETE SET NULL`
 *
 *  SQLite does not support adding a foreign key in-place, so the migration
 *  creates a temporary table mirroring the v1 schema, copies every row,
 *  drops the original, and re-creates it with the FK + new index.
 */
val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create the temporary table with the same shape as v1 (no FK yet).
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `shopping_list_tmp` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`quantity` REAL NOT NULL, " +
                "`unit` TEXT NOT NULL, " +
                "`categoryId` INTEGER, " +
                "`isPurchased` INTEGER NOT NULL, " +
                "`purchasedAt` INTEGER, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL)",
        )
        // 2. Copy every existing row.
        db.execSQL(
            "INSERT OR IGNORE INTO `shopping_list_tmp` " +
                "(`id`, `name`, `quantity`, `unit`, `categoryId`, " +
                "`isPurchased`, `purchasedAt`, `createdAt`, `updatedAt`) " +
                "SELECT `id`, `name`, `quantity`, `unit`, `categoryId`, " +
                "`isPurchased`, `purchasedAt`, `createdAt`, `updatedAt` " +
                "FROM `shopping_list`",
        )
        // 3. Drop the original table.
        db.execSQL("DROP TABLE `shopping_list`")

        // 4. Re-create the table with the foreign key to categories.
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `shopping_list` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`quantity` REAL NOT NULL, " +
                "`unit` TEXT NOT NULL, " +
                "`categoryId` INTEGER, " +
                "`isPurchased` INTEGER NOT NULL, " +
                "`purchasedAt` INTEGER, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`updatedAt` INTEGER NOT NULL, " +
                "FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE SET NULL )",
        )

        // 5. Copy the rows back.
        db.execSQL(
            "INSERT OR IGNORE INTO `shopping_list` " +
                "(`id`, `name`, `quantity`, `unit`, `categoryId`, " +
                "`isPurchased`, `purchasedAt`, `createdAt`, `updatedAt`) " +
                "SELECT `id`, `name`, `quantity`, `unit`, `categoryId`, " +
                "`isPurchased`, `purchasedAt`, `createdAt`, `updatedAt` " +
                "FROM `shopping_list_tmp`",
        )
        // 6. Drop the temp table.
        db.execSQL("DROP TABLE `shopping_list_tmp`")

        // 7. Create the two indices Room expects.
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_shopping_list_isPurchased` " +
                "ON `shopping_list` (`isPurchased`)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_shopping_list_categoryId` " +
                "ON `shopping_list` (`categoryId`)",
        )
    }
}

/**
 * v2 → v3 migration. Preserves all user data.
 *
 * Changes:
 *  1. Add an optional `iconName` column to `grocery_items` so each item can
 *     carry its own visual override. Existing rows get NULL, which the UI
 *     resolves to the parent category's icon.
 *  2. Seed the default grocery items for every default category so the
 *     catalog isn't empty after the upgrade. The seed is `INSERT OR IGNORE`,
 *     so it never overwrites items the user has already added.
 */
val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `grocery_items` ADD COLUMN `iconName` TEXT")
        DefaultGroceryItems.seed(db, System.currentTimeMillis())
    }
}
