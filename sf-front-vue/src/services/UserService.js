import axios from 'axios';

const API_URL = '/api/users';

export default {
    async getAllUsers() {
        return axios.get(API_URL);
    },

    async getUserById(id) {
        return axios.get(`${API_URL}/${id}`);
    },

    async getUserByUsername(username) {
        return axios.get(`${API_URL}/username/${username}`);
    },

    async createUser(user) {
        return axios.post(API_URL, user);
    },

    async updateUser(id, user) {
        return axios.put(`${API_URL}/${id}`, user);
    },

    async deleteUser(id) {
        return axios.delete(`${API_URL}/${id}`);
    },

    async login(credentials) {
        return axios.post(`${API_URL}/login`, credentials);
    },

    async getProfile() {
        return axios.get(`${API_URL}/profile`);
    }
}

export const createUser = async (user) => {
  const response = await axios.post(API_URL, user);
  return response.data;
};

export const updateUser = async (id, user) => {
  const response = await axios.put(`${API_URL}/${id}`, user);
  return response.data;
};

export const login = async (username, password) => {
  const response = await axios.post(`${API_URL}/login`, null, {
    params: { username, password }
  });
  return response.data;
};

export const getUserProfile = async () => {
  const response = await axios.get(`${API_URL}/profile`);
  return response.data;
}; 