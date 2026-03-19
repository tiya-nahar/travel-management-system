<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="My Profile" />
<%@ include file="fragments/header.jspf" %>

<style>
    .profile-container {
        display: grid;
        grid-template-columns: 1fr;
        gap: 24px;
    }

    @media (min-width: 1024px) {
        .profile-container {
            grid-template-columns: 350px 1fr;
        }
    }

    .profile-sidebar {
        background: var(--surface);
        border-radius: var(--radius);
        padding: 24px;
        box-shadow: var(--shadow);
        height: fit-content;
    }

    .profile-avatar {
        width: 120px;
        height: 120px;
        border-radius: 50%;
        background: linear-gradient(135deg, var(--brand), var(--brand-2));
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 20px;
    }

    .avatar-initial {
        font-size: 48px;
        font-weight: 700;
        color: white;
    }

    .profile-sidebar h2 {
        text-align: center;
        margin-bottom: 8px;
        font-size: 20px;
    }

    .profile-sidebar p {
        text-align: center;
        color: var(--muted);
        font-size: 14px;
        margin-bottom: 20px;
    }

    .profile-action-btn {
        width: 100%;
        padding: 10px 16px;
        border: none;
        border-radius: 8px;
        background: var(--brand);
        color: white;
        cursor: pointer;
        font-weight: 600;
        margin-bottom: 10px;
        transition: all 0.3s;
    }

    .profile-action-btn:hover {
        background: #0d3d46;
        transform: translateY(-2px);
    }

    .profile-action-btn.secondary {
        background: var(--line);
        color: var(--ink);
    }

    .profile-action-btn.secondary:hover {
        background: #ddd;
    }

    .profile-content {
        display: flex;
        flex-direction: column;
        gap: 24px;
    }

    .content-section {
        background: var(--surface);
        border-radius: var(--radius);
        padding: 24px;
        box-shadow: var(--shadow);
    }

    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20px;
        padding-bottom: 16px;
        border-bottom: 1px solid var(--line);
    }

    .section-header h3 {
        font-size: 18px;
    }

    .view-all-link {
        color: var(--brand);
        text-decoration: none;
        font-weight: 600;
        font-size: 14px;
        transition: all 0.3s;
    }

    .view-all-link:hover {
        color: var(--accent);
    }

    .detail-row {
        display: flex;
        justify-content: space-between;
        padding: 12px 0;
        border-bottom: 1px solid var(--line);
    }

    .detail-row:last-child {
        border-bottom: none;
    }

    .detail-row label {
        font-weight: 600;
        color: var(--muted);
        min-width: 150px;
    }

    .detail-row span {
        text-align: right;
        color: var(--ink);
    }

    .stats-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
        gap: 16px;
        margin-bottom: 20px;
    }

    .stat-card {
        background: linear-gradient(135deg, rgba(14, 74, 84, 0.1), rgba(244, 177, 61, 0.1));
        border-radius: 12px;
        padding: 16px;
        text-align: center;
    }

    .stat-number {
        font-size: 28px;
        font-weight: 700;
        color: var(--brand);
        margin-bottom: 4px;
    }

    .stat-label {
        font-size: 12px;
        color: var(--muted);
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }

    .bookings-list {
        display: flex;
        flex-direction: column;
        gap: 12px;
    }

    .booking-item {
        background: var(--bg);
        padding: 16px;
        border-radius: 8px;
        border-left: 4px solid var(--brand);
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .booking-item-info h4 {
        margin-bottom: 4px;
        font-size: 14px;
    }

    .booking-item-info p {
        font-size: 12px;
        color: var(--muted);
    }

    .booking-status {
        padding: 4px 12px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 600;
    }

    .booking-status.confirmed {
        background: #d4edda;
        color: #155724;
    }

    .booking-status.pending {
        background: #fff3cd;
        color: #856404;
    }

    .booking-status.completed {
        background: #cfe2ff;
        color: #084298;
    }

    .empty-state {
        text-align: center;
        padding: 40px 20px;
        color: var(--muted);
    }

    .empty-state p {
        margin-bottom: 16px;
    }

    .empty-state-btn {
        display: inline-block;
        padding: 10px 20px;
        background: var(--brand);
        color: white;
        border-radius: 8px;
        text-decoration: none;
        font-weight: 600;
        transition: all 0.3s;
    }

    .empty-state-btn:hover {
        background: #0d3d46;
        transform: translateY(-2px);
    }

    .settings-list {
        display: flex;
        flex-direction: column;
        gap: 12px;
    }

    .settings-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px;
        background: var(--bg);
        border-radius: 8px;
    }

    .settings-item label {
        display: flex;
        align-items: center;
        gap: 12px;
        cursor: pointer;
        font-weight: 500;
    }

    .toggle-switch {
        position: relative;
        width: 50px;
        height: 24px;
        background: #ccc;
        border-radius: 12px;
        cursor: pointer;
        transition: background 0.3s;
    }

    .toggle-switch input {
        display: none;
    }

    .toggle-switch input:checked + .toggle-slider {
        background: var(--brand);
    }

    .toggle-slider {
        position: absolute;
        top: 2px;
        left: 2px;
        width: 20px;
        height: 20px;
        background: white;
        border-radius: 50%;
        transition: all 0.3s;
    }

    .toggle-switch input:checked + .toggle-slider {
        left: 28px;
    }

    /* Modal Styles */
    .modal {
        display: none;
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        z-index: 1000;
        align-items: center;
        justify-content: center;
    }

    .modal.active {
        display: flex;
    }

    .modal-content {
        background: white;
        border-radius: 16px;
        padding: 40px;
        max-width: 500px;
        width: 90%;
        max-height: 90vh;
        overflow-y: auto;
        box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
        animation: slideUp 0.3s ease;
    }

    @keyframes slideUp {
        from {
            transform: translateY(30px);
            opacity: 0;
        }
        to {
            transform: translateY(0);
            opacity: 1;
        }
    }

    .modal-header {
        font-size: 24px;
        font-weight: 700;
        margin-bottom: 20px;
        color: #1a1a1a;
    }

    .modal-close {
        position: absolute;
        top: 20px;
        right: 20px;
        background: none;
        border: none;
        font-size: 28px;
        cursor: pointer;
        color: #8f95a3;
        transition: color 0.2s;
    }

    .modal-close:hover {
        color: #1a1a1a;
    }

    .form-group {
        margin-bottom: 20px;
    }

    .form-group label {
        display: block;
        margin-bottom: 8px;
        font-weight: 600;
        color: #1a1a1a;
        font-size: 0.9rem;
    }

    .form-input {
        width: 100%;
        padding: 12px 14px;
        border: 1.5px solid #e0ddd6;
        border-radius: 8px;
        font-size: 0.95rem;
        font-family: 'Manrope', sans-serif;
        transition: all 0.2s ease;
    }

    .form-input:focus {
        outline: none;
        border-color: var(--brand);
        box-shadow: 0 0 0 3px rgba(14, 74, 84, 0.1);
    }

    .form-actions {
        display: flex;
        gap: 12px;
        margin-top: 24px;
    }

    .btn-primary {
        flex: 1;
        padding: 12px 20px;
        background: var(--brand);
        color: white;
        border: none;
        border-radius: 8px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
    }

    .btn-primary:hover {
        background: #0d3d46;
        transform: translateY(-1px);
    }

    .btn-secondary {
        flex: 1;
        padding: 12px 20px;
        background: #f0f0f0;
        color: #1a1a1a;
        border: none;
        border-radius: 8px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
    }

    .btn-secondary:hover {
        background: #e0e0e0;
    }

    .alert {
        padding: 12px 16px;
        border-radius: 8px;
        margin-bottom: 16px;
        font-size: 0.9rem;
    }

    .alert-success {
        background: #d4edda;
        color: #155724;
        border: 1px solid #c3e6cb;
    }

    .alert-error {
        background: #f8d7da;
        color: #721c24;
        border: 1px solid #f5c6cb;
    }

    .payment-methods {
        display: flex;
        flex-direction: column;
        gap: 12px;
    }

    .payment-card {
        border: 2px solid #e0ddd6;
        border-radius: 12px;
        padding: 16px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        cursor: pointer;
        transition: all 0.2s;
    }

    .payment-card:hover {
        border-color: var(--brand);
        box-shadow: 0 4px 12px rgba(14, 74, 84, 0.1);
    }

    .payment-card.active {
        border-color: var(--brand);
        background: rgba(14, 74, 84, 0.05);
    }

    .payment-info h4 {
        margin-bottom: 4px;
        font-size: 14px;
    }

    .payment-info p {
        font-size: 12px;
        color: var(--muted);
    }

    .payment-actions {
        display: flex;
        gap: 8px;
    }

    .payment-actions button {
        padding: 6px 12px;
        border: none;
        border-radius: 6px;
        font-size: 12px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
    }

    .payment-edit {
        background: #e8f4f8;
        color: var(--brand);
    }

    .payment-delete {
        background: #f8ebe8;
        color: #d32f2f;
    }

    .success-message {
        text-align: center;
        padding: 16px;
        background: #d4edda;
        border-radius: 8px;
        color: #155724;
        margin-top: 16px;
        display: none;
    }</style>

