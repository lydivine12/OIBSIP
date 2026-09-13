package com.example.todoapp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.todoapp.model.Task;
import com.example.todoapp.model.User;
import com.example.todoapp.util.PasswordUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Single SQLite database for the app: a `users` table and a `tasks` table,
 * where every task is linked to the user that owns it via user_id (with
 * ON DELETE CASCADE, so removing a user also removes their tasks).
 *
 * Passwords are never stored in plain text - only a salted SHA-256 hash
 * (see {@link PasswordUtils}).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "todo_app.db";
    private static final int DATABASE_VERSION = 1;

    // users table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_NAME = "name";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PASSWORD_HASH = "password_hash";
    private static final String COL_USER_SALT = "salt";

    // tasks table
    private static final String TABLE_TASKS = "tasks";
    private static final String COL_TASK_ID = "id";
    private static final String COL_TASK_USER_ID = "user_id";
    private static final String COL_TASK_TITLE = "title";
    private static final String COL_TASK_NOTES = "notes";
    private static final String COL_TASK_COMPLETED = "is_completed";
    private static final String COL_TASK_CREATED_AT = "created_at";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_NAME + " TEXT NOT NULL, "
                + COL_USER_EMAIL + " TEXT NOT NULL UNIQUE, "
                + COL_USER_PASSWORD_HASH + " TEXT NOT NULL, "
                + COL_USER_SALT + " TEXT NOT NULL"
                + ")");

        db.execSQL("CREATE TABLE " + TABLE_TASKS + " ("
                + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TASK_USER_ID + " INTEGER NOT NULL, "
                + COL_TASK_TITLE + " TEXT NOT NULL, "
                + COL_TASK_NOTES + " TEXT, "
                + COL_TASK_COMPLETED + " INTEGER NOT NULL DEFAULT 0, "
                + COL_TASK_CREATED_AT + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + COL_TASK_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE INDEX idx_tasks_user_id ON " + TABLE_TASKS + "(" + COL_TASK_USER_ID + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ---------------------------------------------------------------------------------
    // User auth
    // ---------------------------------------------------------------------------------

    public enum RegisterResult { SUCCESS, EMAIL_TAKEN, ERROR }

    /**
     * Registers a new user, hashing their password with a fresh random salt.
     * Returns the new user's id via {@code outUserId[0]} on success.
     */
    public RegisterResult registerUser(String name, String email, String plainPassword, long[] outUserId) {
        SQLiteDatabase db = getWritableDatabase();

        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(plainPassword, salt);

        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_EMAIL, email.trim().toLowerCase());
        values.put(COL_USER_PASSWORD_HASH, hash);
        values.put(COL_USER_SALT, salt);

        try {
            long id = db.insertOrThrow(TABLE_USERS, null, values);
            if (id == -1) {
                return RegisterResult.ERROR;
            }
            if (outUserId != null && outUserId.length > 0) {
                outUserId[0] = id;
            }
            return RegisterResult.SUCCESS;
        } catch (SQLiteConstraintException e) {
            // UNIQUE constraint on email failed
            return RegisterResult.EMAIL_TAKEN;
        } catch (Exception e) {
            return RegisterResult.ERROR;
        }
    }

    /** Looks up a user by email (case-insensitive), including hash/salt for verification. */
    public User findUserByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID, COL_USER_NAME, COL_USER_EMAIL, COL_USER_PASSWORD_HASH, COL_USER_SALT},
                COL_USER_EMAIL + " = ?",
                new String[]{email.trim().toLowerCase()},
                null, null, null
        );

        User user = null;
        try {
            if (cursor.moveToFirst()) {
                user = new User(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD_HASH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_SALT))
                );
            }
        } finally {
            cursor.close();
        }
        return user;
    }

    /** Returns the matching User if the email/password combination is valid, otherwise null. */
    public User verifyLogin(String email, String plainPassword) {
        User user = findUserByEmail(email);
        if (user == null) {
            return null;
        }
        boolean matches = PasswordUtils.verifyPassword(plainPassword, user.getPasswordHash(), user.getSalt());
        return matches ? user : null;
    }

    // ---------------------------------------------------------------------------------
    // Tasks
    // ---------------------------------------------------------------------------------

    public long addTask(long userId, String title, String notes) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_USER_ID, userId);
        values.put(COL_TASK_TITLE, title);
        values.put(COL_TASK_NOTES, notes);
        values.put(COL_TASK_COMPLETED, 0);
        values.put(COL_TASK_CREATED_AT, System.currentTimeMillis());
        return db.insert(TABLE_TASKS, null, values);
    }

    /** Returns all tasks belonging to {@code userId}: incomplete first (newest first), then completed. */
    public List<Task> getTasksForUser(long userId) {
        List<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_TASKS,
                null,
                COL_TASK_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null,
                COL_TASK_COMPLETED + " ASC, " + COL_TASK_CREATED_AT + " DESC"
        );

        try {
            while (cursor.moveToNext()) {
                tasks.add(new Task(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_ID)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_USER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_NOTES)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1,
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_CREATED_AT))
                ));
            }
        } finally {
            cursor.close();
        }
        return tasks;
    }

    public boolean setTaskCompleted(long taskId, boolean completed) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_COMPLETED, completed ? 1 : 0);
        int rows = db.update(TABLE_TASKS, values, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        return rows > 0;
    }

    public boolean deleteTask(long taskId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_TASKS, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        return rows > 0;
    }
}
