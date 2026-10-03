package com.example.data.local.converters

import androidx.room.TypeConverter
import com.example.data.local.entity.PaymentMethod
import com.example.data.local.entity.SaleStatus
import com.example.data.local.entity.StockMovementType
import com.example.data.local.entity.UserRole

class Converters {

    @TypeConverter
    fun fromUserRole(role: UserRole?): String? = role?.name

    @TypeConverter
    fun toUserRole(value: String?): UserRole? = value?.let {
        runCatching { enumValueOf<UserRole>(it) }.getOrNull() ?: UserRole.SELLER
    }

    @TypeConverter
    fun fromStockMovementType(type: StockMovementType?): String? = type?.name

    @TypeConverter
    fun toStockMovementType(value: String?): StockMovementType? = value?.let {
        runCatching { enumValueOf<StockMovementType>(it) }.getOrNull() ?: StockMovementType.SALE
    }

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod?): String? = method?.name

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod? = value?.let {
        runCatching { enumValueOf<PaymentMethod>(it) }.getOrNull() ?: PaymentMethod.CASH
    }

    @TypeConverter
    fun fromSaleStatus(status: SaleStatus?): String? = status?.name

    @TypeConverter
    fun toSaleStatus(value: String?): SaleStatus? = value?.let {
        runCatching { enumValueOf<SaleStatus>(it) }.getOrNull() ?: SaleStatus.COMPLETED
    }
}
