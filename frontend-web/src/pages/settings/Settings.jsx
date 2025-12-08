import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Settings.css';
import { useCurrency } from '../../context/CurrencyContext';
import api from '../../services/api';

const Settings = ({ onLogout }) => {
  const navigate = useNavigate();
  const { currency, updateCurrency } = useCurrency();
  const [activeTab, setActiveTab] = useState('settings');
  const [loading, setLoading] = useState(false);

  // User Profile State
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [selectedCurrency, setSelectedCurrency] = useState(currency);

  // Notification Settings State
  const [budgetAlerts, setBudgetAlerts] = useState(true);
  const [weeklyReports, setWeeklyReports] = useState(true);
  const [reminders, setReminders] = useState(false);

  // Modals
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [show2FAModal, setShow2FAModal] = useState(false);

  // Password Form
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  // Delete Account Form
  const [deletePassword, setDeletePassword] = useState('');

  const currencies = [
    { code: 'USD', name: 'US Dollar', symbol: '$' },
    { code: 'EUR', name: 'Euro', symbol: '€' },
    { code: 'GBP', name: 'British Pound', symbol: '£' },
    { code: 'JPY', name: 'Japanese Yen', symbol: '¥' },
    { code: 'CAD', name: 'Canadian Dollar', symbol: 'C$' },
    { code: 'AUD', name: 'Australian Dollar', symbol: 'A$' },
    { code: 'CHF', name: 'Swiss Franc', symbol: 'Fr' },
    { code: 'CNY', name: 'Chinese Yuan', symbol: '¥' },
    { code: 'INR', name: 'Indian Rupee', symbol: '₹' },
    { code: 'HUF', name: 'Hungarian Forint', symbol: 'Ft' },
    { code: 'NGN', name: 'Nigerian Naira', symbol: '₦' }
  ];

  useEffect(() => {
    const userName = localStorage.getItem('userName');
    const userEmail = localStorage.getItem('userEmail');
    
    if (userName) setFullName(userName);
    if (userEmail) setEmail(userEmail);
    setSelectedCurrency(currency);
    
    loadPreferences();
  }, [currency]);

  const loadPreferences = async () => {
    try {
      const userId = localStorage.getItem('userId');
      if (!userId) return;
      
      const prefs = await api.preferences.get(userId);
      setBudgetAlerts(prefs.budgetAlerts ?? true);
      setWeeklyReports(prefs.weeklyReports ?? true);
      setReminders(prefs.reminders ?? false);
    } catch (err) {
      console.error('Failed to load preferences:', err);
    }
  };

  const handleNavigation = (tab, path) => {
    setActiveTab(tab);
    if (path) {
      navigate(path);
    }
  };

  const handleSaveProfile = async () => {
    setLoading(true);
    try {
      const userId = localStorage.getItem('userId');
      
      localStorage.setItem('userName', fullName);
      localStorage.setItem('userEmail', email);
      
      await updateCurrency(selectedCurrency);
      
      if (userId) {
        await api.preferences.update(userId, {
          currency: selectedCurrency,
          budgetAlerts,
          weeklyReports,
          reminders
        });
      }
      
      alert('Settings saved successfully!');
    } catch (err) {
      alert('Failed to save settings: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      alert('New passwords do not match!');
      return;
    }

    if (passwordForm.newPassword.length < 6) {
      alert('Password must be at least 6 characters long!');
      return;
    }

    setLoading(true);
    try {
      const userId = localStorage.getItem('userId');
      const response = await fetch('http://localhost:8080/api/auth/change-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: parseInt(userId),
          currentPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword
        })
      });

      const data = await response.json();

      if (response.ok) {
        alert('Password changed successfully!');
        setShowPasswordModal(false);
        setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
      } else {
        alert(data.error || 'Failed to change password');
      }
    } catch (err) {
      alert('Error changing password: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteAccount = async (e) => {
    e.preventDefault();
    
    if (!window.confirm('Are you ABSOLUTELY sure? This action cannot be undone!')) {
      return;
    }

    setLoading(true);
    try {
      const userId = localStorage.getItem('userId');
      const response = await fetch(`http://localhost:8080/api/auth/delete-account/${userId}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ password: deletePassword })
      });

      const data = await response.json();

      if (response.ok) {
        alert('Account deleted successfully');
        handleLogout();
      } else {
        alert(data.error || 'Failed to delete account');
      }
    } catch (err) {
      alert('Error deleting account: ' + err.message);
    } finally {
      setLoading(false);
      setShowDeleteModal(false);
      setDeletePassword('');
    }
  };

  const handleEnable2FA = async () => {
    try {
      const userId = localStorage.getItem('userId');
      const response = await fetch(`http://localhost:8080/api/auth/enable-2fa/${userId}`, {
        method: 'POST'
      });

      const data = await response.json();
      alert(data.message || 'Check the console for 2FA setup details');
      setShow2FAModal(false);
    } catch (err) {
      alert('2FA setup is coming soon! Full implementation requires TOTP library integration.');
    }
  };

  const handleExport = async (format) => {
    try {
      alert(`Exporting data as ${format}... (Feature coming soon)`);
    } catch (err) {
      alert('Export failed: ' + err.message);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('userId');
    localStorage.removeItem('userName');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('currency');
    if (onLogout) {
      onLogout();
    }
    navigate('/');
  };

  return (
    <div className="settings-dashboard">
      <header className="settings-header">
        <h1 className="settings-header-title">Settings</h1>
        <button className="share-btn">Share</button>
      </header>

      <div className="settings-container">
        <div className="settings-page-header">
          <h1 className="settings-title">Settings</h1>
          <p className="settings-subtitle">Account & preferences</p>
        </div>

        <div className="settings-content">
          {/* User Profile Section */}
          <div className="settings-section">
            <div className="section-header">
              <div className="section-icon user-icon">👤</div>
              <h2 className="section-title">User Profile</h2>
            </div>

            <div className="form-group">
              <label className="form-label">Full Name</label>
              <input
                type="text"
                className="form-input"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                placeholder="John Doe"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Email</label>
              <input
                type="email"
                className="form-input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="john.doe@example.com"
              />
            </div>

            <div className="form-group">
              <label className="form-label">Currency</label>
              <select
                className="form-select"
                value={selectedCurrency}
                onChange={(e) => setSelectedCurrency(e.target.value)}
              >
                {currencies.map((curr) => (
                  <option key={curr.code} value={curr.code}>
                    {curr.code} - {curr.name} ({curr.symbol})
                  </option>
                ))}
              </select>
            </div>

            <button className="save-btn" onClick={handleSaveProfile} disabled={loading}>
              {loading ? 'Saving...' : 'Save'}
            </button>
          </div>

          {/* Notifications Section */}
          <div className="settings-section">
            <div className="section-header">
              <div className="section-icon notification-icon">🔔</div>
              <h2 className="section-title">Notifications</h2>
            </div>

            <div className="toggle-item">
              <div className="toggle-info">
                <div className="toggle-title">Budget Alerts</div>
                <div className="toggle-description">Notify near limits</div>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={budgetAlerts}
                  onChange={(e) => setBudgetAlerts(e.target.checked)}
                />
                <span className="toggle-slider"></span>
              </label>
            </div>

            <div className="toggle-item">
              <div className="toggle-info">
                <div className="toggle-title">Weekly Reports</div>
                <div className="toggle-description">Weekly summary</div>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={weeklyReports}
                  onChange={(e) => setWeeklyReports(e.target.checked)}
                />
                <span className="toggle-slider"></span>
              </label>
            </div>

            <div className="toggle-item">
              <div className="toggle-info">
                <div className="toggle-title">Reminders</div>
                <div className="toggle-description">Daily reminders</div>
              </div>
              <label className="toggle-switch">
                <input
                  type="checkbox"
                  checked={reminders}
                  onChange={(e) => setReminders(e.target.checked)}
                />
                <span className="toggle-slider"></span>
              </label>
            </div>
          </div>

          {/* Export Data Section */}
          <div className="settings-section">
            <div className="section-header">
              <div className="section-icon export-icon">📥</div>
              <h2 className="section-title">Export Data</h2>
            </div>

            <div className="export-buttons">
              <button className="export-btn" onClick={() => handleExport('CSV')}>CSV</button>
              <button className="export-btn" onClick={() => handleExport('PDF')}>PDF</button>
              <button className="export-btn" onClick={() => handleExport('JSON')}>JSON</button>
            </div>
          </div>

          {/* Security Section */}
          <div className="settings-section">
            <div className="section-header">
              <div className="section-icon security-icon">🔒</div>
              <h2 className="section-title">Security</h2>
            </div>

            <button className="security-btn" onClick={() => setShowPasswordModal(true)}>
              Change Password
            </button>
            <button className="security-btn" onClick={() => setShow2FAModal(true)}>
              2FA Setup
            </button>
            <button className="security-btn delete-btn" onClick={() => setShowDeleteModal(true)}>
              Delete Account
            </button>
          </div>

          {/* Logout Button */}
          <div className="settings-section">
            <button className="security-btn logout-btn" onClick={handleLogout}>
              ↗ Logout
            </button>
          </div>
        </div>
      </div>

      {/* Change Password Modal */}
      {showPasswordModal && (
        <div className="modal-overlay" onClick={() => setShowPasswordModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Change Password</h2>
            <form onSubmit={handleChangePassword}>
              <div className="form-group">
                <label>Current Password</label>
                <input
                  type="password"
                  value={passwordForm.currentPassword}
                  onChange={(e) => setPasswordForm({...passwordForm, currentPassword: e.target.value})}
                  required
                />
              </div>
              <div className="form-group">
                <label>New Password</label>
                <input
                  type="password"
                  value={passwordForm.newPassword}
                  onChange={(e) => setPasswordForm({...passwordForm, newPassword: e.target.value})}
                  required
                  minLength={6}
                />
              </div>
              <div className="form-group">
                <label>Confirm New Password</label>
                <input
                  type="password"
                  value={passwordForm.confirmPassword}
                  onChange={(e) => setPasswordForm({...passwordForm, confirmPassword: e.target.value})}
                  required
                  minLength={6}
                />
              </div>
              <div className="modal-buttons">
                <button type="button" onClick={() => setShowPasswordModal(false)}>Cancel</button>
                <button type="submit" className="save-btn" disabled={loading}>
                  {loading ? 'Changing...' : 'Change Password'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Account Modal */}
      {showDeleteModal && (
        <div className="modal-overlay" onClick={() => setShowDeleteModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2 style={{ color: '#ef4444' }}>⚠️ Delete Account</h2>
            <p style={{ marginBottom: '20px', color: '#666' }}>
              This action is permanent and cannot be undone. All your data will be deleted.
            </p>
            <form onSubmit={handleDeleteAccount}>
              <div className="form-group">
                <label>Confirm your password</label>
                <input
                  type="password"
                  value={deletePassword}
                  onChange={(e) => setDeletePassword(e.target.value)}
                  placeholder="Enter your password"
                  required
                />
              </div>
              <div className="modal-buttons">
                <button type="button" onClick={() => setShowDeleteModal(false)}>Cancel</button>
                <button 
                  type="submit" 
                  className="delete-btn" 
                  disabled={loading}
                  style={{ backgroundColor: '#ef4444' }}
                >
                  {loading ? 'Deleting...' : 'Delete Account'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* 2FA Modal */}
      {show2FAModal && (
        <div className="modal-overlay" onClick={() => setShow2FAModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Two-Factor Authentication</h2>
            <p style={{ marginBottom: '20px', color: '#666' }}>
              2FA adds an extra layer of security to your account. This feature requires TOTP library integration.
            </p>
            <div className="modal-buttons">
              <button onClick={() => setShow2FAModal(false)}>Cancel</button>
              <button onClick={handleEnable2FA} className="save-btn">
                Enable 2FA (Coming Soon)
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Bottom Navigation */}
      <nav className="bottom-nav">
        <button className={`nav-item ${activeTab === 'home' ? 'active' : ''}`} onClick={() => handleNavigation('home', '/dashboard')}>
          <span className="nav-icon">⊞</span>
          <span className="nav-label">Home</span>
        </button>
        <button className={`nav-item ${activeTab === 'transactions' ? 'active' : ''}`} onClick={() => handleNavigation('transactions', '/transactions')}>
          <span className="nav-icon">📋</span>
          <span className="nav-label">Transactions</span>
        </button>
        <button className={`nav-item ${activeTab === 'budgets' ? 'active' : ''}`} onClick={() => handleNavigation('budgets', '/budgets')}>
          <span className="nav-icon">💼</span>
          <span className="nav-label">Budgets</span>
        </button>
        <button className={`nav-item ${activeTab === 'reports' ? 'active' : ''}`}>
          <span className="nav-icon">📈</span>
          <span className="nav-label">Reports</span>
        </button>
        <button className={`nav-item ${activeTab === 'settings' ? 'active' : ''}`} onClick={() => handleNavigation('settings', '/settings')}>
          <span className="nav-icon">⚙️</span>
          <span className="nav-label">Settings</span>
        </button>
      </nav>
    </div>
  );
};

export default Settings;