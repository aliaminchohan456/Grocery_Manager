package com.example.grocerymanager.data.repository

import com.example.grocerymanager.data.local.dao.CategoryDao
import com.example.grocerymanager.data.mapper.toDomain
import com.example.grocerymanager.data.mapper.toEntity
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.repository.CategoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
) : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Category? = dao.getById(id)?.toDomain()

    override suspend fun add(category: Category): Long {
        val now = System.currentTimeMillis()
        val entity = category.copy(id = 0).toEntity(now)
        return dao.insert(entity)
    }

    override suspend fun update(category: Category) {
        val existing = dao.getById(category.id) ?: return
        // The mapper sets createdAt from the argument, so we pass the
        // original timestamp to preserve it. updatedAt is set to "now" by
        // the mapper.
        dao.update(category.toEntity(existing.createdAt))
    }

    /**
     * Persist a new ordering for the categories. The list must contain
     * every id the user wants to keep in the new order; we re-assign
     * sequential sortOrder values starting at 0 (with 100-step gaps so
     * future inserts don't fight with existing values).
     */
    override suspend fun reorder(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id ->
            dao.updateSortOrder(id, (index + 1) * 100)
        }
    }

    override suspend fun delete(id: Long): Boolean = dao.delete(id) > 0
}
