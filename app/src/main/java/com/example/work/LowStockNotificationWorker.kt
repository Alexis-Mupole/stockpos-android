package com.example.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.StockPosApplication

class LowStockNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? StockPosApplication ?: return Result.failure()
        val productRepo = app.container.productRepository

        val lowStockProducts = productRepo.getLowStockProductsList()
        if (lowStockProducts.isNotEmpty()) {
            showNotification(lowStockProducts.size)
        }

        return Result.success()
    }

    private fun showNotification(count: Int) {
        val channelId = "stockpos_alerts"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Inventory Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Low stock and reorder notifications"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Low Stock Alert")
            .setContentText("$count product(s) have reached or fallen below reorder levels.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }
}
