package com.diceapp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.diceapp.core.config.AppConfig
import com.diceapp.todo.ui.TodoScreen
import com.diceapp.randomselector.ui.RandomSelectorScreen
import com.diceapp.dice.ui.DiceScreen

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
    
    // 今回はデフォルトでボトムナビゲーション使用
    val useBottomNav = true
    
    // 3タブ構成に拡張
    val tabs = listOf("TODO", "ダイス", "ランダム選択")
    val icons = listOf(Icons.Default.CheckCircle, Icons.Default.Star, Icons.Default.Settings)
    
    MaterialTheme {
        if (useBottomNav) {
            // モバイル: ボトムナビゲーション
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("DiceApp") }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tabs.forEachIndexed { index, title ->
                            NavigationBarItem(
                                icon = { 
                                    Icon(
                                        imageVector = icons[index], 
                                        contentDescription = title,
                                        modifier = Modifier.size(24.dp)
                                    ) 
                                },
                                label = { 
                                    Text(
                                        text = title,
                                        maxLines = 1
                                    ) 
                                },
                                selected = selectedTab == index,
                                onClick = { selectedTab = index }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (selectedTab) {
                        0 -> TodoScreen()
                        1 -> DiceScreen()
                        2 -> RandomSelectorScreen()
                    }
                }
            }
        } else {
            // デスクトップ/タブレット: トップタブ
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopAppBar(
                    title = { Text("DiceApp") }
                )
                
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
                            modifier = Modifier.height(72.dp)
                        )
                    }
                }
                
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (selectedTab) {
                        0 -> TodoScreen()
                        1 -> DiceScreen()
                        2 -> RandomSelectorScreen()
                    }
                }
            }
        }
    }
}