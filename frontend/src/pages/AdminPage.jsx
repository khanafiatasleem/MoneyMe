import React, { useEffect, useState } from 'react';
import { transactionApi } from '../api/transactionApi';
import { Sidebar } from '../components/Sidebar';
import { Topbar } from '../components/Topbar';
import { StatCard } from '../components/StatCard';
import { TransactionTable } from '../components/TransactionTable';
import { Users, Wallet, ArrowLeftRight, DollarSign, ShieldAlert, RefreshCw, AlertCircle } from 'lucide-react';

export const AdminPage = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchAdminStats = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await transactionApi.getAdminDashboard();
      setStats(data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to fetch admin metrics. Authorization check failed.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAdminStats();
  }, []);

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-wrapper">
        <Topbar title="System Administration Console" />

        <div className="page-container">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
              <div style={{ background: '#fef3c7', color: '#d97706', padding: '10px', borderRadius: '10px' }}>
                <ShieldAlert size={24} />
              </div>
              <div>
                <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>Admin Super-Dashboard</h2>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                  System-wide financial telemetry, auditing, and user management
                </p>
              </div>
            </div>

            <button onClick={fetchAdminStats} className="btn btn-secondary">
              <RefreshCw size={16} />
              <span>Refresh Stats</span>
            </button>
          </div>

          {error && (
            <div className="alert alert-danger">
              <AlertCircle size={18} />
              <span>{error}</span>
            </div>
          )}

          {/* System Metrics Grid */}
          <div className="stats-grid">
            <StatCard
              title="Total Registered Users"
              value={stats?.totalUsers || 0}
              icon={Users}
              variant="primary"
              subtitle="All registered accounts"
            />
            <StatCard
              title="Total System Accounts"
              value={stats?.totalAccounts || 0}
              icon={Wallet}
              variant="warning"
              subtitle="Total active ledgers"
            />
            <StatCard
              title="Total Transactions"
              value={stats?.totalTransactions || 0}
              icon={ArrowLeftRight}
              variant="success"
              subtitle="Completed operations"
            />
            <StatCard
              title="Total System Volume"
              value={`$${parseFloat(stats?.totalVolume || 0).toFixed(2)}`}
              icon={DollarSign}
              variant="primary"
              subtitle="Gross movement volume"
            />
          </div>

          {/* User Management Section */}
          <div className="card" style={{ marginBottom: '28px' }}>
            <div className="card-header">
              <h3 className="card-title">Registered System Users</h3>
            </div>
            <div className="table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>User ID</th>
                    <th>Full Name</th>
                    <th>Email Address</th>
                    <th>Role</th>
                    <th>Registered At</th>
                  </tr>
                </thead>
                <tbody>
                  {stats?.users && stats.users.length > 0 ? (
                    stats.users.map((u) => (
                      <tr key={u.id}>
                        <td style={{ fontFamily: 'var(--font-mono)' }}>#{u.id}</td>
                        <td style={{ fontWeight: 600 }}>{u.fullName}</td>
                        <td>{u.email}</td>
                        <td>
                          <span className={`badge ${u.role === 'ADMIN' ? 'badge-warning' : 'badge-info'}`}>
                            {u.role}
                          </span>
                        </td>
                        <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                          {new Date(u.createdAt).toLocaleDateString()}
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="5" style={{ textAlign: 'center', padding: '24px' }}>
                        No user data available.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* System Transactions */}
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Recent System-Wide Financial Activity</h3>
            </div>
            <TransactionTable
              transactions={stats?.recentTransactions || []}
              emptyMessage="No system-wide transactions found."
            />
          </div>
        </div>
      </div>
    </div>
  );
};

