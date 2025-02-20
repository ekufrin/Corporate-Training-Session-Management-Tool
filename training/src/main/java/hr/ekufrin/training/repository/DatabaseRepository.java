package hr.ekufrin.training.repository;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class for connecting to the database
 */
public class DatabaseRepository {
    public static final String ERROR_CONNECTING_TO_DATABASE = "Error while connecting to database ";
    public static final String HASHED_PASSWORD = "password_hash";

    private DatabaseRepository() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Connects to the database using the properties from the database.properties file
     * @return - Connection to the database
     * @throws IOException - If the database.properties file is not found
     * @throws SQLException - If there is an error while connecting to the database
     */
    public static synchronized Connection connectToDB() throws IOException, SQLException {
        Properties props = new Properties();
        try (FileReader reader = new FileReader("src/main/resources/database.properties")) {
            props.load(reader);
        }

        return DriverManager.getConnection(props.getProperty("databaseUrl"));
    }
}
