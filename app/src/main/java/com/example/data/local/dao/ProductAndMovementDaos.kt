package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.ProductBarcodeEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StockMovementEntity
import com.example.data.local.relations.ProductWithBarcodesAndCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Transaction
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<ProductWithBarcodesAndCategory>>

    @Transaction
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsIncludingArchived(): Flow<List<ProductWithBarcodesAndCategory>>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductWithDetailsFlow(id: Long): Flow<ProductWithBarcodesAndCategory?>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductWithDetails(id: Long): ProductWithBarcodesAndCategory?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Transaction
    @Query(
        """
        SELECT DISTINCT p.* FROM products p 
        LEFT JOIN product_barcodes b ON p.id = b.productId 
        WHERE p.isActive = 1 AND (
            p.name LIKE '%' || :query || '%' 
            OR p.sku LIKE '%' || :query || '%' 
            OR b.barcode LIKE '%' || :query || '%'
        ) 
        ORDER BY p.name ASC
        """
    )
    fun searchProducts(query: String): Flow<List<ProductWithBarcodesAndCategory>>

    @Transaction
    @Query(
        """
        SELECT p.* FROM products p 
        INNER JOIN product_barcodes b ON p.id = b.productId 
        WHERE b.barcode = :barcode AND p.isActive = 1 
        LIMIT 1
        """
    )
    suspend fun findProductByBarcode(barcode: String): ProductWithBarcodesAndCategory?

    @Transaction
    @Query(
        """
        SELECT p.* FROM products p 
        LEFT JOIN product_barcodes b ON p.id = b.productId 
        WHERE (b.barcode = :code OR p.sku = :code) AND p.isActive = 1 
        LIMIT 1
        """
    )
    suspend fun findProductByBarcodeOrSku(code: String): ProductWithBarcodesAndCategory?

    @Query("SELECT * FROM products WHERE isActive = 1 AND stockQty <= reorderLevel ORDER BY stockQty ASC")
    fun getLowStockProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND stockQty <= reorderLevel ORDER BY stockQty ASC")
    suspend fun getLowStockProductsList(): List<ProductEntity>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    fun countTotalProducts(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1 AND stockQty <= reorderLevel AND stockQty > 0")
    fun countLowStock(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1 AND stockQty <= 0")
    fun countOutOfStock(): Flow<Int>

    @Query("SELECT SUM(costPrice * stockQty) FROM products WHERE isActive = 1 AND stockQty > 0")
    suspend fun getTotalStockCostValue(): Long?

    @Query("SELECT SUM(salePrice * stockQty) FROM products WHERE isActive = 1 AND stockQty > 0")
    suspend fun getTotalRetailValue(): Long?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQty = :newQty, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun updateStockQty(productId: Long, newQty: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun setProductActive(productId: Long, isActive: Boolean, updatedAt: Long = System.currentTimeMillis())

    // Barcode sub-operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarcode(barcode: ProductBarcodeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarcodes(barcodes: List<ProductBarcodeEntity>)

    @Query("DELETE FROM product_barcodes WHERE productId = :productId")
    suspend fun deleteBarcodesForProduct(productId: Long)

    @Query("SELECT * FROM product_barcodes WHERE productId = :productId")
    suspend fun getBarcodesForProduct(productId: Long): List<ProductBarcodeEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM product_barcodes WHERE barcode = :barcode AND productId != :excludeProductId)")
    suspend fun isBarcodeTaken(barcode: String, excludeProductId: Long = -1): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM products WHERE sku = :sku AND id != :excludeProductId)")
    suspend fun isSkuTaken(sku: String, excludeProductId: Long = -1): Boolean
}

@Dao
interface StockMovementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: StockMovementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovements(movements: List<StockMovementEntity>)

    @Query("SELECT * FROM stock_movements WHERE productId = :productId ORDER BY createdAt DESC")
    fun getMovementsForProduct(productId: Long): Flow<List<StockMovementEntity>>

    @Query("SELECT * FROM stock_movements ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentMovements(limit: Int = 100): Flow<List<StockMovementEntity>>
}
