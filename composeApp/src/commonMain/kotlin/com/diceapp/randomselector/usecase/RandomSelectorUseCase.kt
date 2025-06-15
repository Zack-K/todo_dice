package com.diceapp.randomselector.usecase

import com.diceapp.dice.model.Dice
import com.diceapp.randomselector.model.Selection
import com.diceapp.randomselector.model.SelectionItem
import com.diceapp.randomselector.model.SelectionResult
import com.diceapp.randomselector.model.toSelection
import com.diceapp.randomselector.repository.RandomSelectorRepository
import com.diceapp.randomselector.repository.SelectionStatistics
import com.diceapp.todo.model.Todo
import com.diceapp.todo.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class RandomSelectorUseCase(
    private val randomSelectorRepository: RandomSelectorRepository,
    private val todoRepository: TodoRepository
) {

    suspend fun createSelectionFromTexts(
        texts: List<String>,
        title: String = "カスタム選択"
    ): Result<Selection> {
        if (texts.isEmpty()) {
            return Result.failure(IllegalArgumentException("選択肢は1つ以上必要です"))
        }
        if (texts.size > 6) {
            return Result.failure(IllegalArgumentException("選択肢は最大6つまでです"))
        }

        return try {
            val items = texts.mapIndexed { index, text ->
                SelectionItem(
                    id = "item_$index",
                    text = text.trim()
                )
            }
            val selection = Selection(items = items, title = title)
            randomSelectorRepository.saveSelection(selection)
            Result.success(selection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSelectionFromTodos(
        todoIds: List<String>,
        title: String = "TODOからの選択"
    ): Result<Selection> {
        return try {
            val todos = todoIds.mapNotNull { id ->
                todoRepository.getTodoById(id)
            }
            
            if (todos.isEmpty()) {
                return Result.failure(IllegalArgumentException("有効なTODOが見つかりません"))
            }
            
            val selection = todos.toSelection(title)
            randomSelectorRepository.saveSelection(selection)
            Result.success(selection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSelectionFromCategory(
        category: String,
        title: String = "${category}からの選択"
    ): Result<Selection> {
        return try {
            val todos = todoRepository.getTodosByCategory(category).first()
                .filter { !it.isCompleted }
            
            if (todos.isEmpty()) {
                return Result.failure(IllegalArgumentException("カテゴリ「$category」に未完了のTODOがありません"))
            }
            
            val selection = todos.take(6).toSelection(title)
            randomSelectorRepository.saveSelection(selection)
            Result.success(selection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun performSelection(
        selectionId: String,
        random: Random = Random.Default
    ): Result<SelectionResult> {
        return try {
            val selection = randomSelectorRepository.getSelectionById(selectionId)
                ?: return Result.failure(IllegalArgumentException("選択が見つかりません"))

            val dice = Dice(sides = selection.items.size)
            val roll = dice.roll(random)
            val diceResult = roll.results[0]
            val selectedItem = selection.items[diceResult - 1]

            val result = SelectionResult(
                selection = selection,
                diceRoll = diceResult,
                selectedItem = selectedItem
            )

            randomSelectorRepository.saveSelectionResult(result)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun performWeightedSelection(
        selectionId: String,
        random: Random = Random.Default
    ): Result<SelectionResult> {
        return try {
            val selection = randomSelectorRepository.getSelectionById(selectionId)
                ?: return Result.failure(IllegalArgumentException("選択が見つかりません"))

            val totalWeight = selection.items.sumOf { it.weight }
            val randomValue = random.nextInt(1, totalWeight + 1)
            
            var currentWeight = 0
            var selectedItem: SelectionItem? = null
            var diceRoll = 1
            
            for ((index, item) in selection.items.withIndex()) {
                currentWeight += item.weight
                if (randomValue <= currentWeight) {
                    selectedItem = item
                    diceRoll = index + 1
                    break
                }
            }

            selectedItem?.let { item ->
                val result = SelectionResult(
                    selection = selection,
                    diceRoll = diceRoll,
                    selectedItem = item
                )
                randomSelectorRepository.saveSelectionResult(result)
                Result.success(result)
            } ?: Result.failure(IllegalStateException("選択に失敗しました"))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllSelections(): Flow<List<Selection>> = 
        randomSelectorRepository.getAllSelections()

    suspend fun getAllSelectionResults(): Flow<List<SelectionResult>> = 
        randomSelectorRepository.getAllSelectionResults()

    suspend fun getRecentSelectionResults(limit: Int = 10): Flow<List<SelectionResult>> = 
        randomSelectorRepository.getRecentSelectionResults(limit)

    suspend fun deleteSelection(selectionId: String): Result<Unit> = 
        randomSelectorRepository.deleteSelection(selectionId)

    suspend fun clearSelectionHistory(): Result<Unit> = 
        randomSelectorRepository.clearSelectionHistory()

    suspend fun getSelectionStatistics(): SelectionStatistics = 
        randomSelectorRepository.getSelectionStatistics()

    suspend fun getAvailableCategories(): List<String> = 
        todoRepository.getAllCategories()

    suspend fun quickSelectFromAllTodos(): Result<SelectionResult> {
        return try {
            val allTodos = todoRepository.getAllTodos().first()
                .filter { !it.isCompleted }
            
            if (allTodos.isEmpty()) {
                return Result.failure(IllegalArgumentException("未完了のTODOがありません"))
            }
            
            val selectedTodos = allTodos.shuffled().take(6)
            val selection = selectedTodos.toSelection("全TODOからのクイック選択")
            randomSelectorRepository.saveSelection(selection)
            
            performSelection(selection.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}