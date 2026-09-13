package com.example.quizapp.data;

import com.example.quizapp.model.Question;

import java.util.List;

/**
 * In-memory session state for a single quiz attempt: the shuffled question
 * order, which question the user is on, and running correct/incorrect
 * counts. A plain singleton is enough here since the whole app is one
 * process and there's no persistence requirement for in-progress quizzes -
 * closing the app simply abandons the attempt.
 *
 * Not thread-safe by design: all calls are expected from the main/UI thread,
 * same as the Activities that own the quiz flow.
 */
public final class QuizManager {

    private static final QuizManager INSTANCE = new QuizManager();

    private List<Question> questions;
    private int currentIndex;
    private int correctCount;
    private int incorrectCount;

    /** True once the current question has been answered, to guard against double submits. */
    private boolean currentQuestionAnswered;

    private QuizManager() {
        // Start with an empty/finished state until startNewQuiz() is called.
        this.questions = QuestionBank.getShuffledQuestions();
        this.currentIndex = 0;
        this.correctCount = 0;
        this.incorrectCount = 0;
        this.currentQuestionAnswered = false;
    }

    public static QuizManager getInstance() {
        return INSTANCE;
    }

    /** Re-shuffles the question bank and resets all progress. Call this before each attempt. */
    public void startNewQuiz() {
        questions = QuestionBank.getShuffledQuestions();
        currentIndex = 0;
        correctCount = 0;
        incorrectCount = 0;
        currentQuestionAnswered = false;
    }

    public Question getCurrentQuestion() {
        if (questions.isEmpty() || currentIndex < 0 || currentIndex >= questions.size()) {
            return null;
        }
        return questions.get(currentIndex);
    }

    public int getTotalQuestions() {
        return questions.size();
    }

    /** 1-based question number for display, e.g. "Question 3 of 10". */
    public int getCurrentQuestionNumber() {
        return currentIndex + 1;
    }

    public boolean isLastQuestion() {
        return currentIndex >= questions.size() - 1;
    }

    public boolean isCurrentQuestionAnswered() {
        return currentQuestionAnswered;
    }

    /**
     * Records the user's answer for the current question. Safe to call only once per
     * question - subsequent calls before advancing are ignored so a rapid double-tap
     * on an answer button can't double-count the score.
     *
     * @return true if the selected option was correct.
     */
    public boolean submitAnswer(int selectedIndex) {
        Question current = getCurrentQuestion();
        if (current == null || currentQuestionAnswered) {
            return false;
        }
        currentQuestionAnswered = true;

        boolean correct = current.isCorrect(selectedIndex);
        if (correct) {
            correctCount++;
        } else {
            incorrectCount++;
        }
        return correct;
    }

    /**
     * Moves to the next question. Returns false (and does nothing) if already on the
     * last question or the current question hasn't been answered yet.
     */
    public boolean moveToNextQuestion() {
        if (!currentQuestionAnswered || isLastQuestion()) {
            return false;
        }
        currentIndex++;
        currentQuestionAnswered = false;
        return true;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public int getIncorrectCount() {
        return incorrectCount;
    }

    /** Percentage score (0-100), rounded to the nearest whole number. */
    public int getScorePercent() {
        int total = questions.size();
        if (total == 0) {
            return 0;
        }
        return Math.round((correctCount * 100f) / total);
    }
}
