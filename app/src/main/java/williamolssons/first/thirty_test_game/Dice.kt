package williamolssons.first.thirty_test_game

/**
 * Data class representing a single die in the game.
 * @property value The current value of the die, randomly initialized between 1 and 6.
 * @property isHeld Indicates whether the die is held and should not be rerolled.
 */
data class Dice(var value: Int = (1..6).random(), var isHeld: Boolean = false)
