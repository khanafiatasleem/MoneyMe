import axiosClient from './axiosClient';

export const accountApi = {
  createAccount: async (initialDeposit = 0, currency = 'USD') => {
    const response = await axiosClient.post('/accounts', { initialDeposit, currency });
    return response.data;
  },

  getAccounts: async () => {
    const response = await axiosClient.get('/accounts');
    return response.data;
  },

  getAccountById: async (id) => {
    const response = await axiosClient.get(`/accounts/${id}`);
    return response.data;
  },
};
