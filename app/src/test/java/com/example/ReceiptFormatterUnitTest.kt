package com.example

import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.PaymentMethod
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.SaleStatus
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRole
import com.example.data.local.relations.SaleWithItemsAndPayments
import com.example.data.repository.BusinessSettings
import com.example.domain.printer.ReceiptFormatter
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptFormatterUnitTest {

    @Test
    fun `formatReceipt creates valid 58mm text containing business info and totals`() {
        val cashier = UserEntity(
            id = 1,
            name = "Alice",
            identifier = "alice@pos.com",
            pinHash = "dummyHash",
            pinSalt = "dummySalt",
            role = UserRole.SELLER
        )

        val sale = SaleEntity(
            id = 10,
            receiptNo = "R260925-0001",
            userId = 1,
            subtotal = 1500L,
            total = 1500L,
            amountPaid = 2000L,
            changeGiven = 500L,
            status = SaleStatus.COMPLETED
        )

        val item = SaleItemEntity(
            id = 101,
            saleId = 10,
            productId = 1,
            productNameSnapshot = "Espresso",
            qty = 3,
            unitPriceSnapshot = 500L,
            unitCostSnapshot = 200L,
            lineTotal = 1500L
        )

        val payment = PaymentEntity(
            id = 201,
            saleId = 10,
            method = PaymentMethod.CASH,
            amount = 2000L
        )

        val saleDetails = SaleWithItemsAndPayments(
            sale = sale,
            items = listOf(item),
            payments = listOf(payment),
            user = cashier,
            customer = null
        )

        val settings = BusinessSettings(
            businessName = "Metro Coffee Bar",
            businessPhone = "+1 555-0199",
            receiptPaperWidth = 58
        )

        val receiptText = ReceiptFormatter.formatReceipt(saleDetails, settings)

        assertTrue(receiptText.contains("METRO COFFEE BAR"))
        assertTrue(receiptText.contains("R260925-0001"))
        assertTrue(receiptText.contains("CASHIER:"))
        assertTrue(receiptText.contains("Alice"))
        assertTrue(receiptText.contains("Espresso"))
        assertTrue(receiptText.contains("TOTAL:"))
        assertTrue(receiptText.contains("$15.00"))
        assertTrue(receiptText.contains("CHANGE:"))
        assertTrue(receiptText.contains("$5.00"))
    }
}
