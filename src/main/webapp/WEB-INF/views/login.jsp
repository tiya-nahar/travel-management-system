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
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;700;800&family=Space+Grotesk:wght@600;700&family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css?v=4" />
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
<body class="auth-cinematic">
    <div class="auth-scene" aria-hidden="true">
        <div class="auth-scene__image"></div>
        <div class="auth-scene__overlay"></div>
        <div class="auth-route"></div>
        <div class="auth-stars"></div>
        <svg class="auth-plane" viewBox="0 0 64 64" aria-hidden="true" focusable="false">
            <path d="M4 32 L60 18 L56 30 L60 46 L4 32 Z" fill="currentColor" opacity="0.9" />
            <path d="M20 30 L28 20 L34 22 L28 32 Z" fill="currentColor" opacity="0.7" />
        </svg>
        <div class="auth-floats">
            <svg class="auth-float auth-float--compass" viewBox="0 0 64 64" aria-hidden="true" focusable="false">
                <circle cx="32" cy="32" r="26" fill="rgba(255,255,255,0.12)" stroke="rgba(255,255,255,0.5)" stroke-width="2" />
                <path d="M32 14 L40 32 L32 50 L24 32 Z" fill="#ff7a18" opacity="0.9" />
                <circle cx="32" cy="32" r="4" fill="#ffffff" />
            </svg>
            <svg class="auth-float auth-float--pin" viewBox="0 0 64 64" aria-hidden="true" focusable="false">
                <path d="M32 6 C20 6 12 15 12 27 C12 42 32 58 32 58 C32 58 52 42 52 27 C52 15 44 6 32 6 Z" fill="rgba(255,255,255,0.2)" stroke="rgba(255,255,255,0.6)" stroke-width="2" />
                <circle cx="32" cy="27" r="8" fill="#ff7a18" />
            </svg>
            <svg class="auth-float auth-float--suitcase" viewBox="0 0 64 64" aria-hidden="true" focusable="false">
                <rect x="14" y="20" width="36" height="30" rx="6" fill="rgba(255,255,255,0.2)" stroke="rgba(255,255,255,0.6)" stroke-width="2" />
                <rect x="24" y="14" width="16" height="8" rx="3" fill="rgba(255,255,255,0.5)" />
                <path d="M22 34 H42" stroke="#ff7a18" stroke-width="3" stroke-linecap="round" />
            </svg>
        </div>
    </div>

    <div class="auth-shell ${authMode eq 'register' ? 'is-register' : 'is-login'}" data-auth-root>
        <div class="auth-brand">
            <span class="auth-brand__mark"></span>
            <div>
                <div class="auth-brand__name">AeroTrail</div>
                <div class="auth-brand__tag">Travel Management</div>
            </div>
        </div>

        <c:if test="${not empty message}">
            <div class="auth-banner">${message}</div>
        </c:if>

        <div class="auth-card" data-auth-card>
            <div class="auth-toggle">
                <button type="button" class="auth-toggle__btn" data-auth-toggle="login">Login</button>
                <button type="button" class="auth-toggle__btn" data-auth-toggle="register">Register</button>
                <span class="auth-toggle__pill" aria-hidden="true"></span>
            </div>

            <div class="auth-forms">
                <section class="auth-panel auth-panel--login" data-auth-panel>
                    <div class="auth-panel__copy">
                        <h1>Welcome back</h1>
                        <p>Sign in to continue your premium travel journey.</p>
                    </div>
                    <form class="auth-form" method="post" action="${pageContext.request.contextPath}/login">
                        <input type="hidden" name="next" value="${next}" />
                        <div class="auth-field">
                            <span class="auth-field__icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24" focusable="false"><path d="M4 6 H20 V18 H4 Z" fill="none" stroke="currentColor" stroke-width="1.6" /><path d="M4 7 L12 13 L20 7" fill="none" stroke="currentColor" stroke-width="1.6" /></svg>
                            </span>
                            <input class="auth-input" type="email" id="login-email" name="email" required autocomplete="username" placeholder=" " />
                            <label class="auth-label" for="login-email">Email Address</label>
                        </div>
                        <div class="auth-field">
                            <span class="auth-field__icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24" focusable="false"><rect x="5" y="10" width="14" height="10" rx="2" fill="none" stroke="currentColor" stroke-width="1.6" /><path d="M8 10 V7 C8 4.8 9.8 3 12 3 C14.2 3 16 4.8 16 7 V10" fill="none" stroke="currentColor" stroke-width="1.6" /></svg>
                            </span>
                            <input class="auth-input" type="password" id="login-password" name="password" required autocomplete="current-password" placeholder=" " />
                            <label class="auth-label" for="login-password">Password</label>
                        </div>
                        <button class="auth-cta" type="submit">Start Your Journey</button>
                    </form>
                    <div class="auth-panel__footer">
                        <span>New to AeroTrail?</span>
                        <button class="auth-link" type="button" data-auth-toggle="register">Create an account</button>
                    </div>
                </section>

                <section class="auth-panel auth-panel--register" data-auth-panel>
                    <div class="auth-panel__copy">
                        <h1>Create your account</h1>
                        <p>Join a premium travel network with smart planning tools.</p>
                    </div>
                    <form class="auth-form" method="post" action="${pageContext.request.contextPath}/users/add">
                        <input type="hidden" name="next" value="${next}" />
                        <input type="hidden" name="autoLogin" value="true" />
                        <div class="auth-field">
                            <span class="auth-field__icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24" focusable="false"><circle cx="12" cy="8" r="4" fill="none" stroke="currentColor" stroke-width="1.6" /><path d="M4 20 C6.5 15 17.5 15 20 20" fill="none" stroke="currentColor" stroke-width="1.6" /></svg>
                            </span>
                            <input class="auth-input" type="text" id="register-name" name="name" required placeholder=" " />
                            <label class="auth-label" for="register-name">Full Name</label>
                        </div>
                        <div class="auth-field">
                            <span class="auth-field__icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24" focusable="false"><path d="M4 6 H20 V18 H4 Z" fill="none" stroke="currentColor" stroke-width="1.6" /><path d="M4 7 L12 13 L20 7" fill="none" stroke="currentColor" stroke-width="1.6" /></svg>
                            </span>
                            <input class="auth-input" type="email" id="register-email" name="email" required autocomplete="username" placeholder=" " />
                            <label class="auth-label" for="register-email">Email Address</label>
                        </div>
                        <div class="auth-field">
                            <span class="auth-field__icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24" focusable="false"><path d="M6 4 H18 V20 H6 Z" fill="none" stroke="currentColor" stroke-width="1.6" /><path d="M9 7 H15" stroke="currentColor" stroke-width="1.6" /><path d="M9 11 H15" stroke="currentColor" stroke-width="1.6" /></svg>
                            </span>
                            <input class="auth-input" type="tel" id="register-phone" name="phone" required placeholder=" " />
                            <label class="auth-label" for="register-phone">Phone Number</label>
                        </div>
                        <div class="auth-field">
                            <span class="auth-field__icon" aria-hidden="true">
                                <svg viewBox="0 0 24 24" focusable="false"><rect x="5" y="10" width="14" height="10" rx="2" fill="none" stroke="currentColor" stroke-width="1.6" /><path d="M8 10 V7 C8 4.8 9.8 3 12 3 C14.2 3 16 4.8 16 7 V10" fill="none" stroke="currentColor" stroke-width="1.6" /></svg>
                            </span>
                            <input class="auth-input" type="password" id="register-password" name="password" required autocomplete="new-password" placeholder=" " />
                            <label class="auth-label" for="register-password">Password</label>
                        </div>
                        <button class="auth-cta" type="submit">Begin the Adventure</button>
                    </form>
                    <div class="auth-panel__footer">
                        <span>Already a member?</span>
                        <button class="auth-link" type="button" data-auth-toggle="login">Sign in instead</button>
                    </div>
                </section>
            </div>
        </div>

        <div class="auth-premium-note">Ensure the UI looks like a premium travel startup landing login page similar to modern travel apps like Airbnb or luxury travel platforms.</div>
        <script src="${pageContext.request.contextPath}/assets/js/site.js"></script>
    </div>
</body>
</html>
