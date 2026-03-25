package com.travel.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class DBConnection {

    private static final Properties PROPS = new Properties();

    private static InputStream openPropsStream() throws IOException {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = contextLoader != null ? contextLoader.getResourceAsStream("db.properties") : null;
        if (input == null) {
            input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties");
        }
        if (input == null) {
            input = DBConnection.class.getResourceAsStream("/db.properties");
        }
        if (input != null) {
            return input;
        }

        String configuredPath = System.getProperty("travel.db.config");
        if (configuredPath == null || configuredPath.isBlank()) {
            configuredPath = System.getenv("TRAVEL_DB_CONFIG");
        }

        List<Path> fallbacks = new ArrayList<>();
        if (configuredPath != null && !configuredPath.isBlank()) {
            fallbacks.add(Paths.get(configuredPath.trim()));
        }

        fallbacks.add(Paths.get("db.properties"));
        fallbacks.add(Paths.get("src", "main", "resources", "db.properties"));

        for (Path candidate : fallbacks) {
            if (Files.exists(candidate) && Files.isReadable(candidate)) {
                return Files.newInputStream(candidate);
            }
        }

        return input;
    }

    static {
        try (InputStream input = openPropsStream()) {
            if (input == null) {
                throw new IllegalStateException(
                        "db.properties not found. Checked classpath, ./db.properties, "
                                + "./src/main/resources/db.properties, and travel.db.config/TRAVEL_DB_CONFIG"
                );
            }
            PROPS.load(input);
            Class.forName(PROPS.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (IOException | ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.username"),
                PROPS.getProperty("db.password")
        );
    }
}
