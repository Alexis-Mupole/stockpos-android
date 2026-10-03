package com.example.di

import android.content.Context
import com.example.data.local.db.StockPosDatabase
import com.example.data.repository.CategoryRepository
import com.example.data.repository.CategoryRepositoryImpl
import com.example.data.repository.CustomerRepository
import com.example.data.repository.CustomerRepositoryImpl
import com.example.data.repository.InventoryRepository
import com.example.data.repository.InventoryRepositoryImpl
import com.example.data.repository.ProductRepository
import com.example.data.repository.ProductRepositoryImpl
import com.example.data.repository.SaleRepository
import com.example.data.repository.SaleRepositoryImpl
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SettingsRepositoryImpl
import com.example.data.repository.SupplierRepository
import com.example.data.repository.SupplierRepositoryImpl
import com.example.data.repository.UserRepository
import com.example.data.repository.UserRepositoryImpl
import com.example.data.session.SessionManager
import com.example.domain.printer.BluetoothPrinterManager
import com.example.domain.usecase.AddToCartUseCase
import com.example.domain.usecase.AdjustStockUseCase
import com.example.domain.usecase.CheckoutUseCase
import com.example.domain.usecase.CreateAdminAccountUseCase
import com.example.domain.usecase.LoginUseCase
import com.example.domain.usecase.RefundSaleUseCase
import com.example.domain.usecase.ScanProductUseCase
import com.example.domain.usecase.UserManagementUseCase

interface AppContainer {
    val database: StockPosDatabase
    val userRepository: UserRepository
    val productRepository: ProductRepository
    val inventoryRepository: InventoryRepository
    val saleRepository: SaleRepository
    val categoryRepository: CategoryRepository
    val supplierRepository: SupplierRepository
    val customerRepository: CustomerRepository
    val settingsRepository: SettingsRepository
    val sessionManager: SessionManager

    val createAdminAccountUseCase: CreateAdminAccountUseCase
    val loginUseCase: LoginUseCase
    val userManagementUseCase: UserManagementUseCase
    val scanProductUseCase: ScanProductUseCase
    val addToCartUseCase: AddToCartUseCase
    val checkoutUseCase: CheckoutUseCase
    val refundSaleUseCase: RefundSaleUseCase
    val adjustStockUseCase: AdjustStockUseCase
    val bluetoothPrinterManager: BluetoothPrinterManager
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: StockPosDatabase by lazy {
        StockPosDatabase.getDatabase(context)
    }

    override val userRepository: UserRepository by lazy {
        UserRepositoryImpl(database.userDao())
    }

    override val productRepository: ProductRepository by lazy {
        ProductRepositoryImpl(database, database.productDao())
    }

    override val inventoryRepository: InventoryRepository by lazy {
        InventoryRepositoryImpl(database, database.productDao(), database.stockMovementDao())
    }

    override val saleRepository: SaleRepository by lazy {
        SaleRepositoryImpl(
            database = database,
            saleDao = database.saleDao(),
            productDao = database.productDao(),
            stockMovementDao = database.stockMovementDao(),
            refundDao = database.refundDao(),
            customerDao = database.customerDao()
        )
    }

    override val categoryRepository: CategoryRepository by lazy {
        CategoryRepositoryImpl(database.categoryDao())
    }

    override val supplierRepository: SupplierRepository by lazy {
        SupplierRepositoryImpl(database.supplierDao())
    }

    override val customerRepository: CustomerRepository by lazy {
        CustomerRepositoryImpl(database.customerDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(database.appSettingDao())
    }

    override val sessionManager: SessionManager by lazy {
        SessionManager(context, userRepository)
    }

    override val createAdminAccountUseCase: CreateAdminAccountUseCase by lazy {
        CreateAdminAccountUseCase(userRepository, sessionManager)
    }

    override val loginUseCase: LoginUseCase by lazy {
        LoginUseCase(userRepository, sessionManager)
    }

    override val userManagementUseCase: UserManagementUseCase by lazy {
        UserManagementUseCase(userRepository)
    }

    override val scanProductUseCase: ScanProductUseCase by lazy {
        ScanProductUseCase(productRepository)
    }

    override val addToCartUseCase: AddToCartUseCase by lazy {
        AddToCartUseCase()
    }

    override val checkoutUseCase: CheckoutUseCase by lazy {
        CheckoutUseCase(saleRepository, settingsRepository)
    }

    override val refundSaleUseCase: RefundSaleUseCase by lazy {
        RefundSaleUseCase(saleRepository)
    }

    override val adjustStockUseCase: AdjustStockUseCase by lazy {
        AdjustStockUseCase(inventoryRepository)
    }

    override val bluetoothPrinterManager: BluetoothPrinterManager by lazy {
        BluetoothPrinterManager(context)
    }
}
