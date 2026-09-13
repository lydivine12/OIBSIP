package com.example.quizapp.model;

/**
 * A single multiple-choice question: the prompt text, exactly four options,
 * and the index (0-3) of the correct option within {@link #options}.
 */
public class Question {

    private final String text;
    private final String[] options;
    private final int correctIndex;

    public Question(String text, String[] options, int correctIndex) {
        if (options == null || options.length != 4) {
            throw new IllegalArgumentException("Question requires exactly 4 options");
        }
        if (correctIndex < 0 || correctIndex > 3) {
            throw new IllegalArgumentException("correctIndex must be between 0 and 3");
        }
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
    }

    public String getText() {
        return text;
    }

    public String[] getOptions() {
        return options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public boolean isCorrect(int selectedIndex) {
        return selectedIndex == correctIndex;
    }
}
