package com.travel.servlet;

import com.travel.dao.TravelDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

@WebServlet("/users/add")
public class AddUserServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = value(req, "name");
        String email = value(req, "email");
        String phone = value(req, "phone");
        String password = value(req, "password");

        if (name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            redirectWithMessage(req, resp, "All user fields are required");
            return;
        }

        try {
            travelDao.addCustomer(name, email, phone, password);
            redirectWithMessage(req, resp, "User added successfully");
        } catch (SQLException e) {
            redirectWithMessage(req, resp, userMessage(e));
        }
    }

    private void redirectWithMessage(HttpServletRequest req, HttpServletResponse resp, String message)
            throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/packages?msg=" + encoded + "#add-user");
    }

    private String value(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        return value == null ? "" : value.trim();
    }

    private String userMessage(SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException || "23000".equals(e.getSQLState())) {
            return "Email already exists";
        }
        return "User creation failed";
    }
}
