package com.reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TrainDAO {
    public Train findByNumber(int trainNumber) {
        String sql = "SELECT train_number, train_name FROM trains WHERE train_number = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, trainNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Train(rs.getInt("train_number"), rs.getString("train_name"));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Train lookup failed: " + e.getMessage(), e);
        }
        return null;
    }
}
