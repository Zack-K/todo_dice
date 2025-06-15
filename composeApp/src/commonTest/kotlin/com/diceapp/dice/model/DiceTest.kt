package com.diceapp.dice.model

import kotlin.test.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DiceTest {

    @Test
    fun `正常な面数でダイスを作成できる`() {
        val dice = Dice(sides = 6)
        assertEquals(6, dice.sides)
        assertEquals(1, dice.count)
    }

    @Test
    fun `複数個のダイスを作成できる`() {
        val dice = Dice(sides = 6, count = 3)
        assertEquals(6, dice.sides)
        assertEquals(3, dice.count)
    }

    @Test
    fun `面数が0以下の場合はエラーになる`() {
        assertFailsWith<IllegalArgumentException> {
            Dice(sides = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            Dice(sides = -1)
        }
    }

    @Test
    fun `個数が0以下の場合はエラーになる`() {
        assertFailsWith<IllegalArgumentException> {
            Dice(sides = 6, count = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            Dice(sides = 6, count = -1)
        }
    }

    @Test
    fun `ダイスを振ると正しい範囲の値が出る`() {
        val dice = Dice(sides = 6)
        repeat(100) {
            val roll = dice.roll()
            assertEquals(1, roll.results.size)
            assertTrue(roll.results[0] in 1..6)
        }
    }

    @Test
    fun `複数ダイスを振ると正しい個数の結果が得られる`() {
        val dice = Dice(sides = 6, count = 3)
        val roll = dice.roll()
        assertEquals(3, roll.results.size)
        roll.results.forEach { result ->
            assertTrue(result in 1..6)
        }
    }

    @Test
    fun `固定シードでダイスを振ると再現可能な結果が得られる`() {
        val dice = Dice(sides = 6)
        val random1 = Random(42)
        val random2 = Random(42)
        
        val roll1 = dice.roll(random1)
        val roll2 = dice.roll(random2)
        
        assertEquals(roll1.results, roll2.results)
    }

    @Test
    fun `StandardDiceで標準的なダイスを作成できる`() {
        val d6 = StandardDice.D6.create()
        assertEquals(6, d6.sides)
        assertEquals(1, d6.count)

        val d20 = StandardDice.D20.create(count = 2)  
        assertEquals(20, d20.sides)
        assertEquals(2, d20.count)
    }

    @Test
    fun `StandardDiceの全ての種類が正しい面数を持つ`() {
        assertEquals(4, StandardDice.D4.sides)
        assertEquals(6, StandardDice.D6.sides)
        assertEquals(8, StandardDice.D8.sides)
        assertEquals(10, StandardDice.D10.sides)
        assertEquals(12, StandardDice.D12.sides)
        assertEquals(20, StandardDice.D20.sides)
        assertEquals(100, StandardDice.D100.sides)
    }
}

class DiceRollTest {

    @Test
    fun `DiceRollの合計値が正しく計算される`() {
        val dice = Dice(sides = 6, count = 3)
        val roll = DiceRoll(dice = dice, results = listOf(1, 3, 5))
        
        assertEquals(9, roll.sum)
        assertEquals(9, roll.total)
    }

    @Test
    fun `修正値が正しく適用される`() {
        val dice = Dice(sides = 6)
        val roll = DiceRoll(dice = dice, results = listOf(4), modifier = 3)
        
        assertEquals(4, roll.sum)
        assertEquals(7, roll.total)
    }

    @Test
    fun `負の修正値が正しく適用される`() {
        val dice = Dice(sides = 6)
        val roll = DiceRoll(dice = dice, results = listOf(4), modifier = -2)
        
        assertEquals(4, roll.sum)
        assertEquals(2, roll.total)
    }

    @Test
    fun `最小値と最大値が正しく計算される`() {
        val dice = Dice(sides = 6, count = 3)
        val roll = DiceRoll(dice = dice, results = listOf(1, 6, 3))
        
        assertEquals(1, roll.min)
        assertEquals(6, roll.max)
    }

    @Test
    fun `平均値が正しく計算される`() {
        val dice = Dice(sides = 6, count = 4)
        val roll = DiceRoll(dice = dice, results = listOf(2, 4, 6, 8))
        
        assertEquals(5.0, roll.average)
    }

    @Test
    fun `単一ダイスの文字列表現が正しい`() {
        val dice = Dice(sides = 6)
        val roll = DiceRoll(dice = dice, results = listOf(4))
        
        assertEquals("1d6 = 4", roll.toString())
    }

    @Test
    fun `単一ダイス+修正値の文字列表現が正しい`() {
        val dice = Dice(sides = 6)
        val roll = DiceRoll(dice = dice, results = listOf(4), modifier = 3)
        
        assertEquals("1d6+3 = 7", roll.toString())
    }

    @Test
    fun `複数ダイスの文字列表現が正しい`() {
        val dice = Dice(sides = 6, count = 3)
        val roll = DiceRoll(dice = dice, results = listOf(1, 3, 5))
        
        assertEquals("3d6 = 1+3+5 = 9", roll.toString())
    }

    @Test
    fun `複数ダイス+修正値の文字列表現が正しい`() {
        val dice = Dice(sides = 6, count = 2)
        val roll = DiceRoll(dice = dice, results = listOf(3, 4), modifier = 2)
        
        assertEquals("2d6+2 = 3+4+2 = 9", roll.toString())
    }

    @Test
    fun `負の修正値の文字列表現が正しい`() {
        val dice = Dice(sides = 6)
        val roll = DiceRoll(dice = dice, results = listOf(5), modifier = -2)
        
        assertEquals("1d6-2 = 3", roll.toString())
    }
}