import React, { useEffect, useState } from 'react';
import { transactionApi } from '../api/transactionApi';
import { Sidebar } from '../components/Sidebar';
import { Topbar } from '../components/Topbar';
import { TransactionTable } from '../components/TransactionTable';
import { RefreshCw, Filter, AlertCircle } from 'lucide-react';

export const HistoryPage = () => {
  const [transactions, setTransactions] = useState([]);
  const [selectedType, setSelectedType] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchTransactions = async (type = selectedType) => {
    setLoading(true);
    setError('');
    try {
      const data = await transactionApi.getTransactions(type || null);
      setTransactions(data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to fetch transaction history');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions(selectedType);
  }, [selectedType]);

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-wrapper">
        <Topbar title="Transaction History" />

        <div className="page-container">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px', flexWrap: 'wrap', gap: '16px' }}>
            <div>
              <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>Ledger Transaction Logs</h2>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                View and filter your deposits, withdrawals, and peer-to-peer transfers
              </p>
            </div>

            <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Filter size={16} color="var(--text-muted)" />
                <select
                  className="form-select"
                  style={{ width: 'auto', padding: '8px 12px' }}
                  value={selectedType}
                  onChange={(e) => setSelectedType(e.target.value)}
                >
                  <option value="">All Transaction Types</option>
                  <option value="DEPOSIT">Deposits Only</option>
                  <option value="WITHDRAWAL">Withdrawals Only</option>
                  <option value="TRANSFER">Transfers Only</option>
                </select>
              </div>

              <button
                onClick={() => fetchTransactions(selectedType)}
                className="btn btn-secondary"
                style={{ padding: '8px 14px' }}
                title="Refresh logs"
              >
                <RefreshCw size={16} />
              </button>
            </div>
          </div>

          {error && (
            <div className="alert alert-danger">
              <AlertCircle size={18} />
              <span>{error}</span>
            </div>
          )}

          <div className="card">
            {loading ? (
              <p style={{ color: 'var(--text-muted)', padding: '24px' }}>Loading transactions...</p>
            ) : (
              <TransactionTable
                transactions={transactions}
                emptyMessage={
                  selectedType
                    ? `No ${selectedType.toLowerCase()} transactions found.`
                    : 'No transactions recorded yet.'
                }
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

