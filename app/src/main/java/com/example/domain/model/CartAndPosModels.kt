package com.example.domain.model

import com.example.data.local.entity.PaymentMethod
import com.example.data.local.entity.ProductEntity

data class CartItem(
    val product: ProductEntity,
    val quantity: Int = 1,
    val unitPrice: Long = product.salePrice,
    val unitCost: Long = product.costPrice,
    val discountPercent: Double = 0.0,
    val barcodeUsed: String = ""
) {
    val totalDiscount: Long
        get() = ((unitPrice * quantity) * (discountPercent / 100.0)).toLong()

    val subtotal: Long
        get() = (unitPrice * quantity) - totalDiscount
}

data class CartState(
    val items: List<CartItem> = emptyList(),
    val overallDiscountPercent: Double = 0.0,
    val taxRatePercent: Double = 0.0,
    val isTaxInclusive: Boolean = true,
    val attachedCustomerId: Long? = null,
    val attachedCustomerName: String? = null,
    val saleNote: String = ""
) {
    val totalItemsCount: Int
        get() = items.sumOf { it.quantity }

    val rawSubtotal: Long
        get() = items.sumOf { it.subtotal }

    val cartDiscountAmount: Long
        get() = (rawSubtotal * (overallDiscountPercent / 100.0)).toLong()

    val subtotalAfterDiscount: Long
        get() = rawSubtotal - cartDiscountAmount

    val taxAmount: Long
        get() = if (taxRatePercent <= 0.0) {
            0L
        } else if (isTaxInclusive) {
            // Price already includes tax: tax = total - (total / (1 + rate))
            (subtotalAfterDiscount - (subtotalAfterDiscount / (1.0 + (taxRatePercent / 100.0)))).toLong()
        } else {
            // Price exclusive of tax: tax = subtotal * rate
            (subtotalAfterDiscount * (taxRatePercent / 100.0)).toLong()
        }

    val grandTotal: Long
        get() = if (isTaxInclusive) {
            subtotalAfterDiscount
        } else {
            subtotalAfterDiscount + taxAmount
        }

    val isEmpty: Boolean
        get() = items.isEmpty()
}

data class PaymentEntry(
    val method: PaymentMethod,
    val amount: Long,
    val reference: String? = null
)

data class ParkedCart(
    val id: String,
    val cartState: CartState,
    val savedAt: Long = System.currentTimeMillis(),
    val label: String
)
