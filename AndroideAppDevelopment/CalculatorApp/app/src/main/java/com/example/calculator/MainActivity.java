package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * A clean, crash-resistant calculator.
 *
 * State model:
 *  - currentInput: the string currently shown on the main display (the number being typed,
 *    or the last computed result).
 *  - previousValue: the operand stored when an operator was pressed.
 *  - pendingOperator: the operator waiting to be applied ( + - × ÷ ), or null.
 *  - startNewInput: true when the next digit press should start a fresh number
 *    (e.g. right after an operator or after "=").
 *  - errorState: true after a division-by-zero or parse failure; any new input clears it.
 *
 * All handlers are defensive (try/catch + state guards) so rapid or repeated taps
 * can never throw an uncaught exception or leave the UI in an inconsistent state.
 */
public class MainActivity extends AppCompatActivity {

    private static final int MAX_DISPLAY_LENGTH = 15;

    private TextView tvDisplay;
    private TextView tvExpression;

    private String currentInput = "0";
    private BigDecimal previousValue = null;
    private Character pendingOperator = null;
    private boolean startNewInput = true;
    private boolean errorState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplay = findViewById(R.id.tvDisplay);
        tvExpression = findViewById(R.id.tvExpression);

        setupNumberButton(R.id.btn0, "0");
        setupNumberButton(R.id.btn1, "1");
        setupNumberButton(R.id.btn2, "2");
        setupNumberButton(R.id.btn3, "3");
        setupNumberButton(R.id.btn4, "4");
        setupNumberButton(R.id.btn5, "5");
        setupNumberButton(R.id.btn6, "6");
        setupNumberButton(R.id.btn7, "7");
        setupNumberButton(R.id.btn8, "8");
        setupNumberButton(R.id.btn9, "9");

        Button btnDecimal = findViewById(R.id.btnDecimal);
        btnDecimal.setOnClickListener(v -> safely(this::onDecimalPressed));

