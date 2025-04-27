import axios from 'axios';

const API_URL = '/api/users';

export default {
    async login(username, password) {
        try {
            const response = await axios.post(`${API_URL}/login`, {
                username,
                password
            });
            if (response.data) {
                localStorage.setItem('user', JSON.stringify(response.data));
            }
            return response.data;
        } catch (error) {
            throw error;
        }
    },

    logout() {
        localStorage.removeItem('user');
    },

    getCurrentUser() {
        const userStr = localStorage.getItem('user');
        if (userStr) return JSON.parse(userStr);
        return null;
    },

    async register(username, password, email) {
        return axios.post(`${API_URL}`, {
            username,
            password,
            email
        });
    },

    async updateProfile(userId, userData) {
        return axios.put(`${API_URL}/${userId}`, userData);
    },

    async getProfile() {
        return axios.get(`${API_URL}/profile`);
    }
} 