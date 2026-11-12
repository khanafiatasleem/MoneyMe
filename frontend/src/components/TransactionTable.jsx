import React from 'react';
import { ArrowDownLeft, ArrowUpRight, ArrowLeftRight } from 'lucide-react';

export const TransactionTable = ({ transactions = [], emptyMessage = "No transactions found." }) => {
  if (!transactions || transactions.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '40px 20px', color: 'var(--text-muted)' }}>
        <p>{emptyMessage}</p>
      </div>
    );
  }

  const getTypeIcon = (type) => {
    switch (type) {
      case 'DEPOSIT':
        return <ArrowDownLeft size={16} color="#059669" />;
      case 'WITHDRAWAL':
        return <ArrowUpRight size={16} color="#dc2626" />;
      case 'TRANSFER':
        return <ArrowLeftRight size={16} color="#2563eb" />;
      default:
        return null;
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'SUCCESS':
        return <span className="badge badge-success">Success</span>;
      case 'PENDING':
        return <span className="badge badge-warning">Pending</span>;
      case 'FAILED':
        return <span className="badge badge-danger">Failed</span>;
      default:
        return <span className="badge badge-info">{status}</span>;
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    try {
      const d = new Date(dateString);
      return d.toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return dateString;
    }
  };

  return (
    <div className="table-container">
      <table className="data-table">
        <thead>
          <tr>
            <th>Ref / ID</th>
            <th>Type</th>
            <th>Source Account</th>
            <th>Destination Account</th>
            <th>Amount</th>
            <th>Status</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((tx) => (
            <tr key={tx.id || tx.transactionRef}>
              <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.82rem', fontWeight: 600 }}>
                {tx.transactionRef || `TX-${tx.id}`}
              </td>
              <td>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 600 }}>
                  {getTypeIcon(tx.type)}
                  <span>{tx.type}</span>
                </div>
              </td>
              <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.85rem' }}>
                {tx.sourceAccountNumber || '-'}
              </td>
              <td style={{ fontFamily: 'var(--font-mono)', fontSize: '0.85rem' }}>
                {tx.destinationAccountNumber || '-'}
              </td>
              <td style={{ fontWeight: 700, fontFamily: 'var(--font-mono)' }}>
                ${parseFloat(tx.amount || 0).toFixed(2)}
              </td>
              <td>{getStatusBadge(tx.status)}</td>
              <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                {formatDate(tx.createdAt)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

