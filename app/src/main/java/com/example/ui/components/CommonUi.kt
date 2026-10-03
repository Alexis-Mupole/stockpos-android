package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SaleStatus
import com.example.data.local.entity.UserRole
import com.example.ui.theme.PosDangerLight
import com.example.ui.theme.PosDangerRed
import com.example.ui.theme.PosPrimaryBlue
import com.example.ui.theme.PosSuccessGreen
import com.example.ui.theme.PosWarningAmber
import java.util.Locale

fun formatMinorMoney(amountMinor: Long, symbol: String = "$"): String {
    val major = amountMinor / 100.0
    return String.format(Locale.US, "%s%.2f", symbol, major)
}

@Composable
fun RoleBadge(role: UserRole, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (role) {
        UserRole.ADMIN -> Color(0xFF7C3AED).copy(alpha = 0.15f) to Color(0xFF7C3AED)
        UserRole.MANAGER -> PosPrimaryBlue.copy(alpha = 0.15f) to PosPrimaryBlue
        UserRole.SELLER -> PosSuccessGreen.copy(alpha = 0.15f) to PosSuccessGreen
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = role.name,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StockBadge(stockQty: Int, reorderLevel: Int, modifier: Modifier = Modifier) {
    val (label, bgColor, textColor) = when {
        stockQty <= 0 -> Triple("OUT OF STOCK", PosDangerRed.copy(alpha = 0.15f), PosDangerRed)
        stockQty <= reorderLevel -> Triple("LOW: $stockQty", PosWarningAmber.copy(alpha = 0.15f), PosWarningAmber)
        else -> Triple("IN STOCK: $stockQty", PosSuccessGreen.copy(alpha = 0.15f), PosSuccessGreen)
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SaleStatusBadge(status: SaleStatus, modifier: Modifier = Modifier) {
    val (label, bgColor, textColor) = when (status) {
        SaleStatus.COMPLETED -> Triple("PAID", PosSuccessGreen.copy(alpha = 0.15f), PosSuccessGreen)
        SaleStatus.REFUNDED -> Triple("REFUNDED", PosDangerRed.copy(alpha = 0.15f), PosDangerRed)
        SaleStatus.PARTIALLY_REFUNDED -> Triple("PARTIAL REFUND", PosWarningAmber.copy(alpha = 0.15f), PosWarningAmber)
        SaleStatus.VOIDED -> Triple("VOIDED", Color.Gray.copy(alpha = 0.2f), Color.DarkGray)
        SaleStatus.HELD -> Triple("HELD", PosPrimaryBlue.copy(alpha = 0.15f), PosPrimaryBlue)
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
