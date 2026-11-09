import axiosClient from './axiosClient';

export const authApi = {
  register: async (fullName, email, password) => {
    const response = await axiosClient.post('/auth/register', { fullName, email, password });
    return response.data;
  },

  login: async (email, password) => {
    const response = await axiosClient.post('/auth/login', { email, password });
    return response.data;
  },
};