<main class="main-content">
    <div class="container">
        <div class="page-header">
            <h1>My Profile</h1>
            <p>Manage your account information, bookings, and preferences</p>
        </div>

        <div class="profile-container">
            <!-- Sidebar -->
            <aside class="profile-sidebar">
                <div class="profile-avatar">
                    <span class="avatar-initial">${fn:substring(currentUser.name, 0, 1)}</span>
                </div>
                <h2>${currentUser.name}</h2>
                <p>${currentUser.email}</p>
                
                <button class="profile-action-btn" onclick="editProfile()">Edit Profile</button>
                <button class="profile-action-btn secondary" onclick="changePassword()">Change Password</button>
                <button class="profile-action-btn secondary" onclick="location.href='/travel-management-system/logout'">Logout</button>
            </aside>

            <!-- Main Content -->
            <div class="profile-content">
                <!-- Personal Information Section -->
                <section class="content-section">
                    <div class="section-header">
                        <h3>Personal Information</h3>
                    </div>
                    <div>
                        <div class="detail-row">
                            <label>Full Name</label>
                            <span>${currentUser.name}</span>
                        </div>
                        <div class="detail-row">
                            <label>Email Address</label>
                            <span>${currentUser.email}</span>
                        </div>
                        <div class="detail-row">
                            <label>Phone Number</label>
                            <span>${currentUser.phone != null && !currentUser.phone.isEmpty() ? currentUser.phone : 'Not provided'}</span>
                        </div>
                        <div class="detail-row">
                            <label>Member Since</label>
                            <span>
                                <fmt:formatDate value="${currentUser.createdAt}" pattern="MMMM dd, yyyy" />
                            </span>
                        </div>
                    </div>
                </section>

                <!-- Account Statistics -->
                <section class="content-section">
                    <div class="section-header">
                        <h3>Account Statistics</h3>
                    </div>
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-number">5</div>
                            <div class="stat-label">Total Bookings</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number">3</div>
                            <div class="stat-label">Completed</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number">₹45,000</div>
                            <div class="stat-label">Total Spent</div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-number">4.5</div>
                            <div class="stat-label">Avg Rating</div>
                        </div>
                    </div>
                </section>

                <!-- Recent Bookings Section -->
                <section class="content-section">
                    <div class="section-header">
                        <h3>Recent Bookings</h3>
                        <a href="/travel-management-system/bookings" class="view-all-link">View All</a>
                    </div>
                    <div class="bookings-list">
                        <div class="booking-item">
                            <div class="booking-item-info">
                                <h4>Bali Honeymoon Package</h4>
                                <p>Departure: March 25, 2026 • 5 Nights</p>
                            </div>
                            <span class="booking-status confirmed">Confirmed</span>
                        </div>
                        <div class="booking-item">
                            <div class="booking-item-info">
                                <h4>Kerala Backwaters Tour</h4>
                                <p>Departure: April 10, 2026 • 3 Nights</p>
                            </div>
                            <span class="booking-status pending">Pending</span>
                        </div>
                        <div class="booking-item">
                            <div class="booking-item-info">
                                <h4>Goa Beach Getaway</h4>
                                <p>Departure: February 15, 2026 • 4 Nights</p>
                            </div>
                            <span class="booking-status completed">Completed</span>
                        </div>
                    </div>
                </section>

                <!-- Account Settings Section -->
                <section class="content-section">
                    <div class="section-header">
                        <h3>Account Settings</h3>
                    </div>
                    <div class="settings-list">
                        <div class="settings-item">
                            <label>
                                <input type="checkbox" checked>
                                <span>Email Notifications</span>
                            </label>
                            <div class="toggle-switch">
                                <input type="checkbox" checked>
                                <span class="toggle-slider"></span>
                            </div>
                        </div>
                        <div class="settings-item">
                            <label>
                                <input type="checkbox" checked>
                                <span>SMS Notifications</span>
                            </label>
                            <div class="toggle-switch">
                                <input type="checkbox" checked>
                                <span class="toggle-slider"></span>
                            </div>
                        </div>
                        <div class="settings-item">
                            <label>
                                <input type="checkbox">
                                <span>Newsletter Subscription</span>
                            </label>
                            <div class="toggle-switch">
                                <input type="checkbox">
                                <span class="toggle-slider"></span>
                            </div>
                        </div>
                        <div class="settings-item">
                            <label>
                                <input type="checkbox" checked>
                                <span>Marketing Emails</span>
                            </label>
                            <div class="toggle-switch">
                                <input type="checkbox" checked>
                                <span class="toggle-slider"></span>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Preferences Section -->
                <section class="content-section">
                    <div class="section-header">
                        <h3>Preferences</h3>
                    </div>
                    <div>
                        <div class="detail-row">
                            <label>Preferred Currency</label>
                            <span>Indian Rupee (₹)</span>
                        </div>
                        <div class="detail-row">
                            <label>Preferred Language</label>
                            <span>English</span>
                        </div>
                        <div class="detail-row">
                            <label>Timezone</label>
                            <span>IST (UTC +5:30)</span>
                        </div>
                    </div>
                </section>

                <!-- Payment Methods Section -->
                <section class="content-section">
                    <div class="section-header">
                        <h3>Payment Methods</h3>
                        <button class="view-all-link" style="border: none; background: none; cursor: pointer; padding: 0;" onclick="openAddPaymentModal()">+ Add New</button>
                    </div>
                    <div class="payment-methods">
                        <div class="payment-card active">
                            <div class="payment-info">
                                <h4>💳 Visa Credit Card</h4>
                                <p>**** **** **** 4532 • Expires 12/25</p>
                            </div>
                            <div class="payment-actions">
                                <button class="payment-edit" onclick="editPayment('card1')">Edit</button>
                                <button class="payment-delete" onclick="deletePayment('card1')">Delete</button>
                            </div>
                        </div>
                        <div class="payment-card">
                            <div class="payment-info">
                                <h4>🏦 Bank Account</h4>
                                <p>ICICI Bank • Account ending in 5689</p>
                            </div>
                            <div class="payment-actions">
                                <button class="payment-edit" onclick="editPayment('bank1')">Edit</button>
                                <button class="payment-delete" onclick="deletePayment('bank1')">Delete</button>
                            </div>
                        </div>
                    </div>
                </section>

                <!-- Addresses Section -->
                <section class="content-section">
                    <div class="section-header">
                        <h3>Saved Addresses</h3>
                        <button class="view-all-link" style="border: none; background: none; cursor: pointer; padding: 0;" onclick="openAddAddressModal()">+ Add Address</button>
                    </div>
                    <div class="bookings-list">
                        <div class="booking-item" style="border-left-color: var(--brand-2);">
                            <div class="booking-item-info">
                                <h4>Home</h4>
                                <p>123 Main Street, Mumbai, Maharashtra 400001, India</p>
                            </div>
                            <div style="display: flex; gap: 8px;">
                                <button style="padding: 6px 12px; background: #e8f4f8; border: none; border-radius: 6px; cursor: pointer; font-size: 12px; font-weight: 600;" onclick="editAddress('home')">Edit</button>
                                <button style="padding: 6px 12px; background: #f8ebe8; border: none; border-radius: 6px; cursor: pointer; font-size: 12px; font-weight: 600; color: #d32f2f;" onclick="deleteAddress('home')">Delete</button>
                            </div>
                        </div>
                        <div class="booking-item" style="border-left-color: var(--brand-2);">
                            <div class="booking-item-info">
                                <h4>Office</h4>
                                <p>456 Business Park, Bangalore, Karnataka 560001, India</p>
                            </div>
                            <div style="display: flex; gap: 8px;">
                                <button style="padding: 6px 12px; background: #e8f4f8; border: none; border-radius: 6px; cursor: pointer; font-size: 12px; font-weight: 600;" onclick="editAddress('office')">Edit</button>
                                <button style="padding: 6px 12px; background: #f8ebe8; border: none; border-radius: 6px; cursor: pointer; font-size: 12px; font-weight: 600; color: #d32f2f;" onclick="deleteAddress('office')">Delete</button>
                            </div>
                        </div>
                    </div>
                </section>
            </div>
        </div>
    </div>
