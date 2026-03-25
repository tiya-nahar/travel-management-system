package com.travel.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;

import com.travel.dao.TravelDao;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/bookings/create")
public class CreateBookingServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            Integer userId = ViewUtil.currentCustomerId(req);
            if (userId == null) {
                String msg = URLEncoder.encode("Please sign in to create a booking", StandardCharsets.UTF_8);
                resp.sendRedirect(req.getContextPath() + "/login?msg=" + msg + "&next=%2Fpackages");
                return;
            }

            int packageId = Integer.parseInt(req.getParameter("packageId"));
            Date travelDate = Date.valueOf(req.getParameter("travelDate"));
            int people = Integer.parseInt(req.getParameter("people"));
            String travelerName = trimOrEmpty(req.getParameter("travelerName"));
            String contactPhone = trimOrEmpty(req.getParameter("contactPhone"));
            String specialRequest = trimOrEmpty(req.getParameter("specialRequest"));
            String status = req.getParameter("status");
            String paymentMethod = req.getParameter("paymentMethod");

            if (travelerName.length() < 2) {
                throw new IllegalArgumentException("Traveler name must be at least 2 characters");
            }

            if (!contactPhone.matches("^[0-9+\\- ]{10,15}$")) {
                throw new IllegalArgumentException("Enter a valid contact phone (10-15 digits)");
            }

            if (specialRequest.length() > 250) {
                throw new IllegalArgumentException("Special request is too long");
            }

                travelDao.createBookingWithPayment(
                    userId,
                    packageId,
                    travelDate,
                    people,
                    travelerName,
                    contactPhone,
                    specialRequest,
                    status,
                    paymentMethod
                );
            String msg = URLEncoder.encode("Booking and payment saved", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/bookings?msg=" + msg);
        } catch (IllegalArgumentException | SQLException e) {
            String msg = URLEncoder.encode("Booking failed: " + e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/packages?msg=" + msg);
        }
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
