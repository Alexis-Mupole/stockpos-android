package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["sku"], unique = true),
        Index(value = ["categoryId"]),
        Index(value = ["supplierId"]),
        Index(value = ["name"]),
        Index(value = ["isActive"]),
        Index(value = ["stockQty"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sku: String,
    val categoryId: Long,
    val supplierId: Long? = null,
    val unit: String = "pcs",
    val costPrice: Long, // In minor units (cents)
    val salePrice: Long, // In minor units (cents)
    val stockQty: Int = 0,
    val reorderLevel: Int = 5,
    val expiryDate: Long? = null,
    val imagePath: String? = null,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "product_barcodes",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["barcode"], unique = true),
        Index(value = ["productId"])
    ]
)
data class ProductBarcodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val barcode: String,
    val packQty: Int = 1
)

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["userId"]),
        Index(value = ["createdAt"]),
        Index(value = ["type"])
    ]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val type: StockMovementType,
    val qtyChange: Int, // Can be positive (restock/return) or negative (sale/damage)
    val qtyAfter: Int,
    val unitCost: Long? = null, // In minor units
    val referenceType: String = "", // e.g. "SALE", "PURCHASE", "MANUAL_ADJUST"
    val referenceId: Long? = null,
    val reason: String = "",
    val userId: Long, // Account responsible for this stock change
    val createdAt: Long = System.currentTimeMillis()
)
