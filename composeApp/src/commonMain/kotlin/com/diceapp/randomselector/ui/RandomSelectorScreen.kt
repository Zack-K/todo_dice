package com.diceapp.randomselector.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diceapp.core.ui.MobileOptimizedButton
import com.diceapp.core.ui.AnimatedResultDisplay
import com.diceapp.core.ui.ResponsiveSpacer
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomSelectorScreen() {
    var currentResult by remember { mutableStateOf<String?>(null) }
    var isAnimating by remember { mutableStateOf(false) }
    var animatingDiceValue by remember { mutableIntStateOf(1) }
    val haptic = LocalHapticFeedback.current
    
    // 仮のサンプルデータ
    val sampleOptions = listOf(
        "北海道に行く", "沖縄に行く", "東京に行く", 
        "大阪に行く", "福岡に行く", "仙台に行く"
    )
    
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            repeat(20) {
                animatingDiceValue = (1..6).random()
                delay(100)
            }
            currentResult = sampleOptions.random()
            isAnimating = false
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // タイトルカード
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎲 ランダム選択",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "水曜どうでしょうのサイコロの旅気分",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // メインダイス表示エリア
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAnimating) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isAnimating) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎲",
                            fontSize = 64.sp
                        )
                        Text(
                            text = "$animatingDiceValue",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "振っています...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (currentResult != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎯",
                            fontSize = 48.sp
                        )
                        Text(
                            text = currentResult!!,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎲",
                            fontSize = 64.sp
                        )
                        Text(
                            text = "タップしてスタート!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // アクションボタン - モバイル最適化
        Button(
            onClick = {
                if (!isAnimating) {
                    isAnimating = true
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            },
            enabled = !isAnimating,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // Material Design推奨のタッチターゲットサイズ
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isAnimating) Icons.Default.Refresh else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAnimating) "振っています..." else "サイコロを振る",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // リセットボタン
        OutlinedButton(
            onClick = {
                currentResult = null
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            enabled = currentResult != null && !isAnimating,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("リセット")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 選択肢一覧カード
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📝 選択肢",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(sampleOptions) { option ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}