package com.diceapp.randomselector.repository

import com.diceapp.randomselector.model.Selection
import com.diceapp.randomselector.model.SelectionResult
import com.diceapp.core.platform.FileSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.diceapp.core.logging.Logger

class LocalRandomSelectorRepository : RandomSelectorRepository {

    private val dataDir = FileSystem.getDataDirectory()
    private val selectionsFilePath = "$dataDir/selections.json"
    private val resultsFilePath = "$dataDir/selection_results.json"
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val _selections = MutableStateFlow<List<Selection>>(emptyList())
    private val _results = MutableStateFlow<List<SelectionResult>>(emptyList())

    init {
        loadSelections()
        loadResults()
    }

    private fun loadSelections() {
        try {
            val jsonContent = FileSystem.readTextFromFile(selectionsFilePath)
            if (!jsonContent.isNullOrBlank()) {
                val selections = json.decodeFromString<List<Selection>>(jsonContent)
                _selections.value = selections
            }
        } catch (e: Exception) {
            Logger.error("LocalRandomSelectorRepository", "Failed to load selections: ${e.message}")
            _selections.value = emptyList()
        }
    }

    private fun loadResults() {
        try {
            val jsonContent = FileSystem.readTextFromFile(resultsFilePath)
            if (!jsonContent.isNullOrBlank()) {
                val results = json.decodeFromString<List<SelectionResult>>(jsonContent)
                _results.value = results
            }
        } catch (e: Exception) {
            Logger.error("LocalRandomSelectorRepository", "Failed to load selection results: ${e.message}")
            _results.value = emptyList()
        }
    }

    private suspend fun saveSelections() {
        try {
            val jsonContent = json.encodeToString(_selections.value)
            FileSystem.writeTextToFile(selectionsFilePath, jsonContent)
        } catch (e: Exception) {
            Logger.error("LocalRandomSelectorRepository", "Failed to save selections: ${e.message}")
        }
    }

    private suspend fun saveResults() {
        try {
            val jsonContent = json.encodeToString(_results.value)
            FileSystem.writeTextToFile(resultsFilePath, jsonContent)
        } catch (e: Exception) {
            Logger.error("LocalRandomSelectorRepository", "Failed to save selection results: ${e.message}")
        }
    }

    override suspend fun saveSelection(selection: Selection): Result<Unit> {
        return try {
            val updatedSelections = _selections.value.filter { it.id != selection.id } + selection
            _selections.value = updatedSelections
            saveSelections()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveSelectionResult(result: SelectionResult): Result<Unit> {
        return try {
            val updatedResults = _results.value + result
            _results.value = updatedResults
            saveResults()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllSelections(): Flow<List<Selection>> = _selections

    override suspend fun getAllSelectionResults(): Flow<List<SelectionResult>> = _results

    override suspend fun getSelectionById(id: String): Selection? {
        return _selections.value.find { it.id == id }
    }

    override suspend fun getRecentSelectionResults(limit: Int): Flow<List<SelectionResult>> {
        return _results.map { results ->
            results.takeLast(limit)
        }
    }

    override suspend fun deleteSelection(id: String): Result<Unit> {
        return try {
            val currentSelections = _selections.value
            val updatedSelections = currentSelections.filter { it.id != id }
            if (updatedSelections.size == currentSelections.size) {
                Result.failure(IllegalArgumentException("Selection not found"))
            } else {
                _selections.value = updatedSelections
                saveSelections()
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearSelectionHistory(): Result<Unit> {
        return try {
            _results.value = emptyList()
            saveResults()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSelectionStatistics(): SelectionStatistics {
        val allResults = _results.value
        
        if (allResults.isEmpty()) {
            return SelectionStatistics(
                totalSelections = 0,
                mostUsedItems = emptyMap(),
                selectionHistory = emptyList()
            )
        }

        val itemCounts = mutableMapOf<String, Int>()
        allResults.forEach { result ->
            val itemText = result.selectedItem.text
            itemCounts[itemText] = (itemCounts[itemText] ?: 0) + 1
        }

        return SelectionStatistics(
            totalSelections = allResults.size,
            mostUsedItems = itemCounts,
            selectionHistory = allResults
        )
    }
}