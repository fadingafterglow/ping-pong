package ua.edu.ukma.cs;

import lombok.SneakyThrows;
import ua.edu.ukma.cs.database.context.PersistenceContext;
import ua.edu.ukma.cs.database.migration.DefaultMigrationRunner;

import java.util.Properties;

public class PingPongGame {

    private static final String PROPERTIES_FILE = "/db-connection.properties";

    public static void main(String[] args) {
        System.out.println("Starting Ping Pong Game...");

        PersistenceContext.init(loadProperties());
        new DefaultMigrationRunner().runMigrations();
    }

    @SneakyThrows
    private static Properties loadProperties() {
        Properties properties = new Properties();
        properties.load(PersistenceContext.class.getResourceAsStream(PROPERTIES_FILE));
        return properties;
    }
}
