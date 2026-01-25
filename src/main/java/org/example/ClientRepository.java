package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientRepository {

    public void addClient(String firstName, String lastName, String driverLicense, String phone, String address) {
        String sql = "INSERT INTO Clients (first_name, last_name, driver_license_no, phone, address) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, driverLicense);
            stmt.setString(4, phone);
            stmt.setString(5, address);
            stmt.executeUpdate();
            System.out.println("Dodano klienta: " + firstName + " " + lastName);

        } catch (SQLException e) {
            System.err.println("Błąd dodawania klienta: " + e.getMessage());
        }
    }

    public void deleteClient(int clientId) {
        String sql = "DELETE FROM Clients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, String.valueOf(clientId));
            stmt.executeUpdate();
            System.out.println("Usunięto klienta o ID: " + clientId);

        } catch (SQLException e) {
            System.err.println("Nie można usunąć klienta (może ma aktywne wypożyczenia?): " + e.getMessage());
        }
    }

    public List<String> getAllClients() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT * FROM Clients";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(String.format("[%d] %s %s | Tel: %s | Adres: %s",
                        rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),
                        rs.getString("phone"), rs.getString("address")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void updateClient(int id, String newPhone, String newAddress) {
        String sql = "UPDATE Clients SET phone = ?, address = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPhone);
            stmt.setString(2, newAddress);
            stmt.setInt(3, id);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Zaktualizowano dane klienta ID: " + id);
            } else {
                System.out.println("Nie znaleziono klienta o podanym ID.");
            }

        } catch (SQLException e) {
            System.err.println("Błąd aktualizacji klienta: " + e.getMessage());
        }
    }
}