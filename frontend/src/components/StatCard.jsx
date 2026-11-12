import React from 'react';

export const StatCard = ({ title, value, icon: Icon, variant = 'primary', subtitle }) => {
  return (
    <div className="stat-card">
      <div className={`stat-icon ${variant}`}>
        {Icon && <Icon size={24} />}
      </div>
      <div>
        <div className="stat-label">{title}</div>
        <div className="stat-value">{value}</div>
        {subtitle && <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '2px' }}>{subtitle}</div>}
      </div>
    </div>
  );
};

