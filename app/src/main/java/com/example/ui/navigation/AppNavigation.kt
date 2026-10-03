package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRole
import com.example.data.repository.BusinessSettings
import com.example.data.session.SessionState
import com.example.di.AppContainer
import com.example.domain.usecase.RoleGuard
import com.example.ui.about.AboutScreen
import com.example.ui.auth.CreateAdminScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.history.SalesHistoryAndReportsScreen
import com.example.ui.home.HomeScreen
import com.example.ui.inventory.InventoryScreen
import com.example.ui.pos.ScanCheckoutScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.UserManagementScreen
import com.example.ui.setup.BusinessSetupWizardScreen
import com.example.util.AppStrings
import kotlinx.coroutines.launch

enum class AppTab(val icon: ImageVector) {
    HOME(Icons.Default.Home),
    SELL(Icons.Default.PointOfSale),
    INVENTORY(Icons.Default.Inventory2),
    HISTORY(Icons.Default.History),
    SETTINGS(Icons.Default.Settings)
}

fun getTabLabel(tab: AppTab, strings: AppStrings): String = when (tab) {
    AppTab.HOME -> strings.navHome
    AppTab.SELL -> strings.navSell
    AppTab.INVENTORY -> strings.navInventory
    AppTab.HISTORY -> strings.navHistory
    AppTab.SETTINGS -> strings.navSettings
}

enum class SubScreen {
    NONE,
    USER_MANAGEMENT,
    BUSINESS_SETUP,
    ABOUT
}

@Composable
fun MainAppNavigation(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val sessionState by container.sessionManager.sessionState.collectAsState(initial = SessionState.Loading)
    val settings by container.settingsRepository.getSettingsFlow().collectAsState(initial = BusinessSettings())
    val S = remember(settings.language) { AppStrings(settings.language) }

    var userCount by remember { mutableStateOf<Int?>(null) }
    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }
    var prefilledBarcodeForInventory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sessionState) {
        userCount = container.userRepository.getUserCount()
    }

    when (val state = sessionState) {
        is SessionState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
            }
        }

        is SessionState.Unauthenticated -> {
            if (userCount == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                }
            } else if (userCount == 0) {
                // First launch: exclusively show Create Admin Account
                CreateAdminScreen(
                    createAdminAccountUseCase = container.createAdminAccountUseCase,
                    onAdminCreated = {
                        userCount = 1
                    }
                )
            } else {
                // Returning users: Login Screen with device keyboard & account switching
                LoginScreen(
                    userRepository = container.userRepository,
                    loginUseCase = container.loginUseCase,
                    onLoginSuccess = { user ->
                        coroutineScope.launch {
                            container.sessionManager.startSession(user)
                        }
                    }
                )
            }
        }

        is SessionState.Locked -> {
            LoginScreen(
                userRepository = container.userRepository,
                loginUseCase = container.loginUseCase,
                onLoginSuccess = { user ->
                    coroutineScope.launch {
                        container.sessionManager.unlockSession()
                    }
                }
            )
        }

        is SessionState.Authenticated -> {
            val user = state.user

            if (currentSubScreen == SubScreen.USER_MANAGEMENT && user.role == UserRole.ADMIN) {
                BackHandler { currentSubScreen = SubScreen.NONE }
                UserManagementScreen(
                    currentUser = user,
                    userRepository = container.userRepository,
                    userManagementUseCase = container.userManagementUseCase,
                    onNavigateBack = { currentSubScreen = SubScreen.NONE }
                )
            } else if (currentSubScreen == SubScreen.BUSINESS_SETUP && user.role == UserRole.ADMIN) {
                BackHandler { currentSubScreen = SubScreen.NONE }
                BusinessSetupWizardScreen(
                    settingsRepository = container.settingsRepository,
                    categoryRepository = container.categoryRepository,
                    productRepository = container.productRepository,
                    onSetupFinished = { currentSubScreen = SubScreen.NONE }
                )
            } else if (currentSubScreen == SubScreen.ABOUT) {
                BackHandler { currentSubScreen = SubScreen.NONE }
                AboutScreen(
                    strings = S,
                    onNavigateBack = { currentSubScreen = SubScreen.NONE }
                )
            } else {
                // Main Authenticated Shell with Role-based Tab Filtering
                val availableTabs = remember(user.role) {
                    if (RoleGuard.canManageInventory(user.role)) {
                        listOf(AppTab.HOME, AppTab.SELL, AppTab.INVENTORY, AppTab.HISTORY, AppTab.SETTINGS)
                    } else {
                        // Seller sees Home, Sell, their own History, and Settings
                        listOf(AppTab.HOME, AppTab.SELL, AppTab.HISTORY, AppTab.SETTINGS)
                    }
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar(tonalElevation = 4.dp) {
                            availableTabs.forEach { tab ->
                                val label = getTabLabel(tab, S)
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = label, modifier = Modifier.size(19.dp)) },
                                    label = { Text(label, fontSize = 10.sp, maxLines = 1) },
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    },
                    modifier = modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            AppTab.HOME -> {
                                HomeScreen(
                                    currentUser = user,
                                    settingsRepository = container.settingsRepository,
                                    productRepository = container.productRepository,
                                    categoryRepository = container.categoryRepository,
                                    saleRepository = container.saleRepository,
                                    onNavigateToSell = { currentTab = AppTab.SELL },
                                    onNavigateToInventory = { currentTab = AppTab.INVENTORY },
                                    onNavigateToHistory = { currentTab = AppTab.HISTORY },
                                    onNavigateToAbout = { currentSubScreen = SubScreen.ABOUT }
                                )
                            }
                            AppTab.SELL -> {
                                ScanCheckoutScreen(
                                    currentUser = user,
                                    productRepository = container.productRepository,
                                    settingsRepository = container.settingsRepository,
                                    scanProductUseCase = container.scanProductUseCase,
                                    addToCartUseCase = container.addToCartUseCase,
                                    checkoutUseCase = container.checkoutUseCase,
                                    onNavigateToAddProductWithBarcode = { code ->
                                        prefilledBarcodeForInventory = code
                                        currentTab = AppTab.INVENTORY
                                    }
                                )
                            }
                            AppTab.INVENTORY -> {
                                if (RoleGuard.canManageInventory(user.role)) {
                                    InventoryScreen(
                                        currentUser = user,
                                        productRepository = container.productRepository,
                                        categoryRepository = container.categoryRepository,
                                        inventoryRepository = container.inventoryRepository,
                                        settingsRepository = container.settingsRepository,
                                        adjustStockUseCase = container.adjustStockUseCase,
                                        prefilledBarcode = prefilledBarcodeForInventory
                                    )
                                } else {
                                    currentTab = AppTab.HOME
                                }
                            }
                            AppTab.HISTORY -> {
                                SalesHistoryAndReportsScreen(
                                    currentUser = user,
                                    saleRepository = container.saleRepository,
                                    settingsRepository = container.settingsRepository,
                                    refundSaleUseCase = container.refundSaleUseCase
                                )
                            }
                            AppTab.SETTINGS -> {
                                SettingsScreen(
                                    currentUser = user,
                                    settingsRepository = container.settingsRepository,
                                    sessionManager = container.sessionManager,
                                    bluetoothPrinterManager = container.bluetoothPrinterManager,
                                    onNavigateToUserManagement = { currentSubScreen = SubScreen.USER_MANAGEMENT },
                                    onNavigateToBusinessSetup = { currentSubScreen = SubScreen.BUSINESS_SETUP },
                                    onNavigateToAbout = { currentSubScreen = SubScreen.ABOUT },
                                    onLogout = {
                                        currentTab = AppTab.HOME
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