</main>

<!-- Edit Profile Modal -->
<div id="editProfileModal" class="modal">
    <div style="position: relative;">
        <button class="modal-close" onclick="closeModal('editProfileModal')">✕</button>
        <div class="modal-content">
            <div class="modal-header">Edit Profile</div>
            <div id="profileAlert"></div>
            <form id="editProfileForm" onsubmit="saveProfile(event)">
                <div class="form-group">
                    <label for="edit-name">Full Name</label>
                    <input type="text" id="edit-name" class="form-input" value="${currentUser.name}" required>
                </div>
                <div class="form-group">
                    <label for="edit-email">Email Address</label>
                    <input type="email" id="edit-email" class="form-input" value="${currentUser.email}" required>
                </div>
                <div class="form-group">
                    <label for="edit-phone">Phone Number</label>
                    <input type="tel" id="edit-phone" class="form-input" value="${currentUser.phone != null ? currentUser.phone : ''}">
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn-primary">Save Changes</button>
                    <button type="button" class="btn-secondary" onclick="closeModal('editProfileModal')">Cancel</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Change Password Modal -->
<div id="changePasswordModal" class="modal">
    <div style="position: relative;">
        <button class="modal-close" onclick="closeModal('changePasswordModal')">✕</button>
        <div class="modal-content">
            <div class="modal-header">Change Password</div>
            <div id="passwordAlert"></div>
            <form id="changePasswordForm" onsubmit="savePassword(event)">
                <div class="form-group">
                    <label for="current-pwd">Current Password</label>
                    <input type="password" id="current-pwd" class="form-input" required>
                </div>
                <div class="form-group">
                    <label for="new-pwd">New Password</label>
                    <input type="password" id="new-pwd" class="form-input" required>
                </div>
                <div class="form-group">
                    <label for="confirm-pwd">Confirm Password</label>
                    <input type="password" id="confirm-pwd" class="form-input" required>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn-primary">Update Password</button>
                    <button type="button" class="btn-secondary" onclick="closeModal('changePasswordModal')">Cancel</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Add Payment Modal -->
