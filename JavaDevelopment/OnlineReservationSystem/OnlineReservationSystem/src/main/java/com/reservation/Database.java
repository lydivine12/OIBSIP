package com.reservation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:sqlite:reservation.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initialize() {
        String users = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            )
            """;

        String trains = """
            CREATE TABLE IF NOT EXISTS trains (
                train_number INTEGER PRIMARY KEY,
                train_name TEXT NOT NULL
            )
            """;

        String reservations = """
            CREATE TABLE IF NOT EXISTS reservations (
                pnr TEXT PRIMARY KEY,
                passenger_name TEXT NOT NULL,
                train_number INTEGER NOT NULL,
                train_name TEXT NOT NULL,
                class_type TEXT NOT NULL,
                journey_date TEXT NOT NULL,
                source_station TEXT NOT NULL,
                destination_station TEXT NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY (train_number) REFERENCES trains(train_number)
            )
            """;

        try (Connection con = getConnection();
             Statement st = con.createStatement()) {
            st.execute(users);
            st.execute(trains);
            st.execute(reservations);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database: " + e.getMessage(), e);
        }
    }
}
