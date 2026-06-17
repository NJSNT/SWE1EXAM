package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    // The project uses a separate PostgreSQL database to avoid mixing its tables with the default 'postgres' database or tables from other projects
    // DatabaseInitializer creates the required tables, but the database itself must be created manually first: CREATE DATABASE skills_tutoring_exchange;
    // Change USER and PASSWORD if your local PostgreSQL setup is different
    private static final String URL = "jdbc:postgresql://localhost:5432/test";
  private static final String USER = "postgres";
  private static final String PASSWORD = "NanjingWjs";

    // Private constructor restricts instantiation from other classes
    private DatabaseConnection() {
        try {
            // Register JDBC driver (optional in newer JDBC versions, but good practice)
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found. Make sure it's added to the project dependencies.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Check credentials and URL.");
            e.printStackTrace();
        }
    }

    // Public method to provide the single instance
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
