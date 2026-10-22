package db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Single place that knows how to connect to MySQL.
 * Settings come from a file called db.properties in the project root
 * (copy db.properties.example to db.properties and put your own password in it).
 * db.properties is in .gitignore so nobody's password ends up on GitHub.
 */
public final class DBConnection {

    private static final Properties CONFIG = loadConfig();

    private DBConnection() { }   // utility class, never instantiated

    private static Properties loadConfig() {
        Properties p = new Properties();
        p.setProperty("db.url",
                "jdbc:mysql://localhost:3306/sportgrid?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata");
        p.setProperty("db.user", "root");
        p.setProperty("db.password", "");
        try (FileInputStream in = new FileInputStream("db.properties")) {
            p.load(in);
        } catch (IOException e) {
            System.err.println("[DBConnection] db.properties not found, using defaults (user root, empty password).");
        }
        return p;
    }

    /** Opens a new connection. Always use it inside try-with-resources so it is closed. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                CONFIG.getProperty("db.url"),
                CONFIG.getProperty("db.user"),
                CONFIG.getProperty("db.password"));
    }
}
