package com.example.quizapp.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.quizapp.R;
import com.example.quizapp.data.QuizManager;
import com.example.quizapp.model.Question;
import com.google.android.material.button.MaterialButton;

public class QuizActivity extends AppCompatActivity {

    private QuizManager quizManager;

    private TextView tvQuestionCounter;
    private TextView tvScore;
    private TextView tvQuestionText;
    private ProgressBar progressBar;
    private MaterialButton[] optionButtons;
    private MaterialButton btnNext;

    /** True while an answer/next transition is in flight, to swallow rapid repeat taps. */
    private boolean isTransitioning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        quizManager = QuizManager.getInstance();

        tvQuestionCounter = findViewById(R.id.tvQuestionCounter);
        tvScore = findViewById(R.id.tvScore);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        progressBar = findViewById(R.id.progressBar);
        btnNext = findViewById(R.id.btnNext);

        optionButtons = new MaterialButton[]{
                findViewById(R.id.btnOption0),
                findViewById(R.id.btnOption1),
                findViewById(R.id.btnOption2),
                findViewById(R.id.btnOption3)
        };

        for (int i = 0; i < optionButtons.length; i++) {
            final int index = i;
            optionButtons[i].setOnClickListener(v -> onOptionSelected(index));
        }

        btnNext.setOnClickListener(v -> onNextClicked());

        // Defensive: if this Activity is somehow entered with no questions loaded
        // (e.g. process death restored an odd state), bail out to the welcome
        // screen instead of crashing on a null question.
        if (quizManager.getCurrentQuestion() == null) {
            goToWelcome();
            return;
        }

        renderCurrentQuestion();
    }

    private void onOptionSelected(int selectedIndex) {
        if (isTransitioning || quizManager.isCurrentQuestionAnswered()) {
            return; // already answered / mid-transition - ignore rapid repeat taps
        }
        isTransitioning = true;

        Question question = quizManager.getCurrentQuestion();
        if (question == null) {
            isTransitioning = false;
            return;
        }

        boolean correct = quizManager.submitAnswer(selectedIndex);
        showAnswerFeedback(selectedIndex, question.getCorrectIndex());
        updateScoreLabel();

        boolean isLast = quizManager.isLastQuestion();
        btnNext.setText(isLast ? R.string.btn_finish : R.string.btn_next);
        btnNext.setEnabled(true);
        btnNext.setAlpha(1f);

        isTransitioning = false;
    }

    private void showAnswerFeedback(int selectedIndex, int correctIndex) {
        int correctBg = ContextCompat.getColor(this, R.color.option_correct_bg);
        int incorrectBg = ContextCompat.getColor(this, R.color.option_incorrect_bg);
        int white = ContextCompat.getColor(this, R.color.white);
        int dimmedText = ContextCompat.getColor(this, R.color.option_dimmed);

        for (int i = 0; i < optionButtons.length; i++) {
            MaterialButton button = optionButtons[i];
            button.setEnabled(false); // lock all options once one has been chosen

            if (i == correctIndex) {
                // Always reveal the correct answer in green, whether or not it was picked.
                button.setBackgroundTintList(ColorStateList.valueOf(correctBg));
                button.setTextColor(white);
                button.setStrokeWidth(0);
            } else if (i == selectedIndex) {
                // The user's wrong pick, highlighted red.
                button.setBackgroundTintList(ColorStateList.valueOf(incorrectBg));
                button.setTextColor(white);
                button.setStrokeWidth(0);
            } else {
                // Every other option just dims slightly so the feedback stands out.
                button.setTextColor(dimmedText);
            }
        }
    }

    private void onNextClicked() {
        if (isTransitioning) {
            return;
        }
        isTransitioning = true;

        boolean advanced = quizManager.moveToNextQuestion();
        if (advanced) {
            renderCurrentQuestion();
            isTransitioning = false;
        } else {
            goToResults();
            // isTransitioning intentionally left true; this Activity is finishing.
        }
    }

    private void renderCurrentQuestion() {
        Question question = quizManager.getCurrentQuestion();
        if (question == null) {
            goToResults();
            return;
        }

        tvQuestionCounter.setText(getString(
                R.string.question_counter_format,
                quizManager.getCurrentQuestionNumber(),
                quizManager.getTotalQuestions()));

        int progress = Math.round(
                (quizManager.getCurrentQuestionNumber() - 1) * 100f / quizManager.getTotalQuestions());
        progressBar.setProgress(progress);

        updateScoreLabel();

        tvQuestionText.setText(question.getText());

        String[] options = question.getOptions();
        int defaultBg = ContextCompat.getColor(this, R.color.option_default_bg);
        int defaultText = ContextCompat.getColor(this, R.color.option_default_text);
        int defaultStroke = ContextCompat.getColor(this, R.color.option_default_stroke);

        for (int i = 0; i < optionButtons.length; i++) {
            MaterialButton button = optionButtons[i];
            button.setText(options[i]);
            button.setEnabled(true);
            button.setBackgroundTintList(ColorStateList.valueOf(defaultBg));
            button.setTextColor(defaultText);
            button.setStrokeWidth(dpToPx(1));
            button.setStrokeColor(ColorStateList.valueOf(defaultStroke));
        }

        btnNext.setEnabled(false);
        btnNext.setAlpha(0.5f);
        btnNext.setText(R.string.btn_next);
    }

    private void updateScoreLabel() {
        tvScore.setText(getString(R.string.score_label_format, quizManager.getCorrectCount()));
    }

    private void goToResults() {
        startActivity(new Intent(QuizActivity.this, ResultsActivity.class));
        finish();
    }

    private void goToWelcome() {
        startActivity(new Intent(QuizActivity.this, WelcomeActivity.class));
        finish();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
