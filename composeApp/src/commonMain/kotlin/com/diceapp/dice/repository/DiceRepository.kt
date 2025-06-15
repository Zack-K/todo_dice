package com.diceapp.dice.repository

import com.diceapp.dice.model.DiceRoll
import kotlinx.coroutines.flow.Flow

interface DiceRepository {
    suspend fun saveRoll(roll: DiceRoll): Result<Unit>
    suspend fun getAllRolls(): Flow<List<DiceRoll>>
    suspend fun getRollsByDiceType(sides: Int): Flow<List<DiceRoll>>
    suspend fun getRecentRolls(limit: Int = 10): Flow<List<DiceRoll>>
    suspend fun clearHistory(): Result<Unit>
    suspend fun getRollStatistics(sides: Int): DiceStatistics
}

data class DiceStatistics(
    val totalRolls: Int,
    val averageResult: Double,
    val minResult: Int,
    val maxResult: Int,
    val mostCommonResult: Int,
    val resultDistribution: Map<Int, Int>
)