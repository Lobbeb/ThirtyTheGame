package williamolssons.first.thirty_test_game

import android.os.Bundle
import android.util.Log
import kotlin.random.Random

/**
 * Manages the game logic for Thirty.
 */
class ThirtyGame {

    val dice: Array<Dice> = Array(6) { Dice() }
    private val scores: MutableMap<ScoreCategory, Int> = mutableMapOf()
    var currentRound: Int = 0
        private set
    private var rollsLeft: Int = 4

    /**
     * Rolls the dice that are not held.
     */
    fun rollDice() {
        if (rollsLeft > 0) {
            for (die in dice) {
                if (!die.isHeld) {
                    die.value = Random.nextInt(1, 7)
                }
            }
            rollsLeft--
        }
        Log.d("ThirtyGame", "Dice rolled: ${dice.map { it.value }}")
    }

    /**
     * Scores the round based on the selected category and dice.
     * @param selectedCategory The category chosen for scoring.
     * @param selectedDice The dice selected for scoring.
     */
    fun scoreRound(selectedCategory: ScoreCategory, selectedDice: List<Dice>) {
        if (scores.containsKey(selectedCategory)) {
            throw IllegalArgumentException("Category already used.")
        }

        val roundScore = if (selectedDice.isEmpty()) {
            calculateScore(selectedCategory)
        } else {
            calculateManualScore(selectedCategory, selectedDice)
        }
        scores[selectedCategory] = roundScore
        Log.d("ThirtyGame", "Scored $roundScore for category $selectedCategory in round $currentRound")
    }

    /**
     * Calculates the score for the selected category.
     * @param selectedCategory The category chosen for scoring.
     * @return The calculated score.
     */
    private fun calculateScore(selectedCategory: ScoreCategory): Int {
        return when (selectedCategory) {
            ScoreCategory.LOW -> dice.filter { it.value <= 3 }.sumOf { it.value }
            else -> calculateOptimalScore(selectedCategory.value, dice.map { it.value })
        }
    }

    /**
     * Calculates the optimal score for the given target value.
     * @param targetValue The target value for scoring.
     * @param diceValues The values of the dice.
     * @return The optimal score.
     */
    private fun calculateOptimalScore(targetValue: Int, diceValues: List<Int>): Int {
        val combinations = findAllUniqueCombinations(diceValues, targetValue)
        val usedDice = BooleanArray(diceValues.size)
        var score = 0

        combinations.forEach { combination ->
            if (isCombinationValid(combination, diceValues, usedDice)) {
                score += targetValue
                markUsedDice(combination, diceValues, usedDice)
            }
        }

        Log.d("ThirtyGame", "Final score: $score, used dice: ${usedDice.toList()}")
        return score
    }

    /**
     * Checks if a combination is valid.
     * @param combination The combination of dice values.
     * @param diceValues The values of the dice.
     * @param usedDice The dice that have been used.
     * @return True if the combination is valid, false otherwise.
     */
    private fun isCombinationValid(combination: List<Int>, diceValues: List<Int>, usedDice: BooleanArray): Boolean {
        val diceCount = mutableMapOf<Int, Int>()
        for (value in combination) {
            diceCount[value] = diceCount.getOrDefault(value, 0) + 1
        }
        for ((value, count) in diceCount) {
            val availableDice = diceValues.indices.count { diceValues[it] == value && !usedDice[it] }
            if (count > availableDice) {
                return false
            }
        }
        return true
    }

    /**
     * Marks the used dice in a combination.
     * @param combination The combination of dice values.
     * @param diceValues The values of the dice.
     * @param usedDice The dice that have been used.
     */
    private fun markUsedDice(combination: List<Int>, diceValues: List<Int>, usedDice: BooleanArray) {
        val diceCount = mutableMapOf<Int, Int>()
        for (value in combination) {
            diceCount[value] = diceCount.getOrDefault(value, 0) + 1
        }
        for ((index, value) in diceValues.withIndex()) {
            if (diceCount.containsKey(value) && diceCount[value]!! > 0 && !usedDice[index]) {
                usedDice[index] = true
                diceCount[value] = diceCount[value]!! - 1
            }
        }
    }

    /**
     * Calculates the manual score based on selected dice.
     * @param selectedCategory The category chosen for scoring.
     * @param selectedDice The dice selected for scoring.
     * @return The calculated score.
     */
    private fun calculateManualScore(selectedCategory: ScoreCategory, selectedDice: List<Dice>): Int {
        val selectedValues = selectedDice.map { it.value }
        return when (selectedCategory) {
            ScoreCategory.LOW -> {
                Log.d("ThirtyGame", "Selected values for LOW: $selectedValues")
                if (selectedValues.any { it > 3 }) {
                    throw IllegalArgumentException("Selected dice for 'Low' category must have values of 3 or lower.")
                }
                selectedValues.sum()
            }
            else -> {
                Log.d("ThirtyGame", "Selected values for ${selectedCategory.name}: $selectedValues")
                val combinations = findAllUniqueCombinations(selectedValues, selectedCategory.value)
                Log.d("ThirtyGame", "Found combinations: $combinations")
                var score = 0
                val usedDice = BooleanArray(selectedValues.size)

                combinations.forEach { combination ->
                    if (isCombinationValid(combination, selectedValues, usedDice)) {
                        score += selectedCategory.value
                        markUsedDice(combination, selectedValues, usedDice)
                    }
                }

                if (score == 0) {
                    throw IllegalArgumentException("Selected dice do not sum up to the target value.")
                }
                score
            }
        }
    }

