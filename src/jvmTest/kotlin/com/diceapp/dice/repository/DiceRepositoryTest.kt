package com.diceapp.dice.repository

import com.diceapp.dice.model.Dice
import com.diceapp.dice.model.DiceRoll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiceRepositoryTest {

    private lateinit var repository: DiceRepository

    @Before
    fun setup() {
        repository = MockDiceRepository()
    }

    @Test
    fun `ダイス結果を保存できる`() = runBlocking {
        val dice = Dice(sides = 6)
        val roll = DiceRoll(dice = dice, results = listOf(4))
        
        val result = repository.saveRoll(roll)
        assertTrue(result.isSuccess)
        
        val allRolls = repository.getAllRolls().first()
        assertEquals(1, allRolls.size)
        assertEquals(roll.results, allRolls[0].results)
    }

    @Test
    fun `特定の面数のダイス結果を取得できる`() = runBlocking {
        val d6Roll = DiceRoll(Dice(6), listOf(3))
        val d20Roll = DiceRoll(Dice(20), listOf(15))
        
        repository.saveRoll(d6Roll)
        repository.saveRoll(d20Roll)
        
        val d6Rolls = repository.getRollsByDiceType(6).first()
        assertEquals(1, d6Rolls.size)
        assertEquals(6, d6Rolls[0].dice.sides)
        
        val d20Rolls = repository.getRollsByDiceType(20).first()
        assertEquals(1, d20Rolls.size)
        assertEquals(20, d20Rolls[0].dice.sides)
    }

    @Test
    fun `最近のダイス結果を制限数で取得できる`() = runBlocking {
        repeat(15) { i ->
            val roll = DiceRoll(Dice(6), listOf(i % 6 + 1))
            repository.saveRoll(roll)
        }
        
        val recentRolls = repository.getRecentRolls(10).first()
        assertEquals(10, recentRolls.size)
    }

    @Test
    fun `履歴をクリアできる`() = runBlocking {
        val roll = DiceRoll(Dice(6), listOf(4))
        repository.saveRoll(roll)
        
        val beforeClear = repository.getAllRolls().first()
        assertEquals(1, beforeClear.size)
        
        val result = repository.clearHistory()
        assertTrue(result.isSuccess)
        
        val afterClear = repository.getAllRolls().first()
        assertEquals(0, afterClear.size)
    }

    @Test
    fun `ダイス統計を取得できる`() = runBlocking {
        val rolls = listOf(
            DiceRoll(Dice(6), listOf(1)),
            DiceRoll(Dice(6), listOf(3)),
            DiceRoll(Dice(6), listOf(6)),
            DiceRoll(Dice(6), listOf(3)),
            DiceRoll(Dice(6), listOf(4))
        )
        
        rolls.forEach { repository.saveRoll(it) }
        
        val statistics = repository.getRollStatistics(6)
        assertEquals(5, statistics.totalRolls)
        assertEquals(3.4, statistics.averageResult)
        assertEquals(1, statistics.minResult)
        assertEquals(6, statistics.maxResult)
        assertEquals(3, statistics.mostCommonResult) // 3が2回出現
        assertTrue(statistics.resultDistribution.containsKey(3))
        assertEquals(2, statistics.resultDistribution[3])
    }
}

// テスト用のMockRepository
class MockDiceRepository : DiceRepository {
    private val rolls = mutableListOf<DiceRoll>()

    override suspend fun saveRoll(roll: DiceRoll): Result<Unit> {
        rolls.add(roll)
        return Result.success(Unit)
    }

    override suspend fun getAllRolls(): Flow<List<DiceRoll>> {
        return flowOf(rolls.toList())
    }

    override suspend fun getRollsByDiceType(sides: Int): Flow<List<DiceRoll>> {
        return flowOf(rolls.filter { it.dice.sides == sides })
    }

    override suspend fun getRecentRolls(limit: Int): Flow<List<DiceRoll>> {
        return flowOf(rolls.takeLast(limit))
    }

    override suspend fun clearHistory(): Result<Unit> {
        rolls.clear()
        return Result.success(Unit)
    }

    override suspend fun getRollStatistics(sides: Int): DiceStatistics {
        val targetRolls = rolls.filter { it.dice.sides == sides }
        
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