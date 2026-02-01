package org.example;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {


    public void ensureAdminAccountExists(String rootPassword) {
        String sqlUpsert = "INSERT INTO AppUsers (username, password_hash, role) VALUES ('admin', ?, 'Admin') " +
                "ON DUPLICATE KEY UPDATE password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlUpsert)) {

            String passHash = SecurityUtils.hashPassword(rootPassword);
            stmt.setString(1, passHash);
            stmt.setString(2, passHash);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Błąd synchronizacji konta admina: " + e.getMessage());
        }
    }

    public boolean addEmployee(String username, String rawPassword) {
        String sql = "INSERT INTO AppUsers (username, password_hash, role) VALUES (?, ?, 'Employee')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, SecurityUtils.hashPassword(rawPassword));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Błąd dodawania pracownika: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteEmployee(String usernameToDelete, String currentLoggedInUser) {
        if (usernameToDelete.equalsIgnoreCase(currentLoggedInUser)) {
            System.out.println("NIE MOŻESZ USUNĄĆ SAMEGO SIEBIE!");
            return false;
        }
        if (usernameToDelete.equalsIgnoreCase("admin")) {
            System.out.println("Nie można usunąć głównego admina.");
            return false;
        }

        String sql = "DELETE FROM AppUsers WHERE id = ? AND role = 'Employee'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usernameToDelete);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAllEmployees() {
        List<String> employees = new ArrayList<>();
        String sql = "SELECT id, username, role FROM AppUsers WHERE role = 'Employee' ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                employees.add(String.format("[%d] %s (%s)",
                        rs.getInt("id"), rs.getString("username"), rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public boolean loginUser(String username, String rawPassword) {
        String sql = "SELECT password_hash, role FROM AppUsers WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");

                if ("Admin".equalsIgnoreCase(role)) {
                    System.out.println("BŁĄD: Konto Admina wymaga logowania w trybie Administratora (opcja 1 w menu startowym).");
                    return false;
                }

                String storedHash = rs.getString("password_hash");
                return SecurityUtils.validatePassword(rawPassword, storedHash);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public int getUserIdByUsername(String username) {
        String sql = "SELECT id FROM AppUsers WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
