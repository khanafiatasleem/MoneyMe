import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/authApi';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('moneyme_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('moneyme_token'));
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (token && user) {
      localStorage.setItem('moneyme_token', token);
      localStorage.setItem('moneyme_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('moneyme_token');
      localStorage.removeItem('moneyme_user');
    }
  }, [token, user]);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const data = await authApi.login(email, password);
      const userData = {
        userId: data.userId,
        email: data.email,
        fullName: data.fullName,
        role: data.role,
      };
      setToken(data.token);
      setUser(userData);
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const register = async (fullName, email, password) => {
    setLoading(true);
    try {
      const data = await authApi.register(fullName, email, password);
      const userData = {
        userId: data.userId,
        email: data.email,
        fullName: data.fullName,
        role: data.role,
      };
      setToken(data.token);
      setUser(userData);
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('moneyme_token');
    localStorage.removeItem('moneyme_user');
  };

  const isAuthenticated = !!token;
  const isAdmin = user?.role === 'ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated,
        isAdmin,
        loading,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
