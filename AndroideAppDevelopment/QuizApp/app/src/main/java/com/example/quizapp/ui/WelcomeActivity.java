package com.example.quizapp.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp.R;
import com.example.quizapp.data.QuizManager;

public class WelcomeActivity extends AppCompatActivity {

    /** Guards against a rapid double-tap launching the quiz twice. */
    private boolean isStarting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        findViewById(R.id.btnStart).setOnClickListener(v -> {
            if (isStarting) {
                return;
            }
            isStarting = true;

            QuizManager.getInstance().startNewQuiz();
            startActivity(new Intent(WelcomeActivity.this, QuizActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset the guard whenever this screen becomes visible again (e.g. user
        // backed out of the quiz), so Start works normally next time.
        isStarting = false;
    }
}
