package williamolssons.first.thirty_test_game

import android.os.Bundle
import android.util.Log
import kotlin.random.Random

class ThirtyGame {

    val dice: Array<Dice> = Array(6) { Dice() }
    private val scores: MutableMap<ScoreCategory, Int> = mutableMapOf()
    var currentRound: Int = 0
        private set
    private var rollsLeft: Int = 3

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

    private fun calculateScore(selectedCategory: ScoreCategory): Int {
        return when (selectedCategory) {
            ScoreCategory.LOW -> dice.filter { it.value <= 3 }.sumOf { it.value }
            else -> calculateOptimalScore(selectedCategory.value, dice.map { it.value })
        }
    }

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

    private fun calculateManualScore(selectedCategory: ScoreCategory, selectedDice: List<Dice>): Int {
        return when (selectedCategory) {
            ScoreCategory.LOW -> {
                val selectedValues = selectedDice.map { it.value }
                Log.d("ThirtyGame", "Selected values for LOW: $selectedValues")
                if (selectedValues.any { it > 3 }) {
                    throw IllegalArgumentException("Selected dice for 'Low' category must have values of 3 or lower.")
                }
                selectedValues.sum()
            }
            else -> {
                val selectedValues = selectedDice.map { it.value }
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

    private fun findAllUniqueCombinations(diceValues: List<Int>, targetValue: Int): List<List<Int>> {
        val result = mutableListOf<List<Int>>()
        findUniqueCombinationsHelper(diceValues.sortedDescending(), targetValue, 0, mutableListOf(), result)
        return result
    }

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

    fun getCurrentScore(): Int {
        return scores.values.sum()
    }

    fun getTotalScore(): Int {
        return scores.values.sum()
    }

    fun getRoundScores(): List<Pair<String, Int>> {
        return scores.map { it.key.name to it.value }
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
        outState.putInt("rollsLeft", rollsLeft)

        val scoresBundle = Bundle()
        for ((key, value) in scores) {
            scoresBundle.putInt(key.name, value)
        }
        outState.putBundle("scoresBundle", scoresBundle)
    }

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

enum class ScoreCategory(val value: Int) {
    LOW(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), ELEVEN(11), TWELVE(12)
}


