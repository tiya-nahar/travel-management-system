<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>AeroTrail | Login & Register</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;700;800&family=Space+Grotesk:wght@600;700&display=swap" rel="stylesheet">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Manrope', sans-serif;
            background: linear-gradient(135deg, #0e4a54 0%, #1a6f7f 50%, #38aa98 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .auth-container {
            width: 100%;
            max-width: 1000px;
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 0;
            border-radius: 20px;
            overflow: hidden;
            box-shadow: 0 30px 80px rgba(0, 0, 0, 0.3);
            background: white;
        }

        @media (max-width: 768px) {
            .auth-container {
                grid-template-columns: 1fr;
            }
            .auth-visual {
                display: none !important;
            }
        }

        .auth-visual {
            background: linear-gradient(135deg, rgba(14, 74, 84, 0.95), rgba(56, 170, 152, 0.95));
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            padding: 60px 40px;
            color: white;
            text-align: center;
            position: relative;
            overflow: hidden;
        }

        .auth-visual::before {
            content: '';
            position: absolute;
            top: -50%;
            right: -50%;
            width: 300px;
            height: 300px;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.1);
            animation: float 6s ease-in-out infinite;
        }

        @keyframes float {
            0%, 100% { transform: translateY(0px); }
            50% { transform: translateY(-30px); }
        }

        .auth-visual h2 {
            font-size: 2.5rem;
            margin-bottom: 20px;
            font-family: 'Space Grotesk', sans-serif;
            font-weight: 700;
            position: relative;
            z-index: 2;
        }

        .auth-visual p {
            font-size: 1.1rem;
            line-height: 1.6;
            margin-bottom: 40px;
            opacity: 0.95;
            position: relative;
            z-index: 2;
        }

        .auth-visual-icons {
            display: flex;
            gap: 30px;
            justify-content: center;
            margin-top: 40px;
            position: relative;
            z-index: 2;
        }

        .icon-item {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 10px;
        }

        .icon-item svg {
            width: 50px;
            height: 50px;
            stroke: white;
            fill: none;
            stroke-width: 2;
        }

        .icon-item span {
            font-size: 0.9rem;
            opacity: 0.9;
        }

        .auth-form-container {
            padding: 60px 40px;
            display: flex;
            flex-direction: column;
            justify-content: center;
        }

        .auth-header {
            margin-bottom: 40px;
            text-align: center;
        }

        .auth-logo {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 12px;
            margin-bottom: 30px;
        }

        .auth-logo-mark {
            width: 45px;
            height: 45px;
            border-radius: 12px;
            background: linear-gradient(135deg, #ff7a18, #ff6b35);
            display: grid;
            place-items: center;
            color: white;
            font-weight: 700;
            font-size: 1.2rem;
        }

        .auth-logo-text {
            display: flex;
            flex-direction: column;
            text-align: left;
        }

        .auth-logo-name {
            font-size: 1.3rem;
            font-weight: 700;
            color: #0e4a54;
            font-family: 'Space Grotesk', sans-serif;
        }

        .auth-logo-tag {
            font-size: 0.75rem;
            color: #8f95a3;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .auth-header h1 {
            font-size: 2rem;
            color: #1a1a1a;
            margin-bottom: 10px;
            font-family: 'Space Grotesk', sans-serif;
        }

        .auth-header p {
            color: #8f95a3;
            font-size: 0.95rem;
        }

        .auth-tabs {
            display: flex;
            gap: 20px;
            margin-bottom: 30px;
            border-bottom: 2px solid #f0f0f0;
        }

        .auth-tab {
            padding: 12px 0;
            border: none;
            background: none;
            color: #8f95a3;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            border-bottom: 3px solid transparent;
            margin-bottom: -2px;
        }

        .auth-tab.active {
            color: #ff7a18;
            border-bottom-color: #ff7a18;
        }

        .auth-tab:hover:not(.active) {
            color: #1a1a1a;
        }

        .auth-panel {
            display: none;
        }

        .auth-panel.active {
            display: block;
            animation: slideIn 0.3s ease;
        }

        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .form-group {
            margin-bottom: 20px;
            position: relative;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #1a1a1a;
            font-weight: 600;
            font-size: 0.9rem;
        }

        .form-input {
            width: 100%;
            padding: 14px 16px;
            border: 1.5px solid #e0ddd6;
            border-radius: 10px;
            font-size: 0.95rem;
            font-family: 'Manrope', sans-serif;
            transition: all 0.2s ease;
            background: #fafaf8;
        }

        .form-input:focus {
            outline: none;
            border-color: #ff7a18;
            background: #ffffff;
            box-shadow: 0 0 0 4px rgba(255, 122, 24, 0.1);
        }

        .form-input::placeholder {
            color: #c2b3a3;
        }

        .form-actions {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
            font-size: 0.9rem;
        }

        .form-remember {
            display: flex;
            align-items: center;
            gap: 8px;
            color: #8f95a3;
        }

        .form-remember input[type="checkbox"] {
            width: 16px;
            height: 16px;
            cursor: pointer;
            accent-color: #ff7a18;
        }

        .form-remember label {
            margin: 0;
            cursor: pointer;
            font-weight: 500;
            font-size: 0.9rem;
        }

        .form-link {
            color: #ff7a18;
            text-decoration: none;
            font-weight: 600;
            transition: all 0.2s ease;
        }

        .form-link:hover {
            color: #ff6b35;
            text-decoration: underline;
        }

        .submit-btn {
            width: 100%;
            padding: 14px 20px;
            border: none;
            border-radius: 10px;
            background: linear-gradient(135deg, #ff7a18, #ff6b35);
            color: white;
            font-weight: 700;
            font-size: 1rem;
            font-family: 'Manrope', sans-serif;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 10px 25px rgba(255, 107, 53, 0.3);
        }

        .submit-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 15px 35px rgba(255, 107, 53, 0.4);
        }

        .submit-btn:active {
            transform: translateY(0);
        }

        .auth-footer {
            margin-top: 24px;
            text-align: center;
            color: #8f95a3;
            font-size: 0.95rem;
        }

        .auth-footer button {
            background: none;
            border: none;
            color: #ff7a18;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
            padding: 0;
            margin: 0;
        }

        .auth-footer button:hover {
            color: #ff6b35;
            text-decoration: underline;
        }

        .auth-message {
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 0.9rem;
            display: none;
        }

        .auth-message.success {
            background: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
            display: block;
        }

        .auth-message.error {
            background: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
            display: block;
        }
    </style>
</head>
<body>
    <div class="auth-container">
        <!-- Visual Side -->
        <div class="auth-visual">
            <h2>Welcome to AeroTrail</h2>
            <p>Your gateway to seamless travel planning and unforgettable journeys across the globe.</p>
            <div class="auth-visual-icons">
                <div class="icon-item">
                    <svg viewBox="0 0 24 24">
                        <path d="M12 2L15 10H24L17 15L20 24L12 19L4 24L7 15L0 10H9L12 2Z"/>
                    </svg>
                    <span>Premium Packages</span>
                </div>
                <div class="icon-item">
                    <svg viewBox="0 0 24 24">
                        <circle cx="12" cy="12" r="10"/>
                        <path d="M12 6V12L16 14"/>
                    </svg>
                    <span>24/7 Support</span>
                </div>
                <div class="icon-item">
                    <svg viewBox="0 0 24 24">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8Z"/>
                    </svg>
                    <span>Community</span>
                </div>
            </div>
        </div>

        <!-- Form Side -->
        <div class="auth-form-container">
            <c:set var="isLogin" value="${param.mode ne 'register'}" />

            <div class="auth-logo">
                <div class="auth-logo-mark">A</div>
                <div class="auth-logo-text">
                    <div class="auth-logo-name">AeroTrail</div>
                    <div class="auth-logo-tag">Travel Management</div>
                </div>
            </div>

            <div class="auth-header">
                <h1 id="authTitle">Welcome back</h1>
                <p id="authSubtitle">Sign in to continue your journey</p>
            </div>

            <div class="auth-tabs">
                <button class="auth-tab ${isLogin ? 'active' : ''}" onclick="switchTab(event, 'login')">Login</button>
                <button class="auth-tab ${!isLogin ? 'active' : ''}" onclick="switchTab(event, 'register')">Register</button>
            </div>

            <c:if test="${not empty message}">
                <div class="auth-message ${message.contains('success') ? 'success' : 'error'}">
                    ${message}
                </div>
            </c:if>

            <!-- Login Panel -->
            <form id="loginPanel" class="auth-panel ${isLogin ? 'active' : ''}" method="post" action="${pageContext.request.contextPath}/login">
                <div class="form-group">
                    <label for="login-email">Email Address</label>
                    <input type="email" id="login-email" name="email" class="form-input" placeholder="your@email.com" required>
                </div>

                <div class="form-group">
                    <label for="login-password">Password</label>
                    <input type="password" id="login-password" name="password" class="form-input" placeholder="••••••••" required>
                </div>

                <div class="form-actions">
                    <label class="form-remember">
                        <input type="checkbox" name="remember">
                        <span>Remember me</span>
                    </label>
                    <a href="#" class="form-link">Forgot password?</a>
                </div>

                <button type="submit" class="submit-btn">Sign In</button>

                <div class="auth-footer">
                    Don't have an account? <button type="button" onclick="switchTab(event, 'register')">Create one</button>
                </div>
            </form>

            <!-- Register Panel -->
            <form id="registerPanel" class="auth-panel ${!isLogin ? 'active' : ''}" method="post" action="${pageContext.request.contextPath}/users/add">
                <div class="form-group">
                    <label for="register-name">Full Name</label>
                    <input type="text" id="register-name" name="name" class="form-input" placeholder="John Doe" required>
                </div>

                <div class="form-group">
                    <label for="register-email">Email Address</label>
                    <input type="email" id="register-email" name="email" class="form-input" placeholder="your@email.com" required>
                </div>

                <div class="form-group">
                    <label for="register-phone">Phone Number</label>
                    <input type="tel" id="register-phone" name="phone" class="form-input" placeholder="+1 (555) 000-0000" required>
                </div>

                <div class="form-group">
                    <label for="register-password">Password</label>
                    <input type="password" id="register-password" name="password" class="form-input" placeholder="••••••••" required>
                </div>

                <div class="form-group">
                    <label class="form-remember">
                        <input type="checkbox" name="terms" required>
                        <span>I agree to the Terms & Conditions</span>
                    </label>
                </div>

                <button type="submit" class="submit-btn">Create Account</button>

                <div class="auth-footer">
                    Already a member? <button type="button" onclick="switchTab(event, 'login')">Sign in</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function switchTab(e, tab) {
            if (e) e.preventDefault();
            
            const loginPanel = document.getElementById('loginPanel');
            const registerPanel = document.getElementById('registerPanel');
            const tabs = document.querySelectorAll('.auth-tab');
            const title = document.getElementById('authTitle');
            const subtitle = document.getElementById('authSubtitle');

            tabs.forEach(t => t.classList.remove('active'));
            
            if (tab === 'login') {
                loginPanel.classList.add('active');
                registerPanel.classList.remove('active');
                tabs[0].classList.add('active');
                title.textContent = 'Welcome back';
                subtitle.textContent = 'Sign in to continue your journey';
            } else {
                registerPanel.classList.add('active');
                loginPanel.classList.remove('active');
                tabs[1].classList.add('active');
                title.textContent = 'Create your account';
                subtitle.textContent = 'Join our travel community today';
            }
        }
    </script>
</body>
</html>
