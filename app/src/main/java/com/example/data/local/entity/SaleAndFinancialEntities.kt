package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["receiptNo"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["userId"]),
        Index(value = ["createdAt"]),
        Index(value = ["status"])
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNo: String,
    val customerId: Long? = null,
    val userId: Long, // Seller account who made the sale
    val subtotal: Long, // Minor units
    val discountTotal: Long = 0L,
    val taxTotal: Long = 0L,
    val total: Long,
    val amountPaid: Long,
    val changeGiven: Long = 0L,
    val status: SaleStatus = SaleStatus.COMPLETED,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["productId"])
    ]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productNameSnapshot: String,
    val barcodeSnapshot: String = "",
    val qty: Int,
    val unitPriceSnapshot: Long,
    val unitCostSnapshot: Long,
    val discount: Long = 0L,
    val taxAmount: Long = 0L,
    val lineTotal: Long
)

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["method"])
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val method: PaymentMethod,
    val amount: Long, // Minor units
    val reference: String? = null
)

@Entity(
    tableName = "refunds",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
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
        Index(value = ["saleId"]),
        Index(value = ["userId"]),
        Index(value = ["createdAt"])
    ]
)
data class RefundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val userId: Long,
    val total: Long,
    val reason: String,
    val restocked: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "refund_items",
    foreignKeys = [
        ForeignKey(
            entity = RefundEntity::class,
            parentColumns = ["id"],
            childColumns = ["refundId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SaleItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleItemId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["refundId"]),
        Index(value = ["saleItemId"]),
        Index(value = ["productId"])
    ]
)
data class RefundItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val refundId: Long,
    val saleItemId: Long,
    val productId: Long,
    val qty: Int,
    val refundAmount: Long
)

@Entity(
    tableName = "purchase_orders",
    foreignKeys = [
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
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
        Index(value = ["supplierId"]),
        Index(value = ["userId"]),
        Index(value = ["referenceNo"])
    ]
)
data class PurchaseOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplierId: Long,
    val referenceNo: String,
    val totalCost: Long,
    val receivedAt: Long = System.currentTimeMillis(),
    val userId: Long
)

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseOrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["purchaseId"]),
        Index(value = ["productId"])
    ]
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val qty: Int,
    val unitCost: Long
)

@Entity(
    tableName = "cash_sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["openedAt"])
    ]
)
data class CashSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val openingFloat: Long,
    val closingCounted: Long? = null,
    val expectedCash: Long = 0L,
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey
    val key: String,
    val value: String
)
