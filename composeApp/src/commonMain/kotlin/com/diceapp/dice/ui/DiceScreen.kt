package com.diceapp.dice.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.diceapp.dice.model.StandardDice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceScreen(
    viewModel: DiceViewModel = remember { DiceViewModel() }
) {
    val recentRolls by viewModel.recentRolls.collectAsState()
    val currentRoll by viewModel.currentRoll.collectAsState()
    val selectedStandardDice by viewModel.selectedStandardDice.collectAsState()
    val customSides by viewModel.customSides.collectAsState()
    val diceCount by viewModel.diceCount.collectAsState()
    val modifier by viewModel.modifier.collectAsState()
    val diceNotation by viewModel.diceNotation.collectAsState()
    val isUsingCustomDice by viewModel.isUsingCustomDice.collectAsState()
    val isUsingNotation by viewModel.isUsingNotation.collectAsState()

    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 左側: ダイス設定とコントロール
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "ダイス設定",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                // 記法モード切り替え
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = isUsingNotation,
                        onCheckedChange = { viewModel.toggleUseNotation() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ダイス記法を使用 (例: 2d6+3)")
                }

                if (isUsingNotation) {
                    // ダイス記法入力
                    OutlinedTextField(
                        value = diceNotation,
                        onValueChange = viewModel::updateDiceNotation,
                        label = { Text("ダイス記法") },
                        placeholder = { Text("例: 2d6+3, 1d20-1") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // 標準/カスタム切り替え
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = isUsingCustomDice,
                            onCheckedChange = { viewModel.toggleUseCustomDice() }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("カスタムダイス")
                    }

                    if (isUsingCustomDice) {
                        // カスタムダイス設定
                        OutlinedTextField(
                            value = customSides.toString(),
                            onValueChange = viewModel::updateCustomSides,
                            label = { Text("面数 (2-100)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // 標準ダイス選択
                        StandardDiceSelector(
                            selectedDice = selectedStandardDice,
                            onDiceSelected = viewModel::updateSelectedStandardDice
                        )
                    }

                    // ダイス個数
                    OutlinedTextField(
                        value = diceCount.toString(),
                        onValueChange = viewModel::updateDiceCount,
                        label = { Text("個数 (1-10)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 修正値
                    OutlinedTextField(
                        value = modifier.toString(),
                        onValueChange = viewModel::updateModifier,
                        label = { Text("修正値 (-50〜+50)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ロールボタン
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = viewModel::rollStandardDice,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Settings, "ダイスロール")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ダイスを振る")
                    }

                    OutlinedButton(
                        onClick = viewModel::rollRandomDice,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, "ランダム")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ランダムダイス")
                    }
                }

                Divider()

                // 履歴操作
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.showStatisticsFor(6) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Info, "統計")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("統計")
                    }

                    OutlinedButton(
                        onClick = viewModel::clearHistory,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Clear, "クリア")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("履歴削除")
                    }
                }
            }
        }

        // 右側: 結果表示と履歴
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "結果",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 現在の結果
                currentRoll?.let { roll ->
                    DiceRollResult(roll = roll)
                } ?: run {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ダイスを振って結果を表示",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "履歴",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 履歴リスト
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentRolls.reversed()) { roll ->
                        DiceHistoryItem(
                            roll = roll,
                            onShowStatistics = { viewModel.showStatisticsFor(roll.dice.sides) }
                        )
                    }
                }
            }
        }
    }

    // 統計ダイアログ
    if (viewModel.showStatistics) {
        DiceStatisticsDialog(
            sides = viewModel.statisticsForSides,
            onDismiss = viewModel::hideStatistics,
            getStatistics = viewModel::getStatistics
        )
    }
}