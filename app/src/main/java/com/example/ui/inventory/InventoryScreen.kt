package com.example.ui.inventory

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.StockMovementType
import com.example.data.local.entity.UserEntity
import com.example.data.local.relations.ProductWithBarcodesAndCategory
import com.example.data.repository.BusinessSettings
import com.example.data.repository.CategoryRepository
import com.example.data.repository.InventoryRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.SettingsRepository
import com.example.domain.usecase.AdjustStockUseCase
import com.example.ui.components.StockBadge
import com.example.ui.components.formatMinorMoney
import com.example.ui.theme.PosDangerRed
import com.example.ui.theme.PosPrimaryBlue
import com.example.ui.theme.PosSuccessGreen
import com.example.ui.theme.PosWarningAmber
import com.example.util.AppStrings
import com.example.util.ProductImageHelper
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    currentUser: UserEntity,
    productRepository: ProductRepository,
    categoryRepository: CategoryRepository,
    inventoryRepository: InventoryRepository,
    settingsRepository: SettingsRepository,
    adjustStockUseCase: AdjustStockUseCase,
    prefilledBarcode: String? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val settings by settingsRepository.getSettingsFlow().collectAsState(initial = BusinessSettings())
    val S = remember(settings.language) { AppStrings(settings.language) }
    val categories by categoryRepository.getAllCategories().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Products, 1: Overview
    var searchQuery by remember { mutableStateOf("") }
    val productList by productRepository.searchProducts(searchQuery).collectAsState(initial = emptyList())

    // KPI values
    val totalCount by productRepository.countTotalProducts().collectAsState(initial = 0)
    val lowStockCount by productRepository.countLowStock().collectAsState(initial = 0)
    val outOfStockCount by productRepository.countOutOfStock().collectAsState(initial = 0)

    var totalCostVal by remember { mutableLongStateOf(0L) }
    var totalRetailVal by remember { mutableLongStateOf(0L) }

    LaunchedEffect(selectedTab, totalCount) {
        if (selectedTab == 1) {
            totalCostVal = productRepository.getTotalStockCostValue()
            totalRetailVal = productRepository.getTotalRetailValue()
        }
    }

    // Add / Edit Product modal
    var showAddEditModal by remember { mutableStateOf(prefilledBarcode != null) }
    var productToEdit by remember { mutableStateOf<ProductWithBarcodesAndCategory?>(null) }

    // Stock Adjustment modal
    var productToAdjust by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = PosPrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(S.inventoryTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    productToEdit = null
                    showAddEditModal = true
                },
                containerColor = PosPrimaryBlue,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = S.addProduct, tint = Color.White)
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("${S.navInventory} ($totalCount)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(S.stockOverview, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTab == 0) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(S.searchPlaceholder, fontSize = 11.5.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                if (productList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                if (searchQuery.isNotBlank()) "No matching products" else "No products in inventory yet",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(productList, key = { it.product.id }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("product_item_${item.product.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Product Image Thumbnail
                                    val imagePath = item.product.imagePath
                                    val imageFile = remember(imagePath) { imagePath?.let { File(it) } }

                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (imageFile != null && imageFile.exists()) {
                                            AsyncImage(
                                                model = imageFile,
                                                contentDescription = item.product.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Inventory2,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Product Info Column
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.product.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "SKU: ${item.product.sku}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                            item.category?.let { cat ->
                                                Text(
                                                    text = " • ${cat.name}",
                                                    fontSize = 10.sp,
                                                    color = PosPrimaryBlue,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = formatMinorMoney(item.product.salePrice, settings.currencySymbol),
                                                fontWeight = FontWeight.Bold,
                                                color = PosPrimaryBlue,
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(Coût: ${formatMinorMoney(item.product.costPrice, settings.currencySymbol)})",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Stock Badge & Actions
                                    Column(horizontalAlignment = Alignment.End) {
                                        StockBadge(stockQty = item.product.stockQty, reorderLevel = item.product.reorderLevel)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row {
                                            IconButton(
                                                onClick = { productToAdjust = item.product },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(Icons.Default.Tune, contentDescription = S.adjustStock, tint = PosPrimaryBlue, modifier = Modifier.size(15.dp))
                                            }
                                            Spacer(modifier = Modifier.width(2.dp))
                                            IconButton(
                                                onClick = {
                                                    productToEdit = item
                                                    showAddEditModal = true
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = S.editProduct, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Stock Overview Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(S.stockOverview, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MetricCard(
                            title = "SKUs",
                            value = totalCount.toString(),
                            color = PosPrimaryBlue,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = S.lowStock,
                            value = lowStockCount.toString(),
                            color = PosWarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = S.outOfStock,
                            value = outOfStockCount.toString(),
                            color = PosDangerRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Stock Valuation Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(S.stockValuation, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${S.retailVal}:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatMinorMoney(totalRetailVal, settings.currencySymbol), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PosPrimaryBlue)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${S.costVal}:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatMinorMoney(totalCostVal, settings.currencySymbol), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            val estMargin = if (totalRetailVal > 0) ((totalRetailVal - totalCostVal) / totalRetailVal.toDouble()) * 100.0 else 0.0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${S.marginAbbrev}:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(String.format("%.1f%%", estMargin), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PosSuccessGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(S.setupRules, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp)
                            Text("• ${S.prodReorder}: ${settings.defaultReorderLevel} ${S.pcsAbbrev}", fontSize = 10.5.sp)
                            Text("• Negative Stock Allowed: ${if (settings.allowNegativeStock) "OUI / YES" else "NON / NO"}", fontSize = 10.5.sp)
                            Text("• Active Categories: ${categories.size}", fontSize = 10.5.sp)
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Product Modal with Image Picking
    if (showAddEditModal) {
        AddEditProductModal(
            existing = productToEdit,
            initialBarcode = prefilledBarcode ?: "",
            categories = categories,
            currencySymbol = settings.currencySymbol,
            strings = S,
            onDismiss = {
                showAddEditModal = false
                productToEdit = null
            },
            onSave = { product, barcodes ->
                coroutineScope.launch {
                    if (productToEdit != null) {
                        productRepository.updateProduct(product.copy(id = productToEdit!!.product.id), barcodes)
                    } else {
                        productRepository.saveProduct(product, barcodes)
                    }
                    showAddEditModal = false
                    productToEdit = null
                }
            }
        )
    }

    // Stock Adjustment Modal
    productToAdjust?.let { product ->
        StockAdjustmentDialog(
            currentUser = currentUser,
            product = product,
            adjustStockUseCase = adjustStockUseCase,
            strings = S,
            onDismiss = { productToAdjust = null },
            onSuccess = { productToAdjust = null }
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 15.sp, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductModal(
    existing: ProductWithBarcodesAndCategory?,
    initialBarcode: String,
    categories: List<CategoryEntity>,
    currencySymbol: String,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSave: (ProductEntity, List<String>) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf(existing?.product?.name ?: "") }
    var sku by remember { mutableStateOf(existing?.product?.sku ?: initialBarcode) }
    var barcode by remember {
        mutableStateOf(existing?.barcodes?.firstOrNull()?.barcode ?: initialBarcode)
    }
    var selectedCategoryId by remember {
        mutableStateOf(existing?.product?.categoryId ?: categories.firstOrNull()?.id ?: 1L)
    }
    var costPriceText by remember {
        mutableStateOf(existing?.let { String.format("%.2f", it.product.costPrice / 100.0) } ?: "")
    }
    var salePriceText by remember {
        mutableStateOf(existing?.let { String.format("%.2f", it.product.salePrice / 100.0) } ?: "")
    }
    var stockQtyText by remember {
        mutableStateOf(existing?.product?.stockQty?.toString() ?: "10")
    }
    var reorderLevelText by remember {
        mutableStateOf(existing?.product?.reorderLevel?.toString() ?: "5")
    }

    // Product image state & photo picker launcher
    var imagePath by remember { mutableStateOf(existing?.product?.imagePath) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val copiedPath = ProductImageHelper.copyUriToInternalStorage(context, it)
            if (copiedPath != null) {
                imagePath = copiedPath
            }
        }
    }

    val costMinor = ((costPriceText.toDoubleOrNull() ?: 0.0) * 100).toLong()
    val saleMinor = ((salePriceText.toDoubleOrNull() ?: 0.0) * 100).toLong()
    val marginPercent = if (saleMinor > 0) ((saleMinor - costMinor) / saleMinor.toDouble()) * 100 else 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (existing != null) strings.editProduct else strings.addProduct,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            // Product Photo Picker Component
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentFile = remember(imagePath) { imagePath?.let { File(it) } }

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentFile != null && currentFile.exists()) {
                        AsyncImage(
                            model = currentFile,
                            contentDescription = strings.productPhoto,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = strings.pickPhoto,
                            tint = PosPrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.productPhoto, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (imagePath != null) "Photo attached" else "Attach image for POS tile",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(if (imagePath != null) strings.changePhoto else strings.pickPhoto, fontSize = 10.5.sp)
                    }

                    if (imagePath != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { imagePath = null },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = strings.removePhoto, tint = PosDangerRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(strings.prodName, fontSize = 11.5.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text(strings.prodSku, fontSize = 11.5.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text(strings.prodBarcode, fontSize = 11.5.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = costPriceText,
                    onValueChange = { costPriceText = it },
                    label = { Text("${strings.prodCost} ($currencySymbol)", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = salePriceText,
                    onValueChange = { salePriceText = it },
                    label = { Text("${strings.prodPrice} ($currencySymbol)", fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            if (saleMinor > 0 && costMinor > saleMinor) {
                Text(
                    text = "Alerte: Prix de vente inférieur au coût! (Marge: ${String.format("%.1f", marginPercent)}%)",
                    color = PosDangerRed,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            } else if (saleMinor > 0) {
                Text(
                    text = "${strings.marginAbbrev}: ${String.format("%.1f", marginPercent)}%",
                    color = PosSuccessGreen,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = stockQtyText,
                    onValueChange = { stockQtyText = it },
                    label = { Text(strings.prodStock, fontSize = 11.5.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = reorderLevelText,
                    onValueChange = { reorderLevelText = it },
                    label = { Text(strings.prodReorder, fontSize = 11.5.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val entity = ProductEntity(
                        id = existing?.product?.id ?: 0,
                        name = name.trim(),
                        sku = sku.trim(),
                        categoryId = selectedCategoryId,
                        costPrice = costMinor,
                        salePrice = saleMinor,
                        stockQty = stockQtyText.toIntOrNull() ?: 0,
                        reorderLevel = reorderLevelText.toIntOrNull() ?: 5,
                        imagePath = imagePath
                    )
                    onSave(entity, listOf(barcode.trim(), sku.trim()).filter { it.isNotBlank() })
                },
                enabled = name.isNotBlank() && sku.isNotBlank() && saleMinor > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("save_product_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(strings.save, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StockAdjustmentDialog(
    currentUser: UserEntity,
    product: ProductEntity,
    adjustStockUseCase: AdjustStockUseCase,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var qtyDeltaText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(StockMovementType.ADJUSTMENT) }
    var reason by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${strings.adjustStock} - ${product.name}", fontWeight = FontWeight.Bold, fontSize = 13.5.sp) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Stock Actuel: ${product.stockQty} ${strings.pcsAbbrev}", fontSize = 11.5.sp)

                OutlinedTextField(
                    value = qtyDeltaText,
                    onValueChange = { qtyDeltaText = it },
                    label = { Text("Delta / Changement (+/-)", fontSize = 11.sp) },
                    placeholder = { Text("Ex: 5 ou -2", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motif / Reason", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorText != null) {
                    Text(errorText!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val delta = qtyDeltaText.toIntOrNull()
                    if (delta != null && delta != 0) {
                        coroutineScope.launch {
                            val result = adjustStockUseCase(
                                actingUser = currentUser,
                                productId = product.id,
                                qtyChange = delta,
                                type = selectedType,
                                reason = reason.ifBlank { "Manual adjustment" }
                            )
                            result.fold(
                                onSuccess = { onSuccess() },
                                onFailure = { errorText = it.message }
                            )
                        }
                    }
                },
                enabled = qtyDeltaText.toIntOrNull() != null && qtyDeltaText.toIntOrNull() != 0
            ) {
                Text(strings.save, fontSize = 11.5.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel, fontSize = 11.5.sp) }
        }
    )
}
