package com.travel.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.travel.dao.TravelDao;

public class AdminUserManagementService {

    private final TravelDao travelDao;

    public AdminUserManagementService(TravelDao travelDao) {
        this.travelDao = travelDao;
    }

    public List<Map<String, Object>> getAllUsers() throws SQLException {
        return travelDao.fetchAdminUsersList();
    }

    public List<Map<String, Object>> getAllUsers(String search) throws SQLException {
        return travelDao.fetchAdminUsersList(search);
    }

    public boolean activateUser(int userId) throws SQLException {
        return travelDao.setUserActiveStatus(userId, true);
    }

    public boolean deactivateUser(int userId) throws SQLException {
        return travelDao.setUserActiveStatus(userId, false);
    }

    public boolean deleteUser(int userId) throws SQLException {
        return travelDao.deleteUserAndRelatedData(userId);
    }

    public List<Map<String, Object>> getUserBookingHistory(int userId) throws SQLException {
        return travelDao.fetchUserBookingHistoryForAdmin(userId);
    }
}
