import React, { useEffect, useState } from 'react';
import { accountApi } from '../api/accountApi';
import { transactionApi } from '../api/transactionApi';
import { Sidebar } from '../components/Sidebar';
import { Topbar } from '../components/Topbar';
import { ConfirmModal } from '../components/ConfirmModal';
import { ArrowLeftRight, CheckCircle2, AlertCircle } from 'lucide-react';

export const TransferPage = () => {
  const [accounts, setAccounts] = useState([]);
  const [sourceAccount, setSourceAccount] = useState('');
  const [destinationAccount, setDestinationAccount] = useState('');
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
      if (data.length > 0 && !sourceAccount) {
        setSourceAccount(data[0].accountNumber);
      }
    } catch {
      setError('Failed to load accounts.');
    }
  };

  useEffect(() => {
    fetchAccounts();
  }, []);

  const activeSourceAcc = accounts.find((a) => a.accountNumber === sourceAccount);
  const availableBalance = activeSourceAcc ? parseFloat(activeSourceAcc.balance) : 0;

  const handlePreSubmit = (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Please enter a valid transfer amount strictly greater than zero.');
      return;
    }

    if (!sourceAccount || !destinationAccount) {
      setError('Please provide both source and destination accounts.');
      return;
    }

    if (sourceAccount.trim().toUpperCase() === destinationAccount.trim().toUpperCase()) {
      setError('Source and destination accounts cannot be identical.');
      return;
    }

    if (parsedAmount > availableBalance) {
      setError(`Transfer amount exceeds available balance ($${availableBalance.toFixed(2)}).`);
      return;
    }

    setShowConfirm(true);
  };

  const handleConfirmTransfer = async () => {
    setLoading(true);
    setShowConfirm(false);
    try {
      const res = await transactionApi.transfer(
        sourceAccount,
        destinationAccount.trim().toUpperCase(),
        amount,
        description || 'P2P Money Transfer'
      );
      setSuccess(`Transfer of $${parseFloat(amount).toFixed(2)} to ${destinationAccount.trim().toUpperCase()} completed! Ref: ${res.transactionRef}`);
      setAmount('');
      setDestinationAccount('');
      setDescription('');
      fetchAccounts();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Transfer failed. Please check accounts and balance.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-wrapper">
        <Topbar title="Transfer Money" />

        <div className="page-container" style={{ maxWidth: '640px' }}>
          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px' }}>
              <div
                style={{
                  width: '42px',
                  height: '42px',
                  borderRadius: '10px',
                  background: '#dbeafe',
                  color: '#2563eb',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <ArrowLeftRight size={24} />
              </div>
              <div>
                <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>Account Transfer</h2>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                  Atomic, double-entry balanced transfer between MoneyMe accounts
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
                <label className="form-label">Transfer From (Your Account)</label>
                <select
                  className="form-select"
                  value={sourceAccount}
                  onChange={(e) => setSourceAccount(e.target.value)}
                  disabled={loading || accounts.length === 0}
                  required
                >
                  {accounts.map((acc) => (
                    <option key={acc.id} value={acc.accountNumber}>
                      {acc.accountNumber} — Available: ${parseFloat(acc.balance).toFixed(2)} ({acc.currency})
                    </option>
                  ))}
                </select>
                {activeSourceAcc && (
                  <span className="form-help">
                    Available balance: <strong>${availableBalance.toFixed(2)}</strong>
                  </span>
                )}
              </div>

              <div className="form-group">
                <label className="form-label">Transfer To (Destination Account Number)</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. MM10002345"
                  value={destinationAccount}
                  onChange={(e) => setDestinationAccount(e.target.value)}
                  disabled={loading}
                  style={{ fontFamily: 'var(--font-mono)' }}
                  required
                />
                <span className="form-help">Enter recipient's full 10-character account number</span>
              </div>

              <div className="form-group">
                <label className="form-label">Transfer Amount ($)</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  max={availableBalance}
                  className="form-input"
                  placeholder="0.00"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                  disabled={loading}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Transfer Note / Description</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Rent, Gift, Project Payment"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  disabled={loading}
                />
              </div>

              <button
                type="submit"
                className="btn btn-primary"
                style={{ width: '100%', marginTop: '8px' }}
                disabled={loading || accounts.length === 0 || availableBalance <= 0}
              >
                {loading ? 'Executing Transfer...' : 'Review & Transfer Funds'}
              </button>
            </form>
          </div>
        </div>
      </div>

      <ConfirmModal
        isOpen={showConfirm}
        title="Confirm Money Transfer"
        message={`Transfer $${parseFloat(amount || 0).toFixed(2)} from account ${sourceAccount} to ${destinationAccount.trim().toUpperCase()}? This transaction is immediate and debit/credit balanced.`}
        onConfirm={handleConfirmTransfer}
        onCancel={() => setShowConfirm(false)}
        confirmText="Confirm Transfer"
        loading={loading}
      />
    </div>
  );
};
