import axiosClient from './axiosClient';

const generateIdempotencyKey = () => {
  return 'IDEM-' + Math.random().toString(36).substring(2, 10).toUpperCase() + '-' + Date.now();
};

export const transactionApi = {
  deposit: async (accountNumber, amount, description) => {
    const key = generateIdempotencyKey();
    const response = await axiosClient.post(
      '/transactions/deposit',
      { accountNumber, amount: parseFloat(amount), description },
      { headers: { 'Idempotency-Key': key } }
    );
    return response.data;
  },

  withdraw: async (accountNumber, amount, description) => {
    const key = generateIdempotencyKey();
    const response = await axiosClient.post(
      '/transactions/withdraw',
      { accountNumber, amount: parseFloat(amount), description },
      { headers: { 'Idempotency-Key': key } }
    );
    return response.data;
  },

  transfer: async (sourceAccountNumber, destinationAccountNumber, amount, description) => {
    const key = generateIdempotencyKey();
    const response = await axiosClient.post(
      '/transactions/transfer',
      { sourceAccountNumber, destinationAccountNumber, amount: parseFloat(amount), description },
      { headers: { 'Idempotency-Key': key } }
    );
    return response.data;
  },

  getTransactions: async (type = null) => {
    const params = type ? { type } : {};
    const response = await axiosClient.get('/transactions', { params });
    return response.data;
  },

  getTransactionById: async (id) => {
    const response = await axiosClient.get(`/transactions/${id}`);
    return response.data;
  },

  getDashboardSummary: async () => {
    const response = await axiosClient.get('/dashboard');
    return response.data;
  },

  // Admin APIs
  getAdminDashboard: async () => {
    const response = await axiosClient.get('/admin/dashboard');
    return response.data;
  },

  getAdminUsers: async () => {
    const response = await axiosClient.get('/admin/users');
    return response.data;
  },

  getAdminAccounts: async () => {
    const response = await axiosClient.get('/admin/accounts');
    return response.data;
  },

  getAdminTransactions: async () => {
    const response = await axiosClient.get('/admin/transactions');
    return response.data;
  },
};

