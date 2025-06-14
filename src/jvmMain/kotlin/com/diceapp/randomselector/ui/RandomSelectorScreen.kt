package com.diceapp.randomselector.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomSelectorScreen(
    viewModel: RandomSelectorViewModel = remember { RandomSelectorViewModel() }
) {
    val recentResults by viewModel.recentResults.collectAsState()
    val currentResult by viewModel.currentResult.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val incompleteTodos by viewModel.incompleteTodos.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val customTexts by viewModel.customTexts.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()
    val isAnimating by viewModel.isAnimating.collectAsState()
    val animatingDiceValue by viewModel.animatingDiceValue.collectAsState()

    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 左側: 選択設定とコントロール
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "ランダム選択",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                // モード選択
                SelectionModeSelector(
                    currentMode = currentMode,
                    onModeChange = viewModel::updateCurrentMode
                )

                when (currentMode) {
                    RandomSelectorViewModel.SelectionMode.FROM_CATEGORY -> {
                        // カテゴリ選択
                        CategorySelector(
                            selectedCategory = selectedCategory,
                            categories = availableCategories,
                            onCategoryChange = viewModel::updateSelectedCategory
                        )

                        Button(
                            onClick = viewModel::performCategorySelection,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Shuffle, "実行")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ランダム選択実行")
                        }
                    }

                    RandomSelectorViewModel.SelectionMode.FROM_SELECTED_TODOS -> {
                        // TODO選択
                        TodoSelector(
                            todos = incompleteTodos,
                            onSelectionPerformed = viewModel::performSelectedTodosSelection
                        )
                    }

                    RandomSelectorViewModel.SelectionMode.CUSTOM -> {
                        // カスタム選択
                        Button(
                            onClick = viewModel::showCustomInputDialog,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, "編集")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("選択肢を入力")
                        }

                        Button(
                            onClick = viewModel::performCustomSelection,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = customTexts.any { it.isNotBlank() }
                        ) {
                            Icon(Icons.Default.Shuffle, "実行")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("カスタム選択実行")
                        }
                    }
                }

                Divider()

                // クイック選択
                OutlinedButton(
                    onClick = viewModel::performQuickSelection,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FlashOn, "クイック")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("全TODOからクイック選択")
                }

                Spacer(modifier = Modifier.weight(1f))

                // ユーティリティボタン
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = viewModel::showStatisticsDialog,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Analytics, "統計")
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
                    text = "選択結果",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // アニメーション表示または現在の結果
                if (isAnimating) {
                    DiceAnimationDisplay(diceValue = animatingDiceValue)
                } else {
                    currentResult?.let { result ->
                        SelectionResultDisplay(result = result)
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
                                    text = "選択肢を選んで実行",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
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
                    items(recentResults.reversed()) { result ->
                        SelectionHistoryItem(result = result)
                    }
                }
            }
        }
    }

    // ダイアログ
    if (viewModel.showCustomInput) {
        CustomInputDialog(
            customTexts = customTexts,
            onTextChange = viewModel::updateCustomText,
            onDismiss = viewModel::hideCustomInputDialog,
            onConfirm = {
                viewModel.hideCustomInputDialog()
                viewModel.performCustomSelection()
            }
        )
    }

    if (viewModel.showStatistics) {
        SelectionStatisticsDialog(
            onDismiss = viewModel::hideStatisticsDialog,
            getStatistics = viewModel::getStatistics
        )
    }
}