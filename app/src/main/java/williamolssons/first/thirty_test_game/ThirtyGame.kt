package williamolssons.first.thirty_test_game

import android.os.Bundle
import android.util.Log
import kotlin.math.min

/**
 * Class representing the Thirty Game logic.
 * Manages the state of the game, dice, scores, and rounds.
 */
class ThirtyGame {

    // Array of six dice used in the game.
    val dice: Array<Dice> = Array(6) { Dice() }
    // List to keep track of scores for each round.
    private val scores: MutableList<Int> = mutableListOf()
    // Current round number.
    var currentRound: Int = 0
        private set
    // Number of rolls left in the current round.
    private var rollsLeft: Int = 3

    /**
     * Rolls the dice that are not held.
     * Decreases the number of rolls left.
     */
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

    /**
     * Scores the current round based on the selected category and selected dice.
     * Adds the score to the scores list.
     * @param selectedCategory The category chosen for scoring.
     * @param selectedDice The dice selected for scoring.
     */
    fun scoreRound(selectedCategory: ScoreCategory, selectedDice: List<Dice>) {
        val roundScore = if (selectedDice.isEmpty()) {
            calculateScore(selectedCategory)
        } else {
            calculateManualScore(selectedCategory.value, selectedDice)
        }
        scores.add(roundScore)
        Log.d("ThirtyGame", "Scored $roundScore for category $selectedCategory in round $currentRound")
    }

    // Calculates score for automatic pairing based on selected category.
    private fun calculateScore(selectedCategory: ScoreCategory): Int {
        return when (selectedCategory) {
            ScoreCategory.LOW -> dice.filter { it.value <= 3 }.sumOf { it.value }
            else -> calculateOptimalScore(selectedCategory.value)
        }
    }

    // Calculates optimal score for the given target value.
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

    // Calculates manual score based on the selected dice.
    private fun calculateManualScore(targetValue: Int, selectedDice: List<Dice>): Int {
        val selectedSum = selectedDice.sumOf { it.value }
        if (selectedSum != targetValue) {
            throw IllegalArgumentException("Selected dice do not sum up to the target value.")
        }
        return selectedSum
    }

    // Gets the current score of the game.
    fun getCurrentScore(): Int {
        return scores.sum()
    }

    // Gets the total score of the game.
    fun getTotalScore(): Int {
        return scores.sum()
    }

    // Gets the scores for each round.
    fun getRoundScores(): List<Int> {
        return scores
    }

    // Moves to the next round, resets rolls and dice hold status.
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

    // Checks if the game is over.
    fun isGameOver(): Boolean {
        val gameOver = currentRound >= 9
        Log.d("ThirtyGame", "Is game over: $gameOver")
        return gameOver
    }

    // Saves the current state of the game to a Bundle.
    fun saveState(outState: Bundle) {
        outState.putInt("currentRound", currentRound)
        outState.putIntArray("diceValues", dice.map { it.value }.toIntArray())
        outState.putBooleanArray("diceHolds", dice.map { it.isHeld }.toBooleanArray())
        outState.putIntegerArrayList("scores", ArrayList(scores))
        outState.putInt("rollsLeft", rollsLeft)
    }

    // Restores the state of the game from a Bundle.
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

/**
 * Enum class representing the different score categories in the game.
 * @param value The value associated with the category.
 */
enum class ScoreCategory(val value: Int) {
    LOW(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), ELEVEN(11), TWELVE(12)
}
