package com.example.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductBarcodeEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.UserEntity

/**
 * Composite relation holding a Product, its Category, Supplier, and Barcodes.
 */
data class ProductWithBarcodesAndCategory(
    @Embedded
    val product: ProductEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?,

    @Relation(
        parentColumn = "supplierId",
        entityColumn = "id"
    )
    val supplier: SupplierEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val barcodes: List<ProductBarcodeEntity> = emptyList()
)

/**
 * Composite relation holding a Sale with its line items, payment records, seller, and customer.
 */
data class SaleWithItemsAndPayments(
    @Embedded
    val sale: SaleEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<SaleItemEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val payments: List<PaymentEntity>,

    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: UserEntity?,

    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: CustomerEntity?
)

/**
 * Summary DTO for seller sales performance.
 */
data class UserSalesSummary(
    val userId: Long,
    val userName: String,
    val userRole: String,
    val salesCount: Int,
    val totalRevenue: Long
)
