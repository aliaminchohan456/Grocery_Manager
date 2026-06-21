package com.example.grocerymanager.domain.usecase

import com.example.grocerymanager.domain.model.Purchase
import com.example.grocerymanager.domain.model.PurchaseItem
import com.example.grocerymanager.domain.model.ShoppingListItem
import com.example.grocerymanager.domain.repository.PurchaseRepository
import com.example.grocerymanager.domain.repository.ShoppingListRepository
import javax.inject.Inject

class AddShoppingListItemUseCase @Inject constructor(
    private val repo: ShoppingListRepository,
) {
    suspend operator fun invoke(name: String, quantity: Double = 1.0, unit: String = "", categoryId: Long? = null): Long {
        val now = System.currentTimeMillis()
        return repo.add(
            ShoppingListItem(
                id = 0,
                name = name.trim(),
                quantity = quantity,
                unit = unit,
                categoryId = categoryId,
                isPurchased = false,
                purchasedAt = null,
            ),
        )
    }
}

class ConvertShoppingListToPurchaseUseCase @Inject constructor(
    private val shopping: ShoppingListRepository,
    private val purchases: PurchaseRepository,
) {
    /**
     * Convert shopping list items into a single draft purchase. Each
     * shopping item becomes a purchase item with the same name, quantity,
     * unit, and category. `pricePerUnit` and `totalPrice` are zero — the
     * user fills them in before saving.
     *
     * Note: this persists a draft with `totalAmount = 0L` **directly** via
     * [PurchaseRepository.save], intentionally **bypassing**
     * [com.example.grocerymanager.domain.usecase.SavePurchaseUseCase] (which
     * rejects non-positive totals). The resulting row is meant to be opened
     * in the AddPurchase screen and saved again through the final-save path
     * where the `> 0` guard then applies. Keeping the two paths distinct
     * avoids the draft creation being blocked.
     */
    suspend operator fun invoke(
        items: List<ShoppingListItem>,
        defaultCategoryId: Long,
    ): Long {
        if (items.isEmpty()) return -1
        val now = System.currentTimeMillis()
        val purchase = Purchase(
            id = 0,
            purchaseDate = now,
            shopName = null,
            totalAmount = 0L,
            notes = null,
            receiptImageUri = null,
            createdAt = now,
            updatedAt = now,
        )
        val purchaseItems = items.map { item ->
            PurchaseItem(
                id = 0,
                purchaseId = 0,
                itemName = item.name,
                groceryItemId = null,
                categoryId = item.categoryId ?: defaultCategoryId,
                quantity = item.quantity,
                unit = item.unit,
                pricePerUnit = null,
                totalPrice = 0L,
            )
        }
        val id = purchases.save(purchase, purchaseItems)
        items.forEach { shopping.delete(it.id) }
        return id
    }
}
