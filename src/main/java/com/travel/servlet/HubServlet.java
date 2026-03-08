package com.travel.servlet;

import com.travel.dao.TravelDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/hub")
public class HubServlet extends HttpServlet {

    private final TravelDao travelDao = new TravelDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ViewUtil.setCommon(req, "hub");
        try {
            req.setAttribute("users", travelDao.fetchUsersHub());
            req.setAttribute("destinations", travelDao.fetchDestinationsHub());
            req.setAttribute("hotels", travelDao.fetchHotelsHub());
            req.setAttribute("transport", travelDao.fetchTransportHub());
            req.getRequestDispatcher("/WEB-INF/views/hub.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Unable to load hub data", e);
        }
    }
}
