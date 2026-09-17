package com.example.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.domain.simulation.CountryData
import com.example.domain.simulation.WorldGenerator
import com.example.presentation.viewmodel.MainUiState
import com.example.ui.theme.CardDark
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.WarmGold
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewLifeSetupScreen(
    state: MainUiState,
    onBack: () -> Unit,
    onStartLife: (name: String, gender: String, country: CountryData, traits: List<String>, difficulty: String, sandbox: Boolean, challenge: Boolean) -> Unit,
    onOpenStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val randomNames = listOf(
        "Julian Vance", "Aria Sterling", "Kaius Thorne", "Seraphina Lin",
        "Dante Morales", "Cora Davenport", "Leo Nakamura", "Valerie Dubois"
    )

    var name by remember { mutableStateOf(randomNames.random()) }
    var gender by remember { mutableStateOf("Female") }
    var selectedTraits by remember { mutableStateOf(setOf("Genius", "Resilient")) }
    var difficulty by remember { mutableStateOf("Adaptive") }
    var isSandbox by remember { mutableStateOf(false) }
    var isChallenge by remember { mutableStateOf(false) }

    val isPaid = state.userPreferences?.isPaidUser ?: false
    val countries = remember(isPaid) { WorldGenerator.getCountriesForUser(isPaid) }
    var selectedCountry by remember { mutableStateOf(countries.first()) }
    var countryMenuExpanded by remember { mutableStateOf(false) }

    val allTraitOptions = listOf("Genius", "Charming", "Athletic", "Creative", "Resilient", "Lucky")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("new_life_setup_screen")
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
                text = "Generate New Timeline",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name & Randomize
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Citizen Name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardDark
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("citizen_name_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { name = randomNames.random() },
                modifier = Modifier
                    .size(50.dp)
                    .background(CardDark, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Casino, contentDescription = "Randomize Name", tint = NeonCyan)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gender Selection
        Text("Biological Identity", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NeonCyan)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Female", "Male", "Non-Binary").forEach { g ->
                val isSelected = gender == g
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) CyberViolet else CardDark,
                            RoundedCornerShape(10.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) NeonCyan else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { gender = g }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = g,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Country Selection Dropdown
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Birth Nation (${countries.size} available)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NeonCyan
            )
            if (!isPaid) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenStore() }
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = WarmGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Unlock 195 Nations", fontSize = 11.sp, color = WarmGold, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = countryMenuExpanded,
            onExpandedChange = { countryMenuExpanded = !countryMenuExpanded }
        ) {
            OutlinedTextField(
                value = "${selectedCountry.name} (${selectedCountry.region})",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryMenuExpanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = CardDark
                ),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = countryMenuExpanded,
                onDismissRequest = { countryMenuExpanded = false }
            ) {
                countries.forEach { c ->
                    DropdownMenuItem(
                        text = { Text("${c.name} • ${c.currency} (Tax: ${(c.taxRate * 100).toInt()}%)") },
                        onClick = {
                            selectedCountry = c
                            countryMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Traits Selection
        Text("Inborn Traits & Talents", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NeonCyan)
        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allTraitOptions.forEach { trait ->
                val isSelected = selectedTraits.contains(trait)
                Box(
                    modifier = Modifier
                        .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CardDark, RoundedCornerShape(8.dp))
                        .border(1.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable {
                            selectedTraits = if (isSelected) selectedTraits - trait else selectedTraits + trait
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = trait,
                            fontSize = 12.sp,
                            color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Difficulty Mode
        Text("Simulation Mode & Difficulty", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NeonCyan)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Adaptive", "Normal", "Hardcore").forEach { diff ->
                val isSelected = difficulty == diff
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (isSelected) Color(0xFF28365E) else CardDark, RoundedCornerShape(10.dp))
                        .border(1.dp, if (isSelected) NeonCyan else Color.Transparent, RoundedCornerShape(10.dp))
                        .clickable { difficulty = diff }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = diff,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NeonCyan else Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sandbox & Challenge Toggles
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sandbox Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Start with $100k and no needs decay.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isSandbox,
                        onCheckedChange = { isSandbox = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Challenge Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Emergent crisis and heightened volatility.", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isChallenge,
                        onCheckedChange = { isChallenge = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberViolet)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onStartLife(
                    name.ifBlank { "Alex Mercer" },
                    gender,
                    selectedCountry,
                    selectedTraits.toList(),
                    difficulty,
                    isSandbox,
                    isChallenge
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_start_life_btn")
        ) {
            Text("Embody Citizen into Multiverse", color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}
