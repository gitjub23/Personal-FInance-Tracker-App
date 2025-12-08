import React from 'react';
import { useCurrency } from '../context/CurrencyContext';

function BarChart({ data }) {
  const { formatAmount } = useCurrency();

  console.log('BarChart received data:', data);

  if (!data || typeof data !== 'object') {
    console.log('BarChart: No data or invalid data type');
    return (
      <div style={{ textAlign: 'center', padding: '40px', color: '#9ca3af' }}>
        No budget data available
      </div>
    );
  }

  const { labels = [], budget = [], spending = [] } = data;

  if (!labels || labels.length === 0) {
    console.log('BarChart: No labels found. Data structure:', { labels, budget, spending });
    return (
      <div style={{ textAlign: 'center', padding: '40px', color: '#9ca3af' }}>
        No budget data available
      </div>
    );
  }

  const maxValue = Math.max(...budget, ...spending, 1);

  return (
    <div style={{ width: '100%', padding: '20px' }}>
      <div style={{ 
        display: 'flex', 
        gap: '8px',
        marginBottom: '20px',
        justifyContent: 'center',
        fontSize: '13px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <div style={{ width: '16px', height: '16px', backgroundColor: '#3b82f6', borderRadius: '3px' }} />
          <span>Budget</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
          <div style={{ width: '16px', height: '16px', backgroundColor: '#ec4899', borderRadius: '3px' }} />
          <span>Spending</span>
        </div>
      </div>

      <div style={{ 
        display: 'flex',
        alignItems: 'flex-end',
        gap: '20px',
        height: '200px',
        padding: '0 10px'
      }}>
        {labels.map((label, index) => {
          const budgetValue = budget[index] || 0;
          const spendingValue = spending[index] || 0;
          const budgetHeight = maxValue > 0 ? (budgetValue / maxValue) * 100 : 0;
          const spendingHeight = maxValue > 0 ? (spendingValue / maxValue) * 100 : 0;
          
          return (
            <div key={label} style={{ 
              flex: 1,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '8px'
            }}>
              <div style={{ 
                width: '100%',
                display: 'flex',
                gap: '4px',
                alignItems: 'flex-end',
                height: '200px'
              }}>
                <div style={{ 
                  flex: 1,
                  backgroundColor: '#3b82f6',
                  height: `${Math.max(budgetHeight, 2)}%`,
                  borderRadius: '4px 4px 0 0',
                  transition: 'height 0.5s ease',
                  position: 'relative'
                }}>
                  {budgetValue > 0 && (
                    <div style={{
                      position: 'absolute',
                      top: '-20px',
                      left: '50%',
                      transform: 'translateX(-50%)',
                      fontSize: '10px',
                      fontWeight: '600',
                      color: '#3b82f6',
                      whiteSpace: 'nowrap'
                    }}>
                      {formatAmount(budgetValue)}
                    </div>
                  )}
                </div>
                <div style={{ 
                  flex: 1,
                  backgroundColor: '#ec4899',
                  height: `${Math.max(spendingHeight, 2)}%`,
                  borderRadius: '4px 4px 0 0',
                  transition: 'height 0.5s ease',
                  position: 'relative'
                }}>
                  {spendingValue > 0 && (
                    <div style={{
                      position: 'absolute',
                      top: '-20px',
                      left: '50%',
                      transform: 'translateX(-50%)',
                      fontSize: '10px',
                      fontWeight: '600',
                      color: '#ec4899',
                      whiteSpace: 'nowrap'
                    }}>
                      {formatAmount(spendingValue)}
                    </div>
                  )}
                </div>
              </div>
              <div style={{ 
                fontSize: '12px',
                fontWeight: '600',
                color: '#6b7280',
                marginTop: '8px'
              }}>
                {label}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default BarChart;