import api from './api';
import { API_ENDPOINTS } from '../constants/api';

class UserService {
  async getProfile() {
    const response = await api.get(API_ENDPOINTS.USERS.PROFILE);
    return response.data;
  }

  async updateProfile(userData) {
    const response = await api.put(API_ENDPOINTS.USERS.UPDATE_PROFILE, userData);
    return response.data;
  }

  async deleteAccount() {
    const response = await api.delete(API_ENDPOINTS.USERS.DELETE_ACCOUNT);
    return response.data;
  }

  async getAllUsers() {
    const response = await api.get('/users/all');
    return response.data;
  }

  async searchUsers(searchTerm) {
    const response = await api.get(`/users/search?q=${encodeURIComponent(searchTerm)}`);
    return response.data;
  }

  async getStatistics() {
    const response = await api.get(API_ENDPOINTS.USERS.STATISTICS);
    return response.data;
  }
}

export default new UserService();
