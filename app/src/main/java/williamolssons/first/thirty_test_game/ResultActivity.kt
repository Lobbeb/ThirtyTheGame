package williamolssons.first.thirty_test_game

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val totalScore = intent.getIntExtra("TOTAL_SCORE", 0)
        val roundScores = intent.getStringArrayListExtra("ROUND_SCORES") ?: arrayListOf()

        val totalScoreTextView: TextView = findViewById(R.id.totalScoreTextView)
        totalScoreTextView.text = getString(R.string.total_score, totalScore)

        val roundScoresTextView: TextView = findViewById(R.id.roundScoresTextView)
        roundScoresTextView.text = roundScores.joinToString(separator = "\n")

        val playAgainButton: Button = findViewById(R.id.playAgainButton)
        playAgainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
