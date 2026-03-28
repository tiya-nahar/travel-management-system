<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="My Profile" />
<%@ include file="fragments/header.jspf" %>

<style>
    .profile-grid {
        display: grid;
        gap: 20px;
        grid-template-columns: 1fr;
    }

    @media (min-width: 980px) {
        .profile-grid {
            grid-template-columns: 360px 1fr;
        }
    }

    .profile-panel {
        background: var(--surface);
        border-radius: var(--radius);
        box-shadow: var(--shadow);
        padding: 20px;
    }

    .profile-title {
        margin: 0 0 10px;
    }

    .profile-meta {
        color: var(--muted);
        margin: 0 0 16px;
    }

    .profile-form {
        display: grid;
        gap: 12px;
    }

    .profile-form label {
        font-weight: 600;
    }

    .profile-form input {
        width: 100%;
        padding: 10px 12px;
        border: 1px solid var(--line);
        border-radius: 8px;
        font-family: inherit;
    }

    .profile-form button {
        justify-self: start;
    }

    .stats-grid {
        display: grid;
        gap: 12px;
        grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    }

    .stat-card {
        background: var(--bg);
        border-radius: 10px;
        border: 1px solid var(--line);
        padding: 12px;
    }

    .stat-card .value {
        display: block;
        font-size: 1.5rem;
        font-weight: 700;
        margin-bottom: 6px;
    }

    .stack-list {
        display: grid;
        gap: 10px;
    }

    .stack-item {
        border: 1px solid var(--line);
        border-radius: 8px;
        padding: 10px 12px;
        background: var(--bg);
    }

    .stack-item h4 {
        margin: 0 0 4px;
        font-size: 1rem;
    }

    .stack-item p {
        margin: 0;
        color: var(--muted);
        font-size: 0.92rem;
    }

    .section-gap {
        margin-top: 20px;
    }

    .alert-box {
        margin-bottom: 12px;
    }

    .alert-box.error {
        background: #f8d7da;
        color: #721c24;
        border: 1px solid #f5c6cb;
        border-radius: 8px;
        padding: 10px 12px;
    }

    .alert-box.success {
        background: #d4edda;
        color: #155724;
        border: 1px solid #c3e6cb;
        border-radius: 8px;
        padding: 10px 12px;
    }

    .quick-links {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        margin-top: 12px;
    }
</style>

<div class="page-head">
    <div>
        <h1>My Profile</h1>
        <p>Update account details, track bookings, payments, and reviews.</p>
    </div>
</div>

<c:if test="${not empty error}">
    <div class="alert-box error">${error}</div>
</c:if>
<c:if test="${not empty message}">
    <div class="alert-box success">${message}</div>
</c:if>

<div class="profile-grid">
    <aside class="profile-panel">
        <h2 class="profile-title">${currentUser.name}</h2>
        <p class="profile-meta">${currentUser.email}</p>

        <form class="profile-form" action="${pageContext.request.contextPath}/profile" method="post">
            <input type="hidden" name="action" value="updateProfile" />

            <label for="name">Full Name</label>
            <input id="name" name="name" type="text" value="${currentUser.name}" required />

            <label for="email">Email Address</label>
            <input id="email" name="email" type="email" value="${currentUser.email}" required />

            <label for="phone">Phone Number</label>
            <input id="phone" name="phone" type="text" value="${currentUser.phone}" placeholder="Enter phone number" />

            <button class="btn btn-primary" type="submit">Save Profile</button>
        </form>

        <div class="section-gap" id="security">
            <h3>Change Password</h3>
            <form class="profile-form" action="${pageContext.request.contextPath}/profile" method="post">
                <input type="hidden" name="action" value="changePassword" />

                <label for="currentPassword">Current Password</label>
                <input id="currentPassword" name="currentPassword" type="password" required />

                <label for="newPassword">New Password</label>
                <input id="newPassword" name="newPassword" type="password" minlength="6" required />

                <label for="confirmPassword">Confirm New Password</label>
                <input id="confirmPassword" name="confirmPassword" type="password" minlength="6" required />

                <button class="btn btn-ghost" type="submit">Update Password</button>
            </form>
        </div>

        <div class="quick-links">
            <a class="btn btn-soft" href="${pageContext.request.contextPath}/bookings">My Trips</a>
            <a class="btn btn-soft" href="${pageContext.request.contextPath}/payments">Payments</a>
            <a class="btn btn-soft" href="${pageContext.request.contextPath}/reviews">My Reviews</a>
            <a class="btn btn-soft" href="${pageContext.request.contextPath}/logout">Logout</a>
        </div>
    </aside>

    <section class="profile-panel">
        <h2 class="profile-title">Account Snapshot</h2>
        <p class="profile-meta">
            Member since
            <fmt:formatDate value="${currentUser.createdAt}" pattern="MMMM dd, yyyy" />
        </p>

        <div class="stats-grid">
            <div class="stat-card">
                <span class="value">${totalBookings}</span>
                <span>Total Bookings</span>
            </div>
            <div class="stat-card">
                <span class="value">${completedBookings}</span>
                <span>Completed Trips</span>
            </div>
            <div class="stat-card">
                <span class="value">Rs. <fmt:formatNumber value="${totalSpent}" type="number" /></span>
                <span>Total Paid</span>
            </div>
            <div class="stat-card">
                <span class="value"><fmt:formatNumber value="${averageRating}" type="number" maxFractionDigits="1" /></span>
                <span>Average Rating</span>
            </div>
        </div>

        <div class="section-gap">
            <h3>Recent Bookings</h3>
            <div class="stack-list">
                <c:choose>
                    <c:when test="${empty recentBookings}">
                        <div class="stack-item"><p>No bookings yet.</p></div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="b" items="${recentBookings}">
                            <div class="stack-item">
                                <h4>${b.packageTitle}</h4>
                                <p>Travel: ${b.travelDate} | People: ${b.numberOfPeople} | Status: ${b.status}</p>
                            </div>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="section-gap">
            <h3>Recent Payments</h3>
            <div class="stack-list">
                <c:choose>
                    <c:when test="${empty payments}">
                        <div class="stack-item"><p>No payments found.</p></div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="p" items="${payments}" varStatus="st">
                            <c:if test="${st.index lt 3}">
                                <div class="stack-item">
                                    <h4>Booking #${p.bookingId} | Rs. <fmt:formatNumber value="${p.amount}" type="number" /></h4>
                                    <p>${p.paymentMethod} | ${p.paymentStatus} | ${p.paymentDate}</p>
                                </div>
                            </c:if>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="section-gap">
            <h3>My Reviews</h3>
            <div class="stack-list">
                <c:choose>
                    <c:when test="${empty reviews}">
                        <div class="stack-item"><p>You have not added any reviews yet.</p></div>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="r" items="${reviews}" varStatus="st">
                            <c:if test="${st.index lt 3}">
                                <div class="stack-item">
                                    <h4>${r.packageTitle} | ${r.rating}/5</h4>
                                    <p>${r.comment}</p>
                                </div>
                            </c:if>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </section>
</div>

<%@ include file="fragments/footer.jspf" %>
