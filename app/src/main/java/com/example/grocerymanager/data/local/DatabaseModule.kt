package com.example.grocerymanager.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GroceryDatabase {
        return Room.databaseBuilder(
            context,
            GroceryDatabase::class.java,
            GroceryDatabase.DATABASE_NAME,
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val now = System.currentTimeMillis()
                    seedDefaultCategories(db, now)
                    // Seed default items after categories so the foreign keys
                    // resolve and the user sees a populated catalog on first
                    // launch instead of empty category cards.
                    DefaultGroceryItems.seed(db, now)
                }
            })
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides fun provideCategoryDao(db: GroceryDatabase) = db.categoryDao()
    @Provides fun provideGroceryItemDao(db: GroceryDatabase) = db.groceryItemDao()
    @Provides fun providePurchaseDao(db: GroceryDatabase) = db.purchaseDao()
    @Provides fun providePurchaseItemDao(db: GroceryDatabase) = db.purchaseItemDao()
    @Provides fun provideBudgetDao(db: GroceryDatabase) = db.budgetDao()
    @Provides fun provideShoppingListDao(db: GroceryDatabase) = db.shoppingListDao()
    @Provides fun provideStoreDao(db: GroceryDatabase) = db.storeDao()
}
