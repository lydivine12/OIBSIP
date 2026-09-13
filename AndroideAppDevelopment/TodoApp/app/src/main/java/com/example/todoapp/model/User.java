package com.example.todoapp.model;

/**
 * Represents a registered user. passwordHash/salt are only ever populated when
 * loaded internally for login verification - they are never displayed or logged.
 */
public class User {
    private final long id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final String salt;

    public User(long id, String name, String email, String passwordHash, String salt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }
}
