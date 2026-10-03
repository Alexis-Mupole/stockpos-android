package com.example

import com.example.data.local.entity.UserRole
import com.example.domain.model.CartItem
import com.example.domain.model.CartState
import com.example.domain.printer.ReceiptFormatter
import com.example.domain.usecase.RoleGuard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StockPosLogicTest {

    @Test
    fun `RoleGuard properly restricts Seller from inventory and user management`() {
        assertFalse(RoleGuard.canManageUsers(UserRole.SELLER))
        assertFalse(RoleGuard.canManageInventory(UserRole.SELLER))
        assertFalse(RoleGuard.canViewAllReports(UserRole.SELLER))
        assertFalse(RoleGuard.canRefundOrVoid(UserRole.SELLER))
        assertTrue(RoleGuard.canPerformSale(UserRole.SELLER))
    }

    @Test
    fun `RoleGuard properly grants Manager inventory access but restricts user management`() {
        assertFalse(RoleGuard.canManageUsers(UserRole.MANAGER))
        assertTrue(RoleGuard.canManageInventory(UserRole.MANAGER))
        assertTrue(RoleGuard.canViewAllReports(UserRole.MANAGER))
        assertTrue(RoleGuard.canRefundOrVoid(UserRole.MANAGER))
        assertTrue(RoleGuard.canPerformSale(UserRole.MANAGER))
    }

    @Test
    fun `RoleGuard grants Admin full system authority`() {
        assertTrue(RoleGuard.canManageUsers(UserRole.ADMIN))
        assertTrue(RoleGuard.canModifyBusinessSettings(UserRole.ADMIN))
        assertTrue(RoleGuard.canManageInventory(UserRole.ADMIN))
        assertTrue(RoleGuard.canViewAllReports(UserRole.ADMIN))
        assertTrue(RoleGuard.canRefundOrVoid(UserRole.ADMIN))
        assertTrue(RoleGuard.canPerformSale(UserRole.ADMIN))
    }

    @Test
    fun `Cart State accurately computes line discounts and grand total in tax inclusive mode`() {
        val dummyProduct = com.example.data.local.entity.ProductEntity(
            id = 1,
            name = "Coffee Bean 500g",
            sku = "COF001",
            categoryId = 1,
            costPrice = 400L, // $4.00
            salePrice = 1000L, // $10.00
            stockQty = 50
        )

        val item1 = CartItem(product = dummyProduct, quantity = 2, unitPrice = 1000L, discountPercent = 10.0)
        // 2 x $10 = $20.00. 10% discount = $2.00. Subtotal = $18.00 (1800 minor units)
        assertEquals(200L, item1.totalDiscount)
        assertEquals(1800L, item1.subtotal)

        val cartState = CartState(
            items = listOf(item1),
            overallDiscountPercent = 0.0,
            taxRatePercent = 10.0,
            isTaxInclusive = true
        )

        assertEquals(1800L, cartState.grandTotal)
        assertEquals(2, cartState.totalItemsCount)
    }

    @Test
    fun `Cart State accurately computes tax in tax exclusive mode`() {
        val dummyProduct = com.example.data.local.entity.ProductEntity(
            id = 2,
            name = "Mineral Water 1L",
            sku = "WAT001",
            categoryId = 1,
            costPrice = 50L,
            salePrice = 200L, // $2.00
            stockQty = 100
        )

        val item = CartItem(product = dummyProduct, quantity = 3, unitPrice = 200L)
        // 3 x $2 = $6.00 (600 minor)
        val cartState = CartState(
            items = listOf(item),
            overallDiscountPercent = 0.0,
            taxRatePercent = 10.0,
            isTaxInclusive = false
        )

        // 10% on $6.00 = $0.60 (60 minor)
        assertEquals(60L, cartState.taxAmount)
        assertEquals(660L, cartState.grandTotal)
    }
}
