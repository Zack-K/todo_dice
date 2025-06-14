package com.diceapp.dice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diceapp.dice.model.DiceRoll
import com.diceapp.dice.model.StandardDice
import com.diceapp.dice.repository.DiceStatistics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardDiceSelector(
    selectedDice: StandardDice,
    onDiceSelected: (StandardDice) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(100.dp)
    ) {
        items(StandardDice.values()) { dice ->
            FilterChip(
                onClick = { onDiceSelected(dice) },
                label = { 
                    Text(
                        text = "D${dice.sides}",
                        fontSize = 12.sp
                    ) 
                },
                selected = selectedDice == dice,
                modifier = Modifier.aspectRatio(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceRollResult(roll: DiceRoll) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ダイス記法表示
            Text(
                text = "${roll.dice.count}d${roll.dice.sides}${
                    if (roll.modifier != 0) {
                        if (roll.modifier > 0) "+${roll.modifier}" else "${roll.modifier}"
                    } else ""
                }",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 結果数値（大きく表示）
            Text(
                text = roll.total.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 詳細結果
            if (roll.dice.count > 1 || roll.modifier != 0) {
                Text(
                    text = roll.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }

            // 統計情報（複数ダイスの場合）
            if (roll.dice.count > 1) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatChip("最小", roll.min.toString())
                    StatChip("最大", roll.max.toString())
                    StatChip("平均", String.format("%.1f", roll.average))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatChip(label: String, value: String) {
    SuggestionChip(
        onClick = { },
        label = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceHistoryItem(
    roll: DiceRoll,
    onShowStatistics: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ダイス結果
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = roll.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = roll.timestamp.substringAfter('T').substringBefore('.'),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 結果値
            Text(
                text = roll.total.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            // 統計ボタン
            IconButton(
                onClick = onShowStatistics,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "統計",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DiceStatisticsDialog(
    sides: Int,
    onDismiss: () -> Unit,
    getStatistics: suspend (Int) -> DiceStatistics
) {
    var statistics by remember { mutableStateOf<DiceStatistics?>(null) }

    LaunchedEffect(sides) {
        statistics = getStatistics(sides)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("D$sides 統計") },
        text = {
            statistics?.let { stats ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatRow("総ロール数", stats.totalRolls.toString())
                    StatRow("平均値", String.format("%.2f", stats.averageResult))
                    StatRow("最小値", stats.minResult.toString())
                    StatRow("最大値", stats.maxResult.toString())
                    StatRow("最頻値", stats.mostCommonResult.toString())
                    
                    if (stats.resultDistribution.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "出目分布",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        stats.resultDistribution.toList()
                            .sortedBy { it.first }
                            .take(10) // 最大10個表示
                            .forEach { (result, count) ->
                                StatRow("$result", "$count 回")
                            }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}