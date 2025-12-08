import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCurrency } from '../../context/CurrencyContext';
import './Budgets.css';

const Budgets = () => {
  const navigate = useNavigate();
  const { formatAmount, currency } = useCurrency();
  const [activeTab, setActiveTab] = useState('budgets');
  const [budgets, setBudgets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingBudget, setEditingBudget] = useState(null);
  
  // Month/Year selection
  const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
  
  const [formData, setFormData] = useState({
    category: '',
    limitAmount: '',
    color: '#3b82f6',
    month: selectedMonth,
    year: selectedYear,
    currency: currency
  });

  const categories = [
    'Food & Dining',
    'Groceries',
    'Transportation',
    'Entertainment',
    'Shopping',
    'Bills & Utilities',
    'Healthcare',
    'Education',
    'Travel',
    'Housing',
    'Other'
  ];

  const months = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const userId = localStorage.getItem('userId') || 1;

  useEffect(() => {
    fetchBudgets();
  }, [selectedMonth, selectedYear]);

  useEffect(() => {
    setFormData(prev => ({ ...prev, currency: currency }));
  }, [currency]);

  const fetchBudgets = async () => {
    try {
      setLoading(true);
      const response = await fetch(
        `http://localhost:8080/api/budgets/user/${userId}/month/${selectedMonth}/year/${selectedYear}`
      );
      
      if (!response.ok) {
        setBudgets([]);
        return;
      }
      
      const data = await response.json();
      setBudgets(data);
      setError(null);
    } catch (err) {
      console.warn('Network error:', err.message);
      setBudgets([]);
      setError('Failed to load budgets');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this budget?')) {
      try {
        await fetch(`http://localhost:8080/api/budgets/${id}`, { method: 'DELETE' });
      } catch (err) {
        console.warn('Backend delete failed');
      }
      setBudgets(budgets.filter(b => b.id !== id));
    }
  };

  const handleEdit = (budget) => {
    setEditingBudget(budget);
    setFormData({
      category: budget.category,
      limitAmount: budget.limitAmount,
      color: budget.color || '#3b82f6',
      month: budget.month,
      year: budget.year,
      currency: budget.currency || 'USD'
    });
    setShowModal(true);
  };

  const handleAdd = () => {
    setEditingBudget(null);
    setFormData({
      category: '',
      limitAmount: '',
      color: '#3b82f6',
      month: selectedMonth,
      year: selectedYear,
      currency: currency
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const budgetData = {
        ...formData,
        limitAmount: parseFloat(formData.limitAmount),
        userId: parseInt(userId),
        month: parseInt(formData.month),
        year: parseInt(formData.year),
        currency: formData.currency || currency
      };
      
      if (editingBudget) {
        const response = await fetch(`http://localhost:8080/api/budgets/${editingBudget.id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(budgetData),
        });
        
        if (response.ok) {
          const updatedBudget = await response.json();
          setBudgets(budgets.map(b => b.id === editingBudget.id ? updatedBudget : b));
        }
      } else {
        const response = await fetch('http://localhost:8080/api/budgets', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(budgetData),
        });
        
        if (response.ok) {
          const newBudget = await response.json();
          setBudgets([...budgets, newBudget]);
        } else {
          const error = await response.json();
          alert(error.error || 'Failed to create budget');
          return;
        }
      }
      
      setShowModal(false);
    } catch (err) {
      alert('Failed to save budget: ' + err.message);
    }
  };

  const handleNavigation = (tab, path) => {
    setActiveTab(tab);
    if (path) {
      navigate(path);
    }
  };

  const calculatePercentage = (spent, limit) => {
    return ((spent / limit) * 100).toFixed(1);
  };

  const getProgressColor = (percentage) => {
    if (percentage >= 90) return '#ef4444';
    if (percentage >= 70) return '#f59e0b';
    return '#10b981';
  };

  const isApproachingLimit = (percentage) => {
    return percentage >= 90;
  };

  const handleMonthChange = (direction) => {
    let newMonth = selectedMonth;
    let newYear = selectedYear;
    
    if (direction === 'prev') {
      newMonth--;
      if (newMonth < 1) {
        newMonth = 12;
        newYear--;
      }
    } else {
      newMonth++;
      if (newMonth > 12) {
        newMonth = 1;
        newYear++;
      }
    }
    
    setSelectedMonth(newMonth);
    setSelectedYear(newYear);
  };

  if (loading) {
    return (
      <div className="budgets-dashboard">
        <div style={{ padding: '50px', textAlign: 'center' }}>Loading budgets...</div>
      </div>
    );
  }

  return (
    <div className="budgets-dashboard">
      <header className="budgets-header">
        <h1 className="budgets-header-title">Budgets</h1>
        <button className="share-btn">Share</button>
      </header>

      <div className="budgets-container">
        <div className="budgets-page-header">
          <div>
            <h2 className="budgets-title">Monthly Budgets</h2>
            <div style={{ display: 'flex', alignItems: 'center', gap: '15px', marginTop: '10px' }}>
              <button 
                onClick={() => handleMonthChange('prev')}
                style={{
                  background: 'white',
                  border: '1px solid #e5e7eb',
                  borderRadius: '4px',
                  padding: '5px 10px',
                  cursor: 'pointer'
                }}
              >
                ◀
              </button>
              <span style={{ fontSize: '18px', fontWeight: '600', minWidth: '150px', textAlign: 'center' }}>
                {months[selectedMonth - 1]} {selectedYear}
              </span>
              <button 
                onClick={() => handleMonthChange('next')}
                style={{
                  background: 'white',
                  border: '1px solid #e5e7eb',
                  borderRadius: '4px',
                  padding: '5px 10px',
                  cursor: 'pointer'
                }}
              >
                ▶
              </button>
            </div>
            {error && (
              <div style={{ color: 'orange', fontSize: '14px', marginTop: '5px' }}>
                ⚠️ {error}
              </div>
            )}
          </div>
          <button className="add-btn" onClick={handleAdd}>+ Add</button>
        </div>

        <div className="budgets-list">
          {budgets.length === 0 ? (
            <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
              <p style={{ fontSize: '16px', marginBottom: '10px' }}>
                No budgets for {months[selectedMonth - 1]} {selectedYear}
              </p>
              <p style={{ fontSize: '14px', color: '#999' }}>
                Click "+ Add" to create a budget for this month
              </p>
            </div>
          ) : (
            budgets.map(budget => {
              const percentage = calculatePercentage(budget.spent, budget.limitAmount);
              const remaining = budget.limitAmount - budget.spent;
              const progressColor = getProgressColor(percentage);
              const showWarning = isApproachingLimit(percentage);

              return (
                <div key={budget.id} className="budget-item">
                  <div className="budget-header">
                    <div className="budget-info">
                      <h3 className="budget-category">{budget.category}</h3>
                      <p className="budget-amounts">
                        {formatAmount(budget.spent, budget.currency || 'USD')} of {formatAmount(budget.limitAmount, budget.currency || 'USD')}
                      </p>
                    </div>
                    <div className="budget-actions">
                      <button className="icon-btn" title="Edit" onClick={() => handleEdit(budget)}>✏️</button>
                      <button 
                        className="icon-btn" 
                        title="Delete"
                        onClick={() => handleDelete(budget.id)}
                      >
                        🗑️
                      </button>
                    </div>
                  </div>

                  <div className="progress-bar-container">
                    <div 
                      className="progress-bar-fill"
                      style={{
                        width: `${Math.min(percentage, 100)}%`,
                        backgroundColor: progressColor
                      }}
                    />
                  </div>

                  <div className="budget-footer">
                    <span className="budget-percentage">{percentage}% used</span>
                    <span className="budget-remaining">{formatAmount(remaining, budget.currency || 'USD')} left</span>
                  </div>

                  {showWarning && (
                    <div className="budget-warning">
                      <span className="warning-icon">⚠</span>
                      <span className="warning-text">Approaching limit</span>
                    </div>
                  )}
                </div>
              );
            })
          )}
        </div>
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{editingBudget ? 'Edit Budget' : 'Add Budget'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Category</label>
                <select
                  value={formData.category}
                  onChange={(e) => setFormData({...formData, category: e.target.value})}
                  required
                >
                  <option value="">Select a category</option>
                  {categories.map(cat => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
              </div>
              
              <div className="form-group">
                <label>Budget Limit ({formData.currency})</label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.limitAmount}
                  onChange={(e) => setFormData({...formData, limitAmount: e.target.value})}
                  required
                />
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div className="form-group">
                  <label>Month</label>
                  <select
                    value={formData.month}
                    onChange={(e) => setFormData({...formData, month: parseInt(e.target.value)})}
                    required
                  >
                    {months.map((month, index) => (
                      <option key={month} value={index + 1}>{month}</option>
                    ))}
                  </select>
                </div>
                
                <div className="form-group">
                  <label>Year</label>
                  <input
                    type="number"
                    value={formData.year}
                    onChange={(e) => setFormData({...formData, year: parseInt(e.target.value)})}
                    min="2000"
                    max="2100"
                    required
                  />
                </div>
              </div>
              
              <div className="form-group">
                <label>Color</label>
                <input
                  type="color"
                  value={formData.color}
                  onChange={(e) => setFormData({...formData, color: e.target.value})}
                />
              </div>
              
              <div className="modal-buttons">
                <button type="button" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="save-btn">
                  {editingBudget ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

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

export default Budgets;