package com.example.domain.printer

import com.example.data.local.relations.SaleWithItemsAndPayments
import com.example.data.repository.BusinessSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptFormatter {

    fun formatReceipt(
        saleDetails: SaleWithItemsAndPayments,
        settings: BusinessSettings
    ): String {
        val width = if (settings.receiptPaperWidth == 80) 48 else 32
        val sb = StringBuilder()
        val div = "-".repeat(width)
        val doubleDiv = "=".repeat(width)

        fun center(text: String): String {
            if (text.length >= width) return text.take(width)
            val left = (width - text.length) / 2
            return " ".repeat(left) + text
        }

        fun row(left: String, right: String): String {
            val maxLeft = width - right.length - 1
            val l = if (left.length > maxLeft) left.take(maxLeft) else left
            val spaces = width - l.length - right.length
            return l + " ".repeat(spaces.coerceAtLeast(1)) + right
        }

        fun formatMoney(amountMinor: Long): String {
            val symbol = settings.currencySymbol
            val major = amountMinor / 100.0
            return String.format(Locale.US, "%s%.2f", symbol, major)
        }

        // Header
        sb.appendLine(center(settings.businessName.uppercase()))
        if (settings.businessAddress.isNotBlank()) {
            sb.appendLine(center(settings.businessAddress))
        }
        if (settings.businessPhone.isNotBlank()) {
            sb.appendLine(center("TEL: " + settings.businessPhone))
        }
        if (settings.businessTaxId.isNotBlank()) {
            sb.appendLine(center("TAX ID: " + settings.businessTaxId))
        }
        sb.appendLine(div)

        // Metadata
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(saleDetails.sale.createdAt))
        sb.appendLine(row("RECEIPT:", saleDetails.sale.receiptNo))
        sb.appendLine(row("DATE:", dateStr))
        saleDetails.user?.let {
            sb.appendLine(row("CASHIER:", it.name))
        }
        saleDetails.customer?.let {
            sb.appendLine(row("CUSTOMER:", it.name))
        }
        sb.appendLine(doubleDiv)

        // Column Header
        sb.appendLine(row("ITEM", "AMOUNT"))
        sb.appendLine(div)

        // Items
        for (item in saleDetails.items) {
            val name = item.productNameSnapshot
            sb.appendLine(name)
            val qtyPrice = "${item.qty} x ${formatMoney(item.unitPriceSnapshot)}"
            val lineTotal = formatMoney(item.lineTotal)
            sb.appendLine(row("  $qtyPrice", lineTotal))
            if (item.discount > 0) {
                sb.appendLine(row("  Discount", "-${formatMoney(item.discount)}"))
            }
        }
        sb.appendLine(div)

        // Totals
        sb.appendLine(row("SUBTOTAL:", formatMoney(saleDetails.sale.subtotal)))
        if (saleDetails.sale.discountTotal > 0) {
            sb.appendLine(row("DISCOUNT:", "-${formatMoney(saleDetails.sale.discountTotal)}"))
        }
        if (saleDetails.sale.taxTotal > 0) {
            sb.appendLine(row("TAX (${settings.defaultTaxRatePercent}%):", formatMoney(saleDetails.sale.taxTotal)))
        }
        sb.appendLine(doubleDiv)
        sb.appendLine(row("TOTAL:", formatMoney(saleDetails.sale.total)))
        sb.appendLine(doubleDiv)

        // Payments
        for (payment in saleDetails.payments) {
            sb.appendLine(row("PAID (${payment.method.name}):", formatMoney(payment.amount)))
        }
        if (saleDetails.sale.changeGiven > 0) {
            sb.appendLine(row("CHANGE:", formatMoney(saleDetails.sale.changeGiven)))
        }

        // Footer
        sb.appendLine(div)
        if (settings.receiptHeader.isNotBlank()) {
            sb.appendLine(center(settings.receiptHeader))
        }
        if (settings.receiptFooter.isNotBlank()) {
            sb.appendLine(center(settings.receiptFooter))
        }
        sb.appendLine(center("*** THANK YOU ***"))

        return sb.toString()
    }
}