        Button btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> safely(() -> onOperatorPressed('+')));

        Button btnSubtract = findViewById(R.id.btnSubtract);
        btnSubtract.setOnClickListener(v -> safely(() -> onOperatorPressed('-')));

        Button btnMultiply = findViewById(R.id.btnMultiply);
        btnMultiply.setOnClickListener(v -> safely(() -> onOperatorPressed('×')));

        Button btnDivide = findViewById(R.id.btnDivide);
        btnDivide.setOnClickListener(v -> safely(() -> onOperatorPressed('÷')));

        Button btnEquals = findViewById(R.id.btnEquals);
        btnEquals.setOnClickListener(v -> safely(this::onEqualsPressed));

        Button btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> safely(this::onClearPressed));

        Button btnBackspace = findViewById(R.id.btnBackspace);
        btnBackspace.setOnClickListener(v -> safely(this::onBackspacePressed));

        updateDisplay();
    }

    // ---- Wiring helpers -----------------------------------------------------------------

    private void setupNumberButton(int id, String digit) {
        Button b = findViewById(id);
        b.setOnClickListener(v -> safely(() -> onDigitPressed(digit)));
    }

    /**
     * Runs a UI action defensively. Every calculator button funnels through here so that
     * no malformed state (e.g. from very fast repeated taps) can ever crash the app -
     * on any unexpected failure we fall back to a clean "Error" state instead of throwing.
     */
    private interface Action {
        void run();
    }

    private void safely(Action action) {
        try {
            action.run();
        } catch (Exception e) {
            enterErrorState();
        }
        updateDisplay();
    }

    // ---- Button handlers ------------------------------------------------------------------

    private void onDigitPressed(String digit) {
        if (errorState) {
            resetAll();
        }

        if (startNewInput) {
            currentInput = digit;
            startNewInput = false;
        } else {
            if (currentInput.equals("0")) {
                currentInput = digit;
            } else if (currentInput.length() < MAX_DISPLAY_LENGTH) {
                currentInput = currentInput + digit;
            }
            // silently ignore extra digits once we hit the max length, rather than crashing
            // or overflowing the display
        }
    }

    private void onDecimalPressed() {
        if (errorState) {
            resetAll();
        }

        if (startNewInput) {
            currentInput = "0.";
            startNewInput = false;
        } else if (!currentInput.contains(".")) {
            currentInput = currentInput + ".";
        }
    }

    private void onOperatorPressed(char op) {
        if (errorState) {
            // an operator right after an error just clears it silently; user needs a fresh start
            resetAll();
            return;
        }

        if (pendingOperator != null && !startNewInput) {
            // chain: apply the pending operation first, e.g. 3 + 4 + -> compute 7, then continue
            boolean ok = applyPendingOperator();
            if (!ok) {
                return; // already entered error state inside applyPendingOperator
            }
        } else {
            previousValue = safeParse(currentInput);
            if (previousValue == null) {
                enterErrorState();
                return;
            }
        }

        pendingOperator = op;
        startNewInput = true;
        updateExpressionLabel();
    }

    private void onEqualsPressed() {
        if (errorState) {
            resetAll();
            return;
        }
        if (pendingOperator == null || previousValue == null) {
            // nothing to evaluate; ignore
            return;
        }
        applyPendingOperator();
        pendingOperator = null;
        startNewInput = true;
        tvExpression.setText("");
    }

    private void onClearPressed() {
        resetAll();
    }

    private void onBackspacePressed() {
        if (errorState) {
            resetAll();
            return;
        }
        if (startNewInput) {
            // nothing meaningful to delete from a fresh/result state; just show 0
            currentInput = "0";
            startNewInput = true;
            return;
        }
        if (currentInput.length() <= 1
                || (currentInput.length() == 2 && currentInput.charAt(0) == '-')) {
            currentInput = "0";
            startNewInput = true;
        } else {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
        }
    }

    // ---- Core math ------------------------------------------------------------------------

    /**
     * Applies previousValue (pendingOperator) currentInput -> result.
     * Returns true on success; on failure (bad parse or divide-by-zero) puts the
     * calculator into a clean error state and returns false.
     */
    private boolean applyPendingOperator() {
        BigDecimal operand = safeParse(currentInput);
        if (operand == null || previousValue == null || pendingOperator == null) {
            enterErrorState();
            return false;
        }

        BigDecimal result;
        try {
            switch (pendingOperator) {
                case '+':
                    result = previousValue.add(operand);
                    break;
                case '-':
                    result = previousValue.subtract(operand);
                    break;
                case '×':
                    result = previousValue.multiply(operand);
                    break;
                case '÷':
                    if (operand.compareTo(BigDecimal.ZERO) == 0) {
                        enterErrorState();
                        return false;
                    }
                    result = previousValue.divide(operand, MathContext.DECIMAL64);
                    break;
                default:
                    enterErrorState();
                    return false;
            }
        } catch (ArithmeticException e) {
            enterErrorState();
            return false;
        }

        currentInput = formatResult(result);
        previousValue = safeParse(currentInput);
        return true;
    }

    private BigDecimal safeParse(String value) {
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatResult(BigDecimal value) {
        try {
            BigDecimal rounded = value.round(new MathContext(12, RoundingMode.HALF_UP));
            rounded = rounded.stripTrailingZeros();
            String plain = rounded.toPlainString();
            if (plain.length() > MAX_DISPLAY_LENGTH) {
                // fall back to scientific notation for very large/small numbers
                return String.format("%." + 6 + "e", value.doubleValue());
            }
            return plain;
        } catch (Exception e) {
            return "Error";
        }
    }

    // ---- State helpers ----------------------------------------------------------------------

    private void enterErrorState() {
        errorState = true;
        currentInput = "Error";
        previousValue = null;
        pendingOperator = null;
        startNewInput = true;
        tvExpression.setText("");
    }

    private void resetAll() {
        currentInput = "0";
        previousValue = null;
        pendingOperator = null;
        startNewInput = true;
        errorState = false;
        tvExpression.setText("");
    }

    private void updateExpressionLabel() {
        if (previousValue != null && pendingOperator != null) {
            tvExpression.setText(formatResult(previousValue) + " " + pendingOperator);
        }
    }

    private void updateDisplay() {
        tvDisplay.setText(currentInput);
    }
}
