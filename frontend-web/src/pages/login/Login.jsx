import React, { useState } from "react";
import "./Login.css";
import { GoogleLogin, GoogleOAuthProvider } from '@react-oauth/google';
import AppleSignin from 'react-apple-signin-auth';
import { jwtDecode } from 'jwt-decode';

// Wrap your App with GoogleOAuthProvider in index.js or App.js with your CLIENT_ID

function Login({ onLogin }) {
  const [tab, setTab] = useState("login");
  const [loginData, setLoginData] = useState({ email: "", password: "", remember: false });
  const [signupData, setSignupData] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
    agree: false,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  
  // Email Verification State
  const [showVerificationModal, setShowVerificationModal] = useState(false);
  const [verificationEmail, setVerificationEmail] = useState("");
  const [verificationOTP, setVerificationOTP] = useState("");
  
  // 2FA State
  const [show2FAModal, setShow2FAModal] = useState(false);
  const [tempToken, setTempToken] = useState("");
  const [twoFACode, setTwoFACode] = useState("");
  
  // Forgot Password State
  const [showForgotPassword, setShowForgotPassword] = useState(false);
  const [forgotPasswordEmail, setForgotPasswordEmail] = useState("");
  const [showResetPassword, setShowResetPassword] = useState(false);
  const [resetOTP, setResetOTP] = useState("");
  const [newPassword, setNewPassword] = useState("");

  const handleLoginChange = (e) => {
    const { name, value, type, checked } = e.target;
    setLoginData((prev) => ({
      ...prev, [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSignupChange = (e) => {
    const { name, value, type, checked } = e.target;
    setSignupData((prev) => ({
      ...prev, [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    
    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: loginData.email,
          password: loginData.password
        })
      });

      const data = await response.json();
      
      if (response.status === 403 && data.requiresVerification) {
        // Email not verified
        setVerificationEmail(data.email);
        setShowVerificationModal(true);
        setLoading(false);
        return;
      }
      
      if (response.ok) {
        if (data.requires2FA) {
          // 2FA required
          setTempToken(data.tempToken);
          setShow2FAModal(true);
          setLoading(false);
          return;
        }

        // Normal login success
        localStorage.setItem('authToken', data.token);
        localStorage.setItem('userId', data.userId);
        localStorage.setItem('userName', data.name);
        localStorage.setItem('userEmail', data.email);
        
        if (data.profilePicture) {
          localStorage.setItem('profilePicture', data.profilePicture);
        }

        alert("Login successful!");
        if (onLogin) onLogin();
      } else {
        setError(data.error || 'Login failed');
      }
    } catch (err) {
      setError("Login failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleSignup = async (e) => {
    e.preventDefault();
    
    if (signupData.password !== signupData.confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    
    if (!signupData.agree) {
      setError("You must agree to the Terms of Service");
      return;
    }
    
    setLoading(true);
    setError("");
    
    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: signupData.name,
          email: signupData.email,
          password: signupData.password
        })
      });

      const data = await response.json();
      
      if (response.ok) {
        setVerificationEmail(data.email);
        setShowVerificationModal(true);
        alert("Registration successful! Please check your email for verification code.");
      } else {
        setError(data.error || 'Registration failed');
      }
    } catch (err) {
      setError("Registration failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyEmail = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const response = await fetch('http://localhost:8080/api/auth/verify-email', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: verificationEmail,
          otp: verificationOTP
        })
      });

      const data = await response.json();
      
      if (response.ok) {
        localStorage.setItem('authToken', data.token);
        localStorage.setItem('userId', data.userId);
        localStorage.setItem('userName', data.name);
        localStorage.setItem('userEmail', data.email);

        setShowVerificationModal(false);
        alert("Email verified successfully!");
        if (onLogin) onLogin();
      } else {
        setError(data.error || 'Invalid verification code');
      }
    } catch (err) {
      setError("Verification failed");
    } finally {
      setLoading(false);
    }
  };

  const handleResendOTP = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/auth/resend-verification', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: verificationEmail })
      });

      if (response.ok) {
        alert('Verification code sent!');
      }
    } catch (err) {
      alert('Failed to resend code');
    }
  };

  const handle2FASubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const response = await fetch('http://localhost:8080/api/auth/2fa/validate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          tempToken: tempToken,
          code: parseInt(twoFACode)
        })
      });

      const data = await response.json();
      
      if (response.ok) {
        localStorage.setItem('authToken', data.token);
        localStorage.setItem('userId', data.userId);
        localStorage.setItem('userName', data.name);
        localStorage.setItem('userEmail', data.email);

        setShow2FAModal(false);
        alert("Login successful!");
        if (onLogin) onLogin();
      } else {
        setError(data.error || 'Invalid 2FA code');
      }
    } catch (err) {
      setError("2FA validation failed");
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const response = await fetch('http://localhost:8080/api/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: forgotPasswordEmail })
      });

      if (response.ok) {
        setShowForgotPassword(false);
        setShowResetPassword(true);
        alert('Password reset code sent to your email!');
      }
    } catch (err) {
      alert('Failed to send reset code');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const response = await fetch('http://localhost:8080/api/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: forgotPasswordEmail,
          otp: resetOTP,
          newPassword: newPassword
        })
      });

      const data = await response.json();
      
      if (response.ok) {
        setShowResetPassword(false);
        alert('Password reset successfully! Please login.');
        setTab('login');
      } else {
        setError(data.error || 'Failed to reset password');
      }
    } catch (err) {
      setError("Reset failed");
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      setLoading(true);
      const decoded = jwtDecode(credentialResponse.credential);
      
      const response = await fetch('http://localhost:8080/api/auth/oauth/google', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          oauthId: decoded.sub,
          email: decoded.email,
          name: decoded.name,
          profilePicture: decoded.picture
        })
      });

      const data = await response.json();
      
      if (response.ok) {
        localStorage.setItem('authToken', data.token);
        localStorage.setItem('userId', data.userId);
        localStorage.setItem('userName', data.name);
        localStorage.setItem('userEmail', data.email);
        localStorage.setItem('profilePicture', data.profilePicture);

        alert("Google login successful!");
        if (onLogin) onLogin();
      } else {
        setError(data.error || 'Google login failed');
      }
    } catch (err) {
      setError("Google login failed");
    } finally {
      setLoading(false);
    }
  };

  const handleAppleSuccess = async (response) => {
    try {
      setLoading(true);
      const { authorization } = response;
      const decoded = jwtDecode(authorization.id_token);
      
      const apiResponse = await fetch('http://localhost:8080/api/auth/oauth/apple', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          oauthId: decoded.sub,
          email: decoded.email,
          name: response.user?.name?.firstName + ' ' + response.user?.name?.lastName || 'Apple User',
          profilePicture: null
        })
      });

      const data = await apiResponse.json();
      
      if (apiResponse.ok) {
        localStorage.setItem('authToken', data.token);
        localStorage.setItem('userId', data.userId);
        localStorage.setItem('userName', data.name);
        localStorage.setItem('userEmail', data.email);

        alert("Apple login successful!");
        if (onLogin) onLogin();
      } else {
        setError(data.error || 'Apple login failed');
      }
    } catch (err) {
      setError("Apple login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-logo">
        <svg width="42" height="42" viewBox="0 0 24 24" fill="none">
          <rect width="24" height="24" rx="12" fill="url(#dollarGradient)" />
          <path d="M12 3v18M7 8.5C7 6.75 8.67 5.5 12 5.5C15.33 5.5 17 6.75 17 8.5C17 10.25 15.33 11.5 12 11.5C8.67 11.5 7 12.75 7 14.5C7 16.25 8.67 17.5 12 17.5C15.33 17.5 17 16.25 17 14.5"
            stroke="#fff" strokeWidth="1.8" strokeLinecap="round" />
          <defs>
            <linearGradient id="dollarGradient" x1="0" y1="0" x2="24" y2="24" gradientUnits="userSpaceOnUse">
              <stop stopColor="#3b82f6" />
              <stop offset="1" stopColor="#2563eb" />
            </linearGradient>
          </defs>
        </svg>
      </div>
      <h2 className="auth-title">FinanceTracker</h2>
      <p className="auth-subtitle">Secure. Simple. Smart.</p>
      
      <div className="auth-card">
        <div className="auth-tabs">
          <button className={tab === "login" ? "active" : ""} onClick={() => setTab("login")}>Login</button>
          <button className={tab === "signup" ? "active" : ""} onClick={() => setTab("signup")}>Sign Up</button>
        </div>
        
        {error && (
          <div style={{ 
            padding: '10px', 
            marginBottom: '15px', 
            backgroundColor: '#fff3cd', 
            color: '#856404', 
            borderRadius: '4px',
            textAlign: 'center',
            border: '1px solid #ffeaa7'
          }}>
            ⚠️ {error}
          </div>
        )}
        
        {tab === "login" && (
          <form onSubmit={handleLogin}>
            <div>
              <label>Email</label>
              <input
                name="email"
                type="email"
                value={loginData.email}
                onChange={handleLoginChange}
                placeholder="you@example.com"
                required
              />
            </div>
            <div>
              <label>Password</label>
              <input
                name="password"
                type="password"
                value={loginData.password}
                onChange={handleLoginChange}
                placeholder="••••••••"
                required
              />
            </div>
            <div className="auth-row">
              <label>
                <input
                  name="remember"
                  type="checkbox"
                  checked={loginData.remember}
                  onChange={handleLoginChange}
                />
                Remember me
              </label>
              <a href="#" onClick={(e) => { e.preventDefault(); setShowForgotPassword(true); }}>
                Forgot password?
              </a>
            </div>
            <button type="submit" className="auth-btn-primary" disabled={loading}>
              {loading ? 'Signing In...' : 'Sign In'}
            </button>
          </form>
        )}
        
        {tab === "signup" && (
          <form onSubmit={handleSignup}>
            <div>
              <label>Full Name</label>
              <input
                name="name"
                type="text"
                value={signupData.name}
                onChange={handleSignupChange}
                placeholder="John Doe"
                required
              />
            </div>
            <div>
              <label>Email</label>
              <input
                name="email"
                type="email"
                value={signupData.email}
                onChange={handleSignupChange}
                placeholder="you@example.com"
                required
              />
            </div>
            <div>
              <label>Password</label>
              <input
                name="password"
                type="password"
                value={signupData.password}
                onChange={handleSignupChange}
                minLength="6"
                required
              />
            </div>
            <div>
              <label>Confirm Password</label>
              <input
                name="confirmPassword"
                type="password"
                value={signupData.confirmPassword}
                onChange={handleSignupChange}
                required
              />
            </div>
            <div className="auth-row">
              <label>
                <input
                  name="agree"
                  type="checkbox"
                  checked={signupData.agree}
                  onChange={handleSignupChange}
                  required
                />
                I agree to the <a href="#">Terms</a>
              </label>
            </div>
            <button type="submit" className="auth-btn-primary" disabled={loading}>
              {loading ? 'Creating Account...' : 'Create Account'}
            </button>
          </form>
        )}
        
        <div className="auth-divider">Or continue with</div>
        
        <div className="auth-social-buttons">
          <GoogleLogin
            onSuccess={handleGoogleSuccess}
            onError={() => setError('Google login failed')}
            useOneTap
            text="signin_with"
            shape="rectangular"
            size="large"
            width="250"
          />
          
          <AppleSignin
            authOptions={{
              clientId: 'YOUR_APPLE_CLIENT_ID',
              scope: 'email name',
              redirectURI: 'http://localhost:3000',
              usePopup: true,
            }}
            onSuccess={handleAppleSuccess}
            onError={(error) => setError('Apple login failed')}
            render={(props) => (
              <button {...props} type="button" className="auth-social-button" style={{
                backgroundColor: '#000', color: '#fff', border: 'none',
                padding: '10px 20px', borderRadius: '4px', cursor: 'pointer',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                gap: '8px', width: '100%'
              }}>
                <svg width="20" height="20" viewBox="0 0 24 24" fill="white">
                  <path d="M17.05 20.28c-.98.95-2.05.8-3.08.35-1.09-.46-2.09-.48-3.24 0-1.44.62-2.2.44-3.06-.35C2.79 15.25 3.51 7.59 9.05 7.31c1.35.07 2.29.74 3.08.8 1.18-.24 2.31-.93 3.57-.84 1.51.12 2.65.72 3.4 1.8-3.12 1.87-2.38 5.98.48 7.13-.57 1.5-1.31 2.99-2.54 4.09l.01-.01zM12.03 7.25c-.15-2.23 1.66-4.07 3.74-4.25.29 2.58-2.34 4.5-3.74 4.25z"/>
                </svg>
                Sign in with Apple
              </button>
            )}
          />
        </div>
      </div>

      {/* Email Verification Modal */}
      {showVerificationModal && (
        <div className="modal-overlay">
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>📧 Verify Your Email</h2>
            <p style={{ marginBottom: '20px', color: '#666' }}>
              We've sent a 6-digit code to <strong>{verificationEmail}</strong>
            </p>
            <form onSubmit={handleVerifyEmail}>
              <div className="form-group">
                <label>Verification Code</label>
                <input
                  type="text"
                  value={verificationOTP}
                  onChange={(e) => setVerificationOTP(e.target.value)}
                  placeholder="000000"
                  maxLength="6"
                  required
                  style={{ textAlign: 'center', fontSize: '24px', letterSpacing: '5px' }}
                />
              </div>
              <div className="modal-buttons">
                <button type="button" onClick={handleResendOTP}>Resend Code</button>
                <button type="submit" className="save-btn" disabled={loading}>
                  {loading ? 'Verifying...' : 'Verify'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* 2FA Modal */}
      {show2FAModal && (
        <div className="modal-overlay">
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>🔐 Two-Factor Authentication</h2>
            <p style={{ marginBottom: '20px', color: '#666' }}>
              Enter the code from your authenticator app or use a backup code.
            </p>
            <form onSubmit={handle2FASubmit}>
              <div className="form-group">
                <input
                  type="text"
                  value={twoFACode}
                  onChange={(e) => setTwoFACode(e.target.value)}
                  placeholder="000000"
                  maxLength="8"
                  required
                  style={{ textAlign: 'center', fontSize: '24px', letterSpacing: '5px' }}
                />
              </div>
              <div className="modal-buttons">
                <button type="button" onClick={() => setShow2FAModal(false)}>Cancel</button>
                <button type="submit" className="save-btn" disabled={loading}>
                  {loading ? 'Verifying...' : 'Verify'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Forgot Password Modal */}
      {showForgotPassword && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>🔑 Forgot Password</h2>
            <form onSubmit={handleForgotPassword}>
              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  value={forgotPasswordEmail}
                  onChange={(e) => setForgotPasswordEmail(e.target.value)}
                  required
                />
              </div>
              <div className="modal-buttons">
                <button type="button" onClick={() => setShowForgotPassword(false)}>Cancel</button>
                <button type="submit" className="save-btn">Send Code</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Reset Password Modal */}
      {showResetPassword && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h2>🔒 Reset Password</h2>
            <form onSubmit={handleResetPassword}>
              <div className="form-group">
                <label>Verification Code</label>
                <input
                  type="text"
                  value={resetOTP}
                  onChange={(e) => setResetOTP(e.target.value)}
                  maxLength="6"
                  required
                />
              </div>
              <div className="form-group">
                <label>New Password</label>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  minLength="6"
                  required
                />
              </div>
              <div className="modal-buttons">
                <button type="button" onClick={() => setShowResetPassword(false)}>Cancel</button>
                <button type="submit" className="save-btn">Reset Password</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Login;