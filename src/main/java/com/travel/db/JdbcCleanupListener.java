package com.travel.db;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

@WebListener
public class JdbcCleanupListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        shutdownMysqlCleanupThread(event);
        deregisterJdbcDrivers(event);
    }

    private void shutdownMysqlCleanupThread(ServletContextEvent event) {
        try {
            AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Exception e) {
            event.getServletContext().log("Unable to stop MySQL cleanup thread cleanly", e);
        }
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
