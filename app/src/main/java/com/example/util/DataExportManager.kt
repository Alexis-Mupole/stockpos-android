package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.repository.ProductRepository
import com.example.data.repository.SaleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestionnaire d'exportation locale des données du POS.
 * Génère des fichiers CSV structurés et sécurisés via FileProvider
 * pour partage direct (WhatsApp, Drive, Email, Fichiers locaux).
 */
object DataExportManager {

    /**
     * Exporte les produits et les ventes sous format CSV structuré et ouvre
     * le menu de partage natif Android (ACTION_SEND / Chooser).
     */
    suspend fun exportAndSharePosData(
        context: Context,
        productRepository: ProductRepository,
        saleRepository: SaleRepository
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Récupération des données depuis les repositories Room
            val products = productRepository.getAllActiveProducts().first()
            val sales = saleRepository.getAllSales().first()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val exportDir = File(context.cacheDir, "exports").apply {
                if (!exists()) mkdirs()
            }
            val exportFile = File(exportDir, "pos_export_$timestamp.csv")

            // 2. Écriture du contenu CSV structuré
            FileWriter(exportFile).use { writer ->
                // Section Produits
                writer.append("=== INVENTAIRE PRODUITS ===\n")
                writer.append("ID,Nom,SKU,Prix_Vente,Cout,Stock,Seuil_Alerte\n")
                for (item in products) {
                    val p = item.product
                    val salePriceMajor = p.salePrice / 100.0
                    val costPriceMajor = p.costPrice / 100.0
                    writer.append("${p.id},\"${p.name.replace("\"", "\"\"")}\",\"${p.sku}\",$salePriceMajor,$costPriceMajor,${p.stockQty},${p.reorderLevel}\n")
                }

                writer.append("\n=== HISTORIQUE DES VENTES ===\n")
                writer.append("Vente_ID,Numero_Recu,Date,Total,Statut,Articles_Vendus\n")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                for (saleWithItems in sales) {
                    val s = saleWithItems.sale
                    val totalMajor = s.total / 100.0
                    val dateStr = dateFormat.format(Date(s.createdAt))
                    val itemsSummary = saleWithItems.items.joinToString(" ; ") { "${it.qty}x ${it.productNameSnapshot}" }
                    writer.append("${s.id},\"${s.receiptNo}\",\"$dateStr\",$totalMajor,${s.status.name},\"${itemsSummary.replace("\"", "\"\"")}\"\n")
                }
            }

            // 3. Génération de l'URI sécurisé via FileProvider
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )

            // 4. Création de l'Intent ACTION_SEND avec Chooser
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "StockPOS - Exportation de données ($timestamp)")
                putExtra(Intent.EXTRA_TEXT, "Veuillez trouver ci-joint l'exportation des stocks et ventes du point de vente.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Exporter les données du POS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            withContext(Dispatchers.Main) {
                context.startActivity(chooserIntent)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
