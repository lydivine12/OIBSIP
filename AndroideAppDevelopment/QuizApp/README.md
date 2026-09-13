# QuizApp (Android, Java)

A general-knowledge multiple-choice quiz: welcome screen → 14 shuffled
questions → results screen, with immediate green/red feedback on each answer.

## How to open
1. Unzip the project.
2. Open Android Studio → **File → Open** → select the `QuizApp` folder.
3. Let Gradle sync (accept the prompt to regenerate the Gradle wrapper jar the
   first time, or run `gradle wrapper` once if you have Gradle installed).
4. Run on an emulator or device (minSdk 21 / Android 5.0+).

## Project structure
```
model/Question.java        - question text + 4 options + correct index
data/QuestionBank.java      - 14 hardcoded general-knowledge questions
data/QuizManager.java       - singleton: shuffled order, current index, score

ui/WelcomeActivity.java     - Start button
ui/QuizActivity.java        - question + 4 answer buttons + feedback + Next
ui/ResultsActivity.java     - final score, correct/incorrect, Restart

res/layout/                 - activity_welcome, activity_quiz, activity_results
```

## Feature checklist
- [x] Welcome screen with a Start button
- [x] Question screen: question text, 4 answer buttons, "Question X of N"
      counter, and a progress bar
- [x] 14 hardcoded questions (more than the required 10) in `QuestionBank`
- [x] Immediate feedback: tapping an option locks all four buttons; the
      correct one turns green and, if you picked wrong, your pick turns red
      (the other two dim slightly so the feedback stands out)
- [x] "Next" button (disabled until you've answered) advances to the next
      question; on the last question it becomes "Finish"
- [x] Running score shown during the quiz ("Score: 3") and tracked via
      `QuizManager`
- [x] Results screen: score as a percentage, correct count, incorrect count,
      and a "Restart Quiz" button
- [x] Questions are shuffled into a new random order every time you start or
      restart the quiz (`Collections.shuffle` in `QuestionBank`)

## Design notes
- **Question source**: `QuestionBank` returns `List<Question>`, so it's a
  drop-in swap to load from a JSON asset or network later without touching
  any Activity code.
- **State**: `QuizManager` is a simple singleton holding the current attempt
  (shuffled questions, index, running score). It intentionally isn't
  persisted across process death - closing the app mid-quiz just abandons
  that attempt, which is expected for a quiz like this.
- **Answer locking**: `QuizManager.submitAnswer()` only accepts one answer
  per question (`currentQuestionAnswered` flag), and `QuizActivity` disables
  all four option buttons the instant one is tapped - so rapid/double taps
  can't double-count a score or corrupt the feedback colors. The same
  in-flight guard pattern protects the Start and Restart buttons.
- **Navigation**: each screen `finish()`es itself when moving forward, so the
  back stack never lets you navigate back into a completed question or a
  stale results screen.
