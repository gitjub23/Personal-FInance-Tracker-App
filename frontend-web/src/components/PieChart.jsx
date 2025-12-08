import React from 'react';
import { useCurrency } from '../context/CurrencyContext';

function PieChart({ data }) {
  const { formatAmount } = useCurrency();

  if (!data || typeof data !== 'object' || Object.keys(data).length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '40px', color: '#9ca3af' }}>
        No expense data available
      </div>
    );
  }

  const categories = Object.keys(data);
  const values = Object.values(data);
  const total = values.reduce((sum, val) => sum + val, 0);

  if (total === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '40px', color: '#9ca3af' }}>
        No expense data available
      </div>
    );
  }

  const colors = ['#3b82f6', '#ec4899', '#10b981', '#8b5cf6', '#f59e0b', '#6366f1', '#ef4444'];

  return (
    <div style={{ width: '100%', padding: '20px' }}>
      {categories.map((category, index) => {
        const percentage = ((values[index] / total) * 100).toFixed(1);
        return (
          <div key={category} style={{ marginBottom: '16px' }}>
            <div style={{ 
              display: 'flex', 
              justifyContent: 'space-between',
              marginBottom: '4px',
              fontSize: '14px'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <div style={{
                  width: '12px',
                  height: '12px',
                  borderRadius: '50%',
                  backgroundColor: colors[index % colors.length]
                }} />
                <span style={{ color: '#374151', fontWeight: '500' }}>{category}</span>
              </div>
              <span style={{ color: '#6b7280' }}>{formatAmount(values[index])} ({percentage}%)</span>
            </div>
            <div style={{ 
              width: '100%', 
              height: '10px', 
              backgroundColor: '#e5e7eb',
              borderRadius: '5px',
              overflow: 'hidden'
            }}>
              <div style={{
                width: `${percentage}%`,
                height: '100%',
                backgroundColor: colors[index % colors.length],
                transition: 'width 0.5s ease'
              }} />
            </div>
          </div>
        );
      })}
      <div style={{ 
        marginTop: '20px',
        paddingTop: '20px',
        borderTop: '2px solid #e5e7eb',
        textAlign: 'center',
        fontWeight: '700',
        fontSize: '16px',
        color: '#1f2937'
      }}>
        Total: {formatAmount(total)}
      </div>
    </div>
  );
}

export default PieChart;