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
<div class="auth-swap ${authMode eq 'register' ? 'is-register' : 'is-login'}" data-auth-root>
    <div class="auth-swap__decor">
        <span class="auth-swap__shape auth-swap__shape--ring"></span>
        <span class="auth-swap__shape auth-swap__shape--glow"></span>
        <span class="auth-swap__shape auth-swap__shape--spark"></span>
    </div>

    <c:if test="${not empty message}">
        <div class="auth-swap__banner">${message}</div>
    </c:if>

    <div class="auth-swap__container">
        <div class="auth-swap__forms">
            <section class="auth-panel auth-panel--login" data-auth-panel="login">
                <div class="auth-panel__brand">
                    <span class="auth-panel__mark"></span>
                    <div>
                        <div class="auth-panel__name">AeroTrail</div>
                        <div class="auth-panel__tag">Traveler Login</div>
                    </div>
                </div>

                <div class="auth-panel__copy">
                    <h1>Sign In</h1>
                    <p>Access your bookings, payments, and reviews with your own session.</p>
                </div>

                <form class="auth-form" method="post" action="${pageContext.request.contextPath}/login">
                    <input type="hidden" name="next" value="${next}" />

                    <label class="auth-field" for="loginEmail">Email Address</label>
                    <input class="auth-input" id="loginEmail" name="email" type="email" autocomplete="username" placeholder="ar@gmail.com" required />

                    <label class="auth-field" for="loginPassword">Password</label>
                    <input class="auth-input" id="loginPassword" name="password" type="password" autocomplete="current-password" placeholder="Enter password" required />

                    <button class="auth-primary" type="submit">Sign In</button>
                </form>

                <div class="auth-panel__footer">
                    <a href="${pageContext.request.contextPath}${closeTarget}">Back to site</a>
                    <button class="auth-inline-toggle" type="button" data-auth-toggle="register">Need an account?</button>
                </div>
            </section>

            <section class="auth-panel auth-panel--register" data-auth-panel="register">
                <div class="auth-panel__header-row">
                    <div class="auth-panel__brand">
                        <span class="auth-panel__mark"></span>
                        <div>
                            <div class="auth-panel__name">Create Account</div>
                            <div class="auth-panel__tag">New Traveler</div>
                        </div>
                    </div>
                    <div class="auth-panel__socials" aria-hidden="true">
                        <span>f</span>
                        <span>G+</span>
                        <span>in</span>
                    </div>
                </div>

                <div class="auth-panel__copy">
                    <h1>Register</h1>
                    <p>Create your customer account and start your own booking session right away.</p>
                </div>

                <form class="auth-form" method="post" action="${pageContext.request.contextPath}/users/add">
                    <input type="hidden" name="autoLogin" value="true" />
                    <input type="hidden" name="next" value="${next}" />

                    <label class="auth-field" for="registerName">Full Name</label>
                    <input class="auth-input" id="registerName" name="name" type="text" autocomplete="name" required />

                    <label class="auth-field" for="registerEmail">Email Address</label>
                    <input class="auth-input" id="registerEmail" name="email" type="email" autocomplete="email" required />

                    <label class="auth-field" for="registerPhone">Phone Number</label>
                    <input class="auth-input" id="registerPhone" name="phone" type="tel" autocomplete="tel" required />

                    <label class="auth-field" for="registerPassword">Password</label>
                    <input class="auth-input" id="registerPassword" name="password" type="password" autocomplete="new-password" required />

                    <button class="auth-primary auth-primary--alt" type="submit">Create Account</button>
                </form>

                <div class="auth-panel__footer">
                    <button class="auth-inline-toggle" type="button" data-auth-toggle="login">Already registered?</button>
                    <a href="${registerUrl}">Stay on register view</a>
                </div>
            </section>
        </div>

        <div class="auth-swap__overlay">
            <div class="auth-overlay">
                <div class="auth-overlay__panel auth-overlay__panel--left">
                    <h2>Welcome Back!</h2>
                    <p>Keep your bookings handy by signing in with your personal details.</p>
                    <button class="auth-ghost" type="button" data-auth-toggle="login">Sign In</button>
                </div>
                <div class="auth-overlay__panel auth-overlay__panel--right">
                    <h2>New Here?</h2>
                    <p>Create an account and start tracking your trips and reviews.</p>
                    <button class="auth-ghost" type="button" data-auth-toggle="register">Sign Up</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script defer src="${pageContext.request.contextPath}/assets/js/site.js"></script>
</body>
</html>
