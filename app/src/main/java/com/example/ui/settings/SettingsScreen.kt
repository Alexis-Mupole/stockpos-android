package com.example.ui.settings

import android.bluetooth.BluetoothDevice
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRole
import com.example.data.repository.BusinessSettings
import com.example.data.repository.SettingsRepository
import com.example.data.session.SessionManager
import com.example.domain.printer.BluetoothPrinterManager
import com.example.ui.components.RoleBadge
import com.example.ui.theme.PosDangerRed
import com.example.ui.theme.PosPrimaryBlue
import com.example.util.AppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentUser: UserEntity,
    settingsRepository: SettingsRepository,
    sessionManager: SessionManager,
    bluetoothPrinterManager: BluetoothPrinterManager,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToBusinessSetup: () -> Unit,
    onNavigateToAbout: () -> Unit = {},
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val settings by settingsRepository.getSettingsFlow().collectAsState(initial = BusinessSettings())
    val S = remember(settings.language) { AppStrings(settings.language) }
    var showPrinterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(S.settingsTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // User Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(currentUser.name, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text("ID: ${currentUser.identifier}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        RoleBadge(role = currentUser.role)
                    }
                }
            }

            // Language Selector Card (English / Français)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = PosPrimaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(S.languageSection, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                Text(S.languageSubtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val isEn = !settings.language.equals("FR", ignoreCase = true)
                            val isFr = settings.language.equals("FR", ignoreCase = true)

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        settingsRepository.saveSettings(settings.copy(language = "EN"))
                                    }
                                },
                                modifier = Modifier.weight(1f).height(34.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = if (isEn) ButtonDefaults.outlinedButtonColors(containerColor = PosPrimaryBlue.copy(alpha = 0.12f)) else ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text("🇬🇧 English", fontSize = 11.sp, fontWeight = if (isEn) FontWeight.Bold else FontWeight.Normal)
                            }

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        settingsRepository.saveSettings(settings.copy(language = "FR"))
                                    }
                                },
                                modifier = Modifier.weight(1f).height(34.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = if (isFr) ButtonDefaults.outlinedButtonColors(containerColor = PosPrimaryBlue.copy(alpha = 0.12f)) else ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text("🇫🇷 Français", fontSize = 11.sp, fontWeight = if (isFr) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }

            // Admin only: User Management
            if (currentUser.role == UserRole.ADMIN) {
                item {
                    SettingsActionItem(
                        icon = Icons.Default.Group,
                        title = S.userManagement,
                        subtitle = S.userManagementSub,
                        onClick = onNavigateToUserManagement
                    )
                }
            }

            // Admin only: Business Profile Setup
            if (currentUser.role == UserRole.ADMIN) {
                item {
                    SettingsActionItem(
                        icon = Icons.Default.Business,
                        title = S.businessSetup,
                        subtitle = S.businessSetupSub,
                        onClick = onNavigateToBusinessSetup
                    )
                }
            }

            // Hardware: ESC/POS Thermal Printer
            item {
                SettingsActionItem(
                    icon = Icons.Default.Print,
                    title = S.thermalPrinter,
                    subtitle = if (settings.pairedPrinterName.isNotBlank()) "Paired: ${settings.pairedPrinterName}" else S.printerNotPaired,
                    onClick = { showPrinterSheet = true }
                )
            }

            // Sound & Haptic Toggles
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = PosPrimaryBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(S.audioBeep, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                            Switch(
                                checked = settings.soundBeepEnabled,
                                onCheckedChange = { checked ->
                                    coroutineScope.launch {
                                        settingsRepository.saveSettings(settings.copy(soundBeepEnabled = checked))
                                    }
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Vibration, contentDescription = null, tint = PosPrimaryBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(S.hapticFeedback, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                            Switch(
                                checked = settings.hapticVibrateEnabled,
                                onCheckedChange = { checked ->
                                    coroutineScope.launch {
                                        settingsRepository.saveSettings(settings.copy(hapticVibrateEnabled = checked))
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // About & Contributors (Alexis Mupole & Open Source)
            item {
                SettingsActionItem(
                    icon = Icons.Default.Info,
                    title = S.aboutApp,
                    subtitle = "${S.initiatorName} • ${S.aboutAppSub}",
                    onClick = onNavigateToAbout
                )
            }

            // Logout Action
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            sessionManager.logout()
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PosDangerRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = PosDangerRed, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(S.logout, color = PosDangerRed, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }
            }
        }
    }

    // Printer Selection BottomSheet
    if (showPrinterSheet) {
        val pairedDevices = remember { bluetoothPrinterManager.getPairedPrinters() }
        ModalBottomSheet(onDismissRequest = { showPrinterSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(S.thermalPrinter, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "Standard 58mm or 80mm ESC/POS printers paired in Android Bluetooth settings.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (pairedDevices.isEmpty()) {
                    Text(
                        if (S.isFr) "Aucun appareil Bluetooth associé. Veuillez associer votre imprimante dans les paramètres Bluetooth d'Android." else "No paired Bluetooth devices found. Please pair your thermal printer in Android Settings > Bluetooth first.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.5.sp
                    )
                } else {
                    pairedDevices.forEach { device ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        settingsRepository.saveSettings(
                                            settings.copy(
                                                pairedPrinterMac = device.address,
                                                pairedPrinterName = device.name ?: device.address
                                            )
                                        )
                                        showPrinterSheet = false
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(device.name ?: "Unknown Printer", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text(device.address, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (settings.pairedPrinterMac == device.address) {
                                    Text(if (S.isFr) "Sélectionné" else "Selected", color = PosPrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { showPrinterSheet = false },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(S.close, fontSize = 11.5.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(PosPrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PosPrimaryBlue, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
