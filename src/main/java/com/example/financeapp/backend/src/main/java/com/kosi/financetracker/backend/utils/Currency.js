// utils/currency.js

const CURRENCY_SYMBOLS = {
  USD: '$',
  EUR: '€',
  GBP: '£',
  JPY: '¥',
  CAD: 'C$',
  AUD: 'A$',
  CHF: 'Fr',
  CNY: '¥',
  INR: '₹',
  BRL: 'R$',
  MXN: 'Mex$',
  ZAR: 'R',
};

export const getCurrencySymbol = (currencyCode) => {
  return CURRENCY_SYMBOLS[currencyCode?.toUpperCase()] || currencyCode || '$';
};

export const formatCurrency = (amount, currencyCode = 'USD') => {
  const symbol = getCurrencySymbol(currencyCode);
  const numAmount = parseFloat(amount) || 0;
  
  // Format with 2 decimal places and thousand separators
  const formatted = numAmount.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
  
  return `${symbol}${formatted}`;
};

// Convert amount from one currency to another
export const convertCurrency = async (amount, fromCurrency, toCurrency) => {
  if (fromCurrency === toCurrency) {
    return amount;
  }
  
  try {
    const response = await fetch(
      `http://localhost:8080/api/currency/convert?amount=${amount}&from=${fromCurrency}&to=${toCurrency}`
    );
    
    if (!response.ok) {
      throw new Error('Currency conversion failed');
    }
    
    const data = await response.json();
    return data.convertedAmount;
  } catch (error) {
    console.error('Currency conversion error:', error);
    return amount; // Fallback to original amount
  }
};

// Get all exchange rates
export const getExchangeRates = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/currency/rates');
    
    if (!response.ok) {
      throw new Error('Failed to fetch exchange rates');
    }
    
    return await response.json();
  } catch (error) {
    console.error('Error fetching exchange rates:', error);
    return {};
  }
};