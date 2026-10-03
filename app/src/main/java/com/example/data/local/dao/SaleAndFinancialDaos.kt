package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.AppSettingEntity
import com.example.data.local.entity.CashSessionEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PurchaseItemEntity
import com.example.data.local.entity.PurchaseOrderEntity
import com.example.data.local.entity.RefundEntity
import com.example.data.local.entity.RefundItemEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.SaleStatus
import com.example.data.local.relations.SaleWithItemsAndPayments
import com.example.data.local.relations.UserSalesSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Transaction
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<SaleWithItemsAndPayments>>

    @Transaction
    @Query("SELECT * FROM sales WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSalesByUserId(userId: Long): Flow<List<SaleWithItemsAndPayments>>

    @Transaction
    @Query("SELECT * FROM sales WHERE createdAt >= :startMs AND createdAt <= :endMs ORDER BY createdAt DESC")
    fun getSalesBetween(startMs: Long, endMs: Long): Flow<List<SaleWithItemsAndPayments>>

    @Transaction
    @Query("SELECT * FROM sales WHERE userId = :userId AND createdAt >= :startMs AND createdAt <= :endMs ORDER BY createdAt DESC")
    fun getSalesForUserBetween(userId: Long, startMs: Long, endMs: Long): Flow<List<SaleWithItemsAndPayments>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleById(id: Long): SaleWithItemsAndPayments?

    @Transaction
    @Query("SELECT * FROM sales WHERE receiptNo = :receiptNo LIMIT 1")
    suspend fun getSaleByReceiptNo(receiptNo: String): SaleWithItemsAndPayments?

    @Query("SELECT COUNT(*) FROM sales WHERE createdAt >= :startOfDayMs")
    suspend fun countSalesSince(startOfDayMs: Long): Int

    @Query("SELECT SUM(total) FROM sales WHERE status = 'COMPLETED' AND createdAt >= :startOfDayMs")
    fun getTodayTotalSales(startOfDayMs: Long): Flow<Long?>

    @Query("SELECT SUM(total) FROM sales WHERE status = 'COMPLETED' AND userId = :userId AND createdAt >= :startOfDayMs")
    fun getTodaySellerTotalSales(userId: Long, startOfDayMs: Long): Flow<Long?>

    @Query("SELECT COUNT(*) FROM sales WHERE status = 'COMPLETED' AND createdAt >= :startOfDayMs")
    fun getTodaySalesCount(startOfDayMs: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM sales WHERE status = 'COMPLETED' AND userId = :userId AND createdAt >= :startOfDayMs")
    fun getTodaySellerSalesCount(userId: Long, startOfDayMs: Long): Flow<Int>

    @Query(
        """
        SELECT 
            u.id AS userId,
            u.name AS userName,
            u.role AS userRole,
            COUNT(s.id) AS salesCount,
            COALESCE(SUM(s.total), 0) AS totalRevenue
        FROM users u
        LEFT JOIN sales s ON u.id = s.userId AND s.status = 'COMPLETED'
        GROUP BY u.id
        ORDER BY totalRevenue DESC
        """
    )
    fun getUserSalesSummaries(): Flow<List<UserSalesSummary>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Query("UPDATE sales SET status = :status WHERE id = :saleId")
    suspend fun updateSaleStatus(saleId: Long, status: SaleStatus)

    @Transaction
    suspend fun insertCompleteSale(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        payments: List<PaymentEntity>
    ): Long {
        val saleId = insertSale(sale)
        val itemsWithId = items.map { it.copy(saleId = saleId) }
        insertSaleItems(itemsWithId)
        val paymentsWithId = payments.map { it.copy(saleId = saleId) }
        insertPayments(paymentsWithId)
        return saleId
    }
}

@Dao
interface RefundDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRefund(refund: RefundEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRefundItems(items: List<RefundItemEntity>)

    @Query("SELECT * FROM refunds WHERE saleId = :saleId ORDER BY createdAt DESC")
    fun getRefundsForSale(saleId: Long): Flow<List<RefundEntity>>
}

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPurchaseOrder(order: PurchaseOrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)

    @Query("SELECT * FROM purchase_orders ORDER BY receivedAt DESC")
    fun getAllPurchases(): Flow<List<PurchaseOrderEntity>>
}

@Dao
interface CashSessionDao {
    @Query("SELECT * FROM cash_sessions WHERE userId = :userId AND closedAt IS NULL ORDER BY openedAt DESC LIMIT 1")
    suspend fun getActiveSessionForUser(userId: Long): CashSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CashSessionEntity): Long

    @Update
    suspend fun updateSession(session: CashSessionEntity)
}

@Dao
interface AppSettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    fun getSettingFlow(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSetting(setting: AppSettingEntity)

    @Query("SELECT * FROM app_settings")
    fun getAllSettings(): Flow<List<AppSettingEntity>>
}
