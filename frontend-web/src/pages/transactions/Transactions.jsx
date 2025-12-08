import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCurrency } from '../../context/CurrencyContext';
import './Transactions.css';
import api from '../../services/api';

const TransactionsDashboard = () => {
  const navigate = useNavigate();
  const { formatAmount, currency } = useCurrency();
  const [activeTab, setActiveTab] = useState('transactions');
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editingTransaction, setEditingTransaction] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    amount: '',
    type: 'expense',
    category: '',
    date: new Date().toISOString().split('T')[0],
    currency: currency
  });

  // Predefined categories
  const expenseCategories = [
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
    'Insurance',
    'Personal Care',
    'Gifts & Donations',
    'Other'
  ];

  const incomeCategories = [
    'Salary',
    'Freelance',
    'Business',
    'Investments',
    'Rental Income',
    'Gifts',
    'Refunds',
    'Other'
  ];

  const userId = localStorage.getItem('userId') || 1;

  useEffect(() => {
    fetchTransactions();
  }, []);

  useEffect(() => {
    // Update currency when it changes
    setFormData(prev => ({ ...prev, currency: currency }));
  }, [currency]);

  const fetchTransactions = async () => {
    try {
      setLoading(true);
      const data = await api.transactions.getAll(userId);
      setTransactions(data);
      setError(null);
    } catch (err) {
      console.warn('Backend not available, using mock data:', err.message);
      setTransactions(getMockTransactions());
      setError('Backend connection failed - showing demo data');
    } finally {
      setLoading(false);
    }
  };

  const getMockTransactions = () => {
    return [
      { id: 1, name: 'Salary', category: 'Salary', date: '2024-01-15', amount: 5000.00, type: 'income', currency: 'USD' },
      { id: 2, name: 'Groceries', category: 'Food & Dining', date: '2024-01-14', amount: 150.00, type: 'expense', currency: 'USD' },
    ];
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this transaction?')) {
      try {
        await api.transactions.delete(id);
      } catch (err) {
        console.warn('Backend delete failed, removing from UI only');
      }
      setTransactions(transactions.filter(t => t.id !== id));
    }
  };

  const handleEdit = (transaction) => {
    setEditingTransaction(transaction);
    setFormData({
      name: transaction.name,
      amount: Math.abs(transaction.amount),
      type: transaction.type,
      category: transaction.category,
      date: transaction.date.split('T')[0],
      currency: transaction.currency || 'USD'
    });
    setShowModal(true);
  };

  const handleAdd = () => {
    setEditingTransaction(null);
    setFormData({
      name: '',
      amount: '',
      type: 'expense',
      category: '',
      date: new Date().toISOString().split('T')[0],
      currency: currency
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const transactionData = {
        ...formData,
        amount: parseFloat(formData.amount),
        userId: parseInt(userId),
        currency: formData.currency || currency
      };
      
      if (editingTransaction) {
        const updated = await api.transactions.update(editingTransaction.id, transactionData);
        setTransactions(transactions.map(t => t.id === editingTransaction.id ? updated : t));
      } else {
        const newTransaction = await api.transactions.create(transactionData);
        setTransactions([newTransaction, ...transactions]);
      }
      
      setShowModal(false);
    } catch (err) {
      alert('Failed to save transaction: ' + err.message);
    }
  };

  const handleNavigation = (tab, path) => {
    setActiveTab(tab);
    if (path) {
      navigate(path);
    }
  };

  const getCurrentCategories = () => {
    return formData.type === 'expense' ? expenseCategories : incomeCategories;
  };

  if (loading) {
    return (
      <div className="dashboard">
        <div className="loading-state">Loading transactions...</div>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1 className="dashboard-title">User Dashboard</h1>
        <button className="share-btn">Share</button>
      </header>

      <div className="transactions-container">
        <div className="transactions-header">
          <div>
            <h2 className="transactions-title">Transactions</h2>
            <p className="transactions-subtitle">Manage your finances</p>
            {error && (
              <div style={{ color: 'orange', fontSize: '14px', marginTop: '5px' }}>
                ⚠️ {error}
              </div>
            )}
          </div>
          <button className="add-btn" onClick={handleAdd}>+ Add</button>
        </div>

        <div className="transactions-list">
          {transactions.length === 0 ? (
            <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
              No transactions found. Click "+ Add" to create one.
            </div>
          ) : (
            transactions.map(transaction => (
              <div key={transaction.id} className="transaction-item">
                <div className="transaction-info">
                  <h3 className="transaction-name">{transaction.name}</h3>
                  <div className="transaction-meta">
                    <span className={`category-badge ${transaction.type}`}>
                      {transaction.category}
                    </span>
                    <span className="transaction-date">
                      {new Date(transaction.date).toLocaleDateString()}
                    </span>
                  </div>
                </div>
                <div className="transaction-actions">
                  <span className={`transaction-amount ${transaction.type}`}>
                    {transaction.type === 'income' ? '+' : '-'}
                    {formatAmount(Math.abs(transaction.amount), transaction.currency || 'USD')}
                  </span>
                  <button className="icon-btn" onClick={() => handleEdit(transaction)}>✏️</button>
                  <button 
                    className="icon-btn" 
                    onClick={() => handleDelete(transaction.id)}
                  >
                    🗑️
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{editingTransaction ? 'Edit Transaction' : 'Add Transaction'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Type</label>
                <select
                  value={formData.type}
                  onChange={(e) => setFormData({...formData, type: e.target.value, category: ''})}
                  required
                >
                  <option value="expense">Expense</option>
                  <option value="income">Income</option>
                </select>
              </div>

              <div className="form-group">
                <label>Category</label>
                <select
                  value={formData.category}
                  onChange={(e) => setFormData({...formData, category: e.target.value})}
                  required
                >
                  <option value="">Select a category</option>
                  {getCurrentCategories().map(cat => (
                    <option key={cat} value={cat}>{cat}</option>
                  ))}
                </select>
              </div>
              
              <div className="form-group">
                <label>Description</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({...formData, name: e.target.value})}
                  placeholder="e.g., Lunch at restaurant"
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Amount ({formData.currency})</label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.amount}
                  onChange={(e) => setFormData({...formData, amount: e.target.value})}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Date</label>
                <input
                  type="date"
                  value={formData.date}
                  onChange={(e) => setFormData({...formData, date: e.target.value})}
                  required
                />
              </div>
              
              <div className="modal-buttons">
                <button type="button" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="save-btn">
                  {editingTransaction ? 'Update' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <nav className="bottom-nav">
        <button 
          className={`nav-item ${activeTab === 'home' ? 'active' : ''}`}
          onClick={() => handleNavigation('home', '/dashboard')}
        >
          <span className="nav-icon">⊞</span>
          <span className="nav-label">Home</span>
        </button>
        <button 
          className={`nav-item ${activeTab === 'transactions' ? 'active' : ''}`}
        >
          <span className="nav-icon">📋</span>
          <span className="nav-label">Transactions</span>
        </button>
        <button 
          className={`nav-item ${activeTab === 'budgets' ? 'active' : ''}`}
          onClick={() => handleNavigation('budgets', '/budgets')}
        >
          <span className="nav-icon">💼</span>
          <span className="nav-label">Budgets</span>
        </button>
        <button 
          className={`nav-item ${activeTab === 'reports' ? 'active' : ''}`}
        >
          <span className="nav-icon">📈</span>
          <span className="nav-label">Reports</span>
        </button>
        <button 
          className={`nav-item ${activeTab === 'settings' ? 'active' : ''}`}
          onClick={() => handleNavigation('settings', '/settings')}
        >
          <span className="nav-icon">⚙️</span>
          <span className="nav-label">Settings</span>
        </button>
      </nav>
    </div>
  );
};

export default TransactionsDashboard;