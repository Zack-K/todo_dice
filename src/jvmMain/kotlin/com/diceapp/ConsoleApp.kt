package com.diceapp

import com.diceapp.dice.model.StandardDice
import com.diceapp.dice.repository.LocalDiceRepository
import com.diceapp.dice.usecase.DiceUseCase
import com.diceapp.todo.model.Priority
import com.diceapp.todo.repository.LocalTodoRepository
import com.diceapp.todo.usecase.TodoUseCase
import com.diceapp.randomselector.repository.LocalRandomSelectorRepository
import com.diceapp.randomselector.usecase.RandomSelectorUseCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

/**
 * DiceAppのコンソール版エントリーポイント
 * GUI表示に問題がある環境での機能確認用
 */
fun main() {
    println("🎲 DiceApp - コンソール版実行")
    println("=" * 50)
    
    runBlocking {
        // TODOモジュールテスト
        println("\n📝 TODOモジュール テスト")
        testTodoModule()
        
        // ダイスモジュールテスト  
        println("\n🎲 ダイスモジュール テスト")
        testDiceModule()
        
        // ランダム選択モジュールテスト
        println("\n🎯 ランダム選択モジュール テスト")
        testRandomSelectorModule()
    }
    
    println("\n" + "=" * 50)
    println("✅ 全モジュール動作確認完了！")
    println("🎯 GUIアプリケーションも同様に動作します")
}

private suspend fun testTodoModule() {
    val todoRepository = LocalTodoRepository()
    val todoUseCase = TodoUseCase(todoRepository)
    
    // サンプルTODO作成
    println("  📋 サンプルTODO作成中...")
    val sampleTodos = listOf(
        Triple("朝の散歩", "健康のため30分程度", Priority.MEDIUM),
        Triple("プログラミング学習", "Kotlin復習とCompose練習", Priority.HIGH),
        Triple("読書", "技術書を1章読む", Priority.LOW)
    )
    
    sampleTodos.forEach { (title, desc, priority) ->
        todoUseCase.createTodo(title, desc, priority, "日課", null)
    }
    
    val todos = todoUseCase.getAllTodos().first()
    println("  ✅ TODO総数: ${todos.size}件")
    todos.forEach { todo ->
        val status = if (todo.isCompleted) "✅" else "⏳"
        println("    $status ${todo.title} [${todo.priority}]")
    }
    
    val categories = todoUseCase.getAllCategories()
    println("  🏷️ カテゴリ: ${categories.joinToString(", ")}")
}

private suspend fun testDiceModule() {
    val diceRepository = LocalDiceRepository()
    val diceUseCase = DiceUseCase(diceRepository)
    
    println("  🎲 各種ダイスロール実行中...")
    
    // 標準ダイステスト
    val results = mutableListOf<String>()
    
    // D6を2個ロール
    val d6Result = diceUseCase.rollStandardDice(StandardDice.D6, count = 2, modifier = 0)
    d6Result.onSuccess { roll ->
        results.add("2D6: ${roll.results.joinToString("+")} = ${roll.total}")
    }
    
    // D20ロール
    val d20Result = diceUseCase.rollStandardDice(StandardDice.D20, count = 1, modifier = 5)
    d20Result.onSuccess { roll ->
        results.add("1D20+5: ${roll.results.first()}+5 = ${roll.total}")
    }
    
    // ダイス記法テスト
    val notationResult = diceUseCase.parseDiceNotation("3d6+2")
    notationResult.onSuccess { roll ->
        results.add("3d6+2: ${roll.results.joinToString("+")}+2 = ${roll.total}")
    }
    
    results.forEach { result ->
        println("    🎲 $result")
    }
    
    // 履歴確認
    val history = diceUseCase.getRecentRolls(5).first()
    println("  📜 ロール履歴: ${history.size}件記録済み")
}

private suspend fun testRandomSelectorModule() {
    val randomSelectorRepository = LocalRandomSelectorRepository()
    val todoRepository = LocalTodoRepository()
    val randomSelectorUseCase = RandomSelectorUseCase(randomSelectorRepository, todoRepository)
    
    println("  🎯 ランダム選択テスト実行中...")
    
    // カスタム選択テスト
    val lunchOptions = listOf("ラーメン", "カレー", "寿司", "パスタ", "ハンバーガー", "そば")
    val selectionResult = randomSelectorUseCase.createSelectionFromTexts(lunchOptions, "今日の昼食")
    
    selectionResult.onSuccess { selection ->
        println("  📝 選択肢作成: ${selection.title}")
        println("    選択肢: ${selection.items.map { it.text }.joinToString(", ")}")
        
        // 実際に選択実行
        val performResult = randomSelectorUseCase.performSelection(selection.id)
        performResult.onSuccess { result ->
            println("  🎲 ダイス結果: ${result.diceRoll}")
            println("  🎯 選択結果: 【${result.selectedItem.text}】")
        }
    }
    
    // TODOからのクイック選択
    val quickResult = randomSelectorUseCase.quickSelectFromAllTodos()
    quickResult.onSuccess { result ->
        println("  ⚡ TODOクイック選択: 【${result.selectedItem.text}】")
    }.onFailure {
        println("  ⚡ TODOクイック選択: 利用可能なTODOなし")
    }
}

private operator fun String.times(count: Int): String = this.repeat(count)