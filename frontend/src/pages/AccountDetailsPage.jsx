import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { accountApi } from '../api/accountApi';
import { transactionApi } from '../api/transactionApi';
import { Sidebar } from '../components/Sidebar';
import { Topbar } from '../components/Topbar';
import { TransactionTable } from '../components/TransactionTable';
import { Wallet, ArrowDownLeft, ArrowUpRight, ArrowLeftRight, AlertCircle, ArrowLeft } from 'lucide-react';

export const AccountDetailsPage = () => {
  const { id } = useParams();
  const [account, setAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadDetails = async () => {
      setLoading(true);
      setError('');
      try {
        const acc = await accountApi.getAccountById(id);
        setAccount(acc);

        // Fetch user transactions and filter by this account
        const allTxs = await transactionApi.getTransactions();
        const filtered = allTxs.filter(
          (t) => t.sourceAccountNumber === acc.accountNumber || t.destinationAccountNumber === acc.accountNumber
        );
        setTransactions(filtered);
      } catch (err) {
        setError(err.response?.data?.message || err.message || 'Failed to load account details');
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      loadDetails();
    }
  }, [id]);

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-wrapper">
        <Topbar title={`Account ${account ? account.accountNumber : ''}`} />

        <div className="page-container">
          <Link to="/accounts" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', color: 'var(--text-muted)', fontSize: '0.88rem', marginBottom: '20px' }}>
            <ArrowLeft size={16} />
            <span>Back to Accounts</span>
          </Link>

          {error && (
            <div className="alert alert-danger">
              <AlertCircle size={18} />
              <span>{error}</span>
            </div>
          )}

          {loading ? (
            <p style={{ color: 'var(--text-muted)' }}>Loading account...</p>
          ) : account ? (
            <>
              {/* Account Hero Card */}
              <div
                className="card"
                style={{
                  background: 'linear-gradient(135deg, #0f172a, #1e293b)',
                  color: 'white',
                  marginBottom: '28px',
                  padding: '32px',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <span style={{ fontSize: '0.82rem', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                      Account Identifier
                    </span>
                    <h2 style={{ fontSize: '1.8rem', fontFamily: 'var(--font-mono)', fontWeight: 800, marginTop: '4px' }}>
                      {account.accountNumber}
                    </h2>
                    <p style={{ color: '#94a3b8', fontSize: '0.88rem', marginTop: '4px' }}>
                      Primary Owner: {account.userFullName}
                    </p>
                  </div>
                  <span className="badge badge-success" style={{ fontSize: '0.85rem', padding: '6px 14px' }}>
                    {account.status}
                  </span>
                </div>

                <div style={{ marginTop: '28px', paddingTop: '24px', borderTop: '1px solid rgba(255,255,255,0.1)', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', flexWrap: 'wrap', gap: '16px' }}>
                  <div>
                    <span style={{ fontSize: '0.82rem', color: '#94a3b8' }}>Available Ledger Balance</span>
                    <div style={{ fontSize: '2.4rem', fontWeight: 800, fontFamily: 'var(--font-mono)', color: '#34d399' }}>
                      ${parseFloat(account.balance || 0).toFixed(2)}
                    </div>
                  </div>

                  <div style={{ display: 'flex', gap: '10px' }}>
                    <Link to="/deposit" className="btn btn-primary" style={{ background: '#10b981' }}>
                      <ArrowDownLeft size={16} />
                      <span>Deposit</span>
                    </Link>
                    <Link to="/withdraw" className="btn btn-secondary" style={{ background: 'rgba(255,255,255,0.1)', color: 'white', borderColor: 'transparent' }}>
                      <ArrowUpRight size={16} />
                      <span>Withdraw</span>
                    </Link>
                    <Link to="/transfer" className="btn btn-secondary" style={{ background: 'rgba(255,255,255,0.1)', color: 'white', borderColor: 'transparent' }}>
                      <ArrowLeftRight size={16} />
                      <span>Transfer</span>
                    </Link>
                  </div>
                </div>
              </div>

              {/* Transactions for this account */}
              <div className="card">
                <div className="card-header">
                  <h3 className="card-title">Transaction Activity for {account.accountNumber}</h3>
                </div>
                <TransactionTable transactions={transactions} emptyMessage="No transactions recorded for this account yet." />
              </div>
            </>
          ) : null}
        </div>
      </div>
    </div>
  );
};
