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
fun RandomSelectorScreen() {
    // 仮のデータ表示（デスクトップ用）
    var currentResult by remember { mutableStateOf<String?>(null) }
    var isAnimating by remember { mutableStateOf(false) }
    var animatingDiceValue by remember { mutableIntStateOf(1) }
    val availableCategories = remember { emptyList<String>() }
    val incompleteTodos = remember { emptyList<com.diceapp.todo.model.Todo>() }
    val customTexts = remember { emptyList<String>() }
    val recentResults = remember { emptyList<String>() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // デスクトップ用のシンプルなプレースホルダー
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "ランダム選択",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "モバイル版で利用可能",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // プレースホルダーメッセージ
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ランダム選択機能は間もなく利用可能になります",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}