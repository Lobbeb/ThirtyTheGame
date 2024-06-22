package williamolssons.first.thirty_test_game

data class Dice(var value: Int = (1..6).random(), var isHeld: Boolean = false)
