package com.example.grocerymanager.domain.usecase

import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.domain.model.GroceryItem
import com.example.grocerymanager.domain.repository.GroceryItemRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetGroceryItemsForCategoryUseCase @Inject constructor(
    private val repo: GroceryItemRepository,
) {
    fun observe(categoryId: Long) = repo.observeForCategory(categoryId)
}

class UpsertGroceryItemUseCase @Inject constructor(
    private val repo: GroceryItemRepository,
) {
    suspend operator fun invoke(
        id: Long,
        name: String,
        defaultCategoryId: Long,
        defaultUnit: String = "",
        iconName: String? = null,
    ): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return Result.failure(IllegalArgumentException("Item name cannot be empty"))
        }
        return runCatching {
            repo.upsert(
                GroceryItem(
                    id = id,
                    name = trimmed,
                    defaultCategoryId = defaultCategoryId,
                    defaultUnit = defaultUnit,
                    iconName = iconName,
                    lastPricePerUnit = null,
                ),
            )
        }
    }
}

class DeleteGroceryItemUseCase @Inject constructor(
    private val repo: GroceryItemRepository,
) {
    suspend operator fun invoke(id: Long): Boolean = repo.delete(id)
}
