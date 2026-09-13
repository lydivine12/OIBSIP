package com.example.todoapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp.R;
import com.example.todoapp.db.DatabaseHelper;
import com.example.todoapp.model.User;
import com.example.todoapp.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    /** Prevents duplicate submissions if the user taps "Log In" rapidly multiple times. */
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        dbHelper = DatabaseHelper.getInstance(this);

        // Already logged in from a previous launch -> skip straight to the task list.
        if (sessionManager.isLoggedIn()) {
            goToTaskList();
            return;
        }

        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        findViewById(R.id.btnLogin).setOnClickListener(v -> attemptLogin());
        findViewById(R.id.tvGoToSignup).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    private void attemptLogin() {
        if (isSubmitting) {
            return; // guard against rapid double-taps re-entering this logic
        }

        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = safeText(etEmail);
        String password = safeText(etPassword);

        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.hint_email) + " is required");
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.hint_password) + " is required");
            valid = false;
        }
        if (!valid) {
            return;
        }

        isSubmitting = true;
        try {
            User user = dbHelper.verifyLogin(email, password);
            if (user == null) {
                tilPassword.setError("Incorrect email or password");
                return;
            }
            sessionManager.createSession(user.getId(), user.getName(), user.getEmail());
            goToTaskList();
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
        } finally {
            isSubmitting = false;
        }
    }

    private void goToTaskList() {
        Intent intent = new Intent(LoginActivity.this, TaskListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String safeText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
