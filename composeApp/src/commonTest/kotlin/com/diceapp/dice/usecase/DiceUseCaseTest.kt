package com.diceapp.dice.usecase

import com.diceapp.dice.model.Dice
import com.diceapp.dice.model.StandardDice
import com.diceapp.dice.repository.DiceRepository
import com.diceapp.dice.usecase.DiceUseCase
import com.diceapp.testutil.MockDiceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiceUseCaseTest {

    private lateinit var useCase: DiceUseCase
    private lateinit var repository: DiceRepository

    @BeforeTest
    fun setup() {
        repository = MockDiceRepository()
        useCase = DiceUseCase(repository)
    }

    @Test
    fun `正常なダイスを振れる`() = runTest {
        val result = useCase.rollDice(sides = 6, count = 1)
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { roll ->
            assertEquals(6, roll.dice.sides)
            assertEquals(1, roll.dice.count)
            assertTrue(roll.results[0] in 1..6)
        }
    }

    @Test
    fun `修正値付きでダイスを振れる`() = runTest {
        val fixedRandom = Random(42)
        val result = useCase.rollDice(sides = 6, modifier = 3, random = fixedRandom)
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { roll ->
            assertEquals(3, roll.modifier)
            assertEquals(roll.sum + 3, roll.total)
        }
    }

    @Test
    fun `複数ダイスを振れる`() = runTest {
        val result = useCase.rollDice(sides = 6, count = 3)
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { roll ->
            assertEquals(3, roll.results.size)
            roll.results.forEach { value ->
                assertTrue(value in 1..6)
            }
        }
    }

    @Test
    fun `不正なダイス面数でエラーになる`() = runTest {
        val result = useCase.rollDice(sides = 0)
        
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `StandardDiceを振れる`() = runTest {
        val result = useCase.rollStandardDice(StandardDice.D20)
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { roll ->
            assertEquals(20, roll.dice.sides)
            assertTrue(roll.results[0] in 1..20)
        }
    }

    @Test
    fun `複数の異なるダイスを同時に振れる`() = runTest {
        val diceList = listOf(
            Dice(sides = 6),
            Dice(sides = 20),
            Dice(sides = 4, count = 2)
        )
        
        val result = useCase.rollMultipleDice(diceList)
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { rolls ->
            assertEquals(3, rolls.size)
            assertEquals(6, rolls[0].dice.sides)
            assertEquals(20, rolls[1].dice.sides)
            assertEquals(4, rolls[2].dice.sides)
            assertEquals(2, rolls[2].results.size)
        }
    }

    @Test
    fun `ダイス記法を解析して振れる - 基本形`() = runTest {
        val result = useCase.parseDiceNotation("2d6")
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { roll ->
            assertEquals(6, roll.dice.sides)
            assertEquals(2, roll.dice.count)
            assertEquals(0, roll.modifier)
        }
    }

    @Test
    fun `ダイス記法を解析して振れる - 修正値付き`() = runTest {
        val result = useCase.parseDiceNotation("1d20+5")
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { roll ->
            assertEquals(20, roll.dice.sides)
            assertEquals(1, roll.dice.count)
            assertEquals(5, roll.modifier)
        }
    }

    @Test
    fun `ダイス記法を解析して振れる - 負の修正値`() = runTest {
        val result = useCase.parseDiceNotation("3d8-2")
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { roll ->
            assertEquals(8, roll.dice.sides)
            assertEquals(3, roll.dice.count)
            assertEquals(-2, roll.modifier)
        }
    }

    @Test
    fun `不正なダイス記法でエラーになる`() = runTest {
        val invalidNotations = listOf(
            "invalid",
            "d6",
            "2x6",
            "2d",
            "2d6+",
            ""
        )
        
        invalidNotations.forEach { notation ->
            val result = useCase.parseDiceNotation(notation)
            assertFalse(result.isSuccess, "記法 '$notation' はエラーになるべきです")
        }
    }

    @Test
    fun `ダイス結果がリポジトリに保存される`() = runTest {
        useCase.rollDice(sides = 6)
        useCase.rollDice(sides = 20)
        
        val allRolls = useCase.getAllRolls().first()
        assertEquals(2, allRolls.size)
    }

    @Test
    fun `特定の面数のダイス結果を取得できる`() = runTest {
        useCase.rollDice(sides = 6)
        useCase.rollDice(sides = 6)
        useCase.rollDice(sides = 20)
        
        val d6Rolls = useCase.getRollsByDiceType(6).first()
        assertEquals(2, d6Rolls.size)
        
        val d20Rolls = useCase.getRollsByDiceType(20).first()
        assertEquals(1, d20Rolls.size)
    }

    @Test
    fun `履歴をクリアできる`() = runTest {
        useCase.rollDice(sides = 6)
        
        val beforeClear = useCase.getAllRolls().first()
        assertEquals(1, beforeClear.size)
        
        val result = useCase.clearHistory()
        assertTrue(result.isSuccess)
        
        val afterClear = useCase.getAllRolls().first()
        assertEquals(0, afterClear.size)
    }

    @Test
    fun `統計情報を取得できる`() = runTest {
        repeat(5) {
            useCase.rollDice(sides = 6)
        }
        
        val statistics = useCase.getDiceStatistics(6)
        assertEquals(5, statistics.totalRolls)
        assertTrue(statistics.averageResult > 0.0)
    }

    @Test
    fun `ランダムなダイスを振れる`() = runTest {
        val result = useCase.getRandomRoll()
        
        assertTrue(result.isSuccess)
        result.getOrNull()?.let { roll ->
            assertTrue(roll.dice.sides in listOf(4, 6, 8, 10, 12, 20))
        }
    }
}