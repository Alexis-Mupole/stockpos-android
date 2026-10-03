package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.RefundDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.StockMovementDao
import com.example.data.local.db.StockPosDatabase
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PaymentMethod
import com.example.data.local.entity.RefundEntity
import com.example.data.local.entity.RefundItemEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.SaleStatus
import com.example.data.local.entity.StockMovementEntity
import com.example.data.local.entity.StockMovementType
import com.example.data.local.relations.SaleWithItemsAndPayments
import com.example.data.local.relations.UserSalesSummary
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

interface SaleRepository {
    fun getAllSales(): Flow<List<SaleWithItemsAndPayments>>
    fun getSalesByUserId(userId: Long): Flow<List<SaleWithItemsAndPayments>>
    fun getSalesBetween(startMs: Long, endMs: Long): Flow<List<SaleWithItemsAndPayments>>
    fun getSalesForUserBetween(userId: Long, startMs: Long, endMs: Long): Flow<List<SaleWithItemsAndPayments>>
    suspend fun getSaleById(id: Long): SaleWithItemsAndPayments?
    suspend fun getSaleByReceiptNo(receiptNo: String): SaleWithItemsAndPayments?
    fun getTodayTotalSales(startOfDayMs: Long): Flow<Long?>
    fun getTodaySellerTotalSales(userId: Long, startOfDayMs: Long): Flow<Long?>
    fun getTodaySalesCount(startOfDayMs: Long): Flow<Int>
    fun getTodaySellerSalesCount(userId: Long, startOfDayMs: Long): Flow<Int>
    fun getUserSalesSummaries(): Flow<List<UserSalesSummary>>

    suspend fun generateNextReceiptNumber(): String

    suspend fun checkout(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        payments: List<PaymentEntity>,
        allowNegativeStock: Boolean
    ): Result<Long>

    suspend fun refundSale(
        saleId: Long,
        actingUserId: Long,
        refundItems: List<Pair<SaleItemEntity, Int>>, // item and return qty
        reason: String,
        restock: Boolean
    ): Result<Long>

    suspend fun voidSale(
        saleId: Long,
        actingUserId: Long,
        reason: String
    ): Result<Unit>
}

