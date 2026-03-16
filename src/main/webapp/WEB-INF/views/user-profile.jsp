<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="My Profile" />
<%@ include file="fragments/header.jspf" %>

<main class="main-content">
    <div class="container">
        <div class="page-header">
            <h1>My Profile</h1>
            <p>Manage your account information</p>
        </div>

        <div class="profile-card">
            <div class="profile-header">
                <div class="profile-avatar">
                    <span class="avatar-initial">${fn:substring(currentUser.name, 0, 1)}</span>
                </div>
                <div class="profile-info">
                    <h2>${currentUser.name}</h2>
                    <p>${currentUser.email}</p>
                </div>
            </div>

            <div class="profile-details">
                <div class="detail-row">
                    <label>Name:</label>
                    <span>${currentUser.name}</span>
                </div>
                <div class="detail-row">
                    <label>Email:</label>
                    <span>${currentUser.email}</span>
                </div>
                <div class="detail-row">
                    <label>Phone:</label>
                    <span>${currentUser.phone != null ? currentUser.phone : 'Not provided'}</span>
                </div>
                <div class="detail-row">
                    <label>Member Since:</label>
                    <span><fmt:formatDate value="${currentUser.createdAt}" pattern="yyyy-MM-dd" /></span>
                </div>
            </div>
        </div>
    </div>
</main>

<%@ include file="fragments/footer.jspf" %>