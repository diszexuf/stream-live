import axios from 'axios';

const API_URL = '/api/stream-key';

export default {
    async getStreamKey() {
        return axios.get(API_URL);
    },

    async resetStreamKey() {
        return axios.post(`${API_URL}/reset`);
    }
} 