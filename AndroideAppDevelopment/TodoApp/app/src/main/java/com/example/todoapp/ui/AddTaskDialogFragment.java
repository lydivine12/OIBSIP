package com.example.todoapp.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.todoapp.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Dialog for creating a new task. Validation (non-empty title) happens
 * before the positive button is allowed to dismiss the dialog, so a stray
 * extra tap can't submit a blank task.
 */
public class AddTaskDialogFragment extends DialogFragment {

    public interface OnTaskSavedListener {
        void onTaskSaved(String title, String notes);
    }

    private OnTaskSavedListener listener;
    private TextInputLayout tilTaskName;
    private TextInputEditText etTaskName;
    private TextInputEditText etTaskNotes;

    public void setOnTaskSavedListener(OnTaskSavedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null);
        tilTaskName = view.findViewById(R.id.tilTaskName);
        etTaskName = view.findViewById(R.id.etTaskName);
        etTaskNotes = view.findViewById(R.id.etTaskNotes);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_task_title)
                .setView(view)
                .setPositiveButton(R.string.btn_save, null) // wired manually below to allow validation
                .setNegativeButton(R.string.btn_cancel, (d, which) -> dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            View positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> trySave());
        });

        return dialog;
    }

    private void trySave() {
        String title = etTaskName.getText() == null ? "" : etTaskName.getText().toString().trim();
        String notes = etTaskNotes.getText() == null ? "" : etTaskNotes.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            tilTaskName.setError(getString(R.string.hint_task_name) + " is required");
            return;
        }
        tilTaskName.setError(null);

        if (listener != null) {
            listener.onTaskSaved(title, notes);
        }
        dismiss();
    }
}
