package com.example.quizapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.R;
import com.example.quizapp.data.QuizManager;

public class ResultsActivity extends AppCompatActivity {

    private QuizManager quizManager;

    /** Guards against a rapid double-tap starting two new quiz attempts at once. */
    private boolean isRestarting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        quizManager = QuizManager.getInstance();

        TextView tvScorePercent = findViewById(R.id.tvScorePercent);
        TextView tvCorrectCount = findViewById(R.id.tvCorrectCount);
        TextView tvIncorrectCount = findViewById(R.id.tvIncorrectCount);

        tvScorePercent.setText(getString(R.string.results_score_format, quizManager.getScorePercent()));
        tvCorrectCount.setText(String.valueOf(quizManager.getCorrectCount()));
        tvIncorrectCount.setText(String.valueOf(quizManager.getIncorrectCount()));

        findViewById(R.id.btnRestart).setOnClickListener(v -> {
            if (isRestarting) {
                return;
            }
            isRestarting = true;

            quizManager.startNewQuiz();
            startActivity(new Intent(ResultsActivity.this, QuizActivity.class));
            // Finish so back-navigation from the new attempt goes to Welcome, not
            // back into this now-stale results screen.
            finish();
        });
    }
}
