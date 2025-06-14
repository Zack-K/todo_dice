package com.diceapp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diceapp.core.config.AppConfig
import com.diceapp.todo.ui.TodoScreen
import com.diceapp.randomselector.ui.RandomSelectorScreen

/**
 * DiceApp - メインアプリケーションUI
 * 
 * TODO管理とランダム選択の2つの機能を統合した
 * Compose Multiplatformアプリケーションです。
 * 
 * 水曜どうでしょうのサイコロの旅インスパイア
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val config = AppConfig.DEFAULT
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // ダイスタブは非表示に設定されているため、2タブ構成
    val tabs = listOf("TODO", "ランダム選択")
    val icons = listOf(Icons.Default.CheckCircle, Icons.Default.Settings)
    
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // トップバー
            TopAppBar(
                title = { Text("DiceApp") }
            )
            
            // タブ - モバイル最適化
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                text = title,
                                maxLines = 1
                            ) 
                        },
                        icon = { 
                            Icon(
                                imageVector = icons[index], 
                                contentDescription = title,
                                modifier = Modifier.size(24.dp)
                            ) 
                        },
                        modifier = Modifier.height(72.dp) // モバイル向けタッチターゲット最適化
                    )
                }
            }
            
            // コンテンツ
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    0 -> TodoScreen()
                    1 -> RandomSelectorScreen()
                }
            }
        }
    }
}