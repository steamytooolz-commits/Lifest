package com.example.presentation.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NPC
import com.example.presentation.viewmodel.MainUiState
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DangerRose
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.WarmGold

@Composable
fun GameScreen(
    state: MainUiState,
    onBackToMenu: () -> Unit,
    onAdvanceDay: () -> Unit,
    onPerformAction: (String) -> Unit,
    onPlayCasino: (Double) -> Unit,
    onTalkToNpc: (NPC) -> Unit,
    onExportPdf: (Context) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val life = state.activeLife ?: return
    val world = state.worldState

    var selectedActionCategory by remember { mutableStateOf("Essentials") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .testTag("game_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackToMenu) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Menu", tint = NeonCyan)
                }
                Column {
                    Text(
                        text = life.playerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Age: ${life.age} • ${life.country} • ${world.inGameSeason} (${world.weather})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Cash Counter
                Box(
                    modifier = Modifier
                        .background(EmeraldSuccess.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$${String.format("%,.0f", life.money)}",
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // PDF Export Icon
                IconButton(
                    onClick = { onExportPdf(context) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Memoir", tint = WarmGold)
                }
            }
        }

        // Stats Gauge Grid
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NeedGaugeItem("Health", life.stats.health, EmeraldSuccess, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    NeedGaugeItem("Hunger", life.stats.hunger, WarmGold, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    NeedGaugeItem("Hygiene", life.stats.hygiene, NeonCyan, Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NeedGaugeItem("Social", life.stats.social, CyberViolet, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    NeedGaugeItem("Fun", life.stats.funLevel, Color(0xFFFF4081), Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    NeedGaugeItem("Karma", life.stats.karma, Color(0xFF64FFDA), Modifier.weight(1f))
                }
            }
        }

        // Lazy Column Content (Active Quests, Citizens, Actions, Chronicles)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Active Procedural Quest Banner
                if (state.activeQuests.isNotEmpty()) {
                    val quest = state.activeQuests.first()
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2844)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(WarmGold.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = WarmGold, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("ACTIVE QUEST: ${quest.title}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WarmGold)
                                Text(quest.description, fontSize = 12.sp, color = Color.White)
                                Text("Objective: ${quest.targetGoal} • Reward: $${quest.rewardMoney.toInt()}", fontSize = 10.sp, color = NeonCyan)
                            }
                        }
                    }
                }
            }

            // Nearby Citizens / Social AI
            item {
                Text(
                    text = "LOCAL CITIZENS (${state.npcs.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.npcs) { npc ->
                        NpcCardItem(npc = npc, onClick = { onTalkToNpc(npc) })
                    }
                }
            }

            // Action Center Tabs
            item {
                Text(
                    text = "ACTION CENTER",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Essentials", "Growth", "Recreation", "Civic").forEach { cat ->
                        val isSelected = selectedActionCategory == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSelected) CyberViolet else SurfaceDark, RoundedCornerShape(8.dp))
                                .clickable { selectedActionCategory = cat }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions based on selected category
                when (selectedActionCategory) {
                    "Essentials" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionButton("Eat Balanced Meal", Icons.Default.Fastfood, Modifier.weight(1f)) { onPerformAction("Eat Balanced Meal") }
                            ActionButton("Sleep & Recharge", Icons.Default.Hotel, Modifier.weight(1f)) { onPerformAction("Sleep & Recharge") }
                            ActionButton("Meditate", Icons.Default.SelfImprovement, Modifier.weight(1f)) { onPerformAction("Meditate") }
                        }
                    }
                    "Growth" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionButton("Gym & Workout", Icons.Default.FitnessCenter, Modifier.weight(1f)) { onPerformAction("Gym & Workout") }
                            ActionButton("Study & Read", Icons.Default.MenuBook, Modifier.weight(1f)) { onPerformAction("Study & Read") }
                            ActionButton("Apply for Job", Icons.Default.Work, Modifier.weight(1f)) { onPerformAction("Apply for Job") }
                        }
                    }
                    "Recreation" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionButton("Video Games", Icons.Default.SportsEsports, Modifier.weight(1f)) { onPerformAction("Play Video Games") }
                            ActionButton("Blackjack ($50)", Icons.Default.Casino, Modifier.weight(1f)) { onPlayCasino(50.0) }
                        }
                    }
                    "Civic" -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionButton("Charity ($100)", Icons.Default.VolunteerActivism, Modifier.weight(1f)) { onPerformAction("Community Charity") }
                        }
                    }
                }
            }

            // Chronicles Log
            item {
                Text(
                    text = "CHRONICLES OF EXISTENCE (${state.recentEvents.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            items(state.recentEvents.take(15)) { event ->
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = event.type.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarmGold
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = event.description,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        if (event.consequences.isNotBlank()) {
                            Text(
                                text = event.consequences,
                                fontSize = 11.sp,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // Bottom Controls Bar (Next Day, Simulation Ticks)
        Card(
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Day ${world.inGameDay} • Year ${world.inGameYear}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Economy: ${world.economyTrend} (Inflation: ${(world.inflationRate * 100).toInt()}%)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onAdvanceDay,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.testTag("advance_day_btn")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Advance Day", color = DeepNavy, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NeedGaugeItem(
    label: String,
    value: Float,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 10.sp, color = Color.LightGray)
            Text("${value.toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            color = barColor,
            trackColor = Color(0xFF19223A),
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NpcCardItem(
    npc: NPC,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = npc.name.take(10),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(Icons.Default.Forum, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Russell circumplex badge
            Box(
                modifier = Modifier
                    .background(CyberViolet.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = npc.emotion.label,
                    fontSize = 10.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = npc.behaviorTreeState.take(18),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
