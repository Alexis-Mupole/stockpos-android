package com.example.data.repository

import com.example.data.local.dao.AppSettingDao
import com.example.data.local.dao.CategoryDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.SupplierDao
import com.example.data.local.entity.AppSettingEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface CategoryRepository {
    fun getAllCategories(): Flow<List<CategoryEntity>>
    suspend fun getCategoryById(id: Long): CategoryEntity?
    suspend fun saveCategory(category: CategoryEntity): Long
    suspend fun updateCategory(category: CategoryEntity)
    suspend fun deleteCategory(category: CategoryEntity)
    suspend fun preseedDefaultCategories()
}

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    override fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    override suspend fun getCategoryById(id: Long): CategoryEntity? = categoryDao.getCategoryById(id)
    override suspend fun saveCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)
    override suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    override suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    override suspend fun preseedDefaultCategories() {
        val defaults = listOf(
            CategoryEntity(name = "Beverages", colorHex = "#0288D1", iconName = "local_drink"),
            CategoryEntity(name = "Snacks & Food", colorHex = "#F57C00", iconName = "fastfood"),
            CategoryEntity(name = "Groceries", colorHex = "#388E3C", iconName = "shopping_basket"),
            CategoryEntity(name = "Household", colorHex = "#7B1FA2", iconName = "home"),
            CategoryEntity(name = "Personal Care", colorHex = "#C2185B", iconName = "clean_hands"),
            CategoryEntity(name = "Electronics", colorHex = "#455A64", iconName = "devices")
        )
        categoryDao.insertCategories(defaults)
    }
}

interface SupplierRepository {
    fun getAllSuppliers(): Flow<List<SupplierEntity>>
    suspend fun getSupplierById(id: Long): SupplierEntity?
    suspend fun saveSupplier(supplier: SupplierEntity): Long
    suspend fun updateSupplier(supplier: SupplierEntity)
    suspend fun archiveSupplier(id: Long)
}

class SupplierRepositoryImpl(
    private val supplierDao: SupplierDao
) : SupplierRepository {
    override fun getAllSuppliers(): Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()
    override suspend fun getSupplierById(id: Long): SupplierEntity? = supplierDao.getSupplierById(id)
    override suspend fun saveSupplier(supplier: SupplierEntity): Long = supplierDao.insertSupplier(supplier)
    override suspend fun updateSupplier(supplier: SupplierEntity) = supplierDao.updateSupplier(supplier)
    override suspend fun archiveSupplier(id: Long) = supplierDao.archiveSupplier(id)
}

interface CustomerRepository {
    fun getAllCustomers(): Flow<List<CustomerEntity>>
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>
    suspend fun getCustomerById(id: Long): CustomerEntity?
    suspend fun saveCustomer(customer: CustomerEntity): Long
    suspend fun updateCustomer(customer: CustomerEntity)
}

class CustomerRepositoryImpl(
    private val customerDao: CustomerDao
) : CustomerRepository {
    override fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    override fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)
    override suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)
    override suspend fun saveCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    override suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
}

data class BusinessSettings(
    val businessName: String = "StockPOS Store",
    val businessAddress: String = "",
    val businessPhone: String = "",
    val businessTaxId: String = "",
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val defaultTaxRatePercent: Double = 0.0,
    val taxInclusive: Boolean = true,
    val receiptPaperWidth: Int = 58, // 58 or 80 mm
    val receiptHeader: String = "Thank you for shopping with us!",
    val receiptFooter: String = "Returnable within 7 days with receipt.",
    val receiptShowQr: Boolean = true,
    val allowNegativeStock: Boolean = false,
    val defaultReorderLevel: Int = 5,
    val businessSetupComplete: Boolean = false,
    val soundBeepEnabled: Boolean = true,
    val hapticVibrateEnabled: Boolean = true,
    val pairedPrinterMac: String = "",
    val pairedPrinterName: String = "",
    val language: String = "EN" // "EN" or "FR"
)

interface SettingsRepository {
    fun getSettingsFlow(): Flow<BusinessSettings>
    suspend fun getSettings(): BusinessSettings
    suspend fun updateSetting(key: String, value: String)
    suspend fun saveSettings(settings: BusinessSettings)
    suspend fun isBusinessSetupComplete(): Boolean
    suspend fun markBusinessSetupComplete()
}

