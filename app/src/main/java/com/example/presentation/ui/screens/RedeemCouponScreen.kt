package com.example.presentation.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.viewmodel.MainUiState
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.WarmGold

@Composable
fun RedeemCouponScreen(
    state: MainUiState,
    onBack: () -> Unit,
    onRedeem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var couponCode by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .padding(16.dp)
            .testTag("redeem_coupon_screen")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NeonCyan)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Redeem Promotion Coupon",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(WarmGold.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = WarmGold, modifier = Modifier.size(30.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Unlock Simulation Passes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Enter a valid promo code or gift pass code to grant free access or exclusive discounts.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = couponCode,
                    onValueChange = { couponCode = it.uppercase() },
                    label = { Text("Coupon Code (e.g. FREEPASS2026)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF2E3D66)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("coupon_input_field")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val clean = couponCode.trim()
                        if (clean.isNotBlank()) {
                            onRedeem(clean)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_coupon_btn")
                ) {
                    Text("Apply & Unlock Pass", color = DeepNavy, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "AVAILABLE COMMUNITY PROMO CODES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
        )

        Spacer(modifier = Modifier.height(10.dp))

        listOf(
            Pair("FREEPASS2026", "100% discount on 7-Day & 30-Day Citizen Passes"),
            Pair("LIFESIM50", "50% discount on Universe Pass"),
            Pair("VIPDEV", "Developer Full Lifetime Unlock Pass")
        ).forEach { (code, desc) ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { couponCode = code }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(code, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WarmGold)
                        Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Tap to paste", fontSize = 10.sp, color = NeonCyan)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
