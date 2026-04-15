package com.travel.servlet;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.travel.dao.TravelDao;
import com.travel.service.AdminPackageManagementService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/admin/packages")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 10 * 1024 * 1024)
public class AdminPackageManagementServlet extends HttpServlet {

    private final AdminPackageManagementService packageService =
            new AdminPackageManagementService(new TravelDao());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String action = value(req, "action");

        try {
            switch (action) {
                case "add" -> addPackage(req, resp);
                case "edit" -> editPackage(req, resp);
                case "delete" -> deletePackage(req, resp);
                default -> redirectWithMessage(req, resp, "Unsupported package action");
            }
        } catch (SQLException e) {
            redirectWithMessage(req, resp, "Package operation failed");
        }
    }

    private void addPackage(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException, SQLException {
        PackageForm form = parsePackageForm(req, -1);
        if (!form.valid) {
            redirectWithMessage(req, resp, form.errorMessage);
            return;
        }

        packageService.addPackage(
                form.destinationId,
                form.title,
                form.description,
                form.price,
                form.durationDays,
                form.maxPeople,
                form.mainImage,
                form.category,
                form.availableSlots,
                form.discountPercent
        );

        redirectWithMessage(req, resp, "Package added successfully");
    }

    private void editPackage(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException, SQLException {
        int packageId = parseInt(req.getParameter("packageId"));
        if (packageId <= 0) {
            redirectWithMessage(req, resp, "Invalid package selected");
            return;
        }

        PackageForm form = parsePackageForm(req, packageId);
        if (!form.valid) {
            resp.sendRedirect(req.getContextPath() + "/admin?editPackageId=" + packageId + "&msg="
                    + encode(form.errorMessage) + "#packages");
            return;
        }

        boolean updated = packageService.updatePackage(
                packageId,
                form.destinationId,
                form.title,
                form.description,
                form.price,
                form.durationDays,
                form.maxPeople,
                form.mainImage,
                form.category,
                form.availableSlots,
                form.discountPercent
        );

        redirectWithMessage(req, resp, updated ? "Package updated successfully" : "No package updated");
    }

    private void deletePackage(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        int packageId = parseInt(req.getParameter("packageId"));
        if (packageId <= 0) {
            redirectWithMessage(req, resp, "Invalid package selected");
            return;
        }

        boolean deleted = packageService.deletePackage(packageId);
        if (!deleted) {
            redirectWithMessage(req, resp, "Cannot delete package with existing bookings");
            return;
        }

        redirectWithMessage(req, resp, "Package deleted successfully");
    }

    private PackageForm parsePackageForm(HttpServletRequest req, int packageId)
            throws IOException, ServletException, SQLException {
        PackageForm form = new PackageForm();

        form.title = value(req, "title");
        form.description = value(req, "description");
        form.category = value(req, "category");
        form.destinationId = parseInt(req.getParameter("destinationId"));
        form.durationDays = parseInt(req.getParameter("durationDays"));
        form.maxPeople = parseInt(req.getParameter("maxPeople"));
        form.availableSlots = parseInt(req.getParameter("availableSlots"));

        String priceRaw = value(req, "price");
        String discountRaw = value(req, "discountPercent");
        form.price = parseDecimal(priceRaw, BigDecimal.ZERO);
        form.discountPercent = parseDecimal(discountRaw, BigDecimal.ZERO);

        String existingImageUrl = value(req, "existingImageUrl");
        String uploadedImage = uploadImage(req);
        form.mainImage = uploadedImage == null || uploadedImage.isBlank() ? existingImageUrl : uploadedImage;

        if (packageId > 0 && (form.mainImage == null || form.mainImage.isBlank())) {
            Map<String, Object> existing = packageService.getPackageById(packageId);
            if (existing != null) {
                Object currentImage = existing.get("mainImage");
                form.mainImage = currentImage == null ? "" : String.valueOf(currentImage);
            }
        }

        if (form.title.isBlank() || form.destinationId <= 0 || form.price.compareTo(BigDecimal.ZERO) < 0
                || form.durationDays <= 0 || form.maxPeople <= 0 || form.availableSlots < 0) {
            form.valid = false;
            form.errorMessage = "Please fill valid package details";
            return form;
        }

        if (form.discountPercent.compareTo(BigDecimal.ZERO) < 0 || form.discountPercent.compareTo(new BigDecimal("90")) > 0) {
            form.valid = false;
            form.errorMessage = "Discount must be between 0 and 90";
            return form;
        }

        if (form.mainImage == null || form.mainImage.isBlank()) {
            form.mainImage = "https://picsum.photos/seed/package-" + UUID.randomUUID() + "/960/540";
        }

        form.valid = true;
        return form;
    }

    private String uploadImage(HttpServletRequest req) throws IOException, ServletException {
        Part imagePart;
        try {
            imagePart = req.getPart("imageFile");
        } catch (IllegalStateException ignored) {
            return "";
        }

        if (imagePart == null || imagePart.getSize() == 0) {
            return "";
        }

        String submitted = imagePart.getSubmittedFileName();
        if (submitted == null || submitted.isBlank()) {
            return "";
        }

        String ext = extension(submitted);
        if (!isAllowedImageExt(ext)) {
            return "";
        }

        String safeName = "pkg-" + System.currentTimeMillis() + "-" + UUID.randomUUID() + ext;
        String uploadDirReal = req.getServletContext().getRealPath("/uploads/packages");
        if (uploadDirReal == null) {
            return "";
        }

        Path uploadDir = Paths.get(uploadDirReal);
        Files.createDirectories(uploadDir);
        Path target = uploadDir.resolve(safeName);
        Files.copy(imagePart.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return req.getContextPath() + "/uploads/packages/" + safeName;
    }

    private boolean isAllowedImageExt(String ext) {
        return ".jpg".equals(ext) || ".jpeg".equals(ext) || ".png".equals(ext) || ".webp".equals(ext);
    }

    private String extension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx < 0) {
            return "";
        }
        return fileName.substring(idx).toLowerCase(Locale.ROOT);
    }

    private String value(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private int parseInt(String raw) {
        if (raw == null || raw.isBlank()) {
            return -1;
        }

        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private BigDecimal parseDecimal(String raw, BigDecimal fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }

        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private void redirectWithMessage(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin?msg=" + encode(message) + "#packages");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static final class PackageForm {
        private String title;
        private String description;
        private String category;
        private String mainImage;
        private int destinationId;
        private int durationDays;
        private int maxPeople;
        private int availableSlots;
        private BigDecimal price;
        private BigDecimal discountPercent;
        private boolean valid;
        private String errorMessage;
    }
}
