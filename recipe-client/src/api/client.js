import axios from 'axios';

const apiClient = axios.create({
    baseURL: 'https://javalab1-production.up.railway.app/api/v1',
    headers: { 'Content-Type': 'application/json' }
});

export default apiClient;