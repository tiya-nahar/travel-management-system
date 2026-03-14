<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>AeroTrail | Admin Login</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;700;800&family=Space+Grotesk:wght@600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
</head>
<body class="admin-body">
<div class="admin-login">
    <section class="admin-card">
        <div class="brand">
            <span class="brand-mark"></span>
            <div class="brand-text">
                <div class="brand-name">AeroTrail Admin</div>
                <div class="brand-tag">Secure Console</div>
            </div>
        </div>
        <h1>Administrator Login</h1>
        <p class="meta">Use a real admin account stored in your database.</p>

        <c:if test="${not empty message}">
            <div class="panel" style="margin-top: 14px; padding: 12px;">${message}</div>
        </c:if>

        <form class="admin-form" method="post" action="${pageContext.request.contextPath}/admin-login">
            <label class="input-wrap">
                <span>Email Address</span>
                <input name="email" type="email" autocomplete="username" required />
            </label>
            <label class="input-wrap">
                <span>Password</span>
                <input name="password" type="password" autocomplete="current-password" required />
            </label>
            <button class="btn btn-primary" type="submit">Sign In</button>
        </form>

        <div class="admin-footer">
            <a href="${pageContext.request.contextPath}/dashboard">Back to site</a>
            <span class="meta">Need help? Contact support.</span>
        </div>
    </section>
</div>
</body>
</html>
