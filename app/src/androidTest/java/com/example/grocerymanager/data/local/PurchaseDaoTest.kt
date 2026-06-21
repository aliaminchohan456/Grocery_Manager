package com.example.grocerymanager.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.grocerymanager.data.local.entity.CategoryEntity
import com.example.grocerymanager.data.local.entity.PurchaseEntity
import com.example.grocerymanager.data.local.entity.PurchaseItemEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PurchaseDaoTest {

    private lateinit var db: GroceryDatabase

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, GroceryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        // Seed a category and a default-style row so we can test the FK.
        db.categoryDao().insert(
            CategoryEntity(
                name = "Dairy",
                iconName = "Egg",
                colorHex = "#000000",
                isDefault = true,
                sortOrder = 1,
                createdAt = 0L,
                updatedAt = 0L,
            ),
        )
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun `savePurchaseWithItems inserts a purchase and its items in one transaction`() = runTest {
        val purchase = PurchaseEntity(
            purchaseDate = 1_700_000_000_000L,
            shopName = "Test Mart",
            totalAmount = 250L,
            notes = null,
            receiptImageUri = null,
            createdAt = 0L,
            updatedAt = 0L,
        )
        val items = listOf(
            PurchaseItemEntity(
                purchaseId = 0L,
                itemName = "Milk",
                groceryItemId = null,
                categoryId = 1L,
                quantity = 1.0,
                unit = "L",
                pricePerUnit = 100L,
                totalPrice = 100L,
                createdAt = 0L,
            ),
            PurchaseItemEntity(
                purchaseId = 0L,
                itemName = "Bread",
                groceryItemId = null,
                categoryId = 1L,
                quantity = 1.0,
                unit = "pcs",
                pricePerUnit = 150L,
                totalPrice = 150L,
                createdAt = 0L,
            ),
        )
        val newId = db.purchaseDao().savePurchaseWithItems(purchase, items)
        val loaded = db.purchaseDao().getWithItems(newId)
        assertThat(loaded).isNotNull()
        assertThat(loaded!!.purchase.shopName).isEqualTo("Test Mart")
        assertThat(loaded.items).hasSize(2)
        assertThat(loaded.items.map { it.itemName }).containsExactly("Milk", "Bread")
    }

    @Test
    fun `savePurchaseWithItems replaces items when called twice for the same purchase id`() = runTest {
        val purchase = PurchaseEntity(
            purchaseDate = 1_700_000_000_000L,
            shopName = "Test Mart",
            totalAmount = 100L,
            notes = null,
            receiptImageUri = null,
            createdAt = 0L,
            updatedAt = 0L,
        )
        val firstItems = listOf(
            PurchaseItemEntity(
                purchaseId = 0L, itemName = "Old1", groceryItemId = null, categoryId = 1L,
                quantity = 1.0, unit = "pcs", pricePerUnit = 50L, totalPrice = 50L, createdAt = 0L,
            ),
            PurchaseItemEntity(
                purchaseId = 0L, itemName = "Old2", groceryItemId = null, categoryId = 1L,
                quantity = 1.0, unit = "pcs", pricePerUnit = 50L, totalPrice = 50L, createdAt = 0L,
            ),
        )
        val id = db.purchaseDao().savePurchaseWithItems(purchase, firstItems)
        val replacement = listOf(
            PurchaseItemEntity(
                purchaseId = id, itemName = "New", groceryItemId = null, categoryId = 1L,
                quantity = 1.0, unit = "pcs", pricePerUnit = 100L, totalPrice = 100L, createdAt = 0L,
            ),
        )
        db.purchaseDao().savePurchaseWithItems(purchase.copy(id = id, totalAmount = 100L), replacement)
        val loaded = db.purchaseDao().getWithItems(id)!!
        assertThat(loaded.items).hasSize(1)
        assertThat(loaded.items.single().itemName).isEqualTo("New")
    }

    @Test
    fun `observeAllWithItems returns all purchases with their items`() = runTest {
        val now = 1_700_000_000_000L
        val p1 = PurchaseEntity(
            purchaseDate = now, shopName = "A", totalAmount = 100L,
            notes = null, receiptImageUri = null, createdAt = 0L, updatedAt = 0L,
        )
        val p1items = listOf(
            PurchaseItemEntity(
                purchaseId = 0L, itemName = "Apple", groceryItemId = null, categoryId = 1L,
                quantity = 1.0, unit = "pcs", pricePerUnit = 100L, totalPrice = 100L, createdAt = 0L,
            ),
        )
        val p2 = PurchaseEntity(
            purchaseDate = now + 1, shopName = "B", totalAmount = 200L,
            notes = null, receiptImageUri = null, createdAt = 0L, updatedAt = 0L,
        )
        val p2items = listOf(
            PurchaseItemEntity(
                purchaseId = 0L, itemName = "Bread", groceryItemId = null, categoryId = 1L,
                quantity = 1.0, unit = "pcs", pricePerUnit = 200L, totalPrice = 200L, createdAt = 0L,
            ),
            PurchaseItemEntity(
                purchaseId = 0L, itemName = "Milk", groceryItemId = null, categoryId = 1L,
                quantity = 1.0, unit = "L", pricePerUnit = 0L, totalPrice = 0L, createdAt = 0L,
            ),
        )
        db.purchaseDao().savePurchaseWithItems(p1, p1items)
        db.purchaseDao().savePurchaseWithItems(p2, p2items)

        val all = db.purchaseDao().observeAllWithItems().first()
        assertThat(all).hasSize(2)
        // Ordered by purchaseDate DESC, id DESC — the second is later so it should be first.
        assertThat(all[0].purchase.shopName).isEqualTo("B")
        assertThat(all[0].items).hasSize(2)
        assertThat(all[1].purchase.shopName).isEqualTo("A")
        assertThat(all[1].items).hasSize(1)
    }
}
