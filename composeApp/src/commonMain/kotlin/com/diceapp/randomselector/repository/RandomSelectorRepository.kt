package com.diceapp.randomselector.repository

import com.diceapp.randomselector.model.Selection
import com.diceapp.randomselector.model.SelectionResult
import kotlinx.coroutines.flow.Flow

interface RandomSelectorRepository {
    suspend fun saveSelection(selection: Selection): Result<Unit>
    suspend fun saveSelectionResult(result: SelectionResult): Result<Unit>
    suspend fun getAllSelections(): Flow<List<Selection>>
    suspend fun getAllSelectionResults(): Flow<List<SelectionResult>>
    suspend fun getSelectionById(id: String): Selection?
    suspend fun getRecentSelectionResults(limit: Int = 10): Flow<List<SelectionResult>>
    suspend fun deleteSelection(id: String): Result<Unit>
    suspend fun clearSelectionHistory(): Result<Unit>
    suspend fun getSelectionStatistics(): SelectionStatistics
}

data class SelectionStatistics(
    val totalSelections: Int,
    val mostUsedItems: Map<String, Int>,
    val selectionHistory: List<SelectionResult>
)