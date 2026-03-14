package com.travel.db;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class JdbcCleanupListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        deregisterJdbcDrivers(event);
    }

    private void deregisterJdbcDrivers(ServletContextEvent event) {
        ClassLoader appClassLoader = JdbcCleanupListener.class.getClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();

        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() != appClassLoader) {
                continue;
            }

            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                event.getServletContext().log("Unable to deregister JDBC driver: " + driver, e);
            }
        }
    }
}
