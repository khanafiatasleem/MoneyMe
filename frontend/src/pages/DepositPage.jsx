import React, { useEffect, useState } from 'react';
import { accountApi } from '../api/accountApi';
import { transactionApi } from '../api/transactionApi';
import { Sidebar } from '../components/Sidebar';
import { Topbar } from '../components/Topbar';
import { ConfirmModal } from '../components/ConfirmModal';
import { ArrowDownLeft, CheckCircle2, AlertCircle } from 'lucide-react';

export const DepositPage = () => {
  const [accounts, setAccounts] = useState([]);
  const [selectedAccount, setSelectedAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showConfirm, setShowConfirm] = useState(false);

  useEffect(() => {
    const loadAccounts = async () => {
      try {
        const data = await accountApi.getAccounts();
        setAccounts(data);
        if (data.length > 0) {
          setSelectedAccount(data[0].accountNumber);
        }
      } catch (err) {
        setError('Failed to load your accounts.');
      }
    };
    loadAccounts();
  }, []);

  const handlePreSubmit = (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Please enter a valid deposit amount strictly greater than zero.');
      return;
    }
    if (!selectedAccount) {
      setError('Please select an account.');
      return;
    }

    setShowConfirm(true);
  };

  const handleConfirmDeposit = async () => {
    setLoading(true);
    setShowConfirm(false);
    try {
      const res = await transactionApi.deposit(selectedAccount, amount, description || 'Direct Deposit');
      setSuccess(`Deposit of $${parseFloat(amount).toFixed(2)} completed successfully! Ref: ${res.transactionRef}`);
      setAmount('');
      setDescription('');
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Deposit failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-wrapper">
        <Topbar title="Deposit Funds" />

        <div className="page-container" style={{ maxWidth: '640px' }}>
          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px' }}>
              <div
                style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: '10px',
                  background: '#d1fae5',
                  color: '#059669',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <ArrowDownLeft size={24} />
              </div>
              <div>
                <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Account Deposit</h2>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  Funds are credited directly to your ledger
                </p>
              </div>
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

            <form onSubmit={handlePreSubmit}>
              <div className="form-group">
                <label className="form-label">Destination Account</label>
                <select
                  className="form-select"
                  value={selectedAccount}
                  onChange={(e) => setSelectedAccount(e.target.value)}
                  disabled={loading || accounts.length === 0}
                  required
                >
                  {accounts.map((acc) => (
                    <option key={acc.id} value={acc.accountNumber}>
                      {acc.accountNumber} — Available: ${parseFloat(acc.balance).toFixed(2)} ({acc.currency})
                    </option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Amount ($)</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  className="form-input"
                  placeholder="0.00"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  disabled={loading}
                  required
                />
                <span className="form-help">Minimum deposit amount is $0.01</span>
              </div>

              <div className="form-group">
                <label className="form-label">Description (Optional)</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Salary, Client Invoice, Savings"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  disabled={loading}
                />
              </div>

              <button
                type="submit"
                className="btn btn-primary"
                style={{ width: '100%', marginTop: '8px', background: '#059669' }}
                disabled={loading || accounts.length === 0}
              >
                {loading ? 'Processing Deposit...' : 'Review & Confirm Deposit'}
              </button>
            </form>
          </div>
        </div>
      </div>

      <ConfirmModal
        isOpen={showConfirm}
        title="Confirm Account Deposit"
        message={`Are you sure you want to deposit $${parseFloat(amount || 0).toFixed(2)} into account ${selectedAccount}? This operation will create a double-entry ledger credit.`}
        onConfirm={handleConfirmDeposit}
        onCancel={() => setShowConfirm(false)}
        confirmText="Yes, Deposit Funds"
        loading={loading}
      />
    </div>
  );
};

