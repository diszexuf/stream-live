import axios from 'axios';

const API_URL = '/api/stream-settings';

export default {
    async updateStreamSettings(settings) {
        return axios.put(API_URL, settings);
    },

    async getStreamSettings() {
        return axios.get(API_URL);
    }
} 