package com.example.data.local.entity

/**
 * Role representing the authority level of a user.
 */
enum class UserRole {
    ADMIN,
    MANAGER,
    SELLER
}

/**
 * Type of stock adjustment or movement.
 */
enum class StockMovementType {
    SALE,
    RETURN,
    PURCHASE,
    ADJUSTMENT,
    DAMAGE,
    VOID
}

/**
 * Payment method used in POS checkout.
 */
enum class PaymentMethod {
    CASH,
    MOBILE_MONEY,
    CARD,
    CREDIT
}

/**
 * Status of a sale transaction.
 */
enum class SaleStatus {
    COMPLETED,
    REFUNDED,
    PARTIALLY_REFUNDED,
    VOIDED,
    HELD
}
