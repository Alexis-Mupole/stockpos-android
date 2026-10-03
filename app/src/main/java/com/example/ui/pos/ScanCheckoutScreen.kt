package com.example.ui.pos

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.camera.BarcodeAnalyzer
import com.example.data.local.entity.PaymentMethod
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.UserEntity
import com.example.data.repository.BusinessSettings
import com.example.data.repository.ProductRepository
import com.example.data.repository.SettingsRepository
import com.example.domain.model.CartItem
import com.example.domain.model.CartState
import com.example.domain.model.ParkedCart
import com.example.domain.model.PaymentEntry
import com.example.domain.printer.PdfReceiptGenerator
import com.example.domain.usecase.AddToCartUseCase
import com.example.domain.usecase.CheckoutUseCase
import com.example.domain.usecase.ScanProductUseCase
import com.example.ui.components.formatMinorMoney
import com.example.ui.theme.PosDangerRed
import com.example.ui.theme.PosPrimaryBlue
import com.example.ui.theme.PosSuccessGreen
import com.example.util.AppStrings
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Inventory2
import coil.compose.AsyncImage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCheckoutScreen(
    currentUser: UserEntity,
    productRepository: ProductRepository,
    settingsRepository: SettingsRepository,
    scanProductUseCase: ScanProductUseCase,
    addToCartUseCase: AddToCartUseCase,
    checkoutUseCase: CheckoutUseCase,
    onNavigateToAddProductWithBarcode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val settings by settingsRepository.getSettingsFlow().collectAsState(initial = BusinessSettings())
    val S = remember(settings.language) { AppStrings(settings.language) }

    var cartState by remember { mutableStateOf(CartState()) }
    val parkedCarts = remember { mutableStateListOf<ParkedCart>() }

    var isCameraActive by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) isCameraActive = true
    }

    // Safe ToneGenerator to prevent crashes on emulators/unsupported audio devices
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
        } catch (_: Throwable) {
            null
        }
    }

    DisposableEffect(toneGenerator) {
        onDispose {
            try {
                toneGenerator?.release()
            } catch (_: Throwable) {}
        }
    }

    val vibrator = remember {
        try {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } catch (_: Throwable) {
            null
        }
    }

    fun triggerFeedback() {
        try {
            if (settings.soundBeepEnabled) {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            }
            if (settings.hapticVibrateEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(40)
                }
            }
        } catch (_: Throwable) {}
    }

    // Unknown Barcode modal state
    var unknownBarcode by remember { mutableStateOf<String?>(null) }

    // Manual search query state
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by productRepository.searchProducts(searchQuery).collectAsState(initial = emptyList())

    // Payment modal state
    var showPaymentSheet by remember { mutableStateOf(false) }
    var lastReceiptPair by remember { mutableStateOf<Pair<Long, String>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = S.posTerminal,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentUser.role.name} • ${currentUser.name}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Park / Recall Carts
                    if (cartState.items.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                parkedCarts.add(
                                    ParkedCart(
                                        id = UUID.randomUUID().toString(),
                                        cartState = cartState,
                                        label = "Cart #${parkedCarts.size + 1}"
                                    )
                                )
                                cartState = CartState()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Hold Sale", modifier = Modifier.size(18.dp))
                        }
                    }

                    if (parkedCarts.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val last = parkedCarts.removeAt(parkedCarts.lastIndex)
                                cartState = last.cartState
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Recall Sale", tint = PosPrimaryBlue, modifier = Modifier.size(18.dp))
                        }
                    }

                    IconButton(
                        onClick = {
                            if (!hasCameraPermission) {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            } else {
                                isCameraActive = !isCameraActive
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isCameraActive) Icons.Default.CameraAlt else Icons.Default.Keyboard,
                            contentDescription = "Toggle Input Mode",
                            tint = if (isCameraActive) PosPrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${S.total} (${cartState.totalItemsCount} ${S.pcsAbbrev})",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatMinorMoney(cartState.grandTotal, settings.currencySymbol),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = { showPaymentSheet = true },
                        enabled = !cartState.isEmpty,
                        colors = ButtonDefaults.buttonColors(containerColor = PosSuccessGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("charge_button")
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${S.charge} ${formatMinorMoney(cartState.grandTotal, settings.currencySymbol)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
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
        ) {
            // Optional Camera Viewfinder (if turned on by user and permission granted)
            if (isCameraActive && hasCameraPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.32f)
                        .background(Color.Black)
                ) {
                    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }

                            try {
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    try {
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }

                                        val analyzer = ImageAnalysis.Builder()
                                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                            .build()
                                            .also { imageAnalysis ->
                                                imageAnalysis.setAnalyzer(
                                                    cameraExecutor,
                                                    BarcodeAnalyzer(debouncePeriodMs = 1200L) { scannedCode ->
                                                        coroutineScope.launch(Dispatchers.Main) {
                                                            triggerFeedback()
                                                            val found = scanProductUseCase(scannedCode)
                                                            if (found != null) {
                                                                cartState = addToCartUseCase(
                                                                    currentState = cartState,
                                                                    product = found.product,
                                                                    barcodeUsed = scannedCode,
                                                                    allowNegativeStock = settings.allowNegativeStock
                                                                )
                                                            } else {
                                                                unknownBarcode = scannedCode
                                                            }
                                                        }
                                                    }
                                                )
                                            }

                                        cameraProvider.unbindAll()
                                        val hasBack = cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                                        val hasFront = cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                                        if (!hasBack && !hasFront) {
                                            isCameraActive = false
                                            return@addListener
                                        }
                                        val selector = if (hasBack) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA
                                        val camera = cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            selector,
                                            preview,
                                            analyzer
                                        )
                                        cameraControl = camera.cameraControl
                                    } catch (_: Throwable) {
                                        // Camera unavailable on device/emulator
                                        isCameraActive = false
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                            } catch (_: Throwable) {
                                isCameraActive = false
                            }

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Target scanning reticle
                    Box(
                        modifier = Modifier
                            .size(200.dp, 100.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    // Torch toggle button
                    IconButton(
                        onClick = {
                            isTorchOn = !isTorchOn
                            cameraControl?.enableTorch(isTorchOn)
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Fast Search & Barcode Keyboard Entry (Responsive & always accessible)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(S.searchPlaceholder, fontSize = 11.5.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(26.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(15.dp))
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        if (searchQuery.isNotBlank()) {
                            coroutineScope.launch {
                                val found = scanProductUseCase(searchQuery)
                                if (found != null) {
                                    triggerFeedback()
                                    cartState = addToCartUseCase(
                                        currentState = cartState,
                                        product = found.product,
                                        barcodeUsed = searchQuery,
                                        allowNegativeStock = settings.allowNegativeStock
                                    )
                                    searchQuery = ""
                                } else {
                                    unknownBarcode = searchQuery
                                }
                            }
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            )

            // Live Search Suggestions Dropdown with Product Images
            if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column {
                        searchResults.take(3).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        cartState = addToCartUseCase(
                                            currentState = cartState,
                                            product = item.product,
                                            barcodeUsed = item.product.sku,
                                            allowNegativeStock = settings.allowNegativeStock
                                        )
                                        searchQuery = ""
                                        focusManager.clearFocus()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val sImgFile = remember(item.product.imagePath) { item.product.imagePath?.let { File(it) } }
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (sImgFile != null && sImgFile.exists()) {
                                            AsyncImage(
                                                model = sImgFile,
                                                contentDescription = item.product.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(item.product.name, fontWeight = FontWeight.SemiBold, fontSize = 11.5.sp, maxLines = 1)
                                        Text("${S.prodStock}: ${item.product.stockQty}", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(
                                    formatMinorMoney(item.product.salePrice, settings.currencySymbol),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = PosPrimaryBlue
                                )
                            }
                        }
                    }
                }
            }

            // Cart Items Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${S.itemsInCart} (${cartState.items.size})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )

                if (cartState.items.isNotEmpty()) {
                    Text(
                        text = S.clearCart,
                        color = PosDangerRed,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { cartState = CartState() }
                    )
                }
            }

            // Live Cart List
            if (cartState.isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = S.emptyCart,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(cartState.items, key = { it.product.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Product Image in Cart
                                val cImgFile = remember(item.product.imagePath) { item.product.imagePath?.let { File(it) } }
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cImgFile != null && cImgFile.exists()) {
                                        AsyncImage(
                                            model = cImgFile,
                                            contentDescription = item.product.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
                                    Text(
                                        text = "${formatMinorMoney(item.unitPrice, settings.currencySymbol)} ${S.eachAbbrev}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Quantity Stepper
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 2.dp, vertical = 1.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            val currentItems = cartState.items.toMutableList()
                                            val idx = currentItems.indexOfFirst { it.product.id == item.product.id }
                                            if (idx >= 0) {
                                                if (item.quantity > 1) {
                                                    currentItems[idx] = item.copy(quantity = item.quantity - 1)
                                                } else {
                                                    currentItems.removeAt(idx)
                                                }
                                                cartState = cartState.copy(items = currentItems)
                                            }
                                        },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }

                                    Text(
                                        text = "${item.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )

                                    IconButton(
                                        onClick = {
                                            if (settings.allowNegativeStock || item.quantity < item.product.stockQty) {
                                                val currentItems = cartState.items.toMutableList()
                                                val idx = currentItems.indexOfFirst { it.product.id == item.product.id }
                                                if (idx >= 0) {
                                                    currentItems[idx] = item.copy(quantity = item.quantity + 1)
                                                    cartState = cartState.copy(items = currentItems)
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(13.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = formatMinorMoney(item.subtotal, settings.currencySymbol),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Unknown Barcode Bottom Sheet
    unknownBarcode?.let { barcode ->
        ModalBottomSheet(onDismissRequest = { unknownBarcode = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Item Not Found", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(barcode, fontSize = 13.sp, color = PosPrimaryBlue, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(14.dp))

                if (currentUser.role != com.example.data.local.entity.UserRole.SELLER) {
                    Button(
                        onClick = {
                            val code = barcode
                            unknownBarcode = null
                            onNavigateToAddProductWithBarcode(code)
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add to Inventory", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedButton(
                    onClick = { unknownBarcode = null },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Dismiss", fontSize = 12.sp)
                }
            }
        }
    }

    // Payment Sheet Modal with Device Keyboard Support
    if (showPaymentSheet) {
        PaymentBottomSheet(
            cartState = cartState,
            settings = settings,
            currentUser = currentUser,
            checkoutUseCase = checkoutUseCase,
            onDismiss = { showPaymentSheet = false },
            onCheckoutSuccess = { saleId, receiptNo ->
                lastReceiptPair = Pair(saleId, receiptNo)
                cartState = CartState()
                showPaymentSheet = false
            }
        )
    }

    // Post-Checkout Success & Receipt Action Modal
    lastReceiptPair?.let { (_, receiptNo) ->
        ModalBottomSheet(onDismissRequest = { lastReceiptPair = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PosSuccessGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, tint = PosSuccessGreen, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(S.saleCompleted, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                Text("${S.receipt}: $receiptNo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val sale = (context.applicationContext as? com.example.StockPosApplication)
                                ?.container?.saleRepository?.getSaleByReceiptNo(receiptNo)
                            if (sale != null) {
                                PdfReceiptGenerator.shareReceiptPdf(context, sale, settings)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("${S.printReceipt} / ${S.sharePdf}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { lastReceiptPair = null },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(S.nextSale, fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
    cartState: CartState,
    settings: BusinessSettings,
    currentUser: UserEntity,
    checkoutUseCase: CheckoutUseCase,
    onDismiss: () -> Unit,
    onCheckoutSuccess: (Long, String) -> Unit
) {
    val S = remember(settings.language) { AppStrings(settings.language) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var tenderedAmountText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val grandTotal = cartState.grandTotal
    val grandTotalMajor = grandTotal / 100.0

    val quickCashOptions = listOf(
        grandTotalMajor,
        (Math.ceil(grandTotalMajor / 5.0) * 5.0),
        (Math.ceil(grandTotalMajor / 10.0) * 10.0),
        (Math.ceil(grandTotalMajor / 20.0) * 20.0),
        (Math.ceil(grandTotalMajor / 50.0) * 50.0)
    ).distinct().filter { it >= grandTotalMajor }

    val tenderedMinor = if (tenderedAmountText.isBlank()) {
        grandTotal
    } else {
        ((tenderedAmountText.toDoubleOrNull() ?: 0.0) * 100).toLong()
    }
    val changeGivenMinor = (tenderedMinor - grandTotal).coerceAtLeast(0L)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${S.totalDue}: ${formatMinorMoney(grandTotal, settings.currencySymbol)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Payment Method Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PaymentMethod.values().forEach { method ->
                    val isSelected = selectedMethod == method
                    val methodLabel = when (method) {
                        PaymentMethod.CASH -> S.cash
                        PaymentMethod.CARD -> S.card
                        PaymentMethod.MOBILE_MONEY -> S.mobileMoney
                        PaymentMethod.CREDIT -> if (S.isFr) "CRÉDIT" else "CREDIT"
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMethod = method },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = methodLabel,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedMethod == PaymentMethod.CASH) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickCashOptions.take(4).forEach { amt ->
                        Button(
                            onClick = { tenderedAmountText = String.format(Locale.US, "%.2f", amt) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${settings.currencySymbol}${amt.toInt()}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tenderedAmountText,
                    onValueChange = { tenderedAmountText = it },
                    label = { Text("${S.tenderedAmount} (${settings.currencySymbol})", fontSize = 11.sp) },
                    placeholder = { Text(String.format(Locale.US, "%.2f", grandTotalMajor), fontSize = 11.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PosSuccessGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${S.change}:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            text = formatMinorMoney(changeGivenMinor, settings.currencySymbol),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = PosSuccessGreen
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    coroutineScope.launch {
                        isProcessing = true
                        errorMessage = null
                        val payment = PaymentEntry(
                            method = selectedMethod,
                            amount = if (selectedMethod == PaymentMethod.CASH) tenderedMinor else grandTotal
                        )
                        val result = checkoutUseCase(
                            actingUser = currentUser,
                            cart = cartState,
                            payments = listOf(payment)
                        )
                        isProcessing = false
                        result.fold(
                            onSuccess = { (saleId, receiptNo) ->
                                onCheckoutSuccess(saleId, receiptNo)
                            },
                            onFailure = {
                                errorMessage = it.message ?: "Checkout failed"
                            }
                        )
                    }
                },
                enabled = !isProcessing && (selectedMethod != PaymentMethod.CASH || tenderedMinor >= grandTotal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("confirm_payment_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PosSuccessGreen)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("${S.charge} ${formatMinorMoney(grandTotal, settings.currencySymbol)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
