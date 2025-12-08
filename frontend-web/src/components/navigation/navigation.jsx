import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { FaHome, FaExchangeAlt, FaWallet, FaCog } from 'react-icons/fa';
import './Navigation.css';

function Navigation() {
  const navigate = useNavigate();
  const location = useLocation();

  const navItems = [
    { path: '/dashboard', icon: FaHome, label: 'Home' },
    { path: '/transactions', icon: FaExchangeAlt, label: 'Transactions' },
    { path: '/budgets', icon: FaWallet, label: 'Budgets' },
    { path: '/settings', icon: FaCog, label: 'Settings' },
  ];

  return (
    <nav className="bottom-navigation">
      {navItems.map((item) => {
        const Icon = item.icon;
        const isActive = location.pathname === item.path;
        
        return (
          <button
            key={item.path}
            className={`nav-item ${isActive ? 'active' : ''}`}
            onClick={() => navigate(item.path)}
          >
            <Icon className="nav-icon" />
            <span className="nav-label">{item.label}</span>
          </button>
        );
      })}
    </nav>
  );
}

export default Navigation;