    /**
     * Finds all unique combinations of dice values that sum to the target value.
     * @param diceValues The values of the dice.
     * @param targetValue The target value for the sum.
     * @return A list of unique combinations.
     */
    private fun findAllUniqueCombinations(diceValues: List<Int>, targetValue: Int): List<List<Int>> {
        val result = mutableListOf<List<Int>>()
        findUniqueCombinationsHelper(diceValues.sortedDescending(), targetValue, 0, mutableListOf(), result)
        return result
    }

    /**
     * Helper function to find unique combinations of dice values that sum to the target value.
     * @param diceValues The values of the dice.
     * @param targetValue The target value for the sum.
     * @param start The starting index.
     * @param currentCombination The current combination being built.
     * @param result The list of resulting combinations.
     */
    private fun findUniqueCombinationsHelper(
        diceValues: List<Int>,
        targetValue: Int,
        start: Int,
        currentCombination: MutableList<Int>,
        result: MutableList<List<Int>>
    ) {
        if (targetValue == 0) {
            result.add(ArrayList(currentCombination))
            return
        }
        if (targetValue < 0) {
            return
        }

        for (i in start until diceValues.size) {
            currentCombination.add(diceValues[i])
            findUniqueCombinationsHelper(diceValues, targetValue - diceValues[i], i + 1, currentCombination, result)
            currentCombination.removeAt(currentCombination.size - 1)
        }
    }

    /**
     * Gets the current score.
     * @return The current score.
     */
    fun getCurrentScore(): Int {
        return scores.values.sum()
    }

    /**
     * Gets the total score.
     * @return The total score.
     */
    fun getTotalScore(): Int {
        return scores.values.sum()
    }

    /**
     * Gets the scores for each round.
     * @return A list of pairs containing the category name and score for each round.
     */
    fun getRoundScores(): List<Pair<String, Int>> {
        return scores.map { it.key.name to it.value }
    }

    /**
     * Moves to the next round.
     */
    fun nextRound() {
        if (currentRound < 9) {
            currentRound++
            rollsLeft = if (currentRound == 0) 4 else 3
            for (die in dice) {
                die.isHeld = false
            }
            Log.d("ThirtyGame", "Moved to next round: $currentRound")
        }
    }

    /**
     * Checks if the game is over.
     * @return True if the game is over, false otherwise.
     */
    fun isGameOver(): Boolean {
        val gameOver = currentRound >= 9
        Log.d("ThirtyGame", "Is game over: $gameOver")
        return gameOver
    }

    /**
     * Saves the current state of the game.
     * @param outState The bundle to save the state.
     */
    fun saveState(outState: Bundle) {
        outState.putInt("currentRound", currentRound)
        outState.putIntArray("diceValues", dice.map { it.value }.toIntArray())
        outState.putBooleanArray("diceHolds", dice.map { it.isHeld }.toBooleanArray())
        outState.putInt("rollsLeft", rollsLeft)

        val scoresBundle = Bundle()
        for ((key, value) in scores) {
            scoresBundle.putInt(key.name, value)
        }
        outState.putBundle("scoresBundle", scoresBundle)
    }

    /**
     * Restores the saved state of the game.
     * @param savedInstanceState The bundle containing the saved state.
     */
    fun restoreState(savedInstanceState: Bundle) {
        currentRound = savedInstanceState.getInt("currentRound", 0)
        val diceValues = savedInstanceState.getIntArray("diceValues") ?: IntArray(6) { 1 }
        val diceHolds = savedInstanceState.getBooleanArray("diceHolds") ?: BooleanArray(6) { false }
        val scoresBundle = savedInstanceState.getBundle("scoresBundle")

        scores.clear()
        scoresBundle?.keySet()?.forEach { key ->
            val scoreCategory = ScoreCategory.valueOf(key)
            val score = scoresBundle.getInt(key)
            scores[scoreCategory] = score
        }

        for (i in dice.indices) {
            dice[i].value = diceValues[i]
            dice[i].isHeld = diceHolds[i]
        }
        rollsLeft = savedInstanceState.getInt("rollsLeft", 3)
    }
}

/**
 * Enum class representing the different score categories.
 */
enum class ScoreCategory(val value: Int) {
    LOW(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), ELEVEN(11), TWELVE(12)
}
