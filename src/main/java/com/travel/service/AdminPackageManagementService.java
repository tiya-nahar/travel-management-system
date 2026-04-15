package com.travel.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

import com.travel.dao.TravelDao;

public class AdminPackageManagementService {

    private final TravelDao travelDao;

    public AdminPackageManagementService(TravelDao travelDao) {
        this.travelDao = travelDao;
    }

    public void addPackage(int destinationId, String title, String description, BigDecimal price,
                           int durationDays, int maxPeople, String mainImage,
                           String category, int availableSlots, BigDecimal discountPercent) throws SQLException {
        travelDao.addPackageForAdmin(destinationId, title, description, price,
                durationDays, maxPeople, mainImage, category, availableSlots, discountPercent);
    }

    public boolean updatePackage(int packageId, int destinationId, String title, String description,
                                 BigDecimal price, int durationDays, int maxPeople, String mainImage,
                                 String category, int availableSlots, BigDecimal discountPercent) throws SQLException {
        return travelDao.updatePackageForAdmin(packageId, destinationId, title, description, price,
                durationDays, maxPeople, mainImage, category, availableSlots, discountPercent);
    }

    public boolean deletePackage(int packageId) throws SQLException {
        return travelDao.deletePackageForAdmin(packageId);
    }

    public Map<String, Object> getPackageById(int packageId) throws SQLException {
        return travelDao.fetchPackageByIdForAdmin(packageId);
    }
}
