# Calculator (Android, Java)

A clean, dark-themed calculator app built with Java + XML.

## How to open
1. Unzip the project.
2. Open Android Studio → **File → Open** → select the `CalculatorApp` folder.
3. Let Gradle sync (Android Studio will offer to regenerate the Gradle wrapper jar
   automatically the first time — accept it, or run `gradle wrapper` once if you have
   Gradle installed locally).
4. Run on an emulator or device (minSdk 21 / Android 5.0+).

## Project structure
```
app/src/main/java/com/example/calculator/MainActivity.java   - all calculator logic
app/src/main/res/layout/activity_main.xml                    - display + button grid (ConstraintLayout + GridLayout)
app/src/main/res/values/{colors,strings,styles,themes}.xml   - theming
app/src/main/res/drawable/                                   - rounded button backgrounds + app icon
```

## Feature checklist
- [x] Display TextView (`tvDisplay`) showing current input/result, plus a smaller
      `tvExpression` line showing the pending calculation (e.g. "12 +")
- [x] Number buttons 0–9 and decimal point, with duplicate-decimal protection
- [x] Operator buttons: + − × ÷
- [x] Equals (=) button evaluates the pending operation
- [x] Clear (C) resets all state
- [x] Backspace (⌫) deletes the last typed character
- [x] Division by zero shows "Error" and the next tap cleanly resets the calculator
- [x] Responsive 4×5 GridLayout inside a ConstraintLayout, evenly weighted so it
      adapts to different screen sizes
- [x] No crashes on rapid/repeated taps — every button handler routes through a
      single `safely()` wrapper that catches exceptions and falls back to a clean
      "Error" state instead of throwing, and all state transitions are guarded
      (e.g. backspace/operator presses check `errorState` and `startNewInput`
      before touching the string being built)

## Design notes
- Uses `BigDecimal` instead of `double` for arithmetic to avoid floating-point
  artifacts (e.g. `0.1 + 0.2` showing as `0.30000000000000004`).
- Chained operations work as you'd expect on a basic calculator:
  `5 + 3 + 2 =` computes left-to-right (8, then 10) rather than requiring
  parentheses.
- Results are trimmed of trailing zeros and capped at 12 significant digits;
  very large/small results fall back to scientific notation rather than
  overflowing the display.
