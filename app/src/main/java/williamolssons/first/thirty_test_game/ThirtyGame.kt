package williamolssons.first.thirty_test_game

import android.os.Bundle
import android.util.Log
import kotlin.math.min

class ThirtyGame {

    val dice: Array<Dice> = Array(6) { Dice() }
    private val scores: MutableList<Int> = mutableListOf()
    var currentRound: Int = 0
        private set
    private var rollsLeft: Int = 3

    fun rollDice() {
        if (rollsLeft > 0) {
            for (die in dice) {
                if (!die.isHeld) {
                    die.value = (1..6).random()
                }
            }
            rollsLeft--
        }
        Log.d("ThirtyGame", "Dice rolled: ${dice.map { it.value }}")
    }

    fun scoreRound(selectedCategory: ScoreCategory, selectedDice: List<Dice>) {
        val roundScore = if (selectedDice.isEmpty()) {
            calculateScore(selectedCategory)
        } else {
            calculateManualScore(selectedCategory.value, selectedDice)
        }
        scores.add(roundScore)
        Log.d("ThirtyGame", "Scored $roundScore for category $selectedCategory in round $currentRound")
    }

    private fun calculateScore(selectedCategory: ScoreCategory): Int {
        return when (selectedCategory) {
            ScoreCategory.LOW -> dice.filter { it.value <= 3 }.sumOf { it.value }
            else -> calculateOptimalScore(selectedCategory.value)
        }
    }

    private fun calculateOptimalScore(targetValue: Int): Int {
        val diceCount = dice.groupBy { it.value }.mapValues { it.value.size }.toMutableMap()
        var score = 0

        while (true) {
            var pairSum = 0
            for ((value, count) in diceCount) {
                if (count > 0 && (diceCount[targetValue - value] ?: 0) > 0) {
                    val pairs = min(count, diceCount[targetValue - value] ?: 0)
                    pairSum += pairs * targetValue
                    diceCount[value] = count - pairs
                    diceCount[targetValue - value] = (diceCount[targetValue - value] ?: 0) - pairs
                }
            }
            if (pairSum == 0) break
            score += pairSum
        }
        return score
    }

    private fun calculateManualScore(targetValue: Int, selectedDice: List<Dice>): Int {
        val selectedSum = selectedDice.sumOf { it.value }
        if (selectedSum != targetValue) {
            throw IllegalArgumentException("Selected dice do not sum up to the target value.")
        }
        return selectedSum
    }

    fun getCurrentScore(): Int {
        return scores.sum()
    }

    fun getTotalScore(): Int {
        return scores.sum()
    }

    fun getRoundScores(): List<Int> {
        return scores
    }

    fun nextRound() {
        if (currentRound < 9) {
            currentRound++
            rollsLeft = 3
            for (die in dice) {
                die.isHeld = false
            }
            Log.d("ThirtyGame", "Moved to next round: $currentRound")
        }
    }

    fun isGameOver(): Boolean {
        val gameOver = currentRound >= 9
        Log.d("ThirtyGame", "Is game over: $gameOver")
        return gameOver
    }

    fun saveState(outState: Bundle) {
        outState.putInt("currentRound", currentRound)
        outState.putIntArray("diceValues", dice.map { it.value }.toIntArray())
        outState.putBooleanArray("diceHolds", dice.map { it.isHeld }.toBooleanArray())
        outState.putIntegerArrayList("scores", ArrayList(scores))
        outState.putInt("rollsLeft", rollsLeft)
    }

    fun restoreState(savedInstanceState: Bundle) {
        currentRound = savedInstanceState.getInt("currentRound", 0)
        val diceValues = savedInstanceState.getIntArray("diceValues") ?: IntArray(6) { 1 }
        val diceHolds = savedInstanceState.getBooleanArray("diceHolds") ?: BooleanArray(6) { false }
        for (i in dice.indices) {
            dice[i].value = diceValues[i]
            dice[i].isHeld = diceHolds[i]
        }
        scores.clear()
        scores.addAll(savedInstanceState.getIntegerArrayList("scores") ?: emptyList())
        rollsLeft = savedInstanceState.getInt("rollsLeft", 3)
    }
}

enum class ScoreCategory(val value: Int) {
    LOW(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), ELEVEN(11), TWELVE(12)
}
