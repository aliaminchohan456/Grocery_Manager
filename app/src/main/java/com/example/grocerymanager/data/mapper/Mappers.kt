package com.example.grocerymanager.data.mapper

import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.data.local.entity.BudgetEntity
import com.example.grocerymanager.data.local.entity.CategoryEntity
import com.example.grocerymanager.data.local.entity.CategoryTotal as CategoryTotalEntity
import com.example.grocerymanager.data.local.entity.GroceryItemEntity
import com.example.grocerymanager.data.local.entity.MonthlyTotal as MonthlyTotalEntity
import com.example.grocerymanager.data.local.entity.PricePoint as PricePointEntity
import com.example.grocerymanager.data.local.entity.PurchaseEntity
import com.example.grocerymanager.data.local.entity.PurchaseItemEntity
import com.example.grocerymanager.data.local.entity.PurchaseWithItems as PurchaseWithItemsEntity
import com.example.grocerymanager.data.local.entity.ShopTotal as ShopTotalEntity
import com.example.grocerymanager.data.local.entity.ShoppingListEntity
import com.example.grocerymanager.data.local.entity.StoreEntity
import com.example.grocerymanager.data.local.entity.TopItem as TopItemEntity
import com.example.grocerymanager.domain.model.Budget
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.CategoryTotal
import com.example.grocerymanager.domain.model.GroceryItem
import com.example.grocerymanager.domain.model.MonthlyTotal
import com.example.grocerymanager.domain.model.PricePoint
import com.example.grocerymanager.domain.model.Purchase
import com.example.grocerymanager.domain.model.PurchaseItem
import com.example.grocerymanager.domain.model.PurchaseWithItems
import com.example.grocerymanager.domain.model.ShopTotal
import com.example.grocerymanager.domain.model.ShoppingListItem
import com.example.grocerymanager.domain.model.Store
import com.example.grocerymanager.domain.model.TopItem

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    iconName = iconName,
    colorHex = colorHex,
    isDefault = isDefault,
    sortOrder = sortOrder,
)

fun Category.toEntity(now: Long): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    iconName = iconName,
    colorHex = colorHex,
    isDefault = isDefault,
    sortOrder = sortOrder,
    createdAt = now,
    updatedAt = now,
)

fun GroceryItemEntity.toDomain(): GroceryItem = GroceryItem(
    id = id,
    name = name,
    defaultCategoryId = defaultCategoryId,
    defaultUnit = defaultUnit,
    iconName = iconName,
    lastPricePerUnit = lastPricePerUnit,
)

fun GroceryItem.toEntity(now: Long): GroceryItemEntity = GroceryItemEntity(
    id = id,
    name = name,
    defaultCategoryId = defaultCategoryId,
    defaultUnit = defaultUnit,
    iconName = iconName,
    lastPricePerUnit = lastPricePerUnit,
    createdAt = now,
    updatedAt = now,
)

fun PurchaseEntity.toDomain(): Purchase = Purchase(
    id = id,
    purchaseDate = purchaseDate,
    shopName = shopName,
    totalAmount = totalAmount,
    notes = notes,
    receiptImageUri = receiptImageUri,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Purchase.toEntity(): PurchaseEntity = PurchaseEntity(
    id = id,
    purchaseDate = purchaseDate,
    shopName = shopName,
    totalAmount = totalAmount,
    notes = notes,
    receiptImageUri = receiptImageUri,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun PurchaseItemEntity.toDomain(): PurchaseItem = PurchaseItem(
    id = id,
    purchaseId = purchaseId,
    itemName = itemName,
    groceryItemId = groceryItemId,
    categoryId = categoryId,
    quantity = quantity,
    unit = unit,
    pricePerUnit = pricePerUnit,
    totalPrice = totalPrice,
)

fun PurchaseItem.toEntity(): PurchaseItemEntity = PurchaseItemEntity(
    id = id,
    purchaseId = purchaseId,
    itemName = itemName,
    groceryItemId = groceryItemId,
    categoryId = categoryId,
    quantity = quantity,
    unit = unit,
    pricePerUnit = pricePerUnit,
    totalPrice = totalPrice,
    createdAt = System.currentTimeMillis(),
)

fun PurchaseWithItemsEntity.toDomain(): PurchaseWithItems = PurchaseWithItems(
    purchase = purchase.toDomain(),
    items = items.map { it.toDomain() },
)

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    month = month,
    year = year,
    budgetAmount = budgetAmount,
    alertThreshold = alertThreshold,
)

fun Budget.toEntity(now: Long): BudgetEntity = BudgetEntity(
    id = id,
    month = month,
    year = year,
    budgetAmount = budgetAmount,
    alertThreshold = alertThreshold,
    createdAt = now,
    updatedAt = now,
)

fun ShoppingListEntity.toDomain(): ShoppingListItem = ShoppingListItem(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit,
    categoryId = categoryId,
    isPurchased = isPurchased,
    purchasedAt = purchasedAt,
)

fun ShoppingListItem.toEntity(now: Long): ShoppingListEntity = ShoppingListEntity(
    id = id,
    name = name,
    quantity = quantity,
    unit = unit,
    categoryId = categoryId,
    isPurchased = isPurchased,
    purchasedAt = purchasedAt,
    createdAt = now,
    updatedAt = now,
)

fun StoreEntity.toDomain(): Store = Store(
    id = id,
    name = name,
    visitCount = visitCount,
    lastVisitAt = lastVisitAt,
)

fun Store.toEntity(now: Long): StoreEntity = StoreEntity(
    id = id,
    name = name,
    visitCount = visitCount,
    lastVisitAt = lastVisitAt,
    createdAt = now,
)

fun CategoryTotalEntity.toDomain(): CategoryTotal = CategoryTotal(categoryId, total as MinorUnits)
fun ShopTotalEntity.toDomain(): ShopTotal = ShopTotal(shopName, total as MinorUnits)
fun MonthlyTotalEntity.toDomain(): MonthlyTotal = MonthlyTotal(year, month, total as MinorUnits)
fun TopItemEntity.toDomain(): TopItem = TopItem(itemName, totalSpent as MinorUnits, purchaseCount)
fun PricePointEntity.toDomain(): PricePoint = PricePoint(purchaseDate, pricePerUnit as MinorUnits)
