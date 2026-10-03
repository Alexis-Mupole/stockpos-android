package com.example.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.PaymentMethod
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRole
import com.example.data.repository.BusinessSettings
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.SaleRepository
import com.example.data.repository.SettingsRepository
import com.example.domain.printer.PdfReceiptGenerator
import com.example.domain.usecase.RoleGuard
import com.example.ui.components.RoleBadge
import com.example.ui.components.SaleStatusBadge
import com.example.ui.components.formatMinorMoney
import com.example.ui.theme.PosDangerRed
import com.example.ui.theme.PosPrimaryBlue
import com.example.ui.theme.PosSuccessGreen
import com.example.ui.theme.PosWarningAmber
import com.example.util.AppStrings
import com.example.util.ProductImageHelper
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DashboardPeriod {
    TODAY, WEEK, MONTH, ALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: UserEntity,
    settingsRepository: SettingsRepository,
    productRepository: ProductRepository,
    categoryRepository: CategoryRepository,
    saleRepository: SaleRepository,
    onNavigateToSell: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAbout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by settingsRepository.getSettingsFlow().collectAsState(initial = BusinessSettings())
    val S = remember(settings.language) { AppStrings(settings.language) }
    val categories by categoryRepository.getAllCategories().collectAsState(initial = emptyList())

    // Inventory metrics
    val totalProducts by productRepository.countTotalProducts().collectAsState(initial = 0)
    val lowStockCount by productRepository.countLowStock().collectAsState(initial = 0)
    val outOfStockCount by productRepository.countOutOfStock().collectAsState(initial = 0)

    var totalCostVal by remember { mutableLongStateOf(0L) }
    var totalRetailVal by remember { mutableLongStateOf(0L) }

    LaunchedEffect(totalProducts) {
        totalCostVal = productRepository.getTotalStockCostValue()
        totalRetailVal = productRepository.getTotalRetailValue()
    }

    // Dashboard Timeframe Filter State (Today / 7 Days / 30 Days / All Time)
    var selectedPeriod by remember { mutableStateOf(DashboardPeriod.TODAY) }

    val allSales by (if (RoleGuard.canViewAllReports(currentUser.role)) {
        saleRepository.getAllSales()
    } else {
        saleRepository.getSalesByUserId(currentUser.id)
    }).collectAsState(initial = emptyList())

    val nowMs = System.currentTimeMillis()
    val startOfPeriodMs = remember(selectedPeriod, nowMs) {
        val cal = Calendar.getInstance()
        when (selectedPeriod) {
            DashboardPeriod.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            DashboardPeriod.WEEK -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                cal.timeInMillis
            }
            DashboardPeriod.MONTH -> {
                cal.add(Calendar.DAY_OF_YEAR, -30)
                cal.timeInMillis
            }
            DashboardPeriod.ALL -> 0L
        }
    }

    val filteredSales = remember(allSales, startOfPeriodMs) {
        allSales.filter { it.sale.createdAt >= startOfPeriodMs }
    }

    val periodSalesRevenue = remember(filteredSales) {
        filteredSales.sumOf { it.sale.total }
    }

    val periodOrdersCount = filteredSales.size

    val periodItemsSold = remember(filteredSales) {
        filteredSales.sumOf { sale -> sale.items.sumOf { it.qty } }
    }

    val periodEstProfit = remember(filteredSales) {
        filteredSales.sumOf { sale ->
            val saleTotal = sale.sale.total
            val costOfItems = sale.items.sumOf { it.unitCostSnapshot * it.qty }
            (saleTotal - costOfItems).coerceAtLeast(0L)
        }
    }

    val periodMarginPercent = remember(periodSalesRevenue, periodEstProfit) {
        if (periodSalesRevenue > 0) (periodEstProfit / periodSalesRevenue.toDouble()) * 100.0 else 0.0
    }

    val avgOrderValue = remember(periodSalesRevenue, periodOrdersCount) {
        if (periodOrdersCount > 0) periodSalesRevenue / periodOrdersCount else 0L
    }

    // Payment method breakdown in period
    val cashTotal = remember(filteredSales) {
        filteredSales.sumOf { it.payments.filter { p -> p.method == PaymentMethod.CASH }.sumOf { p -> p.amount } }
    }
    val cardTotal = remember(filteredSales) {
        filteredSales.sumOf { it.payments.filter { p -> p.method == PaymentMethod.CARD }.sumOf { p -> p.amount } }
    }
    val momoTotal = remember(filteredSales) {
        filteredSales.sumOf { it.payments.filter { p -> p.method == PaymentMethod.MOBILE_MONEY }.sumOf { p -> p.amount } }
    }

    // Business setup wizard modal states
    var activeSetupStep by remember { mutableStateOf<Int?>(null) }
    var isSetupCardExpanded by remember { mutableStateOf(!settings.businessSetupComplete) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(PosPrimaryBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = PosPrimaryBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = settings.businessName,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = if (settings.businessSetupComplete) "Terminal POS • Ready" else "Setup in Progress",
                                fontSize = 9.5.sp,
                                color = if (settings.businessSetupComplete) PosSuccessGreen else PosWarningAmber,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    // Instant Language Switcher (EN / FR)
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val nextLang = if (settings.language.equals("FR", ignoreCase = true)) "EN" else "FR"
                                settingsRepository.saveSettings(settings.copy(language = nextLang))
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(28.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = "Language", modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (S.isFr) "🇫🇷 FR" else "🇬🇧 EN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // About App & Creator Alexis Mupole
                    IconButton(
                        onClick = onNavigateToAbout,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("home_about_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = S.aboutApp,
                            tint = PosPrimaryBlue,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        modifier = Modifier.padding(end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoleBadge(role = currentUser.role)
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Business Setup Checklist Center (Admin only)
            if (currentUser.role == UserRole.ADMIN) {
                item {
                    CompleteBusinessSetupCard(
                        settings = settings,
                        categoryCount = categories.size,
                        productCount = totalProducts,
                        strings = S,
                        isExpanded = isSetupCardExpanded,
                        onToggleExpand = { isSetupCardExpanded = !isSetupCardExpanded },
                        onOpenStep = { step -> activeSetupStep = step },
                        onMarkComplete = {
                            coroutineScope.launch {
                                settingsRepository.markBusinessSetupComplete()
                                isSetupCardExpanded = false
                            }
                        }
                    )
                }
            }

            // 2. High-Impact Point of Sale CTA Button
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onNavigateToSell)
                        .testTag("home_quick_sell_button"),
                    colors = CardDefaults.cardColors(containerColor = PosSuccessGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PointOfSale,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = S.startNewSale,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = S.startSaleSub,
                                    color = Color.White.copy(alpha = 0.88f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // 3. Interactive Operations Dashboard Header & Period Filter
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PosPrimaryBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = S.dashboardTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Timeframe Pill Selector
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PeriodChip(
                                label = S.filterToday,
                                isSelected = selectedPeriod == DashboardPeriod.TODAY,
                                onClick = { selectedPeriod = DashboardPeriod.TODAY }
                            )
                            PeriodChip(
                                label = S.filter7d,
                                isSelected = selectedPeriod == DashboardPeriod.WEEK,
                                onClick = { selectedPeriod = DashboardPeriod.WEEK }
                            )
                            PeriodChip(
                                label = S.filter30d,
                                isSelected = selectedPeriod == DashboardPeriod.MONTH,
                                onClick = { selectedPeriod = DashboardPeriod.MONTH }
                            )
                            PeriodChip(
                                label = S.filterAll,
                                isSelected = selectedPeriod == DashboardPeriod.ALL,
                                onClick = { selectedPeriod = DashboardPeriod.ALL }
                            )
                        }
                    }

                    // Dashboard Primary Metrics Grid (Responsive 2x2 Grid)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        CompactMetricCard(
                            title = S.periodSales,
                            value = formatMinorMoney(periodSalesRevenue, settings.currencySymbol),
                            subtitle = "${periodOrdersCount} ${S.ordersCount}",
                            icon = Icons.Default.Payments,
                            tint = PosPrimaryBlue,
                            modifier = Modifier.weight(1f)
                        )
                        CompactMetricCard(
                            title = S.estProfit,
                            value = formatMinorMoney(periodEstProfit, settings.currencySymbol),
                            subtitle = "${String.format("%.1f", periodMarginPercent)}% ${S.marginAbbrev}",
                            icon = Icons.Default.TrendingUp,
                            tint = PosSuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        CompactMetricCard(
                            title = S.avgOrderVal,
                            value = formatMinorMoney(avgOrderValue, settings.currencySymbol),
                            subtitle = "${periodOrdersCount} ${S.ordersCount}",
                            icon = Icons.Default.PointOfSale,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f)
                        )
                        CompactMetricCard(
                            title = S.itemsSold,
                            value = "$periodItemsSold ${S.pcsAbbrev}",
                            subtitle = "${S.totalUnitsSold}",
                            icon = Icons.Default.Receipt,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Inventory Health Metrics Row (Clickable)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        CompactClickableMetric(
                            title = S.navInventory,
                            value = "$totalProducts ${S.pcsAbbrev}",
                            badge = formatMinorMoney(totalRetailVal, settings.currencySymbol),
                            color = PosPrimaryBlue,
                            onClick = onNavigateToInventory,
                            modifier = Modifier.weight(1.15f)
                        )
                        CompactClickableMetric(
                            title = S.lowStock,
                            value = lowStockCount.toString(),
                            badge = if (lowStockCount > 0) "Alert" else "OK",
                            color = if (lowStockCount > 0) PosWarningAmber else Color.Gray,
                            onClick = onNavigateToInventory,
                            modifier = Modifier.weight(0.9f)
                        )
                        CompactClickableMetric(
                            title = S.outOfStock,
                            value = outOfStockCount.toString(),
                            badge = if (outOfStockCount > 0) "Crit." else "0",
                            color = if (outOfStockCount > 0) PosDangerRed else Color.Gray,
                            onClick = onNavigateToInventory,
                            modifier = Modifier.weight(0.9f)
                        )
                    }

                    // Payment Tender Breakdown Strip (if any sales)
                    if (periodSalesRevenue > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(S.tenderLabel, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("💵 ${S.cash}: ${formatMinorMoney(cashTotal, settings.currencySymbol)}", fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                Text("💳 ${S.card}: ${formatMinorMoney(cardTotal, settings.currencySymbol)}", fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                Text("📱 MoMo: ${formatMinorMoney(momoTotal, settings.currencySymbol)}", fontSize = 9.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // 4. Quick Action Navigation Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (RoleGuard.canManageInventory(currentUser.role)) {
                        OutlinedButton(
                            onClick = onNavigateToInventory,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${S.navInventory} ($totalProducts)", fontSize = 10.5.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onNavigateToHistory,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(S.navHistory, fontSize = 10.5.sp)
                    }
                }
            }

            // 5. Recent Sales Activity Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = S.recentTransactions,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = S.viewAll,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PosPrimaryBlue,
                        modifier = Modifier.clickable(onClick = onNavigateToHistory)
                    )
                }
            }

            if (allSales.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = S.noSalesYet,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = onNavigateToSell,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(S.startNewSale, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                items(allSales.take(5), key = { it.sale.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.sale.receiptNo,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    SaleStatusBadge(status = item.sale.status)
                                }
                                val timeStr = SimpleDateFormat("HH:mm", Locale.US).format(Date(item.sale.createdAt))
                                Text(
                                    text = "$timeStr • ${item.items.sumOf { it.qty }} ${S.itemsSold.lowercase()}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatMinorMoney(item.sale.total, settings.currencySymbol),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        PdfReceiptGenerator.shareReceiptPdf(context, item, settings)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = S.receipt,
                                        modifier = Modifier.size(14.dp),
                                        tint = PosPrimaryBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }

    // Modal Sheet for Setting Up Each Area of the Business
    activeSetupStep?.let { step ->
        BusinessStepModalSheet(
            step = step,
            settings = settings,
            categories = categories,
            strings = S,
            settingsRepository = settingsRepository,
            categoryRepository = categoryRepository,
            productRepository = productRepository,
            onDismiss = { activeSetupStep = null }
        )
    }
}

@Composable
private fun PeriodChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) PosPrimaryBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CompactMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(7.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(11.dp))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = tint,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 8.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CompactClickableMetric(
    title: String,
    value: String,
    badge: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(0.7.dp, color.copy(alpha = 0.25f)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(title, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = color, maxLines = 1)
                Text(badge, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun CompleteBusinessSetupCard(
    settings: BusinessSettings,
    categoryCount: Int,
    productCount: Int,
    strings: AppStrings,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenStep: (Int) -> Unit,
    onMarkComplete: () -> Unit
) {
    val isComplete = settings.businessSetupComplete

    val step1Done = settings.businessName.isNotBlank() && settings.businessName != "StockPOS Store"
    val step2Done = settings.currencySymbol.isNotBlank()
    val step3Done = settings.receiptPaperWidth > 0
    val step4Done = categoryCount > 0
    val step5Done = productCount > 0
    val step6Done = settings.defaultReorderLevel > 0

    val completedCount = listOf(step1Done, step2Done, step3Done, step4Done, step5Done, step6Done).count { it }
    val progress = completedCount / 6f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (!isComplete) PosPrimaryBlue.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            0.9.dp,
            if (!isComplete) PosPrimaryBlue.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isComplete) PosSuccessGreen.copy(alpha = 0.15f) else PosPrimaryBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.AddBusiness,
                            contentDescription = null,
                            tint = if (isComplete) PosSuccessGreen else PosPrimaryBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (isComplete) strings.setupComplete else "${strings.setupCenter} ($completedCount/6)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isComplete) "Tap to review or edit business settings" else "Configure store parameters to streamline sales",
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onToggleExpand, modifier = Modifier.size(22.dp)) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = if (isComplete) PosSuccessGreen else PosPrimaryBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BusinessSetupStepItem(
                        title = strings.setupProfile,
                        summary = "${settings.businessName} • ${if (settings.businessPhone.isNotBlank()) settings.businessPhone else "No phone"}",
                        isDone = step1Done,
                        onClick = { onOpenStep(1) }
                    )
                    BusinessSetupStepItem(
                        title = strings.setupCurrencyTax,
                        summary = "${settings.currencyCode} (${settings.currencySymbol}) • Tax: ${settings.defaultTaxRatePercent}%",
                        isDone = step2Done,
                        onClick = { onOpenStep(2) }
                    )
                    BusinessSetupStepItem(
                        title = strings.setupReceipt,
                        summary = "${settings.receiptPaperWidth}mm ESC/POS • QR: ${if (settings.receiptShowQr) "ON" else "OFF"}",
                        isDone = step3Done,
                        onClick = { onOpenStep(3) }
                    )
                    BusinessSetupStepItem(
                        title = strings.setupCategories,
                        summary = "$categoryCount ${strings.prodCategory.lowercase()}",
                        isDone = step4Done,
                        onClick = { onOpenStep(4) }
                    )
                    BusinessSetupStepItem(
                        title = strings.setupProducts,
                        summary = "$productCount ${strings.navInventory.lowercase()}",
                        isDone = step5Done,
                        onClick = { onOpenStep(5) }
                    )
                    BusinessSetupStepItem(
                        title = strings.setupRules,
                        summary = "Reorder: ${settings.defaultReorderLevel} • Negative stock: ${if (settings.allowNegativeStock) "YES" else "NO"}",
                        isDone = step6Done,
                        onClick = { onOpenStep(6) }
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Button(
                        onClick = onMarkComplete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isComplete) MaterialTheme.colorScheme.secondary else PosPrimaryBlue
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    ) {
                        Text(
                            text = if (isComplete) "Save & Keep Active" else strings.finalizeSetup,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BusinessSetupStepItem(
    title: String,
    summary: String,
    isDone: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 7.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Tune,
                contentDescription = null,
                tint = if (isDone) PosSuccessGreen else PosPrimaryBlue,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp)
                Text(summary, fontSize = 8.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(11.dp),
            tint = PosPrimaryBlue
        )
    }
}

/**
 * Responsive Modal Bottom Sheet with device keyboard & photo picker support for configuring each step.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessStepModalSheet(
    step: Int,
    settings: BusinessSettings,
    categories: List<CategoryEntity>,
    strings: AppStrings,
    settingsRepository: SettingsRepository,
    categoryRepository: CategoryRepository,
    productRepository: ProductRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (step) {
                1 -> {
                    Text(strings.setupProfile, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)

                    var name by remember { mutableStateOf(settings.businessName) }
                    var address by remember { mutableStateOf(settings.businessAddress) }
                    var phone by remember { mutableStateOf(settings.businessPhone) }
                    var taxId by remember { mutableStateOf(settings.businessTaxId) }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Store / Business Name *", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address / City", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = taxId,
                        onValueChange = { taxId = it },
                        label = { Text("Tax Registration / VAT ID", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                settingsRepository.saveSettings(
                                    settings.copy(
                                        businessName = name.trim().ifBlank { "StockPOS Store" },
                                        businessAddress = address.trim(),
                                        businessPhone = phone.trim(),
                                        businessTaxId = taxId.trim()
                                    )
                                )
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Text(strings.save, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                2 -> {
                    Text(strings.setupCurrencyTax, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)

                    var symbol by remember { mutableStateOf(settings.currencySymbol) }
                    var code by remember { mutableStateOf(settings.currencyCode) }
                    var taxRate by remember { mutableStateOf(settings.defaultTaxRatePercent.toString()) }
                    var isInclusive by remember { mutableStateOf(settings.taxInclusive) }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = symbol,
                            onValueChange = { symbol = it },
                            label = { Text("Symbol ($, €, £)", fontSize = 10.5.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it.uppercase() },
                            label = { Text("ISO (USD, EUR)", fontSize = 10.5.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = taxRate,
                        onValueChange = { taxRate = it },
                        label = { Text("Tax Rate (%)", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(checked = isInclusive, onCheckedChange = { isInclusive = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isInclusive) "Tax-Inclusive (sur l'étiquette)" else "Tax-Exclusive (+ à la caisse)",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                settingsRepository.saveSettings(
                                    settings.copy(
                                        currencySymbol = symbol.trim().ifBlank { "$" },
                                        currencyCode = code.trim().ifBlank { "USD" },
                                        defaultTaxRatePercent = taxRate.toDoubleOrNull() ?: 0.0,
                                        taxInclusive = isInclusive
                                    )
                                )
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Text(strings.save, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                3 -> {
                    Text(strings.setupReceipt, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)

                    var paperWidth by remember { mutableIntStateOf(settings.receiptPaperWidth) }
                    var header by remember { mutableStateOf(settings.receiptHeader) }
                    var footer by remember { mutableStateOf(settings.receiptFooter) }
                    var showQr by remember { mutableStateOf(settings.receiptShowQr) }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = paperWidth == 58, onClick = { paperWidth = 58 })
                            Text("58mm", fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = paperWidth == 80, onClick = { paperWidth = 80 })
                            Text("80mm", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = header,
                        onValueChange = { header = it },
                        label = { Text("Header Greeting Line", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = footer,
                        onValueChange = { footer = it },
                        label = { Text("Footer Policy / Window", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(checked = showQr, onCheckedChange = { showQr = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("QR Code sur Reçu (pour retour rapide)", fontSize = 10.5.sp)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                settingsRepository.saveSettings(
                                    settings.copy(
                                        receiptPaperWidth = paperWidth,
                                        receiptHeader = header.trim(),
                                        receiptFooter = footer.trim(),
                                        receiptShowQr = showQr
                                    )
                                )
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Text(strings.save, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                4 -> {
                    Text(strings.setupCategories, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)

                    var newCat by remember { mutableStateOf("") }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newCat,
                            onValueChange = { newCat = it },
                            placeholder = { Text("Category name...", fontSize = 11.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newCat.isNotBlank()) {
                                    coroutineScope.launch {
                                        categoryRepository.saveCategory(CategoryEntity(name = newCat.trim()))
                                        newCat = ""
                                    }
                                }
                            }),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (newCat.isNotBlank()) {
                                    coroutineScope.launch {
                                        categoryRepository.saveCategory(CategoryEntity(name = newCat.trim()))
                                        newCat = ""
                                    }
                                }
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(strings.save, fontSize = 11.sp)
                        }
                    }

                    if (categories.isEmpty()) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    categoryRepository.preseedDefaultCategories()
                                }
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("Pre-seed Default Retail Categories", fontSize = 10.5.sp)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        categories.forEach { c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(c.name, fontSize = 11.5.sp, fontWeight = FontWeight.Medium)
                                IconButton(
                                    onClick = { coroutineScope.launch { categoryRepository.deleteCategory(c) } },
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(13.dp))
                                }
                            }
                        }
                    }

                    Button(onClick = onDismiss, shape = RoundedCornerShape(6.dp), modifier = Modifier.fillMaxWidth().height(36.dp)) {
                        Text(strings.done, fontSize = 11.sp)
                    }
                }

                5 -> {
                    Text(strings.addProduct, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)

                    var prodName by remember { mutableStateOf("") }
                    var prodBarcode by remember { mutableStateOf("") }
                    var prodCost by remember { mutableStateOf("") }
                    var prodPrice by remember { mutableStateOf("") }
                    var prodStock by remember { mutableStateOf("10") }
                    var selectedCategory by remember { mutableStateOf(categories.firstOrNull()) }
                    var addSuccessMsg by remember { mutableStateOf<String?>(null) }

                    // Image picker for quick product creation
                    var imagePath by remember { mutableStateOf<String?>(null) }
                    val photoPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.PickVisualMedia()
                    ) { uri: Uri? ->
                        uri?.let {
                            val path = ProductImageHelper.copyUriToInternalStorage(context, it)
                            if (path != null) imagePath = path
                        }
                    }

                    // Photo selector strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val file = remember(imagePath) { imagePath?.let { File(it) } }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (file != null && file.exists()) {
                                AsyncImage(
                                    model = file,
                                    contentDescription = strings.productPhoto,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = PosPrimaryBlue, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(strings.productPhoto, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(if (imagePath != null) "Photo attached" else "Tap to add image", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(5.dp),
                            modifier = Modifier.height(28.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                        ) {
                            Text(if (imagePath != null) strings.changePhoto else strings.pickPhoto, fontSize = 9.5.sp)
                        }
                    }

                    OutlinedTextField(
                        value = prodName,
                        onValueChange = { prodName = it },
                        label = { Text(strings.prodName, fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = prodBarcode,
                        onValueChange = { prodBarcode = it },
                        label = { Text("${strings.prodBarcode} / SKU (opt.)", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = prodCost,
                            onValueChange = { prodCost = it },
                            label = { Text("${strings.prodCost} (${settings.currencySymbol})", fontSize = 10.5.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = prodPrice,
                            onValueChange = { prodPrice = it },
                            label = { Text("${strings.prodPrice} (${settings.currencySymbol}) *", fontSize = 10.5.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = prodStock,
                        onValueChange = { prodStock = it },
                        label = { Text("${strings.prodStock} (${strings.pcsAbbrev})", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (addSuccessMsg != null) {
                        Text(addSuccessMsg!!, color = PosSuccessGreen, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (prodName.isNotBlank() && prodPrice.isNotBlank()) {
                                coroutineScope.launch {
                                    val priceMinor = ((prodPrice.toDoubleOrNull() ?: 0.0) * 100).toLong()
                                    val costMinor = ((prodCost.toDoubleOrNull() ?: 0.0) * 100).toLong()
                                    val stockQty = prodStock.toIntOrNull() ?: 0
                                    val skuVal = prodBarcode.trim().ifBlank { "SKU-" + System.currentTimeMillis().toString().takeLast(6) }

                                    val catId = selectedCategory?.id ?: categories.firstOrNull()?.id ?: run {
                                        val firstCatId = categoryRepository.saveCategory(CategoryEntity(name = "General"))
                                        firstCatId
                                    }

                                    val newProd = ProductEntity(
                                        name = prodName.trim(),
                                        sku = skuVal,
                                        categoryId = catId,
                                        costPrice = costMinor,
                                        salePrice = priceMinor,
                                        stockQty = stockQty,
                                        reorderLevel = settings.defaultReorderLevel,
                                        imagePath = imagePath
                                    )
                                    val barcodes = if (prodBarcode.isNotBlank()) listOf(prodBarcode.trim()) else emptyList()
                                    productRepository.saveProduct(newProd, barcodes)

                                    addSuccessMsg = "Produit '${prodName.trim()}' ajouté avec succès !"
                                    prodName = ""
                                    prodBarcode = ""
                                    prodCost = ""
                                    prodPrice = ""
                                    prodStock = "10"
                                    imagePath = null
                                }
                            }
                        },
                        enabled = prodName.isNotBlank() && prodPrice.isNotBlank(),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Text(strings.save, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(34.dp)
                    ) {
                        Text(strings.done, fontSize = 11.sp)
                    }
                }

                6 -> {
                    Text(strings.setupRules, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)

                    var reorderLevel by remember { mutableStateOf(settings.defaultReorderLevel.toString()) }
                    var allowNegative by remember { mutableStateOf(settings.allowNegativeStock) }
                    var soundBeep by remember { mutableStateOf(settings.soundBeepEnabled) }
                    var hapticVibrate by remember { mutableStateOf(settings.hapticVibrateEnabled) }

                    OutlinedTextField(
                        value = reorderLevel,
                        onValueChange = { reorderLevel = it },
                        label = { Text("Seuil d'Alerte Stock Faible (${strings.pcsAbbrev})", fontSize = 11.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(checked = allowNegative, onCheckedChange = { allowNegative = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Autoriser Vente Stock Négatif", fontSize = 10.5.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(checked = soundBeep, onCheckedChange = { soundBeep = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bip Sonore au Scan", fontSize = 10.5.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(checked = hapticVibrate, onCheckedChange = { hapticVibrate = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vibration Haptique au Scan", fontSize = 10.5.sp)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                settingsRepository.saveSettings(
                                    settings.copy(
                                        defaultReorderLevel = reorderLevel.toIntOrNull() ?: 5,
                                        allowNegativeStock = allowNegative,
                                        soundBeepEnabled = soundBeep,
                                        hapticVibrateEnabled = hapticVibrate
                                    )
                                )
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Text(strings.save, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
