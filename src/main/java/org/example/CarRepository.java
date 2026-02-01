package org.example;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CarRepository {
    public List<String> getAllCars(){
        List<String> cars= new ArrayList<>();

        String sql = "SELECT c.id, b.name AS brand, m.name AS model, c.production_year, m.category, m.default_daily_rate, c.status " +
                "FROM Cars c " +
                "JOIN Models m ON c.model_id = m.id " +
                "JOIN Brands b ON m.brand_id = b.id ";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String brand = rs.getString("brand");
                String model = rs.getString("model");
                int year = rs.getInt("production_year");
                String category = rs.getString("category");
                double price = rs.getDouble("default_daily_rate");
                String status = rs.getString("status");

                String carInfo = String.format("[%d] %s %s (%d) - %s | Cena: %.2f PLN/dzień | %s",
                        id, brand, model, year, category, price, status);
                cars.add(carInfo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cars;
    }

    public void addCar(int modelId, String regNumber, int year, int mileage) {
        String sql = "INSERT INTO Cars (model_id, registration_number, production_year, status, current_mileage) " +
                "VALUES (?, ?, ?, 'Dostepny', ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, modelId);
            stmt.setString(2, regNumber);
            stmt.setInt(3, year);
            stmt.setInt(4, mileage);
            stmt.executeUpdate();
            System.out.println("Dodano nowe auto z przebiegiem: "+mileage);
        } catch (SQLException e) {
            System.err.println("Błąd dodawania auta: " + e.getMessage());
        }
    }

    public void deleteCar(int carId) {
        String sql = "DELETE FROM Cars WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, carId);
            stmt.executeUpdate();
            System.out.println("Usunięto auto ID: " + carId);
        } catch (SQLException e) {
            System.err.println("Błąd usuwania (może auto jest w archiwum wypożyczeń?): " + e.getMessage());
        }
    }

    public List<String> searchCars(String keyword) {
        List<String> cars = new ArrayList<>();
        String sql = "SELECT c.id, b.name, m.name, c.production_year " +
                "FROM Cars c " +
                "JOIN Models m ON c.model_id = m.id " +
                "JOIN Brands b ON m.brand_id = b.id " +
                "WHERE b.name LIKE ? OR m.name LIKE ? OR c.status LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            stmt.setString(3, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                cars.add(rs.getString(2) + " " + rs.getString(3) + " (" + rs.getInt(4) + ") [ID: " + rs.getInt(1) + "]");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return cars;
    }


    public void addBrand(String name) {
        String sql = "INSERT INTO Brands (name) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
            System.out.println("Dodano nową markę: " + name);
        } catch (SQLException e) {
            System.err.println("Błąd dodawania marki: " + e.getMessage());
        }
    }

    public void addModel(int brandId, String name, String category, double dailyRate) {
        String sql = "INSERT INTO Models (brand_id, name, category, default_daily_rate) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, brandId);
            stmt.setString(2, name);
            stmt.setString(3, category);
            stmt.setDouble(4, dailyRate);
            stmt.executeUpdate();
            System.out.println("Dodano nowy model: " + name);
        } catch (SQLException e) {
            System.err.println("Błąd dodawania modelu: " + e.getMessage());
        }
    }
    public void showAllBrands() {
        String sql = "SELECT * FROM Brands GROUP BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("--- DOSTĘPNE MARKI ---");
            while (rs.next()) {
                System.out.println("[" + rs.getInt("id") + "] " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void showAllModels() {
        String sql = "SELECT m.id, b.name AS brand_name, m.name AS model_name, m.category, m.default_daily_rate " +
                "FROM Models m " +
                "JOIN Brands b ON m.brand_id = b.id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("--- DOSTĘPNE MODELE ---");
            while (rs.next()) {
                System.out.printf("[%d] %s %s (%s) - %.2f PLN/doba%n",
                        rs.getInt("id"),
                        rs.getString("brand_name"),
                        rs.getString("model_name"),
                        rs.getString("category"),
                        rs.getDouble("default_daily_rate"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void sendToService(int id){
        String sql = "UPDATE Cars SET status = 'Serwis' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Wysłano model "+ id +" do serwisu");
        } catch (SQLException e) {
            System.err.println("Błąd wysyłania modelu na serwis: " + e.getMessage());
        }
    }

    public void getFromService(int id){
        String sql = "UPDATE Cars SET status = 'Dostepny' WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Zwrócono model "+ id +" z serwisu");
        } catch (SQLException e) {
            System.err.println("Błąd zwracania modelu z serwisu: " + e.getMessage());
        }
    }

    public List<String> getCars(String status) {
        List<String> cars = new ArrayList<>();

        String sql = "SELECT c.id, b.name AS brand, m.name AS model, c.production_year, m.category, m.default_daily_rate " +
                "FROM Cars c " +
                "JOIN Models m ON c.model_id = m.id " +
                "JOIN Brands b ON m.brand_id = b.id " +
                "WHERE c.status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1,status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String brand = rs.getString("brand");
                String model = rs.getString("model");
                int year = rs.getInt("production_year");
                String category = rs.getString("category");
                double price = rs.getDouble("default_daily_rate");

                String carInfo = String.format("[%d] %s %s (%d) - %s | Cena: %.2f PLN/dzień",
                        id, brand, model, year, category, price);
                cars.add(carInfo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cars;
    }
}