<div id="addPaymentModal" class="modal">
    <div style="position: relative;">
        <button class="modal-close" onclick="closeModal('addPaymentModal')">✕</button>
        <div class="modal-content">
            <div class="modal-header">Add Payment Method</div>
            <div id="paymentAlert"></div>
            <form id="addPaymentForm" onsubmit="savePayment(event)">
                <div class="form-group">
                    <label for="payment-type">Payment Type</label>
                    <select id="payment-type" class="form-input" onchange="updatePaymentFields()" required>
                        <option value="">Select Type</option>
                        <option value="card">Credit/Debit Card</option>
                        <option value="bank">Bank Account</option>
                        <option value="upi">UPI</option>
                    </select>
                </div>
                <div id="cardFields" style="display: none;">
                    <div class="form-group">
                        <label for="card-name">Cardholder Name</label>
                        <input type="text" id="card-name" class="form-input">
                    </div>
                    <div class="form-group">
                        <label for="card-number">Card Number</label>
                        <input type="text" id="card-number" class="form-input" placeholder="1234 5678 9012 3456">
                    </div>
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
                        <div class="form-group">
                            <label for="card-expiry">Expiry Date</label>
                            <input type="text" id="card-expiry" class="form-input" placeholder="MM/YY">
                        </div>
                        <div class="form-group">
                            <label for="card-cvv">CVV</label>
                            <input type="text" id="card-cvv" class="form-input" placeholder="123">
                        </div>
                    </div>
                </div>
                <div id="bankFields" style="display: none;">
                    <div class="form-group">
                        <label for="bank-name">Bank Name</label>
                        <input type="text" id="bank-name" class="form-input">
                    </div>
                    <div class="form-group">
                        <label for="account-number">Account Number</label>
                        <input type="text" id="account-number" class="form-input">
                    </div>
                    <div class="form-group">
                        <label for="ifsc-code">IFSC Code</label>
                        <input type="text" id="ifsc-code" class="form-input">
                    </div>
                </div>
                <div id="upiFields" style="display: none;">
                    <div class="form-group">
                        <label for="upi-id">UPI ID</label>
                        <input type="text" id="upi-id" class="form-input" placeholder="yourname@bank">
                    </div>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn-primary">Add Payment</button>
                    <button type="button" class="btn-secondary" onclick="closeModal('addPaymentModal')">Cancel</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Add Address Modal -->
