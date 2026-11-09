import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Sidebar } from '../components/Sidebar';
import { Topbar } from '../components/Topbar';
import { User, Shield, Key, LogOut } from 'lucide-react';

export const ProfilePage = () => {
  const { user, logout } = useAuth();

  return (
    <div className="app-layout">
      <Sidebar />
      <div className="main-wrapper">
        <Topbar title="User Profile & Security" />

        <div className="page-container" style={{ maxWidth: '680px' }}>
          <div className="card" style={{ marginBottom: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '20px', marginBottom: '24px' }}>
              <div
                style={{
                  width: '64px',
                  height: '64px',
                  borderRadius: '50%',
                  background: 'linear-gradient(135deg, #2563eb, #10b981)',
                  color: 'white',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '1.6rem',
                  fontWeight: 800,
                }}
              >
                {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
              </div>
              <div>
                <h2 style={{ fontSize: '1.3rem', fontWeight: 800 }}>{user?.fullName}</h2>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>{user?.email}</p>
                <div style={{ marginTop: '6px' }}>
                  <span className={`badge ${user?.role === 'ADMIN' ? 'badge-warning' : 'badge-info'}`}>
                    {user?.role} ACCESS
                  </span>
                </div>
              </div>
            </div>

            <div style={{ borderTop: '1px solid var(--border)', paddingTop: '20px' }}>
              <h4 style={{ fontSize: '0.95rem', fontWeight: 700, marginBottom: '16px' }}>
                Account Information
              </h4>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', fontSize: '0.9rem' }}>
                <div>
                  <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.8rem' }}>User ID</span>
                  <span style={{ fontFamily: 'var(--font-mono)', fontWeight: 600 }}>#{user?.userId}</span>
                </div>
                <div>
                  <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.8rem' }}>Security Protocol</span>
                  <span style={{ fontWeight: 600 }}>JWT Bearer / BCrypt-12</span>
                </div>
              </div>
            </div>
          </div>

          {/* Security & Session Info */}
          <div className="card" style={{ marginBottom: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '12px' }}>
              <Shield size={20} color="var(--primary)" />
              <h3 className="card-title" style={{ margin: 0 }}>Security Configuration</h3>
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', lineHeight: '1.6' }}>
              Your session is protected with cryptographically signed JSON Web Tokens (JWT). Authorization is verified server-side on every request.
            </p>
            <div style={{ marginTop: '20px', paddingTop: '16px', borderTop: '1px solid var(--border)' }}>
              <button onClick={logout} className="btn btn-danger">
                <LogOut size={16} />
                <span>Sign Out of MoneyMe</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
