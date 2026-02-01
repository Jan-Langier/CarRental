package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RentalRepository {

    public boolean rentCar(int clientId, int carId, int userId, int days) {
        Connection conn = null;
        PreparedStatement stmtRent = null;
        PreparedStatement stmtUpdateCar = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlRent = "INSERT INTO Rentals (client_id, car_id, user_id, date_start, date_end_planned) VALUES (?, ?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL ? DAY))";
            stmtRent = conn.prepareStatement(sqlRent);
            stmtRent.setInt(1, clientId);
            stmtRent.setInt(2, carId);
            stmtRent.setInt(3, userId);
            stmtRent.setInt(4,days);
            stmtRent.executeUpdate();

            String sqlCar = "UPDATE Cars SET status = 'Wypozyczony' WHERE id = ?";
            stmtUpdateCar = conn.prepareStatement(sqlCar);
            stmtUpdateCar.setInt(1, carId);
            stmtUpdateCar.executeUpdate();

            conn.commit();
            System.out.println("SUKCES: Auto zostało wypożyczone.");
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("BŁĄD TRANSAKCJI (Wypożyczenie): " + e.getMessage());
            return false;
        } finally {
            try {
                if (stmtRent != null) stmtRent.close();
                if (stmtUpdateCar != null) stmtUpdateCar.close();
                if (conn != null) conn.setAutoCommit(true);
                if (conn != null) conn.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void returnCar(int carId, int endMileage) {
        Connection conn = null;
        PreparedStatement stmtReturn = null;
        PreparedStatement stmtCarStatus = null;
        PreparedStatement stmtGetCost = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlReturn = "UPDATE Rentals SET date_returned = CURDATE(), end_mileage = ? " +
                    "WHERE car_id = ? AND date_returned IS NULL";
            stmtReturn = conn.prepareStatement(sqlReturn);
            stmtReturn.setInt(1, endMileage);
            stmtReturn.setInt(2, carId);
            int rows = stmtReturn.executeUpdate();

            if (rows == 0) {
                System.out.println("BŁĄD: Nie znaleziono aktywnego wypożyczenia dla tego auta.");
                conn.rollback();
                return;
            }

            String sqlStatus = "UPDATE Cars SET status = 'Dostepny' WHERE id = ?";
            stmtCarStatus = conn.prepareStatement(sqlStatus);
            stmtCarStatus.setInt(1, carId);
            stmtCarStatus.executeUpdate();

            String sqlCost = "SELECT total_cost FROM Rentals WHERE car_id = ? ORDER BY id DESC LIMIT 1";
            stmtGetCost = conn.prepareStatement(sqlCost);
            stmtGetCost.setInt(1, carId);
            ResultSet rs = stmtGetCost.executeQuery();
            double cost = 0;
            if (rs.next()) {
                cost = rs.getDouble("total_cost");
            }

            conn.commit();
            System.out.println("SUKCES: Auto zwrócone.");
            System.out.println("=========================================");
            System.out.printf("DO ZAPŁATY: %.2f PLN \n", cost);
            System.out.println("=========================================");

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            System.err.println("BŁĄD TRANSAKCJI (Zwrot): " + e.getMessage());
        } finally {
            try {
                if (stmtReturn != null) stmtReturn.close();
                if (stmtCarStatus != null) stmtCarStatus.close();
                if (conn != null) conn.setAutoCommit(true);
                if (conn != null) conn.close();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    public List<String> getRentalHistory(){
        List<String> rentals = new ArrayList<>();

        String sql = "SELECT r.id, cl.first_name AS client_name, cl.last_name AS client_last_name ,ca.registration_number AS registration," +
                "a.username AS employee, r.date_start, r.date_returned, r.start_mileage, r.end_mileage, r.total_cost " +
                "FROM Rentals r " +
                "JOIN Clients cl ON r.client_id = cl.id " +
                "JOIN Cars ca ON r.car_id = ca.id " +
                "JOIN AppUsers a ON r.user_id = a.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String clientName = rs.getString("client_name");
                String clientLastName = rs.getString("client_last_name");
                String registration = rs.getString("registration");
                String username = rs.getString("employee");
                String dateStart = rs.getString("date_start");
                String dateReturned = rs.getString("date_returned");
                int startMileage = rs.getInt("start_mileage");
                int endMileage = rs.getInt("end_mileage");
                double totalCost = rs.getDouble("total_cost");

                String carInfo = String.format("[%d] Klient: %s %s | Rejestracja: %s | Pracownik; %s |Od %s Do %s | Przejechano: %d Km | Cena: %.2f",
                        id, clientName, clientLastName, registration, username, dateStart, dateReturned, endMileage-startMileage, totalCost);
                rentals.add(carInfo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rentals;
    }
}
