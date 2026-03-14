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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=3" />
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
    <div class="auth-swap <c:if test="${authMode eq 'register'}">is-register</c:if>">
        <div class="auth-swap__decor">
            <div class="auth-swap__shape auth-swap__shape--ring"></div>
            <div class="auth-swap__shape auth-swap__shape--glow"></div>
            <div class="auth-swap__shape auth-swap__shape--spark"></div>
        </div>
        <c:if test="${not empty message}">
            <div class="auth-swap__banner">${message}</div>
        </c:if>
        <div class="auth-swap__container">
            <div class="auth-swap__forms">
                <div class="auth-panel auth-panel--login">
                    <div class="auth-panel__brand">
                        <div class="auth-panel__mark"></div>
                        <div>
                            <div class="auth-panel__name">AeroTrail</div>
                            <div class="auth-panel__tag">Travel Management</div>
                        </div>
                    </div>
                    <div class="auth-panel__copy">
                        <h1>Welcome back</h1>
                        <p>Sign in to your account to continue your journey.</p>
                    </div>
                    <form class="auth-form" method="post" action="${pageContext.request.contextPath}/login">
                        <input type="hidden" name="next" value="${next}" />
                        <label class="auth-label" for="login-email">Email Address</label>
                        <input class="auth-input" type="email" id="login-email" name="email" required autocomplete="username" />
                        <label class="auth-label" for="login-password">Password</label>
                        <input class="auth-input" type="password" id="login-password" name="password" required autocomplete="current-password" />
                        <button class="auth-primary" type="submit">Sign In</button>
                    </form>
                    <div class="auth-panel__footer">
                        <span>Don't have an account?</span>
                        <button class="auth-inline-toggle" onclick="window.location.href='${registerUrl}'">Create one</button>
                    </div>
                </div>
                <div class="auth-panel auth-panel--register">
                    <div class="auth-panel__brand">
                        <div class="auth-panel__mark"></div>
                        <div>
                            <div class="auth-panel__name">AeroTrail</div>
                            <div class="auth-panel__tag">Travel Management</div>
                        </div>
                    </div>
                    <div class="auth-panel__copy">
                        <h1>Join AeroTrail</h1>
                        <p>Create your account to start planning amazing trips.</p>
                    </div>
                    <form class="auth-form" method="post" action="${pageContext.request.contextPath}/users/add">
                        <input type="hidden" name="next" value="${next}" />
                        <input type="hidden" name="autoLogin" value="true" />
                        <label class="auth-label" for="register-name">Full Name</label>
                        <input class="auth-input" type="text" id="register-name" name="name" required />
                        <label class="auth-label" for="register-email">Email Address</label>
                        <input class="auth-input" type="email" id="register-email" name="email" required autocomplete="username" />
                        <label class="auth-label" for="register-phone">Phone Number</label>
                        <input class="auth-input" type="tel" id="register-phone" name="phone" required />
                        <label class="auth-label" for="register-password">Password</label>
                        <input class="auth-input" type="password" id="register-password" name="password" required autocomplete="new-password" />
                        <button class="auth-primary" type="submit">Create Account</button>
                    </form>
                    <div class="auth-panel__footer">
                        <span>Already have an account?</span>
                        <button class="auth-inline-toggle" onclick="window.location.href='${loginUrl}'">Sign in</button>
                    </div>
                </div>
            </div>
            <div class="auth-swap__aside" aria-hidden="true">
                <div class="auth-aside__stack auth-aside__stack--login">
                    <div class="auth-aside__card auth-aside__card--hero">
                        <svg width="100%" height="100%" viewBox="0 0 360 420" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Skyline route illustration">
                            <defs>
                                <linearGradient id="authLoginHeroBg" x1="0%" y1="0%" x2="100%" y2="100%">
                                    <stop offset="0%" stop-color="#ffe5c7" />
                                    <stop offset="100%" stop-color="#c7e8f2" />
                                </linearGradient>
                                <linearGradient id="authLoginHeroTrail" x1="0%" y1="0%" x2="100%" y2="0%">
                                    <stop offset="0%" stop-color="#ff7a47" />
                                    <stop offset="100%" stop-color="#0e4a54" />
                                </linearGradient>
                            </defs>
                            <rect x="0" y="0" width="360" height="420" rx="28" fill="url(#authLoginHeroBg)" />
                            <circle cx="70" cy="90" r="32" fill="#ffffff" opacity="0.6" />
                            <circle cx="280" cy="120" r="20" fill="#ffffff" opacity="0.5" />
                            <path d="M40 300 C90 250 170 240 230 260 C290 280 330 330 340 300" stroke="#ffffff" stroke-width="8" fill="none" opacity="0.8"/>
                            <path d="M90 300 L130 250 L170 300" stroke="#ffffff" stroke-width="10" fill="none" stroke-linecap="round" />
                            <path d="M200 280 C220 250 250 230 280 220" stroke="url(#authLoginHeroTrail)" stroke-width="6" fill="none" stroke-linecap="round" />
                            <circle cx="290" cy="215" r="8" fill="#ffffff" />
                            <path d="M210 215 L244 198 L236 230 Z" fill="#ffffff" opacity="0.9" />
                        </svg>
                    </div>
                    <div class="auth-aside__card auth-aside__card--mini">
                        <svg width="100%" height="100%" viewBox="0 0 240 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Suitcase illustration">
                            <defs>
                                <linearGradient id="authLoginMiniBg" x1="0%" y1="0%" x2="100%" y2="100%">
                                    <stop offset="0%" stop-color="#fff2e6" />
                                    <stop offset="100%" stop-color="#d9efe8" />
                                </linearGradient>
                            </defs>
                            <rect x="0" y="0" width="240" height="240" rx="24" fill="url(#authLoginMiniBg)" />
                            <rect x="58" y="78" width="124" height="96" rx="18" fill="#ffffff" opacity="0.9" />
                            <rect x="88" y="60" width="64" height="28" rx="12" fill="#ffffff" opacity="0.95" />
                            <circle cx="92" cy="184" r="10" fill="#ffffff" opacity="0.9" />
                            <circle cx="148" cy="184" r="10" fill="#ffffff" opacity="0.9" />
                            <path d="M78 120 H162" stroke="#0e4a54" stroke-width="6" stroke-linecap="round" opacity="0.5" />
                        </svg>
                    </div>
                </div>
                <div class="auth-aside__stack auth-aside__stack--register">
                    <div class="auth-aside__card auth-aside__card--hero">
                        <svg width="100%" height="100%" viewBox="0 0 400 420" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Travel illustration">
                            <defs>
                                <linearGradient id="authRegisterHeroBg" x1="0%" y1="0%" x2="100%" y2="100%">
                                    <stop offset="0%" stop-color="#ff7a47" />
                                    <stop offset="100%" stop-color="#1f78ff" />
                                </linearGradient>
                            </defs>
                            <rect x="0" y="0" width="400" height="420" rx="32" fill="url(#authRegisterHeroBg)" opacity="0.18" />
                            <path d="M80 320 C90 280 160 260 220 270 C280 280 340 320 360 280" stroke="#ffffff" stroke-width="6" fill="none" opacity="0.85"/>
                            <circle cx="60" cy="120" r="28" fill="#ffffff" opacity="0.6" />
                            <circle cx="320" cy="90" r="18" fill="#ffffff" opacity="0.5" />
                            <path d="M120 310 L160 260 L200 310" stroke="#ffffff" stroke-width="10" fill="none" stroke-linecap="round" />
                            <path d="M210 240 L260 200" stroke="#ffffff" stroke-width="8" stroke-linecap="round" />
                            <path d="M260 200 L300 220 L280 240" stroke="#ffffff" stroke-width="8" fill="none" stroke-linecap="round" />
                            <circle cx="310" cy="250" r="10" fill="#ffffff" />
                        </svg>
                    </div>
                    <div class="auth-aside__card auth-aside__card--mini">
                        <svg width="100%" height="100%" viewBox="0 0 240 240" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Compass illustration">
                            <defs>
                                <linearGradient id="authRegisterMiniBg" x1="0%" y1="0%" x2="100%" y2="100%">
                                    <stop offset="0%" stop-color="#ffe7d1" />
                                    <stop offset="100%" stop-color="#cfe8f9" />
                                </linearGradient>
                            </defs>
                            <rect x="0" y="0" width="240" height="240" rx="24" fill="url(#authRegisterMiniBg)" />
                            <circle cx="120" cy="120" r="64" fill="#ffffff" opacity="0.9" />
                            <circle cx="120" cy="120" r="44" fill="none" stroke="#0e4a54" stroke-width="6" opacity="0.5" />
                            <path d="M120 78 L138 122 L120 162 L102 118 Z" fill="#ff7a47" opacity="0.9" />
                            <circle cx="120" cy="120" r="6" fill="#0e4a54" />
                        </svg>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
