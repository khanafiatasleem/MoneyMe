import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { accountApi } from '../api/accountApi';
import { Sidebar } from '../components/Sidebar';
import { Topbar } from '../components/Topbar';
import { Wallet, PlusCircle, AlertCircle, CheckCircle2 } from 'lucide-react';

export const AccountsPage = () => {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Create form state
  const [showCreate, setShowCreate] = useState(false);
  const [initialDeposit, setInitialDeposit] = useState('0');
  const [currency, setCurrency] = useState('USD');
  const [createLoading, setCreateLoading] = useState(false);

  const fetchAccounts = async () => {
    setLoading(true);
    try {
      const data = await accountApi.getAccounts();
      setAccounts(data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load accounts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAccounts();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setCreateLoading(true);

    try {
      const newAcc = await accountApi.createAccount(parseFloat(initialDeposit || 0), currency);
      setSuccess(`Account ${newAcc.accountNumber} successfully created!`);
      setShowCreate(false);
      setInitialDeposit('0');
      fetchAccounts();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to create account');
    } finally {
      setCreateLoading(false);
    }
  };

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-wrapper">
        <Topbar title="Account Management" />

        <div className="page-container">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
            <div>
              <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>Your Financial Accounts</h2>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                Manage balances, review account IDs, and open new accounts
              </p>
            </div>
            <button
              onClick={() => setShowCreate(!showCreate)}
              className="btn btn-primary"
            >
              <PlusCircle size={18} />
              <span>{showCreate ? 'Cancel' : 'Open New Account'}</span>
            </button>
          </div>

          {error && (
            <div className="alert alert-danger">
              <AlertCircle size={18} />
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="alert alert-success">
              <CheckCircle2 size={18} />
              <span>{success}</span>
            </div>
          )}

          {/* Create Form Drawer / Card */}
          {showCreate && (
            <div className="card" style={{ marginBottom: '28px', borderLeft: '4px solid var(--primary)' }}>
              <h3 className="card-title" style={{ marginBottom: '16px' }}>Open a New MoneyMe Account</h3>
              <form onSubmit={handleCreate}>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px' }}>
                  <div className="form-group" style={{ margin: 0 }}>
                    <label className="form-label">Currency</label>
                    <select
                      className="form-select"
                      value={currency}
                      onChange={(e) => setCurrency(e.target.value)}
                    >
                      <option value="USD">USD - US Dollar</option>
                      <option value="INR">INR - Indian Rupee</option>
                      <option value="EUR">EUR - Euro</option>
                      <option value="GBP">GBP - British Pound</option>
                    </select>
                  </div>

                  <div className="form-group" style={{ margin: 0 }}>
                    <label className="form-label">Initial Opening Deposit ($)</label>
                    <input
                      type="number"
                      step="0.01"
                      min="0"
                      className="form-input"
                      value={initialDeposit}
                      onChange={(e) => setInitialDeposit(e.target.value)}
                      placeholder="0.00"
                    />
                    <span className="form-help">Enter 0 for empty account or deposit funds now</span>
                  </div>
                </div>

                <div style={{ marginTop: '20px', display: 'flex', gap: '12px' }}>
                  <button type="submit" className="btn btn-primary" disabled={createLoading}>
                    {createLoading ? 'Opening Account...' : 'Confirm Account Creation'}
                  </button>
                  <button type="button" className="btn btn-secondary" onClick={() => setShowCreate(false)}>
                    Cancel
                  </button>
                </div>
              </form>
            </div>
          )}

          {/* Accounts Grid */}
          {loading ? (
            <p style={{ color: 'var(--text-muted)' }}>Loading accounts...</p>
          ) : accounts.length === 0 ? (
            <div className="card" style={{ textAlign: 'center', padding: '48px' }}>
              <Wallet size={48} style={{ margin: '0 auto 16px', color: 'var(--text-muted)' }} />
              <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '8px' }}>No Accounts Found</h3>
              <p style={{ color: 'var(--text-muted)', marginBottom: '20px' }}>
                Open your first checking or savings account to start depositing and moving funds.
              </p>
              <button onClick={() => setShowCreate(true)} className="btn btn-primary">
                Open Your First Account
              </button>
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
              {accounts.map((acc) => (
                <div key={acc.id} className="card">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <span className="badge badge-info">{acc.currency}</span>
                    <span className="badge badge-success">{acc.status}</span>
                  </div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Account Number</div>
                  <div style={{ fontFamily: 'var(--font-mono)', fontSize: '1.2rem', fontWeight: 800, margin: '4px 0 16px' }}>
                    {acc.accountNumber}
                  </div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Available Balance</div>
                  <div style={{ fontSize: '1.8rem', fontWeight: 800, fontFamily: 'var(--font-mono)', color: 'var(--text-main)', marginBottom: '20px' }}>
                    ${parseFloat(acc.balance || 0).toFixed(2)}
                  </div>
                  <div style={{ display: 'flex', gap: '8px' }}>
                    <Link to={`/accounts/${acc.id}`} className="btn btn-secondary" style={{ flex: 1, fontSize: '0.85rem' }}>
                      Details & Ledger
                    </Link>
                    <Link to="/deposit" className="btn btn-primary" style={{ fontSize: '0.85rem' }}>
                      Deposit
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
