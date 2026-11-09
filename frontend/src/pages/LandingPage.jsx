import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldCheck, ArrowLeftRight, Landmark, Lock, CheckCircle2, Zap } from 'lucide-react';

export const LandingPage = () => {
  return (
    <div style={{ minHeight: '100vh', background: '#0f172a', color: '#f8fafc', display: 'flex', flexDirection: 'column' }}>
      {/* Navbar */}
      <header style={{ padding: '20px 48px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ width: '38px', height: '38px', background: 'linear-gradient(135deg, #2563eb, #10b981)', borderRadius: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 800, fontSize: '1.2rem', color: 'white' }}>
            M
          </div>
          <span style={{ fontSize: '1.3rem', fontWeight: 800, letterSpacing: '-0.02em' }}>MoneyMe</span>
        </div>
        <div style={{ display: 'flex', gap: '16px' }}>
          <Link to="/login" className="btn btn-secondary" style={{ background: 'transparent', color: '#ffffff', borderColor: 'rgba(255,255,255,0.2)' }}>
            Sign In
          </Link>
          <Link to="/register" className="btn btn-primary" style={{ background: '#2563eb', color: '#ffffff' }}>
            Open Account
          </Link>
        </div>
      </header>

      {/* Hero Section */}
      <main style={{ flex: 1, padding: '80px 24px', maxWidth: '1100px', margin: '0 auto', textAlign: 'center' }}>
        <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', background: 'rgba(37,99,235,0.15)', border: '1px solid rgba(37,99,235,0.3)', padding: '6px 16px', borderRadius: '9999px', fontSize: '0.85rem', color: '#60a5fa', marginBottom: '24px' }}>
          <ShieldCheck size={16} />
          <span>Snovasys Fresher Hiring Challenge 2026 Production Build</span>
        </div>

        <h1 style={{ fontSize: '3.5rem', fontWeight: 800, lineHeight: 1.15, letterSpacing: '-0.03em', marginBottom: '20px' }}>
          Smart Money Movement with a <br />
          <span style={{ background: 'linear-gradient(135deg, #60a5fa, #34d399)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            Ledger You Can Trust.
          </span>
        </h1>

        <p style={{ fontSize: '1.2rem', color: '#94a3b8', maxWidth: '700px', margin: '0 auto 36px', lineHeight: 1.6 }}>
          MoneyMe provides bulletproof financial transactions backed by an immutable double-entry ledger, deadlock-free concurrency locking, and cryptographically verified idempotency.
        </p>

        <div style={{ display: 'flex', justifyContent: 'center', gap: '16px', marginBottom: '64px' }}>
          <Link to="/register" className="btn btn-primary" style={{ padding: '14px 28px', fontSize: '1.05rem', borderRadius: '8px' }}>
            Get Started Now
          </Link>
          <Link to="/login" className="btn btn-secondary" style={{ padding: '14px 28px', fontSize: '1.05rem', borderRadius: '8px', background: 'rgba(255,255,255,0.06)', color: 'white', borderColor: 'rgba(255,255,255,0.15)' }}>
            Access Demo Account
          </Link>
        </div>

        {/* Feature Cards Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '24px', textAlign: 'left' }}>
          <div style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.08)', borderRadius: '12px', padding: '28px' }}>
            <div style={{ width: '44px', height: '44px', borderRadius: '8px', background: 'rgba(16,185,129,0.15)', color: '#34d399', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px' }}>
              <Landmark size={24} />
            </div>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '8px' }}>Double-Entry Ledger</h3>
            <p style={{ color: '#94a3b8', fontSize: '0.92rem', lineHeight: 1.6 }}>
              Every cent deposited, withdrawn, or transferred is strictly accounted for. The sum of debits identically equals credits with zero leakage.
            </p>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.08)', borderRadius: '12px', padding: '28px' }}>
            <div style={{ width: '44px', height: '44px', borderRadius: '8px', background: 'rgba(37,99,235,0.15)', color: '#60a5fa', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px' }}>
              <ArrowLeftRight size={24} />
            </div>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '8px' }}>Atomic Transfers & Concurrency</h3>
            <p style={{ color: '#94a3b8', fontSize: '0.92rem', lineHeight: 1.6 }}>
              Pessimistic database write locks arranged in strict ascending key order prevent race conditions and eliminate deadlocks during concurrent transfers.
            </p>
          </div>

          <div style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.08)', borderRadius: '12px', padding: '28px' }}>
            <div style={{ width: '44px', height: '44px', borderRadius: '8px', background: 'rgba(245,158,11,0.15)', color: '#fbbf24', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px' }}>
              <Lock size={24} />
            </div>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, marginBottom: '8px' }}>Idempotency & Audit Logs</h3>
            <p style={{ color: '#94a3b8', fontSize: '0.92rem', lineHeight: 1.6 }}>
              Network retries never create duplicate debits or credits. All financial events maintain an append-only immutable audit trail.
            </p>
          </div>
        </div>
      </main>

      <footer style={{ padding: '24px', textAlign: 'center', borderTop: '1px solid rgba(255,255,255,0.08)', color: '#64748b', fontSize: '0.85rem' }}>
        MoneyMe &copy; 2026 Snovasys Fresher Hiring Challenge. Built with Spring Boot 3 & React.
      </footer>
    </div>
  );
};
