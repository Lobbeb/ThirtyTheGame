package williamolssons.first.thirty_test_game

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity to display the result at the end of the game.
 */
class ResultActivity : AppCompatActivity() {

    /**
     * Called when the activity is starting.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Retrieve total score and round scores from the intent
        val totalScore = intent.getIntExtra("TOTAL_SCORE", 0)
        val roundScores = intent.getStringArrayListExtra("ROUND_SCORES") ?: arrayListOf()

        // Display total score
        val totalScoreTextView: TextView = findViewById(R.id.totalScoreTextView)
        totalScoreTextView.text = getString(R.string.total_score, totalScore)

        // Display round scores
        val roundScoresTextView: TextView = findViewById(R.id.roundScoresTextView)
        roundScoresTextView.text = roundScores.joinToString(separator = "\n")

        // Set up play again button to restart the game
        val playAgainButton: Button = findViewById(R.id.playAgainButton)
        playAgainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
