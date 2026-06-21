package com.example.grocerymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.grocerymanager.data.local.entity.CategoryTotal
import com.example.grocerymanager.data.local.entity.MonthlyTotal
import com.example.grocerymanager.data.local.entity.PurchaseEntity
import com.example.grocerymanager.data.local.entity.PurchaseItemEntity
import com.example.grocerymanager.data.local.entity.PurchaseWithItems
import com.example.grocerymanager.data.local.entity.ShopTotal
import com.example.grocerymanager.data.local.entity.TopItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PurchaseItemEntity>): List<Long>

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Query("DELETE FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun deleteItemsForPurchase(purchaseId: Long)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseEntity)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun deletePurchaseById(id: Long): Int

    @Transaction
    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getWithItems(id: Long): PurchaseWithItems?

    @Transaction
    @Query("SELECT * FROM purchases WHERE id = :id")
    fun observeWithItems(id: Long): Flow<PurchaseWithItems?>

    @Transaction
    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC, id DESC")
    fun observeAllWithItems(): Flow<List<PurchaseWithItems>>

    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC, id DESC")
    fun observeAll(): Flow<List<PurchaseEntity>>

    @Query(
        """
        SELECT * FROM purchases
        WHERE purchaseDate BETWEEN :start AND :end
        ORDER BY purchaseDate DESC, id DESC
        """,
    )
    fun observeBetween(start: Long, end: Long): Flow<List<PurchaseEntity>>

    /**
     * Same range filter as [observeBetween] but the result is hydrated
     * with the line items via a single Room `@Transaction` (one SQL
     * statement + the in-memory join). Used by the Records screen so each
     * card can render the inline item preview without a second DB hit
     * per row.
     */
    @Transaction
    @Query(
        """
        SELECT * FROM purchases
        WHERE purchaseDate BETWEEN :start AND :end
        ORDER BY purchaseDate DESC, id DESC
        """,
    )
    fun observeBetweenWithItems(start: Long, end: Long): Flow<List<PurchaseWithItems>>

    @Query(
        """
        SELECT * FROM purchases
        WHERE purchaseDate BETWEEN :start AND :end
        ORDER BY purchaseDate DESC, id DESC
        LIMIT :limit
        """,
    )
    fun observeRecent(start: Long, end: Long, limit: Int): Flow<List<PurchaseEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(totalAmount), 0) FROM purchases
        WHERE purchaseDate BETWEEN :start AND :end
        """,
    )
    fun observeSpendBetween(start: Long, end: Long): Flow<Long>

    @Query(
        """
        SELECT pi.categoryId AS categoryId, COALESCE(SUM(pi.totalPrice), 0) AS total
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.id
        WHERE p.purchaseDate BETWEEN :start AND :end
        GROUP BY pi.categoryId
        ORDER BY total DESC
        """,
    )
    fun observeCategoryBreakdown(start: Long, end: Long): Flow<List<CategoryTotal>>

    @Query(
        """
        SELECT COALESCE(SUM(totalAmount), 0) FROM purchases
        WHERE purchaseDate BETWEEN :start AND :end
        """,
    )
    suspend fun totalBetween(start: Long, end: Long): Long

    @Query(
        """
        SELECT
            CAST(strftime('%Y', purchaseDate / 1000, 'unixepoch', 'localtime') AS INTEGER) AS year,
            CAST(strftime('%m', purchaseDate / 1000, 'unixepoch', 'localtime') AS INTEGER) AS month,
            COALESCE(SUM(totalAmount), 0) AS total
        FROM purchases
        WHERE purchaseDate >= :since
        GROUP BY year, month
        ORDER BY year DESC, month DESC
        """,
    )
    fun observeMonthlyTotals(since: Long): Flow<List<MonthlyTotal>>

    @Query(
        """
        SELECT shopName AS shopName, COALESCE(SUM(totalAmount), 0) AS total
        FROM purchases
        WHERE purchaseDate BETWEEN :start AND :end
        GROUP BY shopName
        ORDER BY total DESC
        """,
    )
    fun observeShopBreakdown(start: Long, end: Long): Flow<List<ShopTotal>>

    @Query(
        """
        SELECT pi.itemName AS itemName,
               COALESCE(SUM(pi.totalPrice), 0) AS totalSpent,
               COUNT(*) AS purchaseCount
        FROM purchase_items pi
        INNER JOIN purchases p ON pi.purchaseId = p.id
        WHERE p.purchaseDate BETWEEN :start AND :end
        GROUP BY LOWER(pi.itemName)
        ORDER BY totalSpent DESC
        LIMIT :limit
        """,
    )
    fun observeTopItems(start: Long, end: Long, limit: Int = 10): Flow<List<TopItem>>

    @Query(
        """
        SELECT DISTINCT shopName FROM purchases
        WHERE shopName IS NOT NULL AND shopName != ''
        ORDER BY shopName ASC
        """,
    )
    fun observeShopNames(): Flow<List<String>>

    @Transaction
    suspend fun savePurchaseWithItems(purchase: PurchaseEntity, items: List<PurchaseItemEntity>): Long {
        val purchaseId = if (purchase.id == 0L) {
            insertPurchase(purchase)
        } else {
            updatePurchase(purchase)
            deleteItemsForPurchase(purchase.id)
            purchase.id
        }
        val withId = items.map { it.copy(purchaseId = purchaseId) }
        insertItems(withId)
        return purchaseId
    }
}
