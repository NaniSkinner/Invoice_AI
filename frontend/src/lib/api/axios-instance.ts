import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

/**
 * Configured axios instance with Basic Auth
 */
export const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Add Basic Auth header to all requests
 */
axiosInstance.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      const credentials = localStorage.getItem('auth_credentials');
      if (credentials) {
        config.headers.Authorization = `Basic ${credentials}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Handle 401 errors by redirecting to login
 */
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && typeof window !== 'undefined') {
      localStorage.removeItem('auth_credentials');
      localStorage.removeItem('user_info');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
