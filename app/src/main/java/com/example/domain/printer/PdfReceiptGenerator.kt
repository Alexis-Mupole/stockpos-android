package com.example.domain.printer

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.relations.SaleWithItemsAndPayments
import com.example.data.repository.BusinessSettings
import java.io.File
import java.io.FileOutputStream

object PdfReceiptGenerator {

    fun generatePdfFile(
        context: Context,
        saleDetails: SaleWithItemsAndPayments,
        settings: BusinessSettings
    ): File {
        val receiptText = ReceiptFormatter.formatReceipt(saleDetails, settings)
        val lines = receiptText.lines()

        val pageWidth = 384 // ~58mm in points (72 dpi scale ~200-384)
        val lineHeight = 20
        val pageHeight = (lines.size * lineHeight) + 60

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        var y = 30f
        for (line in lines) {
            canvas.drawText(line, 16f, y, paint)
            y += lineHeight
        }

        document.finishPage(page)

        val outputDir = File(context.cacheDir, "receipts").apply { mkdirs() }
        val file = File(outputDir, "receipt_${saleDetails.sale.receiptNo}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    fun shareReceiptPdf(
        context: Context,
        saleDetails: SaleWithItemsAndPayments,
        settings: BusinessSettings
    ) {
        try {
            val file = generatePdfFile(context, saleDetails, settings)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Receipt #${saleDetails.sale.receiptNo}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, "Share Receipt").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Throwable) {
            // Gracefully handle missing share handler on emulators/devices
        }
    }
}
