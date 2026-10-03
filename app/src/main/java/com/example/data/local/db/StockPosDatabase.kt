package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.converters.Converters
import com.example.data.local.dao.AppSettingDao
import com.example.data.local.dao.CashSessionDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.dao.PurchaseDao
import com.example.data.local.dao.RefundDao
import com.example.data.local.dao.SaleDao
import com.example.data.local.dao.StockMovementDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.AppSettingEntity
import com.example.data.local.entity.CashSessionEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.PaymentEntity
import com.example.data.local.entity.ProductBarcodeEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.PurchaseItemEntity
import com.example.data.local.entity.PurchaseOrderEntity
import com.example.data.local.entity.RefundEntity
import com.example.data.local.entity.RefundItemEntity
import com.example.data.local.entity.SaleEntity
import com.example.data.local.entity.SaleItemEntity
import com.example.data.local.entity.StockMovementEntity
import com.example.data.local.entity.SupplierEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        SupplierEntity::class,
        CustomerEntity::class,
        ProductEntity::class,
        ProductBarcodeEntity::class,
        StockMovementEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PaymentEntity::class,
        RefundEntity::class,
        RefundItemEntity::class,
        PurchaseOrderEntity::class,
        PurchaseItemEntity::class,
        CashSessionEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StockPosDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun supplierDao(): SupplierDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun saleDao(): SaleDao
    abstract fun refundDao(): RefundDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun cashSessionDao(): CashSessionDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: StockPosDatabase? = null

        fun getDatabase(context: Context): StockPosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockPosDatabase::class.java,
                    "stockpos_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
