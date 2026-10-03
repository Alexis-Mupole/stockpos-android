package com.example.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SaleStatus
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRole
import com.example.data.local.relations.SaleWithItemsAndPayments
import com.example.data.repository.BusinessSettings
import com.example.data.repository.SaleRepository
import com.example.data.repository.SettingsRepository
import com.example.domain.printer.PdfReceiptGenerator
import com.example.domain.printer.ReceiptFormatter
import com.example.domain.usecase.RefundSaleUseCase
import com.example.domain.usecase.RoleGuard
import com.example.ui.components.SaleStatusBadge
import com.example.ui.components.formatMinorMoney
import com.example.ui.theme.PosDangerRed
import com.example.ui.theme.PosPrimaryBlue
import com.example.ui.theme.PosSuccessGreen
import com.example.ui.theme.PosWarningAmber
import com.example.util.AppStrings
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryAndReportsScreen(
    currentUser: UserEntity,
    saleRepository: SaleRepository,
    settingsRepository: SettingsRepository,
    refundSaleUseCase: RefundSaleUseCase,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by settingsRepository.getSettingsFlow().collectAsState(initial = BusinessSettings())
    val S = remember(settings.language) { AppStrings(settings.language) }

    val isFullManagerOrAdmin = RoleGuard.canViewAllReports(currentUser.role)

    // Role-scoped query: Sellers see ONLY their own sales; Admin/Manager see all
    val salesList by (if (isFullManagerOrAdmin) {
        saleRepository.getAllSales()
    } else {
        saleRepository.getSalesByUserId(currentUser.id)
    }).collectAsState(initial = emptyList())

    val userSalesSummaries by saleRepository.getUserSalesSummaries().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedSaleDetails by remember { mutableStateOf<SaleWithItemsAndPayments?>(null) }
    var showRefundDialog by remember { mutableStateOf(false) }
    var showVoidDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isFullManagerOrAdmin) S.salesAndReports else S.mySalesHistory,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isFullManagerOrAdmin) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("${S.receiptsTab} (${salesList.size})", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(S.sellerPerfTab, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            if (selectedTab == 0 || !isFullManagerOrAdmin) {
                // Personal or Store Sales Header
                val totalRevenue = salesList.filter { it.sale.status == SaleStatus.COMPLETED }.sumOf { it.sale.total }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isFullManagerOrAdmin) S.storeTotalRevenue else S.myTotalRevenue,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatMinorMoney(totalRevenue, settings.currencySymbol),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = "${salesList.size} ${S.receiptsTab.lowercase()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(salesList, key = { it.sale.id }) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSaleDetails = item },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.sale.receiptNo, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        SaleStatusBadge(status = item.sale.status)
                                    }
                                    val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(item.sale.createdAt))
                                    val cashierText = if (isFullManagerOrAdmin && item.user != null) " • Cashier: ${item.user.name}" else ""
                                    Text(
                                        text = "$dateStr • ${item.items.sumOf { it.qty }} items$cashierText",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = formatMinorMoney(item.sale.total, settings.currencySymbol),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // Reports: Sales by Seller Breakdown (Admin/Manager only)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "Staff Sales Attribution",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Complete traceability of revenue generated by each cashier account.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(userSalesSummaries) { summary ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(summary.userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(
                                        text = "${summary.userRole} • ${summary.salesCount} completed sales",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = formatMinorMoney(summary.totalRevenue, settings.currencySymbol),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = PosSuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Receipt Detail Bottom Sheet (Reprint / PDF / Share / Refund)
    selectedSaleDetails?.let { saleDetails ->
        ModalBottomSheet(onDismissRequest = { selectedSaleDetails = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Receipt #${saleDetails.sale.receiptNo}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    SaleStatusBadge(status = saleDetails.sale.status)
                }

                // Line items preview
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    saleDetails.items.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${line.qty}x ${line.productNameSnapshot}", fontSize = 14.sp)
                            Text(formatMinorMoney(line.lineTotal, settings.currencySymbol), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total:", fontWeight = FontWeight.Bold)
                    Text(
                        text = formatMinorMoney(saleDetails.sale.total, settings.currencySymbol),
                        fontWeight = FontWeight.Bold,
                        color = PosPrimaryBlue
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            PdfReceiptGenerator.shareReceiptPdf(context, saleDetails, settings)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reprint / Share")
                    }

                    // Admin / Manager only: Refund & Void
                    if (isFullManagerOrAdmin && saleDetails.sale.status == SaleStatus.COMPLETED) {
                        Button(
                            onClick = { showRefundDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PosWarningAmber),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Refund")
                        }

                        Button(
                            onClick = { showVoidDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PosDangerRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Void")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Refund Dialog
    if (showRefundDialog && selectedSaleDetails != null) {
        var refundReason by remember { mutableStateOf("Customer return") }
        AlertDialog(
            onDismissRequest = { showRefundDialog = false },
            title = { Text("Issue Refund for #${selectedSaleDetails!!.sale.receiptNo}") },
            text = {
                Column {
                    Text("This will mark the receipt as refunded and automatically return items back to inventory stock.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = refundReason,
                        onValueChange = { refundReason = it },
                        label = { Text("Reason for refund") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val itemsToRefund = selectedSaleDetails!!.items.map { it to it.qty }
                            refundSaleUseCase(
                                actingUser = currentUser,
                                saleId = selectedSaleDetails!!.sale.id,
                                refundItems = itemsToRefund,
                                reason = refundReason,
                                restock = true
                            )
                            showRefundDialog = false
                            selectedSaleDetails = null
                        }
                    }
                ) {
                    Text("Confirm Refund & Restock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRefundDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Void Dialog
    if (showVoidDialog && selectedSaleDetails != null) {
        var voidReason by remember { mutableStateOf("Cashier error") }
        AlertDialog(
            onDismissRequest = { showVoidDialog = false },
            title = { Text("Void Sale #${selectedSaleDetails!!.sale.receiptNo}") },
            text = {
                Column {
                    Text("Voiding completely reverses the sale and returns all items to stock.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = voidReason,
                        onValueChange = { voidReason = it },
                        label = { Text("Reason for void") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            saleRepository.voidSale(
                                saleId = selectedSaleDetails!!.sale.id,
                                actingUserId = currentUser.id,
                                reason = voidReason
                            )
                            showVoidDialog = false
                            selectedSaleDetails = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PosDangerRed)
                ) {
                    Text("Confirm Void")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoidDialog = false }) { Text("Cancel") }
            }
        )
    }
}
