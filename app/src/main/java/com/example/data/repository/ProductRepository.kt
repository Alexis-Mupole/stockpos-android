package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.StockMovementDao
import com.example.data.local.db.StockPosDatabase
import com.example.data.local.entity.ProductBarcodeEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StockMovementEntity
import com.example.data.local.entity.StockMovementType
import com.example.data.local.relations.ProductWithBarcodesAndCategory
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllActiveProducts(): Flow<List<ProductWithBarcodesAndCategory>>
    fun getAllProductsIncludingArchived(): Flow<List<ProductWithBarcodesAndCategory>>
    fun getProductWithDetailsFlow(id: Long): Flow<ProductWithBarcodesAndCategory?>
    suspend fun getProductWithDetails(id: Long): ProductWithBarcodesAndCategory?
    suspend fun getProductById(id: Long): ProductEntity?
    fun searchProducts(query: String): Flow<List<ProductWithBarcodesAndCategory>>
    suspend fun findProductByBarcode(barcode: String): ProductWithBarcodesAndCategory?
    suspend fun findProductByBarcodeOrSku(code: String): ProductWithBarcodesAndCategory?
    fun getLowStockProductsFlow(): Flow<List<ProductEntity>>
    suspend fun getLowStockProductsList(): List<ProductEntity>
    fun countTotalProducts(): Flow<Int>
    fun countLowStock(): Flow<Int>
    fun countOutOfStock(): Flow<Int>
    suspend fun getTotalStockCostValue(): Long
    suspend fun getTotalRetailValue(): Long
    suspend fun saveProduct(product: ProductEntity, barcodes: List<String>): Long
    suspend fun updateProduct(product: ProductEntity, barcodes: List<String>)
    suspend fun setProductActive(productId: Long, isActive: Boolean)
    suspend fun isBarcodeTaken(barcode: String, excludeProductId: Long): Boolean
    suspend fun isSkuTaken(sku: String, excludeProductId: Long): Boolean
}

class ProductRepositoryImpl(
    private val database: StockPosDatabase,
    private val productDao: ProductDao
) : ProductRepository {

    override fun getAllActiveProducts(): Flow<List<ProductWithBarcodesAndCategory>> =
        productDao.getAllActiveProducts()

    override fun getAllProductsIncludingArchived(): Flow<List<ProductWithBarcodesAndCategory>> =
        productDao.getAllProductsIncludingArchived()

    override fun getProductWithDetailsFlow(id: Long): Flow<ProductWithBarcodesAndCategory?> =
        productDao.getProductWithDetailsFlow(id)

    override suspend fun getProductWithDetails(id: Long): ProductWithBarcodesAndCategory? =
        productDao.getProductWithDetails(id)

    override suspend fun getProductById(id: Long): ProductEntity? =
        productDao.getProductById(id)

    override fun searchProducts(query: String): Flow<List<ProductWithBarcodesAndCategory>> =
        productDao.searchProducts(query)

    override suspend fun findProductByBarcode(barcode: String): ProductWithBarcodesAndCategory? =
        productDao.findProductByBarcode(barcode)

    override suspend fun findProductByBarcodeOrSku(code: String): ProductWithBarcodesAndCategory? =
        productDao.findProductByBarcodeOrSku(code)

    override fun getLowStockProductsFlow(): Flow<List<ProductEntity>> =
        productDao.getLowStockProductsFlow()

    override suspend fun getLowStockProductsList(): List<ProductEntity> =
        productDao.getLowStockProductsList()

    override fun countTotalProducts(): Flow<Int> = productDao.countTotalProducts()

    override fun countLowStock(): Flow<Int> = productDao.countLowStock()

    override fun countOutOfStock(): Flow<Int> = productDao.countOutOfStock()

    override suspend fun getTotalStockCostValue(): Long =
        productDao.getTotalStockCostValue() ?: 0L

    override suspend fun getTotalRetailValue(): Long =
        productDao.getTotalRetailValue() ?: 0L

    override suspend fun saveProduct(product: ProductEntity, barcodes: List<String>): Long {
        return database.withTransaction {
            val productId = productDao.insertProduct(product)
            val barcodeEntities = barcodes
                .filter { it.isNotBlank() }
                .distinct()
                .map { ProductBarcodeEntity(productId = productId, barcode = it.trim()) }
            if (barcodeEntities.isNotEmpty()) {
                productDao.insertBarcodes(barcodeEntities)
            }
            productId
        }
    }

    override suspend fun updateProduct(product: ProductEntity, barcodes: List<String>) {
        database.withTransaction {
            productDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
            productDao.deleteBarcodesForProduct(product.id)
            val barcodeEntities = barcodes
                .filter { it.isNotBlank() }
                .distinct()
                .map { ProductBarcodeEntity(productId = product.id, barcode = it.trim()) }
            if (barcodeEntities.isNotEmpty()) {
                productDao.insertBarcodes(barcodeEntities)
            }
        }
    }

    override suspend fun setProductActive(productId: Long, isActive: Boolean) {
        productDao.setProductActive(productId, isActive)
    }

    override suspend fun isBarcodeTaken(barcode: String, excludeProductId: Long): Boolean =
        productDao.isBarcodeTaken(barcode, excludeProductId)

    override suspend fun isSkuTaken(sku: String, excludeProductId: Long): Boolean =
        productDao.isSkuTaken(sku, excludeProductId)
}

interface InventoryRepository {
    suspend fun adjustStock(
        productId: Long,
        qtyChange: Int,
        type: StockMovementType,
        reason: String,
        userId: Long
    ): Result<Int>

    fun getMovementsForProduct(productId: Long): Flow<List<StockMovementEntity>>
    fun getRecentMovements(limit: Int): Flow<List<StockMovementEntity>>
}

class InventoryRepositoryImpl(
    private val database: StockPosDatabase,
    private val productDao: ProductDao,
    private val stockMovementDao: StockMovementDao
) : InventoryRepository {

    override suspend fun adjustStock(
        productId: Long,
        qtyChange: Int,
        type: StockMovementType,
        reason: String,
        userId: Long
    ): Result<Int> {
        return runCatching {
            database.withTransaction {
                val product = productDao.getProductById(productId)
                    ?: throw IllegalArgumentException("Product not found")
                val newQty = product.stockQty + qtyChange
                productDao.updateStockQty(productId, newQty)

                val movement = StockMovementEntity(
                    productId = productId,
                    type = type,
                    qtyChange = qtyChange,
                    qtyAfter = newQty,
                    unitCost = product.costPrice,
                    referenceType = "MANUAL_ADJUSTMENT",
                    reason = reason,
                    userId = userId
                )
                stockMovementDao.insertMovement(movement)
                newQty
            }
        }
    }

    override fun getMovementsForProduct(productId: Long): Flow<List<StockMovementEntity>> =
        stockMovementDao.getMovementsForProduct(productId)

    override fun getRecentMovements(limit: Int): Flow<List<StockMovementEntity>> =
        stockMovementDao.getRecentMovements(limit)
}
