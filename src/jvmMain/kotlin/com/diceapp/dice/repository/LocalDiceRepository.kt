package com.diceapp.dice.repository

import com.diceapp.dice.model.DiceRoll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class LocalDiceRepository(
    private val dataDir: String = System.getProperty("user.home") + "/.diceapp"
) : DiceRepository {

    private val rollsFile = File(dataDir, "dice_rolls.json")
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val _rolls = MutableStateFlow<List<DiceRoll>>(emptyList())

    init {
        File(dataDir).mkdirs()
        loadRolls()
    }

    private fun loadRolls() {
        try {
            if (rollsFile.exists()) {
                val jsonContent = rollsFile.readText()
                if (jsonContent.isNotBlank()) {
                    val rolls = json.decodeFromString<List<DiceRoll>>(jsonContent)
                    _rolls.value = rolls
                }
            }
        } catch (e: Exception) {
            println("Failed to load dice rolls: ${e.message}")
            _rolls.value = emptyList()
        }
    }

    private suspend fun saveRolls() {
        try {
            val jsonContent = json.encodeToString(_rolls.value)
            rollsFile.writeText(jsonContent)
        } catch (e: Exception) {
            println("Failed to save dice rolls: ${e.message}")
        }
    }

    override suspend fun saveRoll(roll: DiceRoll): Result<Unit> {
        return try {
            val updatedRolls = _rolls.value + roll
            _rolls.value = updatedRolls
            saveRolls()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllRolls(): Flow<List<DiceRoll>> = _rolls

    override suspend fun getRollsByDiceType(sides: Int): Flow<List<DiceRoll>> {
        return _rolls.map { rolls ->
            rolls.filter { it.dice.sides == sides }
        }
    }

    override suspend fun getRecentRolls(limit: Int): Flow<List<DiceRoll>> {
        return _rolls.map { rolls ->
            rolls.takeLast(limit)
        }
    }

    override suspend fun clearHistory(): Result<Unit> {
        return try {
            _rolls.value = emptyList()
            saveRolls()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRollStatistics(sides: Int): DiceStatistics {
        val targetRolls = _rolls.value.filter { it.dice.sides == sides }
        
        if (targetRolls.isEmpty()) {
            return DiceStatistics(
                totalRolls = 0,
                averageResult = 0.0,
                minResult = 0,
                maxResult = 0,
                mostCommonResult = 0,
                resultDistribution = emptyMap()
            )
        }

        val allResults = targetRolls.flatMap { it.results }
        val distribution = allResults.groupingBy { it }.eachCount()
        val mostCommon = distribution.maxByOrNull { it.value }?.key ?: 0

        return DiceStatistics(
            totalRolls = targetRolls.size,
            averageResult = allResults.average(),
            minResult = allResults.minOrNull() ?: 0,
            maxResult = allResults.maxOrNull() ?: 0,
            mostCommonResult = mostCommon,
            resultDistribution = distribution
        )
    }
}