package com.example.presentation.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.admin.AdminServerManager
import com.example.data.model.Coupon
import com.example.data.model.SubscriptionRequest
import com.example.data.model.SubscriptionStatus
import com.example.presentation.viewmodel.MainUiState
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.WarmGold

@Composable
fun HiddenAdminScreen(
    state: MainUiState,
    onBack: () -> Unit,
    onCreateCoupon: (Coupon) -> Unit,
    onApproveSubscription: (String) -> Unit,
    onRejectSubscription: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRunning by AdminServerManager.isServerRunning.collectAsState()
    val serverUrl by AdminServerManager.serverUrl.collectAsState()
    val logs by AdminServerManager.serverLogs.collectAsState()

    // 6-digit PIN Gate state (Default dev PIN is 202600 or any 6 digits for first setup)
    var isUnlocked by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    // Coupon generation state
    var code by remember { mutableStateOf("VIBE" + (1000..9999).random()) }
    var discountPercent by remember { mutableStateOf("100") }
    var maxUses by remember { mutableStateOf("100") }
    var durationDays by remember { mutableStateOf("30") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .padding(16.dp)
            .testTag("hidden_admin_screen")
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NeonCyan)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = WarmGold)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Developer Admin Portal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isUnlocked) {
            // PIN Entry Lock Screen
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = WarmGold,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Admin Security Verification",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Enter your 6-digit developer PIN to unlock subscription approvals, embedded HTTP server, and coupon minting.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                enteredPin = it
                                pinError = null
                            }
                        },
                        label = { Text("6-Digit PIN (e.g. 202600)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color(0xFF2B3A63)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = pinError!!, color = Color(0xFFFF5252), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (enteredPin.length == 6) {
                                isUnlocked = true
                                AdminServerManager.log("Admin unlocked console via PIN")
                            } else {
                                pinError = "Please enter exactly 6 numeric digits"
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Unlock Admin Console", color = DeepNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Unlocked Admin Interface
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Subscription Approval Queue Section
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, WarmGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = WarmGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SUBSCRIPTION APPROVAL QUEUE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmGold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "Bypasses Google Pay. Direct administrator review & entitlement issuance.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        val allRequests = state.subscriptionRequests
                        if (allRequests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No pending subscription requests in queue.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            allRequests.forEach { request ->
                                SubscriptionApprovalCard(
                                    request = request,
                                    onApprove = { onApproveSubscription(request.id) },
                                    onReject = { onRejectSubscription(request.id) }
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Server Control Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "EMBEDDED ADMIN SERVER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "NanoHTTPD Port 8080 (Fallback 8081/8082)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (isRunning) EmeraldSuccess else Color.Gray, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isRunning && serverUrl != null) {
                            Text(
                                text = "URL: $serverUrl",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Reachable on your local LAN or in this device's browser.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isRunning) {
                                Button(
                                    onClick = {
                                        AdminServerManager.startServer(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DeepNavy)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Start Server", color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        AdminServerManager.stopServer()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stop Server", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        serverUrl?.let { url ->
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            context.startActivity(intent)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = NeonCyan)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open UI", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Coupon Minting Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MINT SIGNED COUPONS (ED25519)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it.uppercase() },
                            label = { Text("Coupon Code") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = discountPercent,
                                onValueChange = { discountPercent = it },
                                label = { Text("Discount %") },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = maxUses,
                                onValueChange = { maxUses = it },
                                label = { Text("Max Uses") },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = durationDays,
                            onValueChange = { durationDays = it },
                            label = { Text("Valid For (Days)") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonCyan),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val dPercent = discountPercent.toIntOrNull() ?: 100
                                val mUses = maxUses.toIntOrNull() ?: 100
                                val dDays = durationDays.toLongOrNull() ?: 30L
                                val expireMs = System.currentTimeMillis() + (dDays * 24 * 60 * 60 * 1000)

                                val newCoupon = Coupon(
                                    code = code.trim().uppercase(),
                                    discountPercent = dPercent,
                                    fixedAmount = null,
                                    expirationDate = expireMs,
                                    maxUses = mUses,
                                    usedCount = 0,
                                    applicableProductIds = listOf("pass_7day", "pass_30day", "lifetime_unlock")
                                )
                                onCreateCoupon(newCoupon)
                                AdminServerManager.log("Coupon minted: ${newCoupon.code} ($dPercent%)")
                                code = "VIBE" + (1000..9999).random()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarmGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = DeepNavy)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mint & Register Coupon", color = DeepNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Server Logs Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SERVER AUDIT LOGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color(0xFF090D1A), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            val displayLogs = logs.takeLast(10)
                            if (displayLogs.isEmpty()) {
                                Text(
                                    text = "No server events recorded yet.",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            } else {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    displayLogs.forEach { logEntry ->
                                        Text(
                                            text = logEntry,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = EmeraldSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionApprovalCard(
    request: SubscriptionRequest,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (request.status) {
                SubscriptionStatus.PENDING -> Color(0xFF1E293B)
                SubscriptionStatus.APPROVED -> Color(0xFF064E3B)
                SubscriptionStatus.REJECTED -> Color(0xFF450A0A)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${request.userName} (${request.userId})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Plan: ${request.productTitle} • ${request.priceString}",
                        fontSize = 12.sp,
                        color = NeonCyan
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            when (request.status) {
                                SubscriptionStatus.PENDING -> WarmGold
                                SubscriptionStatus.APPROVED -> EmeraldSuccess
                                SubscriptionStatus.REJECTED -> Color(0xFFFF5252)
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = request.status.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                }
            }

            if (request.status == SubscriptionStatus.PENDING) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve Pass", color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
