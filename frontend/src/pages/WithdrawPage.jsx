import React, { useEffect, useState } from 'react';
import { accountApi } from '../api/accountApi';
import { transactionApi } from '../api/transactionApi';
import { Sidebar } from '../components/Sidebar';
import { Topbar } from '../components/Topbar';
import { ConfirmModal } from '../components/ConfirmModal';
import { ArrowUpRight, CheckCircle2, AlertCircle } from 'lucide-react';

export const WithdrawPage = () => {
  const [accounts, setAccounts] = useState([]);
  const [selectedAccount, setSelectedAccount] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showConfirm, setShowConfirm] = useState(false);

  const fetchAccounts = async () => {
    try {
      const data = await accountApi.getAccounts();
      setAccounts(data);
      if (data.length > 0 && !selectedAccount) {
        setSelectedAccount(data[0].accountNumber);
      }
    } catch {
      setError('Failed to load accounts.');
    }
  };

  useEffect(() => {
    fetchAccounts();
  }, []);

  const activeAccObj = accounts.find((a) => a.accountNumber === selectedAccount);
  const currentBalance = activeAccObj ? parseFloat(activeAccObj.balance) : 0;

  const handlePreSubmit = (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Please enter a valid withdrawal amount strictly greater than zero.');
      return;
    }
    if (parsedAmount > currentBalance) {
      setError(`Withdrawal exceeds available balance ($${currentBalance.toFixed(2)}).`);
      return;
    }

    setShowConfirm(true);
  };

  const handleConfirmWithdraw = async () => {
    setLoading(true);
    setShowConfirm(false);
    try {
      const res = await transactionApi.withdraw(selectedAccount, amount, description || 'ATM / Cash Withdrawal');
      setSuccess(`Withdrawal of $${parseFloat(amount).toFixed(2)} completed successfully! Ref: ${res.transactionRef}`);
      setAmount('');
      setDescription('');
      fetchAccounts();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Withdrawal failed. Please check your balance.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-wrapper">
        <Topbar title="Withdraw Funds" />

        <div className="page-container" style={{ maxWidth: '640px' }}>
          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px' }}>
              <div
                style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: '10px',
                  background: '#fee2e2',
                  color: '#dc2626',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <ArrowUpRight size={24} />
              </div>
              <div>
                <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Account Withdrawal</h2>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  Safely withdraw funds with real-time balance validation
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
                <label className="form-label">Source Account</label>
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
                {activeAccObj && (
                  <span className="form-help">
                    Maximum withdrawable funds: <strong>${currentBalance.toFixed(2)}</strong>
                  </span>
                )}
              </div>

              <div className="form-group">
                <label className="form-label">Withdrawal Amount ($)</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  max={currentBalance}
                  className="form-input"
                  placeholder="0.00"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  disabled={loading}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Reason / Reference</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Living expenses, Cash Out"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  disabled={loading}
                />
              </div>

              <button
                type="submit"
                className="btn btn-primary"
                style={{ width: '100%', marginTop: '8px', background: '#dc2626' }}
                disabled={loading || accounts.length === 0 || currentBalance <= 0}
              >
                {loading ? 'Processing Withdrawal...' : 'Review & Confirm Withdrawal'}
              </button>
            </form>
          </div>
        </div>
      </div>

      <ConfirmModal
        isOpen={showConfirm}
        title="Confirm Withdrawal"
        message={`Are you sure you want to withdraw $${parseFloat(amount || 0).toFixed(2)} from account ${selectedAccount}? Your remaining balance will be $${(currentBalance - parseFloat(amount || 0)).toFixed(2)}.`}
        onConfirm={handleConfirmWithdraw}
        onCancel={() => setShowConfirm(false)}
        confirmText="Confirm Withdrawal"
        isDanger={true}
        loading={loading}
      />
    </div>
  );
};
