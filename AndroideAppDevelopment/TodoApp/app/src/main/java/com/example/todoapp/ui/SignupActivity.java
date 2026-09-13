package com.example.todoapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp.R;
import com.example.todoapp.db.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private TextInputLayout tilName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private DatabaseHelper dbHelper;
    private com.example.todoapp.util.SessionManager sessionManager;

    /** Prevents duplicate account creation if "Register" is tapped rapidly multiple times. */
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new com.example.todoapp.util.SessionManager(this);

        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        findViewById(R.id.btnRegister).setOnClickListener(v -> attemptRegister());
        findViewById(R.id.tvGoToLogin).setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        if (isSubmitting) {
            return;
        }

        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String name = safeText(etName);
        String email = safeText(etEmail);
        String password = safeText(etPassword);
        String confirmPassword = safeText(etConfirmPassword);

        boolean valid = true;

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Name is required");
            valid = false;
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Password is required");
            valid = false;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            tilPassword.setError("Use at least " + MIN_PASSWORD_LENGTH + " characters");
            valid = false;
        }
        if (!TextUtils.equals(password, confirmPassword)) {
            tilConfirmPassword.setError("Passwords don't match");
            valid = false;
        }

        if (!valid) {
            return;
        }

        isSubmitting = true;
        try {
            long[] outUserId = new long[1];
            DatabaseHelper.RegisterResult result = dbHelper.registerUser(name, email, password, outUserId);

            switch (result) {
                case SUCCESS:
                    sessionManager.createSession(outUserId[0], name, email.trim().toLowerCase());
                    Intent intent = new Intent(SignupActivity.this, TaskListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    break;
                case EMAIL_TAKEN:
                    tilEmail.setError("An account with this email already exists");
                    break;
                case ERROR:
                default:
                    Toast.makeText(this, "Couldn't create your account. Please try again.", Toast.LENGTH_SHORT).show();
                    break;
            }
        } finally {
            isSubmitting = false;
        }
    }

    private String safeText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
