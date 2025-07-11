package com.ansid.arbi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.ansid.arbi.ui.theme.ArbiTheme
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by remember { mutableStateOf(true) }
            ArbiTheme(darkTheme = darkTheme) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(if (darkTheme) "Dark" else "Light", modifier = Modifier.align(Alignment.CenterVertically))
                            Switch(
                                checked = darkTheme,
                                onCheckedChange = { darkTheme = it },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        ArbitrageCalculatorUI(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun ArbitrageCalculatorUI(modifier: Modifier = Modifier) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var numOutcomes by remember { mutableStateOf(2) }
    var odds by remember { mutableStateOf(List(2) { "" }) }
    var totalBet by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Arbitrage Calculator", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Number of Outcomes:", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            (2..4).forEach { n ->
                RadioButton(
                    selected = numOutcomes == n,
                    onClick = {
                        numOutcomes = n
                        odds = List(n) { if (it < odds.size) odds[it] else "" }
                    },
                    colors = RadioButtonDefaults.colors()
                )
                Text("$n", modifier = Modifier.padding(end = 8.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        odds.forEachIndexed { i, value ->
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    odds = odds.toMutableList().also { it[i] = newValue }
                },
                label = { Text("Odds for Outcome ${i + 1}") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .width(220.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = totalBet,
            onValueChange = { totalBet = it },
            label = { Text("Total Bet Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .width(220.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                focusManager.clearFocus() // Collapse keyboard
                val oddsD = odds.mapNotNull { it.toDoubleOrNull() }
                val bet = totalBet.toDoubleOrNull()
                if (oddsD.size == numOutcomes && bet != null && oddsD.all { it > 0 }) {
                    val invSum = oddsD.sumOf { 1.0 / it }
                    if (invSum < 1) {
                        val stakes = oddsD.map { (bet / it) / invSum }
                        val payouts = stakes.zip(oddsD).map { it.first * it.second }
                        val minReturn = payouts.minOrNull() ?: 0.0
                        val profit = minReturn - bet
                        val payoutStr = payouts.mapIndexed { i, p -> "Outcome ${i + 1}: ${"%.2f".format(p)}" }.joinToString("\n")
                        result = "To guarantee a profit, bet:\n" +
                            stakes.mapIndexed { i, s -> "Outcome ${i + 1}: ${"%.2f".format(s)}" }.joinToString("\n") +
                            "\n\nTotal payout for each outcome:\n" + payoutStr +
                            "\n\nGuaranteed profit: ${"%.2f".format(profit)}"
                    } else {
                        result = "No arbitrage opportunity."
                    }
                } else {
                    result = "Please enter valid odds and bet."
                }
            },
            modifier = Modifier.width(220.dp),
            colors = ButtonDefaults.buttonColors()
        ) {
            Text("Calculate")
        }
        Spacer(modifier = Modifier.height(32.dp))
        if (result.isNotEmpty()) {
            Card(
                modifier = Modifier.widthIn(max = 320.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    result,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ArbiTheme {
        ArbitrageCalculatorUI()
    }
}