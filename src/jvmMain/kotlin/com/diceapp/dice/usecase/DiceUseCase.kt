package com.diceapp.dice.usecase

import com.diceapp.dice.model.Dice
import com.diceapp.dice.model.DiceRoll
import com.diceapp.dice.model.StandardDice
import com.diceapp.dice.repository.DiceRepository
import com.diceapp.dice.repository.DiceStatistics
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class DiceUseCase(private val repository: DiceRepository) {

    suspend fun rollDice(
        sides: Int,
        count: Int = 1,
        modifier: Int = 0,
        random: Random = Random.Default
    ): Result<DiceRoll> {
        return try {
            val dice = Dice(sides = sides, count = count)
            val roll = dice.roll(random).copy(modifier = modifier)
            
            repository.saveRoll(roll)
            Result.success(roll)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rollStandardDice(
        standardDice: StandardDice,
        count: Int = 1,
        modifier: Int = 0,
        random: Random = Random.Default
    ): Result<DiceRoll> {
        return rollDice(
            sides = standardDice.sides,
            count = count,
            modifier = modifier,
            random = random
        )
    }

    suspend fun rollMultipleDice(
        diceList: List<Dice>,
        modifier: Int = 0,
        random: Random = Random.Default
    ): Result<List<DiceRoll>> {
        return try {
            val rolls = diceList.map { dice ->
                dice.roll(random).copy(modifier = modifier)
            }
            
            rolls.forEach { repository.saveRoll(it) }
            Result.success(rolls)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun parseDiceNotation(notation: String): Result<DiceRoll> {
        return try {
            val result = parseDiceString(notation)
            rollDice(
                sides = result.sides,
                count = result.count,
                modifier = result.modifier
            )
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("無効なダイス記法です: $notation"))
        }
    }

    private fun parseDiceString(notation: String): DiceNotation {
        val cleanNotation = notation.replace(" ", "").lowercase()
        
        // "2d6+3" や "1d20-2" のような形式を解析
        val regex = Regex("""(\d+)?d(\d+)([+-]\d+)?""")
        val match = regex.matchEntire(cleanNotation)
            ?: throw IllegalArgumentException("無効な形式")

        val count = match.groupValues[1].takeIf { it.isNotEmpty() }?.toInt() ?: 1
        val sides = match.groupValues[2].toInt()
        val modifierStr = match.groupValues[3]
        val modifier = when {
            modifierStr.isEmpty() -> 0
            modifierStr.startsWith("+") -> modifierStr.substring(1).toInt()
            modifierStr.startsWith("-") -> modifierStr.toInt()
            else -> 0
        }

        return DiceNotation(count, sides, modifier)
    }

    suspend fun getAllRolls(): Flow<List<DiceRoll>> = repository.getAllRolls()

    suspend fun getRollsByDiceType(sides: Int): Flow<List<DiceRoll>> = 
        repository.getRollsByDiceType(sides)

    suspend fun getRecentRolls(limit: Int = 10): Flow<List<DiceRoll>> = 
        repository.getRecentRolls(limit)

    suspend fun clearHistory(): Result<Unit> = repository.clearHistory()

    suspend fun getDiceStatistics(sides: Int): DiceStatistics = 
        repository.getRollStatistics(sides)

    suspend fun getRandomRoll(
        minSides: Int = 4,
        maxSides: Int = 20
    ): Result<DiceRoll> {
        val randomSides = listOf(4, 6, 8, 10, 12, 20).random()
        return rollDice(sides = randomSides)
    }
}

private data class DiceNotation(
    val count: Int,
    val sides: Int, 
    val modifier: Int
)