<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>AeroTrail | User Login</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;700;800&family=Space+Grotesk:wght@600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
</head>
<c:set var="authMode" value="${param.mode eq 'register' or mode eq 'register' ? 'register' : 'login'}" />
<c:url var="loginUrl" value="/login">
    <c:if test="${not empty next}">
        <c:param name="next" value="${next}" />
    </c:if>
</c:url>
<c:url var="registerUrl" value="/login">
    <c:param name="mode" value="register" />
    <c:if test="${not empty next}">
        <c:param name="next" value="${next}" />
    </c:if>
</c:url>
<c:set var="closeTarget" value="${not empty next ? next : '/dashboard'}" />
<body class="auth-body">
<div class="auth-duo-stage" data-auth-root>
    <div class="auth-duo-decor">
        <span class="auth-duo-blob auth-duo-blob--sky"></span>
        <span class="auth-duo-blob auth-duo-blob--coral"></span>
        <span class="auth-duo-blob auth-duo-blob--gold"></span>
        <span class="auth-duo-gridline auth-duo-gridline--one"></span>
        <span class="auth-duo-gridline auth-duo-gridline--two"></span>
    </div>

    <c:if test="${not empty message}">
        <div class="auth-duo-banner ${authMode eq 'register' ? 'is-register' : 'is-login'}">${message}</div>
    </c:if>

    <div class="auth-duo-grid">
        <section class="auth-duo-card auth-duo-card--login ${authMode eq 'login' ? 'is-current' : ''}" data-auth-card="login">
            <div class="auth-duo-card__shine"></div>
            <div class="auth-duo-card__inner">
                <div class="auth-duo-brand">
                    <span class="auth-duo-brand__mark"></span>
                    <div>
                        <div class="auth-duo-brand__name">AeroTrail</div>
                        <div class="auth-duo-brand__tag">Traveler Login</div>
                    </div>
                </div>

                <div class="auth-duo-copy">
                    <h1>Sign In</h1>
                    <p>Access your bookings, payments, and reviews with your own session.</p>
                </div>

                <form class="auth-duo-form" method="post" action="${pageContext.request.contextPath}/login">
                    <input type="hidden" name="next" value="${next}" />

                    <label class="auth-duo-field-label" for="loginEmail">Email Address</label>
                    <input class="auth-duo-input auth-duo-input--warm" id="loginEmail" name="email" type="email" autocomplete="username" placeholder="ar@gmail.com" required />

                    <label class="auth-duo-field-label" for="loginPassword">Password</label>
                    <input class="auth-duo-input auth-duo-input--warm" id="loginPassword" name="password" type="password" autocomplete="current-password" placeholder="Enter password" required />

                    <button class="auth-duo-button auth-duo-button--login" type="submit">Sign In</button>
                </form>

                <div class="auth-duo-footer">
                    <a href="${pageContext.request.contextPath}${closeTarget}">Back to site</a>
                    <span>Already registered travelers can sign in here.</span>
                </div>
            </div>
        </section>

        <section class="auth-duo-card auth-duo-card--register ${authMode eq 'register' ? 'is-current' : ''}" data-auth-card="register">
            <div class="auth-duo-card__shine"></div>
            <div class="auth-duo-card__inner">
                <div class="auth-duo-header-row">
                    <div class="auth-duo-brand">
                        <span class="auth-duo-brand__mark"></span>
                        <div>
                            <div class="auth-duo-brand__name">Create Account</div>
                            <div class="auth-duo-brand__tag">New Traveler</div>
                        </div>
                    </div>
                    <div class="auth-duo-socials" aria-hidden="true">
                        <span>f</span>
                        <span>G+</span>
                        <span>in</span>
                    </div>
                </div>

                <div class="auth-duo-copy auth-duo-copy--register">
                    <h1>Register</h1>
                    <p>Create your customer account and start your own booking session right away.</p>
                </div>

                <form class="auth-duo-form auth-duo-form--register" method="post" action="${pageContext.request.contextPath}/users/add">
                    <input type="hidden" name="autoLogin" value="true" />
                    <input type="hidden" name="next" value="${next}" />

                    <label class="auth-duo-field-label" for="registerName">Full Name</label>
                    <input class="auth-duo-input" id="registerName" name="name" type="text" autocomplete="name" required />

                    <label class="auth-duo-field-label" for="registerEmail">Email Address</label>
                    <input class="auth-duo-input" id="registerEmail" name="email" type="email" autocomplete="email" required />

                    <label class="auth-duo-field-label" for="registerPhone">Phone Number</label>
                    <input class="auth-duo-input" id="registerPhone" name="phone" type="tel" autocomplete="tel" required />

                    <label class="auth-duo-field-label" for="registerPassword">Password</label>
                    <input class="auth-duo-input" id="registerPassword" name="password" type="password" autocomplete="new-password" required />

                    <button class="auth-duo-button auth-duo-button--register" type="submit">Create Account</button>
                </form>

                <div class="auth-duo-register-links">
                    <a href="${loginUrl}">Already have an account?</a>
                    <a href="${registerUrl}">Stay on register view</a>
                </div>
            </div>
        </section>
    </div>
</div>
<script defer src="${pageContext.request.contextPath}/assets/js/site.js"></script>
</body>
</html>
