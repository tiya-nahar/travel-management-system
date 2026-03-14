package com.travel.servlet;

import com.travel.dao.TravelDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ViewUtil.setCommon(req, "dashboard");
        req.setAttribute("message", req.getParameter("msg"));
        try {
            req.setAttribute("stats", travelDao.fetchDashboardStats());
            req.setAttribute("topPackages", travelDao.fetchTopPackages(3));
            req.setAttribute("latestReviews", travelDao.fetchLatestReviews(3));
            req.setAttribute("cities", travelDao.fetchCities());
            req.getRequestDispatcher("/WEB-INF/views/index.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Unable to load dashboard", e);
        }
    }
}
