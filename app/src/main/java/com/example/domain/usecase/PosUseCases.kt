package com.example.domain.usecase

import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.StockMovementType
import com.example.data.local.entity.UserEntity
import com.example.data.local.relations.ProductWithBarcodesAndCategory
import com.example.data.local.relations.SaleWithItemsAndPayments
import com.example.data.repository.InventoryRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.SaleRepository
import com.example.data.repository.SettingsRepository
import com.example.domain.model.CartItem
import com.example.domain.model.CartState
import com.example.domain.model.PaymentEntry

class ScanProductUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(rawBarcode: String): ProductWithBarcodesAndCategory? {
        val cleaned = rawBarcode.trim()
        if (cleaned.isBlank()) return null
        return productRepository.findProductByBarcodeOrSku(cleaned)
    }
}

class AddToCartUseCase {
    operator fun invoke(
        currentState: CartState,
        product: ProductEntity,
        barcodeUsed: String = "",
        allowNegativeStock: Boolean = false
    ): CartState {
        val existingIndex = currentState.items.indexOfFirst { it.product.id == product.id }
        val updatedItems = currentState.items.toMutableList()

        if (existingIndex >= 0) {
            val existing = updatedItems[existingIndex]
            val newQty = existing.quantity + 1
            if (!allowNegativeStock && newQty > product.stockQty) {
                // If negative stock disallowed, cap at max available
                return currentState
            }
            updatedItems[existingIndex] = existing.copy(quantity = newQty)
        } else {
            if (!allowNegativeStock && product.stockQty <= 0) {
                return currentState
            }
            updatedItems.add(
                CartItem(
                    product = product,
                    quantity = 1,
                    unitPrice = product.salePrice,
                    unitCost = product.costPrice,
                    barcodeUsed = barcodeUsed
                )
            )
        }
        return currentState.copy(items = updatedItems)
    }
}

class CheckoutUseCase(
    private val saleRepository: SaleRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(
        actingUser: UserEntity,
        cart: CartState,
        payments: List<PaymentEntry>
    ): Result<Pair<Long, String>> {
        if (!RoleGuard.canPerformSale(actingUser.role)) {
            return Result.failure(SecurityException("Unauthorized: Seller permission required"))
        }
        if (cart.isEmpty) {
            return Result.failure(IllegalArgumentException("Cart is empty"))
        }

        val totalPaid = payments.sumOf { it.amount }
        val grandTotal = cart.grandTotal
        if (totalPaid < grandTotal) {
            return Result.failure(
                IllegalArgumentException("Amount paid ($totalPaid) is less than total ($grandTotal)")
            )
        }

        val changeGiven = (totalPaid - grandTotal).coerceAtLeast(0L)
        val settings = settingsRepository.getSettings()
        val receiptNo = saleRepository.generateNextReceiptNumber()

        val saleEntity = SaleEntity(
            receiptNo = receiptNo,
            customerId = cart.attachedCustomerId,
            userId = actingUser.id,
            subtotal = cart.rawSubtotal,
            discountTotal = cart.cartDiscountAmount + cart.items.sumOf { it.totalDiscount },
            taxTotal = cart.taxAmount,
            total = grandTotal,
            amountPaid = totalPaid,
            changeGiven = changeGiven,
            note = cart.saleNote
        )

        val saleItems = cart.items.map { item ->
            SaleItemEntity(
                id = 0,
                saleId = 0,
                productId = item.product.id,
                productNameSnapshot = item.product.name,
                barcodeSnapshot = item.barcodeUsed.ifBlank { item.product.sku },
                qty = item.quantity,
                unitPriceSnapshot = item.unitPrice,
                unitCostSnapshot = item.unitCost,
                discount = item.totalDiscount,
                taxAmount = 0L,
                lineTotal = item.subtotal
            )
        }

        val paymentEntities = payments.map { p ->
            PaymentEntity(
                id = 0,
                saleId = 0,
                method = p.method,
                amount = p.amount,
                reference = p.reference
            )
        }

        val result = saleRepository.checkout(
            sale = saleEntity,
            items = saleItems,
            payments = paymentEntities,
            allowNegativeStock = settings.allowNegativeStock
        )

        return result.map { saleId -> Pair(saleId, receiptNo) }
    }
}

class RefundSaleUseCase(
    private val saleRepository: SaleRepository
) {
    suspend operator fun invoke(
        actingUser: UserEntity,
        saleId: Long,
        refundItems: List<Pair<SaleItemEntity, Int>>,
        reason: String,
        restock: Boolean
    ): Result<Long> {
        if (!RoleGuard.canRefundOrVoid(actingUser.role)) {
            return Result.failure(SecurityException("Unauthorized: Only Admin or Manager can issue refunds"))
        }
        return saleRepository.refundSale(
            saleId = saleId,
            actingUserId = actingUser.id,
            refundItems = refundItems,
            reason = reason,
            restock = restock
        )
    }
}

class AdjustStockUseCase(
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(
        actingUser: UserEntity,
        productId: Long,
        qtyChange: Int,
        type: StockMovementType,
        reason: String
    ): Result<Int> {
        if (!RoleGuard.canAdjustStock(actingUser.role)) {
            return Result.failure(SecurityException("Unauthorized: Only Admin or Manager can adjust stock"))
        }
        return inventoryRepository.adjustStock(
            productId = productId,
            qtyChange = qtyChange,
            type = type,
            reason = reason,
            userId = actingUser.id
        )
    }
}