class SettingsRepositoryImpl(
    private val appSettingDao: AppSettingDao
) : SettingsRepository {

    override fun getSettingsFlow(): Flow<BusinessSettings> {
        return appSettingDao.getAllSettings().map { list ->
            val map = list.associate { it.key to it.value }
            mapToSettings(map)
        }
    }

    override suspend fun getSettings(): BusinessSettings {
        val all = appSettingDao.getAllSettings()
        // Fetch snapshot
        val map = mutableMapOf<String, String>()
        for (key in ALL_KEYS) {
            appSettingDao.getSetting(key)?.let { map[key] = it }
        }
        return mapToSettings(map)
    }

    override suspend fun updateSetting(key: String, value: String) {
        appSettingDao.saveSetting(AppSettingEntity(key = key, value = value))
    }

    override suspend fun saveSettings(settings: BusinessSettings) {
        appSettingDao.saveSetting(AppSettingEntity(KEY_BUSINESS_NAME, settings.businessName))
        appSettingDao.saveSetting(AppSettingEntity(KEY_BUSINESS_ADDRESS, settings.businessAddress))
        appSettingDao.saveSetting(AppSettingEntity(KEY_BUSINESS_PHONE, settings.businessPhone))
        appSettingDao.saveSetting(AppSettingEntity(KEY_BUSINESS_TAX_ID, settings.businessTaxId))
        appSettingDao.saveSetting(AppSettingEntity(KEY_CURRENCY_CODE, settings.currencyCode))
        appSettingDao.saveSetting(AppSettingEntity(KEY_CURRENCY_SYMBOL, settings.currencySymbol))
        appSettingDao.saveSetting(AppSettingEntity(KEY_DEFAULT_TAX_RATE, settings.defaultTaxRatePercent.toString()))
        appSettingDao.saveSetting(AppSettingEntity(KEY_TAX_INCLUSIVE, settings.taxInclusive.toString()))
        appSettingDao.saveSetting(AppSettingEntity(KEY_RECEIPT_PAPER_WIDTH, settings.receiptPaperWidth.toString()))
        appSettingDao.saveSetting(AppSettingEntity(KEY_RECEIPT_HEADER, settings.receiptHeader))
        appSettingDao.saveSetting(AppSettingEntity(KEY_RECEIPT_FOOTER, settings.receiptFooter))
        appSettingDao.saveSetting(AppSettingEntity(KEY_RECEIPT_SHOW_QR, settings.receiptShowQr.toString()))
        appSettingDao.saveSetting(AppSettingEntity(KEY_ALLOW_NEGATIVE_STOCK, settings.allowNegativeStock.toString()))
        appSettingDao.saveSetting(AppSettingEntity(KEY_DEFAULT_REORDER_LEVEL, settings.defaultReorderLevel.toString()))
        appSettingDao.saveSetting(AppSettingEntity(KEY_SETUP_COMPLETE, settings.businessSetupComplete.toString()))
        appSettingDao.saveSetting(AppSettingEntity(KEY_SOUND_BEEP, settings.soundBeepEnabled.toString()))
        appSettingDao.saveSetting(AppSettingEntity(KEY_HAPTIC_VIBRATE, settings.hapticVibrateEnabled.toString()))
        appSettingDao.saveSetting(AppSettingEntity(KEY_PRINTER_MAC, settings.pairedPrinterMac))
        appSettingDao.saveSetting(AppSettingEntity(KEY_PRINTER_NAME, settings.pairedPrinterName))
        appSettingDao.saveSetting(AppSettingEntity(KEY_LANGUAGE, settings.language))
    }

    override suspend fun isBusinessSetupComplete(): Boolean {
        return appSettingDao.getSetting(KEY_SETUP_COMPLETE)?.toBoolean() ?: false
    }

    override suspend fun markBusinessSetupComplete() {
        appSettingDao.saveSetting(AppSettingEntity(KEY_SETUP_COMPLETE, "true"))
    }

    private fun mapToSettings(map: Map<String, String>): BusinessSettings {
        return BusinessSettings(
            businessName = map[KEY_BUSINESS_NAME] ?: "StockPOS Store",
            businessAddress = map[KEY_BUSINESS_ADDRESS] ?: "",
            businessPhone = map[KEY_BUSINESS_PHONE] ?: "",
            businessTaxId = map[KEY_BUSINESS_TAX_ID] ?: "",
            currencyCode = map[KEY_CURRENCY_CODE] ?: "USD",
            currencySymbol = map[KEY_CURRENCY_SYMBOL] ?: "$",
            defaultTaxRatePercent = map[KEY_DEFAULT_TAX_RATE]?.toDoubleOrNull() ?: 0.0,
            taxInclusive = map[KEY_TAX_INCLUSIVE]?.toBooleanStrictOrNull() ?: true,
            receiptPaperWidth = map[KEY_RECEIPT_PAPER_WIDTH]?.toIntOrNull() ?: 58,
            receiptHeader = map[KEY_RECEIPT_HEADER] ?: "Thank you for shopping with us!",
            receiptFooter = map[KEY_RECEIPT_FOOTER] ?: "Returnable within 7 days with receipt.",
            receiptShowQr = map[KEY_RECEIPT_SHOW_QR]?.toBooleanStrictOrNull() ?: true,
            allowNegativeStock = map[KEY_ALLOW_NEGATIVE_STOCK]?.toBooleanStrictOrNull() ?: false,
            defaultReorderLevel = map[KEY_DEFAULT_REORDER_LEVEL]?.toIntOrNull() ?: 5,
            businessSetupComplete = map[KEY_SETUP_COMPLETE]?.toBooleanStrictOrNull() ?: false,
            soundBeepEnabled = map[KEY_SOUND_BEEP]?.toBooleanStrictOrNull() ?: true,
            hapticVibrateEnabled = map[KEY_HAPTIC_VIBRATE]?.toBooleanStrictOrNull() ?: true,
            pairedPrinterMac = map[KEY_PRINTER_MAC] ?: "",
            pairedPrinterName = map[KEY_PRINTER_NAME] ?: "",
            language = map[KEY_LANGUAGE] ?: "EN"
        )
    }

    companion object {
        const val KEY_BUSINESS_NAME = "business_name"
        const val KEY_BUSINESS_ADDRESS = "business_address"
        const val KEY_BUSINESS_PHONE = "business_phone"
        const val KEY_BUSINESS_TAX_ID = "business_tax_id"
        const val KEY_CURRENCY_CODE = "currency_code"
        const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        const val KEY_DEFAULT_TAX_RATE = "default_tax_rate"
        const val KEY_TAX_INCLUSIVE = "tax_inclusive"
        const val KEY_RECEIPT_PAPER_WIDTH = "receipt_paper_width"
        const val KEY_RECEIPT_HEADER = "receipt_header"
        const val KEY_RECEIPT_FOOTER = "receipt_footer"
        const val KEY_RECEIPT_SHOW_QR = "receipt_show_qr"
        const val KEY_ALLOW_NEGATIVE_STOCK = "allow_negative_stock"
        const val KEY_DEFAULT_REORDER_LEVEL = "default_reorder_level"
        const val KEY_SETUP_COMPLETE = "business_setup_complete"
        const val KEY_SOUND_BEEP = "sound_beep_enabled"
        const val KEY_HAPTIC_VIBRATE = "haptic_vibrate_enabled"
        const val KEY_PRINTER_MAC = "paired_printer_mac"
        const val KEY_PRINTER_NAME = "paired_printer_name"
        const val KEY_LANGUAGE = "app_language"

        val ALL_KEYS = listOf(
            KEY_BUSINESS_NAME, KEY_BUSINESS_ADDRESS, KEY_BUSINESS_PHONE, KEY_BUSINESS_TAX_ID,
            KEY_CURRENCY_CODE, KEY_CURRENCY_SYMBOL, KEY_DEFAULT_TAX_RATE, KEY_TAX_INCLUSIVE,
            KEY_RECEIPT_PAPER_WIDTH, KEY_RECEIPT_HEADER, KEY_RECEIPT_FOOTER, KEY_RECEIPT_SHOW_QR,
            KEY_ALLOW_NEGATIVE_STOCK, KEY_DEFAULT_REORDER_LEVEL, KEY_SETUP_COMPLETE,
            KEY_SOUND_BEEP, KEY_HAPTIC_VIBRATE, KEY_PRINTER_MAC, KEY_PRINTER_NAME, KEY_LANGUAGE
        )
    }
}
