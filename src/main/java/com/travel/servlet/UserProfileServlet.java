package com.travel.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/profile")
public class UserProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ViewUtil.currentCustomer(req) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        ViewUtil.setCommon(req, "profile");
        req.getRequestDispatcher("/WEB-INF/views/user-profile.jsp").forward(req, resp);
    }
}