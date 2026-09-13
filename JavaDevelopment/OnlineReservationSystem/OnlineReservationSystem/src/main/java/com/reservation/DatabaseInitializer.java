package com.reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void seed() {
        try (Connection con = Database.getConnection()) {
            insertUser(con, "admin", "admin123");
            insertUser(con, "user", "user123");

            insertTrain(con, 101, "Kigali Express");
            insertTrain(con, 202, "Huye Shuttle");
            insertTrain(con, 303, "Musanze Express");
            insertTrain(con, 404, "Rubavu Transport");
            insertTrain(con, 505, "Kayonza Line");
        } catch (SQLException e) {
            throw new RuntimeException("Could not seed database: " + e.getMessage(), e);
        }
    }

    private static void insertUser(Connection con, String username, String password) throws SQLException {
        String sql = "INSERT OR IGNORE INTO users(username, password) VALUES(?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
        }
    }

    private static void insertTrain(Connection con, int number, String name) throws SQLException {
        String sql = "INSERT OR IGNORE INTO trains(train_number, train_name) VALUES(?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, number);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }
}
