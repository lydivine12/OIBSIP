package com.example.quizapp.data;

import com.example.quizapp.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Source of truth for quiz content. Questions are hardcoded here rather than
 * loaded from JSON/network - swap this out for a JSON asset loader later
 * without touching any UI code, since callers only ever see List&lt;Question&gt;.
 */
public final class QuestionBank {

    private QuestionBank() {
        // no instances
    }

    private static final List<Question> ALL_QUESTIONS = buildQuestions();

    /** Returns a freshly shuffled copy of the full question set. */
    public static List<Question> getShuffledQuestions() {
        List<Question> copy = new ArrayList<>(ALL_QUESTIONS);
        Collections.shuffle(copy);
        return copy;
    }

    private static List<Question> buildQuestions() {
        List<Question> questions = new ArrayList<>();

        questions.add(new Question(
                "What is the capital of France?",
                new String[]{"Paris", "London", "Berlin", "Madrid"},
                0));

        questions.add(new Question(
                "Which planet is known as the Red Planet?",
                new String[]{"Venus", "Mars", "Jupiter", "Saturn"},
                1));

        questions.add(new Question(
                "Who wrote the play 'Romeo and Juliet'?",
                new String[]{"Charles Dickens", "Mark Twain", "William Shakespeare", "Jane Austen"},
                2));

        questions.add(new Question(
                "What is the largest ocean on Earth?",
                new String[]{"Atlantic Ocean", "Indian Ocean", "Arctic Ocean", "Pacific Ocean"},
                3));

        questions.add(new Question(
                "How many continents are there on Earth?",
                new String[]{"5", "6", "7", "8"},
                2));

        questions.add(new Question(
                "What is the chemical symbol for gold?",
                new String[]{"Go", "Gd", "Au", "Ag"},
                2));

        questions.add(new Question(
                "Which country is home to the kangaroo?",
                new String[]{"South Africa", "Australia", "Brazil", "India"},
                1));

        questions.add(new Question(
                "What is the smallest prime number?",
                new String[]{"0", "1", "2", "3"},
                2));

        questions.add(new Question(
                "Which gas do plants absorb from the atmosphere for photosynthesis?",
                new String[]{"Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen"},
                2));

        questions.add(new Question(
                "Who painted the Mona Lisa?",
                new String[]{"Vincent van Gogh", "Pablo Picasso", "Leonardo da Vinci", "Claude Monet"},
                2));

        questions.add(new Question(
                "What is the tallest mountain in the world?",
                new String[]{"K2", "Mount Kilimanjaro", "Mount Everest", "Denali"},
                2));

        questions.add(new Question(
                "In which year did World War II end?",
                new String[]{"1943", "1944", "1945", "1946"},
                2));

        questions.add(new Question(
                "What is the largest mammal in the world?",
                new String[]{"African Elephant", "Blue Whale", "Giraffe", "Polar Bear"},
                1));

        questions.add(new Question(
                "Which language has the most native speakers worldwide?",
                new String[]{"English", "Hindi", "Mandarin Chinese", "Spanish"},
                2));

        return questions;
    }
}
