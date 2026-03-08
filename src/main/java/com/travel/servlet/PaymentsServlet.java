package com.travel.servlet;

import com.travel.dao.TravelDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/payments")
public class PaymentsServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ViewUtil.setCommon(req, "payments");
        try {
            req.setAttribute("payments", travelDao.fetchPayments());
            req.getRequestDispatcher("/WEB-INF/views/payments.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Unable to load payments", e);
        }
    }
}
