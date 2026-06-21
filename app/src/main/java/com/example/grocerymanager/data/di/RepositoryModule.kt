package com.example.grocerymanager.data.di

import com.example.grocerymanager.data.repository.BudgetRepositoryImpl
import com.example.grocerymanager.data.repository.CategoryRepositoryImpl
import com.example.grocerymanager.data.repository.GroceryItemRepositoryImpl
import com.example.grocerymanager.data.repository.MaintenanceRepositoryImpl
import com.example.grocerymanager.data.repository.PriceHistoryRepositoryImpl
import com.example.grocerymanager.data.repository.PurchaseRepositoryImpl
import com.example.grocerymanager.data.repository.ShoppingListRepositoryImpl
import com.example.grocerymanager.data.repository.StoreRepositoryImpl
import com.example.grocerymanager.domain.repository.BudgetRepository
import com.example.grocerymanager.domain.repository.CategoryRepository
import com.example.grocerymanager.domain.repository.GroceryItemRepository
import com.example.grocerymanager.domain.repository.MaintenanceRepository
import com.example.grocerymanager.domain.repository.PriceHistoryRepository
import com.example.grocerymanager.domain.repository.PurchaseRepository
import com.example.grocerymanager.domain.repository.ShoppingListRepository
import com.example.grocerymanager.domain.repository.StoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindCategory(impl: CategoryRepositoryImpl): CategoryRepository
    @Binds @Singleton abstract fun bindGroceryItem(impl: GroceryItemRepositoryImpl): GroceryItemRepository
    @Binds @Singleton abstract fun bindPurchase(impl: PurchaseRepositoryImpl): PurchaseRepository
    @Binds @Singleton abstract fun bindPriceHistory(impl: PriceHistoryRepositoryImpl): PriceHistoryRepository
    @Binds @Singleton abstract fun bindBudget(impl: BudgetRepositoryImpl): BudgetRepository
    @Binds @Singleton abstract fun bindShoppingList(impl: ShoppingListRepositoryImpl): ShoppingListRepository
    @Binds @Singleton abstract fun bindStore(impl: StoreRepositoryImpl): StoreRepository
    @Binds @Singleton abstract fun bindMaintenance(impl: MaintenanceRepositoryImpl): MaintenanceRepository
}
