import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  Wallet,
  ArrowDownLeft,
  ArrowUpRight,
  ArrowLeftRight,
  History,
  ShieldCheck,
  User,
  LogOut,
} from 'lucide-react';

export const Sidebar = () => {
  const { user, isAdmin, logout } = useAuth();

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <div className="sidebar-logo">M</div>
        <div>
          <h2 style={{ fontSize: '1.15rem', fontWeight: 800, letterSpacing: '-0.02em', color: '#ffffff' }}>MoneyMe</h2>
          <p style={{ fontSize: '0.72rem', color: 'var(--sidebar-muted)' }}>Ledger You Can Trust</p>
        </div>
      </div>

      <nav className="sidebar-nav">
        <span className="nav-label">Main Menu</span>
        <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <LayoutDashboard size={18} />
          <span>Dashboard</span>
        </NavLink>
        <NavLink to="/accounts" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <Wallet size={18} />
          <span>My Accounts</span>
        </NavLink>

        <span className="nav-label">Transactions</span>
        <NavLink to="/deposit" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <ArrowDownLeft size={18} />
          <span>Deposit</span>
        </NavLink>
        <NavLink to="/withdraw" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <ArrowUpRight size={18} />
          <span>Withdraw</span>
        </NavLink>
        <NavLink to="/transfer" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <ArrowLeftRight size={18} />
          <span>Transfer</span>
        </NavLink>
        <NavLink to="/history" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <History size={18} />
          <span>History</span>
        </NavLink>

        {isAdmin && (
          <>
            <div className="nav-divider" />
            <span className="nav-label" style={{ color: '#fbbf24' }}>Administration</span>
            <NavLink to="/admin" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <ShieldCheck size={18} color="#fbbf24" />
              <span style={{ color: '#fbbf24', fontWeight: 600 }}>Admin Console</span>
            </NavLink>
          </>
        )}

        <div className="nav-divider" />
        <NavLink to="/profile" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <User size={18} />
          <span>Profile</span>
        </NavLink>
      </nav>

      <div className="sidebar-user">
        <div className="user-info">
          <span className="user-name">{user?.fullName || 'User'}</span>
          <span className="user-role">{user?.role || 'USER'}</span>
        </div>
        <button onClick={logout} className="logout-btn" title="Sign Out">
          <LogOut size={18} />
        </button>
      </div>
    </aside>
  );
};
