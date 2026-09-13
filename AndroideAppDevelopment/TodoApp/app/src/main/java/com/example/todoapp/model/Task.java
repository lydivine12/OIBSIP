package com.example.todoapp.model;

/**
 * Represents a single to-do item, always scoped to the user that owns it.
 */
public class Task {
    private long id;
    private final long userId;
    private String title;
    private String notes;
    private boolean completed;
    private final long createdAt;

    public Task(long id, long userId, String title, String notes, boolean completed, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.notes = notes;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
