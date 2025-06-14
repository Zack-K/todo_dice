package com.diceapp

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.diceapp.todo.ui.TodoScreen
import com.diceapp.dice.ui.DiceScreen
import com.diceapp.randomselector.ui.RandomSelectorScreen
import com.diceapp.core.config.AppConfig

// String拡張関数
private operator fun String.times(count: Int): String = this.repeat(count)

/**
 * アプリケーションのエントリーポイント
 * 
 * TODO管理、ダイス機能、ランダム選択の3つの機能を統合した
 * Compose Desktopアプリケーションです。
 */
fun main() {
    // コンソールで機能確認メッセージを表示
    println("🎆 DiceApp 起動中...")
    println("📝 TODO管理・🎲 ダイス機能・🎯 ランダム選択が利用可能です")
    println("✨ 水曜どうでしょうのサイコロの旅インスパイア ✨")
    println("=" * 60)
    
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "DiceApp - TODO・ダイス・ランダム選択"
        ) {
            DiceApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * メインアプリケーションのComposable関数
 * 
 * タブベースのUIでTODO、ダイス、ランダム選択の機能を切り替えます。
 */
@Composable
fun DiceApp() {
    val config = AppConfig.DEFAULT
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = if (config.showDiceTab) {
        listOf("TODO", "ダイス", "ランダム選択")
    } else {
        listOf("TODO", "ランダム選択")
    }
    val icons = if (config.showDiceTab) {
        listOf(Icons.Default.CheckCircle, Icons.Default.Casino, Icons.Default.Shuffle)
    } else {
        listOf(Icons.Default.CheckCircle, Icons.Default.Shuffle)
    }
    
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // トップバー
            TopAppBar(
                title = { Text("DiceApp") }
            )
            
            // タブ
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                        icon = { Icon(icons[index], contentDescription = title) }
                    )
                }
            }
            
            // コンテンツ
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (config.showDiceTab) {
                    when (selectedTab) {
                        0 -> TodoScreen()
                        1 -> DiceScreen()
                        2 -> RandomSelectorScreen()
                    }
                } else {
                    when (selectedTab) {
                        0 -> TodoScreen()
                        1 -> RandomSelectorScreen()
                    }
                }
            }
        }
    }
}




@Preview
@Composable
fun DiceAppPreview() {
    DiceApp()
}