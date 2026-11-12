import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { transactionApi } from '../api/transactionApi';
import { Sidebar } from '../components/Sidebar';
import { Topbar } from '../components/Topbar';
import { StatCard } from '../components/StatCard';
import { TransactionTable } from '../components/TransactionTable';
import {
  Wallet,
  ArrowDownLeft,
  ArrowUpRight,
  ArrowLeftRight,
  PlusCircle,
  RefreshCw,
  AlertCircle,
} from 'lucide-react';

export const DashboardPage = () => {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchDashboardData = async () => {
    setLoading(true);
    setError('');
    try {
      const result = await transactionApi.getDashboardSummary();
      setData(result);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const accounts = data?.accounts || [];
  const recentTransactions = data?.recentTransactions || [];

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-wrapper">
        <Topbar title="Financial Dashboard" />

        <div className="page-container">
          {/* Welcome Banner */}
          <div
            style={{
              background: 'linear-gradient(135deg, #1e3a8a, #0f766e)',
              borderRadius: 'var(--radius-md)',
              padding: '28px 32px',
              color: 'white',
              marginBottom: '28px',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <div>
              <h2 style={{ fontSize: '1.6rem', fontWeight: 800 }}>Welcome, {user?.fullName}!</h2>
              <p style={{ color: '#93c5fd', marginTop: '4px', fontSize: '0.95rem' }}>
                All systems healthy. Double-entry balanced ledger active.
              </p>
            </div>
            <button
              onClick={fetchDashboardData}
              className="btn"
              style={{ background: 'rgba(255,255,255,0.15)', color: 'white' }}
              title="Refresh"
            >
              <RefreshCw size={16} className={loading ? 'spin' : ''} />
              <span>Refresh</span>
            </button>
          </div>

          {error && (
            <div className="alert alert-danger">
              <AlertCircle size={18} />
              <span>{error}</span>
            </div>
          )}

          {/* Quick Stats Grid */}
          <div className="stats-grid">
            <StatCard
              title="Total Available Balance"
              value={`$${parseFloat(data?.totalBalance || 0).toFixed(2)}`}
              icon={Wallet}
              variant="primary"
              subtitle={`${accounts.length} active account${accounts.length === 1 ? '' : 's'}`}
            />
            <StatCard
              title="Total Deposits"
              value={`$${parseFloat(data?.totalDeposits || 0).toFixed(2)}`}
              icon={ArrowDownLeft}
              variant="success"
              subtitle="All-time verified inflows"
            />
            <StatCard
              title="Total Withdrawals"
              value={`$${parseFloat(data?.totalWithdrawals || 0).toFixed(2)}`}
              icon={ArrowUpRight}
              variant="danger"
              subtitle="All-time outflows"
            />
          </div>

          {/* Quick Actions Row */}
          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', marginBottom: '32px' }}>
            <Link to="/deposit" className="btn btn-primary">
              <ArrowDownLeft size={18} />
              <span>Make Deposit</span>
            </Link>
            <Link to="/withdraw" className="btn btn-secondary">
              <ArrowUpRight size={18} />
              <span>Withdraw Money</span>
            </Link>
            <Link to="/transfer" className="btn btn-secondary">
              <ArrowLeftRight size={18} />
              <span>Transfer Funds</span>
            </Link>
            <Link to="/accounts" className="btn btn-secondary" style={{ marginLeft: 'auto' }}>
              <PlusCircle size={18} />
              <span>Open New Account</span>
            </Link>
          </div>

          {/* My Accounts Section */}
          <div className="card" style={{ marginBottom: '32px' }}>
            <div className="card-header">
              <h3 className="card-title">My Accounts</h3>
              <Link to="/accounts" style={{ fontSize: '0.88rem', color: 'var(--primary)', fontWeight: 600 }}>
                Manage Accounts &rarr;
              </Link>
            </div>

            {accounts.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '32px' }}>
                <p style={{ color: 'var(--text-muted)', marginBottom: '16px' }}>
                  You don't have any accounts created yet.
                </p>
                <Link to="/accounts" className="btn btn-primary">
                  Open First Account
                </Link>
              </div>
            ) : (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px' }}>
                {accounts.map((acc) => (
                  <div
                    key={acc.id}
                    style={{
                      border: '1px solid var(--border)',
                      borderRadius: 'var(--radius-sm)',
                      padding: '20px',
                      background: '#f8fafc',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', fontWeight: 600 }}>
                        {acc.currency} Account
                      </span>
                      <span className="badge badge-success">{acc.status}</span>
                    </div>
                    <div style={{ fontFamily: 'var(--font-mono)', fontSize: '1rem', fontWeight: 700, marginBottom: '12px' }}>
                      {acc.accountNumber}
                    </div>
                    <div style={{ fontSize: '1.4rem', fontWeight: 800, color: 'var(--text-main)', fontFamily: 'var(--font-mono)' }}>
                      ${parseFloat(acc.balance || 0).toFixed(2)}
                    </div>
                    <div style={{ marginTop: '16px', display: 'flex', gap: '8px' }}>
                      <Link
                        to={`/accounts/${acc.id}`}
                        style={{ fontSize: '0.82rem', color: 'var(--primary)', fontWeight: 600 }}
                      >
                        View Details & Ledgers &rarr;
                      </Link>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Recent Activity Table */}
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Recent Transactions</h3>
              <Link to="/history" style={{ fontSize: '0.88rem', color: 'var(--primary)', fontWeight: 600 }}>
                View Full History &rarr;
              </Link>
            </div>
            <TransactionTable transactions={recentTransactions} emptyMessage="No transactions have been recorded yet." />
          </div>
        </div>
      </div>
    </div>
  );
};

