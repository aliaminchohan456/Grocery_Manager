package com.example.grocerymanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.grocerymanager.data.local.dao.BudgetDao
import com.example.grocerymanager.data.local.dao.CategoryDao
import com.example.grocerymanager.data.local.dao.GroceryItemDao
import com.example.grocerymanager.data.local.dao.PurchaseDao
import com.example.grocerymanager.data.local.dao.PurchaseItemDao
import com.example.grocerymanager.data.local.dao.ShoppingListDao
import com.example.grocerymanager.data.local.dao.StoreDao
import com.example.grocerymanager.data.local.entity.BudgetEntity
import com.example.grocerymanager.data.local.entity.CategoryEntity
import com.example.grocerymanager.data.local.entity.GroceryItemEntity
import com.example.grocerymanager.data.local.entity.PurchaseEntity
import com.example.grocerymanager.data.local.entity.PurchaseItemEntity
import com.example.grocerymanager.data.local.entity.ShoppingListEntity
import com.example.grocerymanager.data.local.entity.StoreEntity

@Database(
    entities = [
        CategoryEntity::class,
        GroceryItemEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        BudgetEntity::class,
        ShoppingListEntity::class,
        StoreEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class GroceryDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun groceryItemDao(): GroceryItemDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun purchaseItemDao(): PurchaseItemDao
    abstract fun budgetDao(): BudgetDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun storeDao(): StoreDao

    companion object {
        const val DATABASE_NAME = "grocery_manager.db"
    }
}
