const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// Helper function to get auth token
const getAuthToken = () => {
  return localStorage.getItem('authToken');
};

// Helper function to handle responses
const handleResponse = async (response) => {
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Request failed' }));
    throw new Error(error.message || 'Request failed');
  }
  return response.json();
};

// API Service
const api = {
  // Auth endpoints
  auth: {
    register: async (userData) => {
      const response = await fetch(`${API_URL}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(userData),
      });
      return handleResponse(response);
    },
    
    login: async (credentials) => {
      const response = await fetch(`${API_URL}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials),
      });
      const data = await handleResponse(response);
      // Store user data
      if (data.userId) {
        localStorage.setItem('userId', data.userId);
        localStorage.setItem('userName', data.name);
        localStorage.setItem('userEmail', data.email);
      }
      return data;
    },
    
    logout: () => {
      localStorage.removeItem('userId');
      localStorage.removeItem('userName');
      localStorage.removeItem('userEmail');
    }
  },

  // Transaction endpoints
  transactions: {
    getAll: async (userId) => {
      const response = await fetch(`${API_URL}/api/transactions/user/${userId}`);
      return handleResponse(response);
    },
    
    create: async (transactionData) => {
      const response = await fetch(`${API_URL}/api/transactions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(transactionData),
      });
      return handleResponse(response);
    },
    
    update: async (id, transactionData) => {
      const response = await fetch(`${API_URL}/api/transactions/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(transactionData),
      });
      return handleResponse(response);
    },
    
    delete: async (id) => {
      const response = await fetch(`${API_URL}/api/transactions/${id}`, {
        method: 'DELETE',
      });
      return handleResponse(response);
    }
  },

  // Budget endpoints
  budgets: {
    getAll: async (userId) => {
      const response = await fetch(`${API_URL}/api/budgets/user/${userId}`);
      return handleResponse(response);
    },
    
    create: async (budgetData) => {
      const response = await fetch(`${API_URL}/api/budgets`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(budgetData),
      });
      return handleResponse(response);
    },
    
    update: async (id, budgetData) => {
      const response = await fetch(`${API_URL}/api/budgets/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(budgetData),
      });
      return handleResponse(response);
    },
    
    delete: async (id) => {
      const response = await fetch(`${API_URL}/api/budgets/${id}`, {
        method: 'DELETE',
      });
      return handleResponse(response);
    }
  },

  // Dashboard endpoints
  dashboard: {
    getSummary: async (userId) => {
      const response = await fetch(`${API_URL}/api/dashboard/user/${userId}`);
      return handleResponse(response);
    },
    
    getExpensesByCategory: async (userId) => {
      const response = await fetch(`${API_URL}/api/dashboard/user/${userId}/expenses-by-category`);
      return handleResponse(response);
    },
    
    getBudgetVsSpending: async (userId) => {
      const response = await fetch(`${API_URL}/api/dashboard/user/${userId}/budget-vs-spending`);
      return handleResponse(response);
    },
    
    getRecentTransactions: async (userId) => {
      const response = await fetch(`${API_URL}/api/dashboard/user/${userId}/recent-transactions`);
      return handleResponse(response);
    }
  },

  // User Preferences endpoints
  preferences: {
    get: async (userId) => {
      const response = await fetch(`${API_URL}/api/preferences/user/${userId}`);
      return handleResponse(response);
    },
    
    save: async (userId, preferences) => {
      const response = await fetch(`${API_URL}/api/preferences/user/${userId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(preferences),
      });
      return handleResponse(response);
    },
    
    update: async (userId, preferences) => {
      const response = await fetch(`${API_URL}/api/preferences/user/${userId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(preferences),
      });
      return handleResponse(response);
    }
  }
};

export default api;