class SaleRepositoryImpl(
    private val database: StockPosDatabase,
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao,
    private val refundDao: RefundDao,
    private val customerDao: CustomerDao
) : SaleRepository {

    private val seqCounter = AtomicInteger(0)

    override fun getAllSales(): Flow<List<SaleWithItemsAndPayments>> = saleDao.getAllSales()

    override fun getSalesByUserId(userId: Long): Flow<List<SaleWithItemsAndPayments>> =
        saleDao.getSalesByUserId(userId)

    override fun getSalesBetween(startMs: Long, endMs: Long): Flow<List<SaleWithItemsAndPayments>> =
        saleDao.getSalesBetween(startMs, endMs)

    override fun getSalesForUserBetween(
        userId: Long,
        startMs: Long,
        endMs: Long
    ): Flow<List<SaleWithItemsAndPayments>> =
        saleDao.getSalesForUserBetween(userId, startMs, endMs)

    override suspend fun getSaleById(id: Long): SaleWithItemsAndPayments? =
        saleDao.getSaleById(id)

    override suspend fun getSaleByReceiptNo(receiptNo: String): SaleWithItemsAndPayments? =
        saleDao.getSaleByReceiptNo(receiptNo)

    override fun getTodayTotalSales(startOfDayMs: Long): Flow<Long?> =
        saleDao.getTodayTotalSales(startOfDayMs)

    override fun getTodaySellerTotalSales(userId: Long, startOfDayMs: Long): Flow<Long?> =
        saleDao.getTodaySellerTotalSales(userId, startOfDayMs)

    override fun getTodaySalesCount(startOfDayMs: Long): Flow<Int> =
        saleDao.getTodaySalesCount(startOfDayMs)

    override fun getTodaySellerSalesCount(userId: Long, startOfDayMs: Long): Flow<Int> =
        saleDao.getTodaySellerSalesCount(userId, startOfDayMs)

    override fun getUserSalesSummaries(): Flow<List<UserSalesSummary>> =
        saleDao.getUserSalesSummaries()

    override suspend fun generateNextReceiptNumber(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        val countToday = saleDao.countSalesSince(startOfDay)
        val nextSeq = countToday + seqCounter.incrementAndGet()
        val datePart = SimpleDateFormat("yyMMdd", Locale.US).format(Date())
        return String.format(Locale.US, "R%s-%04d", datePart, nextSeq)
    }

    override suspend fun checkout(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        payments: List<PaymentEntity>,
        allowNegativeStock: Boolean
    ): Result<Long> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Cart cannot be empty"))
        }

        return runCatching {
            database.withTransaction {
                // 1. Stock validation check if negative stock is disallowed
                for (item in items) {
                    val product = productDao.getProductById(item.productId)
                        ?: throw IllegalStateException("Product '${item.productNameSnapshot}' not found")

                    if (!allowNegativeStock && product.stockQty < item.qty) {
                        throw IllegalStateException(
                            "Insufficient stock for '${product.name}'. Available: ${product.stockQty}, Requested: ${item.qty}"
                        )
                    }
                }

                // 2. Insert Sale header
                val saleId = saleDao.insertSale(sale)

                // 3. Insert Sale Items with saleId
                val itemsWithId = items.map { it.copy(saleId = saleId) }
                saleDao.insertSaleItems(itemsWithId)

                // 4. Insert Payments with saleId
                val paymentsWithId = payments.map { it.copy(saleId = saleId) }
                saleDao.insertPayments(paymentsWithId)

                // 5. Decrement stock & record StockMovement for each item
                for (item in items) {
                    val product = productDao.getProductById(item.productId)!!
                    val newQty = product.stockQty - item.qty
                    productDao.updateStockQty(item.productId, newQty)

                    val movement = StockMovementEntity(
                        productId = item.productId,
                        type = StockMovementType.SALE,
                        qtyChange = -item.qty,
                        qtyAfter = newQty,
                        unitCost = item.unitCostSnapshot,
                        referenceType = "SALE",
                        referenceId = saleId,
                        reason = "Sale #${sale.receiptNo}",
                        userId = sale.userId
                    )
                    stockMovementDao.insertMovement(movement)
                }

                // 6. If credit payment was made and customer attached, update customer owed balance
                val creditPayment = payments.find { it.method == PaymentMethod.CREDIT }
                if (creditPayment != null && sale.customerId != null) {
                    customerDao.updateBalance(sale.customerId, creditPayment.amount)
                }

                saleId
            }
        }
    }

    override suspend fun refundSale(
        saleId: Long,
        actingUserId: Long,
        refundItems: List<Pair<SaleItemEntity, Int>>,
        reason: String,
        restock: Boolean
    ): Result<Long> {
        return runCatching {
            database.withTransaction {
                val originalSale = saleDao.getSaleById(saleId)
                    ?: throw IllegalArgumentException("Sale not found")

                var refundTotal = 0L
                for ((item, qty) in refundItems) {
                    val itemRefundAmount = (item.lineTotal / item.qty) * qty
                    refundTotal += itemRefundAmount
                }

                val refund = RefundEntity(
                    saleId = saleId,
                    userId = actingUserId,
                    total = refundTotal,
                    reason = reason,
                    restocked = restock
                )
                val refundId = refundDao.insertRefund(refund)

                val refundItemEntities = refundItems.map { (item, qty) ->
                    val itemRefundAmount = (item.lineTotal / item.qty) * qty
                    RefundItemEntity(
                        refundId = refundId,
                        saleItemId = item.id,
                        productId = item.productId,
                        qty = qty,
                        refundAmount = itemRefundAmount
                    )
                }
                refundDao.insertRefundItems(refundItemEntities)

                // Update sale status
                val isFullRefund = refundItems.all { (item, qty) -> qty == item.qty }
                val newStatus = if (isFullRefund) SaleStatus.REFUNDED else SaleStatus.PARTIALLY_REFUNDED
                saleDao.updateSaleStatus(saleId, newStatus)

                // If restock requested, return to stock
                if (restock) {
                    for ((item, qty) in refundItems) {
                        val product = productDao.getProductById(item.productId)
                        if (product != null) {
                            val newQty = product.stockQty + qty
                            productDao.updateStockQty(product.id, newQty)

                            val movement = StockMovementEntity(
                                productId = product.id,
                                type = StockMovementType.RETURN,
                                qtyChange = qty,
                                qtyAfter = newQty,
                                unitCost = item.unitCostSnapshot,
                                referenceType = "REFUND",
                                referenceId = refundId,
                                reason = "Refund #${originalSale.sale.receiptNo}: $reason",
                                userId = actingUserId
                            )
                            stockMovementDao.insertMovement(movement)
                        }
                    }
                }

                refundId
            }
        }
    }

    override suspend fun voidSale(
        saleId: Long,
        actingUserId: Long,
        reason: String
    ): Result<Unit> {
        return runCatching {
            database.withTransaction {
                val saleDetails = saleDao.getSaleById(saleId)
                    ?: throw IllegalArgumentException("Sale not found")

                if (saleDetails.sale.status == SaleStatus.VOIDED) {
                    throw IllegalStateException("Sale is already voided")
                }

                saleDao.updateSaleStatus(saleId, SaleStatus.VOIDED)

                // Reverse stock deductions
                for (item in saleDetails.items) {
                    val product = productDao.getProductById(item.productId)
                    if (product != null) {
                        val newQty = product.stockQty + item.qty
                        productDao.updateStockQty(product.id, newQty)

                        val movement = StockMovementEntity(
                            productId = product.id,
                            type = StockMovementType.VOID,
                            qtyChange = item.qty,
                            qtyAfter = newQty,
                            unitCost = item.unitCostSnapshot,
                            referenceType = "VOID",
                            referenceId = saleId,
                            reason = "Void #${saleDetails.sale.receiptNo}: $reason",
                            userId = actingUserId
                        )
                        stockMovementDao.insertMovement(movement)
                    }
                }
            }
        }
    }
}
