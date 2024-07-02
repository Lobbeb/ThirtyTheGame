package williamolssons.first.thirty_test_game

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * MainActivity handles the user interface and interaction for the Thirty game.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var game: ThirtyGame
    private lateinit var diceImageViews: List<ImageView>
    private lateinit var rollButton: Button
    private lateinit var scoreButton: Button
    private lateinit var scoreTextView: TextView
    private lateinit var roundTextView: TextView
    private lateinit var categorySpinner: Spinner
    private val usedCategories = mutableSetOf<ScoreCategory>()

    /**
     * Called when the activity is first created. Initializes the game and UI components.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeGame()
        initializeUI()
        setupListeners()
        restoreGameState(savedInstanceState)

        updateUI()
        rollDice()
    }

    /**
     * Initializes the game logic.
     */
    private fun initializeGame() {
        game = ThirtyGame()
    }

    /**
     * Initializes the UI components.
     */
    private fun initializeUI() {
        diceImageViews = listOf(
            findViewById(R.id.dice1),
            findViewById(R.id.dice2),
            findViewById(R.id.dice3),
            findViewById(R.id.dice4),
            findViewById(R.id.dice5),
            findViewById(R.id.dice6)
        )
        rollButton = findViewById(R.id.rollButton)
        scoreButton = findViewById(R.id.scoreButton)
        scoreTextView = findViewById(R.id.scoreTextView)
        roundTextView = findViewById(R.id.roundTextView)
        categorySpinner = findViewById(R.id.categorySpinner)

        setupCategorySpinner()
    }

    /**
     * Sets up the category spinner.
     */
    private fun setupCategorySpinner() {
        updateCategorySpinner()
    }

    /**
     * Updates the category spinner with available categories.
     */
    private fun updateCategorySpinner() {
        val categories = ScoreCategory.values().map { it.name }
        val adapter = CustomAdapter(this, android.R.layout.simple_spinner_item, categories, usedCategories.map { it.name }.toSet())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    /**
     * Sets up the event listeners for UI components.
     */
    private fun setupListeners() {
        rollButton.setOnClickListener { rollDice() }
        scoreButton.setOnClickListener { scoreRound() }
        diceImageViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener { toggleHoldDice(index) }
        }
    }

    /**
     * Saves the game state when the activity is destroyed.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        game.saveState(outState)
    }

    /**
     * Rolls the dice and updates the UI.
     */
    private fun rollDice() {
        game.rollDice()
        Log.d("MainActivity", "Rolled dice: ${game.dice.map { it.value }}")
        updateUI()
    }

    /**
     * Restores the game state if it was previously saved.
     */
    private fun restoreGameState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            game.restoreState(savedInstanceState)
        }
    }

    /**
     * Scores the current round based on the selected category and dice.
     */
    private fun scoreRound() {
        val selectedCategory = categorySpinner.selectedItem.toString()
        val category = ScoreCategory.valueOf(selectedCategory)
        val selectedDice = game.dice.filter { it.isHeld }

        try {
            Log.d("MainActivity", "Selected dice: ${selectedDice.map { it.value }} for category: $category")
            game.scoreRound(category, selectedDice)
            usedCategories.add(category)
            updateCategorySpinner()

            Log.d("MainActivity", "Scored round with category $category, current round: ${game.currentRound}, total score: ${game.getTotalScore()}")

            if (game.isGameOver()) {
                val totalScore = game.getTotalScore()
                val roundScores = game.getRoundScores()
                val roundScoresList = ArrayList<String>()
                for (round in roundScores) {
                    roundScoresList.add("${round.first}: ${round.second}")
                }
                Log.d("MainActivity", "Game over. Transitioning to ResultActivity with total score $totalScore and round scores ${roundScoresList.joinToString(", ")}")

                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtra("TOTAL_SCORE", totalScore)
                intent.putStringArrayListExtra("ROUND_SCORES", roundScoresList)
                startActivity(intent)
            } else {
                game.nextRound()
                rollDice() // Roll dice for the next round
                updateUI()
                selectNextAvailableCategory()
            }
        } catch (e: IllegalArgumentException) {
            Log.e("MainActivity", "Error: ${e.message}")
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Selects the next available category in the spinner.
     */
    private fun selectNextAvailableCategory() {
        val categories = ScoreCategory.values().map { it.name }
        val nextAvailableCategory = categories.firstOrNull { !usedCategories.contains(ScoreCategory.valueOf(it)) }
        nextAvailableCategory?.let {
            categorySpinner.setSelection(categories.indexOf(it))
        }
    }

    /**
     * Toggles the hold state of a die.
     */
    private fun toggleHoldDice(index: Int) {
        game.dice[index].isHeld = !game.dice[index].isHeld
        Log.d("MainActivity", "Toggled hold for dice $index: now held = ${game.dice[index].isHeld}")
        updateUI()
    }

    /**
     * Updates the UI to reflect the current game state.
     */
    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        for (i in diceImageViews.indices) {
            diceImageViews[i].setImageResource(getDiceResource(game.dice[i].value))
            diceImageViews[i].alpha = if (game.dice[i].isHeld) 0.5f else 1.0f
        }
        scoreTextView.text = "Score: ${game.getCurrentScore()}"
        roundTextView.text = "Round: ${game.currentRound + 1}"
        Log.d("MainActivity", "Updated UI: Round ${game.currentRound + 1}, Score ${game.getCurrentScore()}")
    }

    /**
     * Returns the drawable resource ID for a given dice value.
     */
    private fun getDiceResource(value: Int): Int {
        return when (value) {
            1 -> R.drawable.white1
            2 -> R.drawable.white2
            3 -> R.drawable.white3
            4 -> R.drawable.white4
            5 -> R.drawable.white5
            6 -> R.drawable.white6
            else -> R.drawable.white1
        }
    }
}
