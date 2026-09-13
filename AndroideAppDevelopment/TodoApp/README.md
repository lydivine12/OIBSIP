# TodoApp (Android, Java + SQLite)

A local, account-based to-do list app: users register/log in, then manage a
personal task list stored in SQLite.

## How to open
1. Unzip the project.
2. Open Android Studio → **File → Open** → select the `TodoApp` folder.
3. Let Gradle sync (accept the prompt to regenerate the Gradle wrapper jar the
   first time, or run `gradle wrapper` once if you have Gradle installed).
4. Run on an emulator or device (minSdk 21 / Android 5.0+).

## Project structure
```
ui/LoginActivity.java          - email + password login
ui/SignupActivity.java         - name, email, password, confirm password
ui/TaskListActivity.java       - main screen: task list, add/complete/delete, logout
ui/AddTaskDialogFragment.java  - "Add Task" dialog (name + optional notes)
ui/TaskAdapter.java            - RecyclerView adapter (strikethrough on completion)

db/DatabaseHelper.java         - SQLiteOpenHelper: users + tasks tables, all CRUD

model/User.java, model/Task.java

util/PasswordUtils.java        - salted SHA-256 hashing/verification
util/SessionManager.java       - SharedPreferences-backed "who's logged in" state

res/layout/                    - activity_login, activity_signup, activity_task_list,
                                  item_task, dialog_add_task
```

## How auth + persistence work
- **Database**: one SQLite database (`todo_app.db`) with two tables.
  `tasks.user_id` has a foreign key to `users.id` with `ON DELETE CASCADE`, so
  every task is scoped to its owner and all queries filter by the logged-in
  user's id — one user can never see another's tasks.
- **Passwords**: never stored in plain text. Each user gets a random salt
  (`PasswordUtils.generateSalt()`) generated at registration; the password is
  combined with that salt and hashed with SHA-256 before being written to
  SQLite. Logging in re-hashes the entered password with the stored salt and
  compares digests (constant-time comparison) — the plain-text password is
  never persisted or directly compared.
- **Session**: `SessionManager` stores the logged-in user's id/name/email in a
  private `SharedPreferences` file. `LoginActivity` checks this on launch and
  skips straight to the task list if already logged in. Logout clears the
  preferences and returns to `LoginActivity` with `FLAG_ACTIVITY_CLEAR_TASK`
  so the back stack can't return to the task list.

## Feature checklist
- [x] Login screen: email + password fields, Login button, link to sign up
- [x] Sign-up screen: name, email, password, confirm password, Register button
- [x] Passwords stored as salted SHA-256 hashes (never plain text)
- [x] Logout button (toolbar) returns to login and clears the session
- [x] Task list screen showing all tasks for the logged-in user only
- [x] "Add Task" opens a dialog with a name field and optional notes field
- [x] Tasks can be marked complete — shown with strikethrough + dimmed, and
      sorted below active tasks
- [x] Tasks can be deleted permanently (with a confirmation dialog)
- [x] Tasks are linked to their owning user via `tasks.user_id`
- [x] Friendly empty state when the task list is empty

## Robustness notes
- Login/Register/Delete all guard against rapid double-taps with simple
  in-flight flags so repeated submissions can't create duplicate accounts,
  duplicate tasks, or stack multiple confirmation dialogs.
- `TaskAdapter` re-checks bind positions defensively and always clears the
  checkbox listener before restoring state, so fast scrolling/tapping never
  fires stale callbacks on recycled rows.
- All password comparisons happen server-side (i.e. inside `DatabaseHelper`/
  `PasswordUtils`) — the UI layer never sees a raw password/hash pair beyond
  the single login attempt.

## Note on password hashing
This app uses salted SHA-256, per the "basic hashing" requirement. For a
production app you'd want a slower, purpose-built password hash (bcrypt,
scrypt, or Argon2) to resist brute-force attacks — SHA-256 is fast, which is
good for file integrity but not ideal for password storage at scale.
