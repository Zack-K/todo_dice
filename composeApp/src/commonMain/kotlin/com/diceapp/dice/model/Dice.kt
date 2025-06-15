package com.diceapp.dice.model

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class Dice(
    val sides: Int,
    val count: Int = 1
) {
    init {
        require(sides > 0) { "ダイスの面数は1以上である必要があります" }
        require(count > 0) { "ダイスの個数は1以上である必要があります" }
    }

    fun roll(random: Random = Random.Default): DiceRoll {
        val results = (1..count).map { random.nextInt(1, sides + 1) }
        return DiceRoll(dice = this, results = results)
    }
}

@Serializable
data class DiceRoll(
    val dice: Dice,
    val results: List<Int>,
    val modifier: Int = 0,
    val timestamp: String = kotlinx.datetime.Clock.System.now().toString()
) {
    val sum: Int = results.sum()
    val total: Int = sum + modifier
    val min: Int = results.minOrNull() ?: 0
    val max: Int = results.maxOrNull() ?: 0
    val average: Double = if (results.isNotEmpty()) results.average() else 0.0

    override fun toString(): String {
        val diceNotation = "${dice.count}d${dice.sides}"
        val modifierText = if (modifier != 0) {
            if (modifier > 0) "+$modifier" else "$modifier"
        } else ""
        
        return if (dice.count == 1) {
            "$diceNotation$modifierText = $total"
        } else {
            "$diceNotation$modifierText = ${results.joinToString("+")}$modifierText = $total"
        }
    }
}

enum class StandardDice(val sides: Int) {
    D4(4),
    D6(6), 
    D8(8),
    D10(10),
    D12(12),
    D20(20),
    D100(100);

    fun create(count: Int = 1): Dice = Dice(sides = sides, count = count)
}