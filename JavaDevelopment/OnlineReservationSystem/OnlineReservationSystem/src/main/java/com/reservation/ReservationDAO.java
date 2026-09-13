package com.reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReservationDAO {

    public boolean insert(Reservation r) {
        String sql = """
            INSERT INTO reservations
            (pnr, passenger_name, train_number, train_name, class_type,
             journey_date, source_station, destination_station, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getPnr());
            ps.setString(2, r.getPassengerName());
            ps.setInt(3, r.getTrainNumber());
            ps.setString(4, r.getTrainName());
            ps.setString(5, r.getClassType());
            ps.setString(6, r.getJourneyDate());
            ps.setString(7, r.getSourceStation());
            ps.setString(8, r.getDestinationStation());
            ps.setString(9, r.getCreatedAt());
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException("Could not save reservation: " + e.getMessage(), e);
        }
    }

    public Reservation findByPNR(String pnr) {
        String sql = "SELECT * FROM reservations WHERE pnr = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not fetch reservation: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean deleteByPNR(String pnr) {
        String sql = "DELETE FROM reservations WHERE pnr = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pnr);
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            throw new RuntimeException("Could not cancel reservation: " + e.getMessage(), e);
        }
    }

    private Reservation map(ResultSet rs) throws Exception {
        return new Reservation(
            rs.getString("pnr"),
            rs.getString("passenger_name"),
            rs.getInt("train_number"),
            rs.getString("train_name"),
            rs.getString("class_type"),
            rs.getString("journey_date"),
            rs.getString("source_station"),
            rs.getString("destination_station"),
            rs.getString("created_at")
        );
    }
}
