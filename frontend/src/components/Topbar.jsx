import React from 'react';
import { useAuth } from '../context/AuthContext';

export const Topbar = ({ title }) => {
  const { user } = useAuth();

  return (
    <header className="topbar">
      <h1 className="topbar-title">{title}</h1>
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        <div style={{ textAlign: 'right' }}>
          <div style={{ fontSize: '0.88rem', fontWeight: 600 }}>{user?.fullName}</div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{user?.email}</div>
        </div>
        <div
          style={{
            width: '38px',
            height: '38px',
            borderRadius: '50%',
            background: 'linear-gradient(135deg, #2563eb, #10b981)',
            color: 'white',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontWeight: 700,
            fontSize: '1rem',
          }}
        >
          {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
        </div>
      </div>
    </header>
  );
};

