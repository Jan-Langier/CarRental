package org.example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mariadb://localhost:3306/CarRentalDB";

    private static String currentDbUser;
    private static String currentDbPassword;

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, currentDbUser, currentDbPassword);
    }

    public static void switchToAdminMode(String rootPassword) {
        currentDbUser = "admin";
        currentDbPassword = rootPassword;
        System.out.println(">>> TRYB ADMINA (DB ROOT) AKTYWOWANY <<<");
    }

    public static void switchToEmployeeMode(String emplPassword) {
        currentDbUser = "employee";
        currentDbPassword = emplPassword;
        System.out.println(">>> TRYB PRACOWNIKA (DB EMPLOYEE) AKTYWOWANY <<<");
    }
}