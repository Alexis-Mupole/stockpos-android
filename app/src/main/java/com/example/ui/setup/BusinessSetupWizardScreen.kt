package com.example.ui.setup

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.repository.BusinessSettings
import com.example.data.repository.CategoryRepository
import com.example.data.repository.ProductRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.theme.PosPrimaryBlue
import com.example.util.ProductImageHelper
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSetupWizardScreen(
    settingsRepository: SettingsRepository,
    categoryRepository: CategoryRepository,
    productRepository: ProductRepository,
    onSetupFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableIntStateOf(1) } // 1 to 7

    // State holders
    var businessName by remember { mutableStateOf("StockPOS Store") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var taxId by remember { mutableStateOf("") }

    var currencyCode by remember { mutableStateOf("USD") }
    var currencySymbol by remember { mutableStateOf("$") }
    var defaultTaxRate by remember { mutableStateOf("0.0") }
    var isTaxInclusive by remember { mutableStateOf(true) }

    var receiptPaperWidth by remember { mutableIntStateOf(58) }
    var receiptHeader by remember { mutableStateOf("Thank you for shopping with us!") }
    var receiptFooter by remember { mutableStateOf("Returnable within 7 days with receipt.") }
    var showQr by remember { mutableStateOf(true) }

    val categories by categoryRepository.getAllCategories().collectAsState(initial = emptyList())
    var newCategoryName by remember { mutableStateOf("") }

    var defaultReorderLevel by remember { mutableStateOf("5") }
    var allowNegativeStock by remember { mutableStateOf(false) }

    // Load initial settings
    LaunchedEffect(Unit) {
        val s = settingsRepository.getSettings()
        businessName = s.businessName
        address = s.businessAddress
        phone = s.businessPhone
        taxId = s.businessTaxId
        currencyCode = s.currencyCode
        currencySymbol = s.currencySymbol
        defaultTaxRate = s.defaultTaxRatePercent.toString()
        isTaxInclusive = s.taxInclusive
        receiptPaperWidth = s.receiptPaperWidth
        receiptHeader = s.receiptHeader
        receiptFooter = s.receiptFooter
        showQr = s.receiptShowQr
        defaultReorderLevel = s.defaultReorderLevel.toString()
        allowNegativeStock = s.allowNegativeStock

        // Preseed categories if empty
        categoryRepository.preseedDefaultCategories()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Business Setup ($currentStep/7)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    if (currentStep > 1) {
                        IconButton(onClick = { currentStep -= 1 }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep < 7) {
                    OutlinedButton(
                        onClick = { currentStep = 7 },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Skip to Review")
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                // Save intermediate progress
                                val partial = BusinessSettings(
                                    businessName = businessName,
                                    businessAddress = address,
                                    businessPhone = phone,
                                    businessTaxId = taxId,
                                    currencyCode = currencyCode,
                                    currencySymbol = currencySymbol,
                                    defaultTaxRatePercent = defaultTaxRate.toDoubleOrNull() ?: 0.0,
                                    taxInclusive = isTaxInclusive,
                                    receiptPaperWidth = receiptPaperWidth,
                                    receiptHeader = receiptHeader,
                                    receiptFooter = receiptFooter,
                                    receiptShowQr = showQr,
                                    defaultReorderLevel = defaultReorderLevel.toIntOrNull() ?: 5,
                                    allowNegativeStock = allowNegativeStock
                                )
                                settingsRepository.saveSettings(partial)
                                currentStep += 1
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("setup_next_step")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val completeSettings = BusinessSettings(
                                    businessName = businessName,
                                    businessAddress = address,
                                    businessPhone = phone,
                                    businessTaxId = taxId,
                                    currencyCode = currencyCode,
                                    currencySymbol = currencySymbol,
                                    defaultTaxRatePercent = defaultTaxRate.toDoubleOrNull() ?: 0.0,
                                    taxInclusive = isTaxInclusive,
                                    receiptPaperWidth = receiptPaperWidth,
                                    receiptHeader = receiptHeader,
                                    receiptFooter = receiptFooter,
                                    receiptShowQr = showQr,
                                    defaultReorderLevel = defaultReorderLevel.toIntOrNull() ?: 5,
                                    allowNegativeStock = allowNegativeStock,
                                    businessSetupComplete = true
                                )
                                settingsRepository.saveSettings(completeSettings)
                                settingsRepository.markBusinessSetupComplete()
                                onSetupFinished()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("setup_finish_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Finish Setup & Open Terminal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            LinearProgressIndicator(
                progress = { currentStep / 7f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (currentStep) {
                1 -> StepBusinessProfile(
                    name = businessName, onNameChange = { businessName = it },
                    address = address, onAddressChange = { address = it },
                    phone = phone, onPhoneChange = { phone = it },
                    taxId = taxId, onTaxIdChange = { taxId = it }
                )
                2 -> StepCurrencyAndTax(
                    currencyCode = currencyCode, onCurrencyCodeChange = { currencyCode = it },
                    currencySymbol = currencySymbol, onCurrencySymbolChange = { currencySymbol = it },
                    taxRate = defaultTaxRate, onTaxRateChange = { defaultTaxRate = it },
                    isInclusive = isTaxInclusive, onInclusiveChange = { isTaxInclusive = it }
                )
                3 -> StepReceiptSettings(
                    paperWidth = receiptPaperWidth, onPaperWidthChange = { receiptPaperWidth = it },
                    header = receiptHeader, onHeaderChange = { receiptHeader = it },
                    footer = receiptFooter, onFooterChange = { receiptFooter = it },
                    showQr = showQr, onShowQrChange = { showQr = it }
                )
                4 -> StepCategories(
                    categories = categories,
                    newCategoryName = newCategoryName,
                    onNewCategoryNameChange = { newCategoryName = it },
                    onAddCategory = {
                        if (newCategoryName.isNotBlank()) {
                            coroutineScope.launch {
                                categoryRepository.saveCategory(CategoryEntity(name = newCategoryName.trim()))
                                newCategoryName = ""
                            }
                        }
                    },
                    onDeleteCategory = { category ->
                        coroutineScope.launch { categoryRepository.deleteCategory(category) }
                    }
                )
                5 -> StepFirstProducts(productRepository, categories)
                6 -> StepLowStockDefaults(
                    reorderLevel = defaultReorderLevel,
                    onReorderLevelChange = { defaultReorderLevel = it },
                    allowNegative = allowNegativeStock,
                    onAllowNegativeChange = { allowNegativeStock = it }
                )
                7 -> StepReview(
                    businessName = businessName,
                    currencySymbol = currencySymbol,
                    taxRate = defaultTaxRate,
                    paperWidth = receiptPaperWidth,
                    categoryCount = categories.size
                )
            }
        }
    }
}

@Composable
private fun StepBusinessProfile(
    name: String, onNameChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    taxId: String, onTaxIdChange: (String) -> Unit
) {
    Text("1. Business Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text("Enter your retail store details for receipts and invoices.", color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text("Store / Business Name *") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = address,
        onValueChange = onAddressChange,
        label = { Text("Store Address") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = phone,
        onValueChange = onPhoneChange,
        label = { Text("Phone Number") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = taxId,
        onValueChange = onTaxIdChange,
        label = { Text("Tax Registration / VAT ID") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun StepCurrencyAndTax(
    currencyCode: String, onCurrencyCodeChange: (String) -> Unit,
    currencySymbol: String, onCurrencySymbolChange: (String) -> Unit,
    taxRate: String, onTaxRateChange: (String) -> Unit,
    isInclusive: Boolean, onInclusiveChange: (Boolean) -> Unit
) {
    Text("2. Currency & Tax Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text("Define the currency format and sales tax computation.", color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(16.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = currencySymbol,
            onValueChange = onCurrencySymbolChange,
            label = { Text("Symbol (e.g. $, €)") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = currencyCode,
            onValueChange = onCurrencyCodeChange,
            label = { Text("ISO Code (e.g. USD)") },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = taxRate,
        onValueChange = onTaxRateChange,
        label = { Text("Default Tax Rate (%)") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tax Application Mode", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isInclusive, onClick = { onInclusiveChange(true) })
                Text("Prices are Tax-Inclusive (Standard retail)")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = !isInclusive, onClick = { onInclusiveChange(false) })
                Text("Add tax on top at checkout (Tax-Exclusive)")
            }
        }
    }
}

@Composable
private fun StepReceiptSettings(
    paperWidth: Int, onPaperWidthChange: (Int) -> Unit,
    header: String, onHeaderChange: (String) -> Unit,
    footer: String, onFooterChange: (String) -> Unit,
    showQr: Boolean, onShowQrChange: (Boolean) -> Unit
) {
    Text("3. Receipt & Thermal Printing", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text("Customize the ESC/POS thermal printer layout.", color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(16.dp))
    Text("Paper Width", fontWeight = FontWeight.SemiBold)
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = paperWidth == 58, onClick = { onPaperWidthChange(58) })
            Text("58mm (32 chars)")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = paperWidth == 80, onClick = { onPaperWidthChange(80) })
            Text("80mm (48 chars)")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = header,
        onValueChange = onHeaderChange,
        label = { Text("Receipt Header Greeting") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))
    OutlinedTextField(
        value = footer,
        onValueChange = onFooterChange,
        label = { Text("Receipt Footer Policy") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = showQr, onCheckedChange = onShowQrChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Include QR code with Receipt Number for fast returns")
    }
}

@Composable
private fun StepCategories(
    categories: List<CategoryEntity>,
    newCategoryName: String,
    onNewCategoryNameChange: (String) -> Unit,
    onAddCategory: () -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    Text("4. Product Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text("Categories organize your items on the POS terminal.", color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(16.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newCategoryName,
            onValueChange = onNewCategoryNameChange,
            label = { Text("Add custom category") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Button(
            onClick = onAddCategory,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { cat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(cat.name, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = { onDeleteCategory(cat) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepFirstProducts(
    productRepository: ProductRepository,
    categories: List<CategoryEntity>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var pName by remember { mutableStateOf("") }
    var pSku by remember { mutableStateOf("") }
    var pPrice by remember { mutableStateOf("") }
    var pStock by remember { mutableStateOf("20") }
    var imagePath by remember { mutableStateOf<String?>(null) }
    var addSuccess by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val path = ProductImageHelper.copyUriToInternalStorage(context, it)
            if (path != null) imagePath = path
        }
    }

    Text("5. Quick Add Products (Optional)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
    Text("Add your first few items with photos now or skip and do it later.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(10.dp))

    // Product Photo selector
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val file = remember(imagePath) { imagePath?.let { File(it) } }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
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
                    contentDescription = "Product image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = PosPrimaryBlue, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Product Image", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
            Text(if (imagePath != null) "Photo attached" else "Tap to choose image", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        OutlinedButton(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.height(30.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
        ) {
            Text(if (imagePath != null) "Change" else "Pick", fontSize = 10.sp)
        }

        if (imagePath != null) {
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = { imagePath = null }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(15.dp))
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(value = pName, onValueChange = { pName = it }, label = { Text("Product Name *", fontSize = 11.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(6.dp))
    OutlinedTextField(value = pSku, onValueChange = { pSku = it }, label = { Text("SKU / Barcode *", fontSize = 11.sp) }, singleLine = true, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(value = pPrice, onValueChange = { pPrice = it }, label = { Text("Sale Price ($) *", fontSize = 11.sp) }, singleLine = true, modifier = Modifier.weight(1f))
        OutlinedTextField(value = pStock, onValueChange = { pStock = it }, label = { Text("Initial Stock", fontSize = 11.sp) }, singleLine = true, modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(10.dp))
    Button(
        onClick = {
            if (pName.isNotBlank() && pSku.isNotBlank()) {
                coroutineScope.launch {
                    val priceMinor = ((pPrice.toDoubleOrNull() ?: 1.0) * 100).toLong()
                    val catId = categories.firstOrNull()?.id ?: 1L
                    val product = ProductEntity(
                        name = pName.trim(),
                        sku = pSku.trim(),
                        categoryId = catId,
                        costPrice = (priceMinor * 0.6).toLong(),
                        salePrice = priceMinor,
                        stockQty = pStock.toIntOrNull() ?: 10,
                        imagePath = imagePath
                    )
                    productRepository.saveProduct(product, listOf(pSku.trim()))
                    pName = ""
                    pSku = ""
                    pPrice = ""
                    imagePath = null
                    addSuccess = true
                }
            }
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().height(38.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Save Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }

    if (addSuccess) {
        Spacer(modifier = Modifier.height(6.dp))
        Text("✓ Product added successfully!", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StepLowStockDefaults(
    reorderLevel: String, onReorderLevelChange: (String) -> Unit,
    allowNegative: Boolean, onAllowNegativeChange: (Boolean) -> Unit
) {
    Text("6. Stock & Inventory Rules", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text("Control how stock levels and alerts behave during sales.", color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = reorderLevel,
        onValueChange = onReorderLevelChange,
        label = { Text("Default Reorder Level Threshold") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(20.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Allow Negative Stock", fontWeight = FontWeight.Bold)
                Text(
                    "Allows sales to proceed even when tracked inventory count is zero or negative.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = allowNegative, onCheckedChange = onAllowNegativeChange)
        }
    }
}

@Composable
private fun StepReview(
    businessName: String,
    currencySymbol: String,
    taxRate: String,
    paperWidth: Int,
    categoryCount: Int
) {
    Text("7. Review & Complete", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text("Everything is set up. You can modify these settings anytime in Settings.", color = MaterialTheme.colorScheme.onSurfaceVariant)

    Spacer(modifier = Modifier.height(16.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Store Name: $businessName", fontWeight = FontWeight.Bold)
            Text("Currency: $currencySymbol")
            Text("Default Tax: $taxRate%")
            Text("Thermal Paper: ${paperWidth}mm")
            Text("Active Categories: $categoryCount")
        }
    }
}
