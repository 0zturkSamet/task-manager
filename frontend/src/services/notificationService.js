import api from './api';

const notificationService = {
  // Get all notifications
  async getNotifications() {
    const response = await api.get('/notifications');
    return response.data;
  },

  // Get unread notifications
  async getUnreadNotifications() {
    const response = await api.get('/notifications/unread');
    return response.data;
  },

  // Get unread notification count
  async getUnreadCount() {
    const response = await api.get('/notifications/count');
    return response.data.count;
  },

  // Mark notification as read
  async markAsRead(notificationId) {
    const response = await api.put(`/notifications/${notificationId}/read`);
    return response.data;
  },

  // Mark all notifications as read
  async markAllAsRead() {
    const response = await api.put('/notifications/read-all');
    return response.data;
  }
};

export default notificationService;
