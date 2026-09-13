package com.example.todoapp.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Tracks who is currently logged in using a private SharedPreferences file.
 * This is intentionally lightweight (no auth tokens/expiry) since the app has
 * no backend - "session" here just means "which local user is active".
 */
public class SessionManager {

    private static final String PREF_NAME = "todo_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void createSession(long userId, String name, String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getLong(KEY_USER_ID, -1L) != -1L;
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    /** Clears the active session. Task/user data in SQLite is untouched. */
    public void logout() {
        prefs.edit().clear().apply();
    }
}
