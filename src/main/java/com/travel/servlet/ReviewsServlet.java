package com.travel.servlet;

import com.travel.dao.TravelDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/reviews")
public class ReviewsServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ViewUtil.setCommon(req, "reviews");
        req.setAttribute("message", req.getParameter("msg"));
        try {
            req.setAttribute("reviews", travelDao.fetchReviews());
            req.setAttribute("users", travelDao.fetchCustomers());
            req.setAttribute("packageOptions", travelDao.fetchPackageOptions());
            req.getRequestDispatcher("/WEB-INF/views/reviews.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Unable to load reviews", e);
        }
    }
}
