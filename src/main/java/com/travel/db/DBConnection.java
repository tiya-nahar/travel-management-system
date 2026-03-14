package com.travel.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private static final Properties PROPS = new Properties();

    private static InputStream openPropsStream() {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = contextLoader != null ? contextLoader.getResourceAsStream("db.properties") : null;
        if (input == null) {
            input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties");
        }
        if (input == null) {
            input = DBConnection.class.getResourceAsStream("/db.properties");
        }
        return input;
    }

    static {
        try (InputStream input = openPropsStream()) {
            if (input == null) {
                throw new IllegalStateException("db.properties not found in classpath");
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
