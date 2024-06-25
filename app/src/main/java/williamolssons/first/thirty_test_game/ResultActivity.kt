package williamolssons.first.thirty_test_game

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * ResultActivity displays the total score and round scores at the end of the game.
 */
class ResultActivity : AppCompatActivity() {

    /**
     * Called when the activity is first created.
     * Initializes the UI elements and displays the scores.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Retrieve total score and round scores from the intent
        val totalScore = intent.getIntExtra("TOTAL_SCORE", 0)
        val roundScores = intent.getIntArrayExtra("ROUND_SCORES") ?: intArrayOf()

        // Display total score
        val totalScoreTextView: TextView = findViewById(R.id.totalScoreTextView)
        totalScoreTextView.text = getString(R.string.total_score, totalScore)

        // Display scores for each round
        val roundScoresTextView: TextView = findViewById(R.id.roundScoresTextView)
        roundScoresTextView.text = roundScores.withIndex().joinToString(separator = "\n") { (index, score) ->
            "Round ${index + 1}: $score"
        }

        // Set up play again button to start a new game
        val playAgainButton: Button = findViewById(R.id.playAgainButton)
        playAgainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