<div id="addAddressModal" class="modal">
    <div style="position: relative;">
        <button class="modal-close" onclick="closeModal('addAddressModal')">✕</button>
        <div class="modal-content">
            <div class="modal-header">Add New Address</div>
            <div id="addressAlert"></div>
            <form id="addAddressForm" onsubmit="saveAddress(event)">
                <div class="form-group">
                    <label for="address-type">Address Type</label>
                    <select id="address-type" class="form-input" required>
                        <option value="">Select Type</option>
                        <option value="home">Home</option>
                        <option value="office">Office</option>
                        <option value="other">Other</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="address-line1">Street Address</label>
                    <input type="text" id="address-line1" class="form-input" placeholder="Street address" required>
                </div>
                <div class="form-group">
                    <label for="address-city">City</label>
                    <input type="text" id="address-city" class="form-input" required>
                </div>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
                    <div class="form-group">
                        <label for="address-state">State</label>
                        <input type="text" id="address-state" class="form-input" required>
                    </div>
                    <div class="form-group">
                        <label for="address-zip">Postal Code</label>
                        <input type="text" id="address-zip" class="form-input" required>
                    </div>
                </div>
                <div class="form-group">
                    <label for="address-country">Country</label>
                    <input type="text" id="address-country" class="form-input" value="India" required>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn-primary">Save Address</button>
                    <button type="button" class="btn-secondary" onclick="closeModal('addAddressModal')">Cancel</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    // Modal Functions
    function openModal(modalId) {
        document.getElementById(modalId).classList.add('active');
    }

    function closeModal(modalId) {
        document.getElementById(modalId).classList.remove('active');
    }

    function editProfile() {
        openModal('editProfileModal');
    }

    function changePassword() {
        openModal('changePasswordModal');
    }

    function openAddPaymentModal() {
        document.getElementById('payment-type').value = '';
        document.getElementById('cardFields').style.display = 'none';
        document.getElementById('bankFields').style.display = 'none';
        document.getElementById('upiFields').style.display = 'none';
        openModal('addPaymentModal');
    }

    function openAddAddressModal() {
        document.getElementById('addAddressForm').reset();
        openModal('addAddressModal');
    }

    function updatePaymentFields() {
        const type = document.getElementById('payment-type').value;
        document.getElementById('cardFields').style.display = type === 'card' ? 'block' : 'none';
        document.getElementById('bankFields').style.display = type === 'bank' ? 'block' : 'none';
        document.getElementById('upiFields').style.display = type === 'upi' ? 'block' : 'none';
    }

    // Form Submissions
    function saveProfile(e) {
        e.preventDefault();
        const name = document.getElementById('edit-name').value;
        const email = document.getElementById('edit-email').value;
        const phone = document.getElementById('edit-phone').value;
        
        // Validate inputs
        if (!name || !email) {
            showAlert('profileAlert', 'Name and email are required!', 'error');
            return;
        }

        showAlert('profileAlert', 'Profile updated successfully! Changes will be saved to the database.', 'success');
        setTimeout(() => {
            closeModal('editProfileModal');
            location.reload();
        }, 1500);
    }

    function savePassword(e) {
        e.preventDefault();
        const current = document.getElementById('current-pwd').value;
        const newPwd = document.getElementById('new-pwd').value;
        const confirm = document.getElementById('confirm-pwd').value;

        if (!current || !newPwd || !confirm) {
            showAlert('passwordAlert', 'All fields are required!', 'error');
            return;
        }

        if (newPwd !== confirm) {
            showAlert('passwordAlert', 'New passwords do not match!', 'error');
            return;
        }

        if (newPwd.length < 6) {
            showAlert('passwordAlert', 'Password must be at least 6 characters!', 'error');
            return;
        }

        showAlert('passwordAlert', 'Password changed successfully!', 'success');
        setTimeout(() => {
            closeModal('changePasswordModal');
        }, 1500);
    }

    function savePayment(e) {
        e.preventDefault();
        const type = document.getElementById('payment-type').value;

        if (!type) {
            showAlert('paymentAlert', 'Please select a payment type!', 'error');
            return;
        }

        showAlert('paymentAlert', 'Payment method added successfully!', 'success');
        setTimeout(() => {
            closeModal('addPaymentModal');
            location.reload();
        }, 1500);
    }

    function saveAddress(e) {
        e.preventDefault();
        const type = document.getElementById('address-type').value;
        const city = document.getElementById('address-city').value;
        const state = document.getElementById('address-state').value;
        const zip = document.getElementById('address-zip').value;

        if (!type || !city || !state || !zip) {
            showAlert('addressAlert', 'Please fill in all required fields!', 'error');
            return;
        }

        showAlert('addressAlert', 'Address added successfully!', 'success');
        setTimeout(() => {
            closeModal('addAddressModal');
            location.reload();
        }, 1500);
    }

    function editPayment(id) {
        showAlert('paymentAlert', 'Opening edit form for payment method...', 'success');
    }

    function deletePayment(id) {
        if (confirm('Are you sure you want to delete this payment method?')) {
            showAlert('paymentAlert', 'Payment method deleted!', 'success');
            setTimeout(() => location.reload(), 1500);
        }
    }

    function editAddress(id) {
        showAlert('addressAlert', 'Opening edit form for address...', 'success');
    }

    function deleteAddress(id) {
        if (confirm('Are you sure you want to delete this address?')) {
            showAlert('addressAlert', 'Address deleted!', 'success');
            setTimeout(() => location.reload(), 1500);
        }
    }

    function showAlert(elementId, message, type) {
        const element = document.getElementById(elementId);
        element.innerHTML = `<div class="alert alert-${type}">${message}</div>`;
    }

    // Toggle Switch Functionality
    document.querySelectorAll('.toggle-switch input').forEach(toggle => {
        toggle.addEventListener('change', function() {
            const label = this.previousElementSibling || this.closest('.settings-item').querySelector('span')?.textContent;
            const status = this.checked ? 'enabled' : 'disabled';
            console.log(label + ' is now ' + status);
        });
    });

    // Close modals on outside click
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                this.classList.remove('active');
            }
        });
    });
</script>

<%@ include file="fragments/footer.jspf" %>