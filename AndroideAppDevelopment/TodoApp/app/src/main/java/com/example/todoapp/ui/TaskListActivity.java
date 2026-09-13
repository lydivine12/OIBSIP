package com.example.todoapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todoapp.R;
import com.example.todoapp.db.DatabaseHelper;
import com.example.todoapp.model.Task;
import com.example.todoapp.util.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class TaskListActivity extends AppCompatActivity implements TaskAdapter.Listener {

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private TaskAdapter adapter;

    private RecyclerView rvTasks;
    private View emptyState;

    /** Guards against a rapid double-tap opening two delete confirmation dialogs at once. */
    private boolean isDeleteDialogShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Safety net: if there's somehow no active session (e.g. deep link, restored
        // task without state), bounce back to the login screen instead of crashing
        // on a missing user id.
        if (!sessionManager.isLoggedIn()) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_task_list);
        dbHelper = DatabaseHelper.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rvTasks = findViewById(R.id.rvTasks);
        emptyState = findViewById(R.id.emptyState);

        adapter = new TaskAdapter(this);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        rvTasks.setAdapter(adapter);

        FloatingActionButton fabAddTask = findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(v -> showAddTaskDialog());

        refreshTasks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-check on every resume in case the session was cleared elsewhere.
        if (!sessionManager.isLoggedIn()) {
            goToLogin();
            return;
        }
        refreshTasks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            confirmLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_logout)
                .setMessage("You'll need to log in again to see your tasks.")
                .setPositiveButton(R.string.action_logout, (dialog, which) -> doLogout())
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void doLogout() {
        sessionManager.logout();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(TaskListActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showAddTaskDialog() {
        AddTaskDialogFragment dialog = new AddTaskDialogFragment();
        dialog.setOnTaskSavedListener((title, notes) -> {
            long userId = sessionManager.getUserId();
            dbHelper.addTask(userId, title, notes);
            refreshTasks();
        });
        dialog.show(getSupportFragmentManager(), "add_task");
    }

    private void refreshTasks() {
        long userId = sessionManager.getUserId();
        if (userId == -1L) {
            goToLogin();
            return;
        }

        List<Task> tasks = dbHelper.getTasksForUser(userId);
        adapter.submitList(tasks);

        boolean isEmpty = adapter.getTaskCount() == 0;
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // ---- TaskAdapter.Listener ---------------------------------------------------------

    @Override
    public void onTaskCheckedChanged(Task task, boolean isChecked) {
        dbHelper.setTaskCompleted(task.getId(), isChecked);
        // Re-query so completed tasks settle to the bottom of the list, matching
        // the sort order used for a fresh load.
        refreshTasks();
    }

    @Override
    public void onTaskDeleteClicked(Task task) {
        if (isDeleteDialogShowing) {
            return; // ignore a rapid repeat tap while a confirmation is already up
        }
        isDeleteDialogShowing = true;

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_task_title)
                .setMessage(R.string.delete_task_message)
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    boolean deleted = dbHelper.deleteTask(task.getId());
                    if (!deleted) {
                        Toast.makeText(this, "Couldn't delete that task.", Toast.LENGTH_SHORT).show();
                    }
                    refreshTasks();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .setOnDismissListener(d -> isDeleteDialogShowing = false)
                .show();
    }
}
