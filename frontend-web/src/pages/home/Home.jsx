import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Home.css";
import PieChart from "../../components/PieChart";
import BarChart from "../../components/BarChart";
import api from "../../services/api";
import { useCurrency } from "../../context/CurrencyContext";

function Home() {
  const navigate = useNavigate();
  const { formatAmount } = useCurrency();
  const [activeTab, setActiveTab] = useState('home');
  const [dashboardData, setDashboardData] = useState({
    income: 0,
    expenses: 0,
    budget: 0,
    savings: 0,
    transactions: 0,
    budgetUsed: 0,
    monthlySpending: 0
  });
  const [chartData, setChartData] = useState({
    expensesByCategory: {},
    budgetVsSpending: {
      labels: [],
      budget: [],
      spending: []
    }
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userName, setUserName] = useState('');

  const userId = localStorage.getItem('userId') || 1;

  useEffect(() => {
    const storedUserName = localStorage.getItem('userName');
    if (storedUserName) {
      setUserName(storedUserName);
    }
    
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError(null);

      const [summary, expensesByCategory, budgetVsSpending] = await Promise.all([
        api.dashboard.getSummary(userId),
        api.dashboard.getExpensesByCategory(userId),
        api.dashboard.getBudgetVsSpending(userId)
      ]);

      let transformedBudgetData = { labels: [], budget: [], spending: [] };
      
      if (budgetVsSpending && budgetVsSpending.labels) {
        transformedBudgetData = budgetVsSpending;
      }

      setDashboardData(summary);
      setChartData({
        expensesByCategory: expensesByCategory || {},
        budgetVsSpending: transformedBudgetData
      });

    } catch (err) {
      console.error('Error fetching dashboard data:', err);
      setError(`Failed to load dashboard data: ${err.message}`);
      
      const fallbackData = {
        expensesByCategory: {},
        budgetVsSpending: {
          labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
          budget: [0, 0, 0, 0, 0, 0],
          spending: [0, 0, 0, 0, 0, 0]
        }
      };
      
      setDashboardData({
        income: 0,
        expenses: 0,
        budget: 0,
        savings: 0,
        transactions: 0,
        budgetUsed: 0,
        monthlySpending: 0
      });
      
      setChartData(fallbackData);
    } finally {
      setLoading(false);
    }
  };

  const handleNavigation = (tab, path) => {
    setActiveTab(tab);
    if (path) {
      navigate(path);
    }
  };

  const handleRefresh = () => {
    fetchDashboardData();
  };

  const getFinancialTip = () => {
    const { budgetUsed, budget, monthlySpending, savings } = dashboardData;
    
    if (budget === 0) {
      return { icon: '💡', text: 'Set up monthly budgets to track your spending better', color: '#3b82f6' };
    }
    
    if (budgetUsed > 100) {
      return { icon: '⚠️', text: `You've exceeded your budget by ${(budgetUsed - 100).toFixed(0)}%. Consider reducing spending`, color: '#ef4444' };
    }
    
    if (budgetUsed > 90) {
      return { icon: '⚠️', text: `You're using ${budgetUsed.toFixed(0)}% of your budget. Watch your spending closely`, color: '#f59e0b' };
    }
    
    if (budgetUsed > 75) {
      return { icon: '⚡', text: `${budgetUsed.toFixed(0)}% budget used. You're on track but getting close to your limit`, color: '#f59e0b' };
    }
    
    if (savings < 0) {
      return { icon: '📉', text: 'Your expenses exceed income. Review your spending to improve savings', color: '#ef4444' };
    }
    
    if (savings === 0) {
      return { icon: '💰', text: 'Try to save at least 10-20% of your income for emergencies', color: '#3b82f6' };
    }
    
    if (budgetUsed > 0) {
      return { icon: '✅', text: `Great job! You're using ${budgetUsed.toFixed(0)}% of your budget. Keep it up!`, color: '#10b981' };
    }
    
    return { icon: '🎯', text: 'Start tracking transactions to get personalized financial insights', color: '#3b82f6' };
  };

  const tip = getFinancialTip();

  if (loading) {
    return (
      <div className="dashboard-container">
        <div style={{ 
          padding: '50px', 
          textAlign: 'center',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: '20px'
        }}>
          <div className="loading-spinner"></div>
          <div>Loading your financial dashboard...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div>
          <h1 className="dashboard-main-title">Financial Dashboard</h1>
          {userName && <p style={{ margin: 0, color: '#666', fontSize: '14px' }}>Welcome back, {userName}!</p>}
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="refresh-btn" onClick={handleRefresh}>
            🔄 Refresh
          </button>
          <button className="share-btn">Share</button>
        </div>
      </header>

      <div className="dashboard-content">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
          <div>
            <h1 className="dashboard-title">Financial Overview</h1>
            <p className="dashboard-subtitle">Your complete financial picture at a glance</p>
          </div>
          <div style={{ fontSize: '14px', color: '#666', textAlign: 'right' }}>
            <div>User ID: {userId}</div>
            <div style={{ fontSize: '12px', color: '#999' }}>Last updated: {new Date().toLocaleTimeString()}</div>
          </div>
        </div>
        
        {error && (
          <div style={{ 
            backgroundColor: '#fff3cd', 
            color: '#856404', 
            padding: '10px', 
            borderRadius: '4px',
            marginBottom: '20px',
            border: '1px solid #ffeaa7',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
          }}>
            <span>⚠️ {error}</span>
            <button 
              onClick={handleRefresh}
              style={{ 
                background: 'none', 
                border: '1px solid #856404', 
                color: '#856404',
                padding: '5px 10px',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '12px'
              }}
            >
              Retry
            </button>
          </div>
        )}

        <div className="dashboard-cards">
          <div className="dashboard-card income">
            <div className="card-icon">💰</div>
            <span>Total Income</span>
            <h2>{formatAmount(dashboardData.income)}</h2>
            <small>All time earnings</small>
          </div>
          <div className="dashboard-card expenses">
            <div className="card-icon">💸</div>
            <span>Total Expenses</span>
            <h2>{formatAmount(dashboardData.expenses)}</h2>
            <small>All time spending</small>
          </div>
          <div className="dashboard-card budget">
            <div className="card-icon">📊</div>
            <span>Monthly Budget</span>
            <h2>{formatAmount(dashboardData.budget)}</h2>
            <small>Current month limit</small>
          </div>
          <div className="dashboard-card savings">
            <div className="card-icon">🏦</div>
            <span>Net Savings</span>
            <h2>{formatAmount(dashboardData.savings)}</h2>
            <small>Income - Expenses</small>
          </div>
        </div>

        <div className="charts-container">
          <div className="chart-section">
            <h3>📊 Expenses by Category</h3>
            <p className="chart-description">Where your money is going</p>
            <div className="chart-wrapper">
              <PieChart data={chartData.expensesByCategory} />
            </div>
          </div>
          
          <div className="chart-section">
            <h3>📈 Budget vs Spending (Last 6 Months)</h3>
            <p className="chart-description">Monthly comparison</p>
            <div className="chart-wrapper">
              <BarChart data={chartData.budgetVsSpending} />
            </div>
          </div>
        </div>
        
        <div className="quick-summary">
          <h3>📋 Quick Stats</h3>
          <div className="stats-grid">
            <div>
              <span>Net Savings</span>
              <span className={dashboardData.savings >= 0 ? 'positive' : 'negative'}>
                {formatAmount(dashboardData.savings)}
              </span>
            </div>
            <div>
              <span>Budget Used This Month</span>
              <span className={dashboardData.budgetUsed > 100 ? 'negative' : dashboardData.budgetUsed > 80 ? 'warning' : 'positive'}>
                {dashboardData.budgetUsed.toFixed(1)}%
              </span>
            </div>
            <div>
              <span>Monthly Spending</span>
              <span>{formatAmount(dashboardData.monthlySpending)}</span>
            </div>
            <div>
              <span>Total Transactions</span>
              <span>{dashboardData.transactions}</span>
            </div>
          </div>
          <div style={{ 
            marginTop: '15px', 
            paddingTop: '15px', 
            borderTop: '1px solid #eee',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            backgroundColor: '#f9fafb',
            padding: '12px',
            borderRadius: '8px'
          }}>
            <span style={{ fontSize: '24px' }}>{tip.icon}</span>
            <div style={{ flex: 1 }}>
              <strong style={{ color: tip.color }}>Financial Tip:</strong>
              <p style={{ margin: '4px 0 0 0', color: '#666', fontSize: '14px' }}>
                {tip.text}
              </p>
            </div>
          </div>
        </div>
      </div>

      <nav className="dashboard-nav">
        <button 
          className={`nav-item ${activeTab === 'home' ? 'active' : ''}`}
          onClick={() => handleNavigation('home', '/dashboard')}
        >
          <span className="nav-icon">⊞</span>
          <span className="nav-label">Home</span>
        </button>
        <button 
          className={`nav-item ${activeTab === 'transactions' ? 'active' : ''}`}
          onClick={() => handleNavigation('transactions', '/transactions')}
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
}

export default Home;