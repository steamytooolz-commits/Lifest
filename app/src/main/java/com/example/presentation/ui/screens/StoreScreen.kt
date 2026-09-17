package com.example.presentation.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubscriptionStatus
import com.example.data.repository.BillingRepository
import com.example.data.repository.StorePassItem
import com.example.presentation.viewmodel.MainUiState
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.WarmGold

@Composable
fun StoreScreen(
    state: MainUiState,
    passes: List<StorePassItem>,
    onBack: () -> Unit,
    onPurchasePass: (String) -> Unit,
    onOpenRedeemCoupon: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPaid = state.userPreferences?.isPaidUser ?: false
    val pendingRequests = state.subscriptionRequests.filter { it.status == SubscriptionStatus.PENDING }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("store_screen")
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
                text = "Simulation Membership Passes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Admin Approval Notice Banner (No Google Pay reliance)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF162544)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Admin Verified Subscriptions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Text(
                        text = "Direct admin verification system active. Requests are queued and reviewed directly by the Administrator without third-party paywall risks.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active Status Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = if (isPaid) Color(0xFF1F2D54) else CardDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isPaid) "ACTIVE PASS STATUS" else "CURRENT PLAN: FREE TIER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) WarmGold else NeonCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPaid) "Unlimited Lives • Full Multiverse Access"
                        else "3 Lives / Day • 25 Nations • 1 Save Slot",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onOpenRedeemCoupon,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = WarmGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Redeem Code", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        // Pending Request notice if user has requested
        if (pendingRequests.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2415)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarmGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HourglassTop, contentDescription = null, tint = WarmGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Pending Admin Approval (${pendingRequests.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmGold
                        )
                        Text(
                            text = "Your request for ${pendingRequests.first().productTitle} is in the queue for Admin sign-off.",
                            fontSize = 11.sp,
                            color = Color(0xFFDCD6CA)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "CHOOSE YOUR SIMULATION PASS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
        )

        Spacer(modifier = Modifier.height(10.dp))

        passes.forEach { pass ->
            val isPending = pendingRequests.any { it.productId == pass.productId }
            PassCard(
                item = pass,
                isPending = isPending,
                onBuy = { onPurchasePass(pass.productId) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Free vs Paid Tier Comparison Table
        Text(
            text = "TIER COMPARISON",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                ComparisonRow("Daily Lives", "3 / day", "Unlimited (⚡)")
                ComparisonRow("Countries Available", "25 starter", "All 195 Nations")
                ComparisonRow("AI Models", "Fast 4o-mini", "Claude Sonnet 4.6 & Gemini 2.5 Pro")
                ComparisonRow("Professions & Businesses", "10 careers", "50+ & Enterprise Ownership")
                ComparisonRow("Memory Depth", "Last 20 / NPC", "Infinite Room Depth")
                ComparisonRow("Save Slots", "1 Slot", "10 Slots")
                ComparisonRow("PDF Memoir Export", "Locked", "Unlocked")
                ComparisonRow("Image Scene Generation", "Locked", "Unlocked")
            }
        }
    }
}

@Composable
private fun PassCard(
    item: StorePassItem,
    isPending: Boolean = false,
    onBuy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isPending) WarmGold else if (item.isPopular || item.isLifetime) WarmGold else Color(0xFF263353),
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (item.isPopular) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(WarmGold, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("POPULAR", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepNavy)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onBuy,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPending) Color(0xFF795548) else if (item.isLifetime) WarmGold else CyberViolet
                )
            ) {
                Text(
                    text = if (isPending) "Pending..." else "Request ${item.priceString}",
                    color = if (isPending) Color.White else if (item.isLifetime) DeepNavy else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ComparisonRow(feature: String, freeVal: String, paidVal: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(feature, fontSize = 11.sp, color = Color.White, modifier = Modifier.weight(1.2f))
        Text(freeVal, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(0.9f))
        Text(paidVal, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess, modifier = Modifier.weight(1.1f))
    }
}
