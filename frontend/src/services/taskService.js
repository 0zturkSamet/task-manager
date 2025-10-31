import api from './api';
import { API_ENDPOINTS } from '../constants/api';

class TaskService {
  async getAllTasks() {
    const response = await api.get(API_ENDPOINTS.TASKS.BASE);
    return response.data;
  }

  async getProjectTasks(projectId) {
    const response = await api.get(API_ENDPOINTS.TASKS.PROJECT_TASKS(projectId));
    return response.data;
  }

  async getTaskById(id) {
    const response = await api.get(API_ENDPOINTS.TASKS.BY_ID(id));
    return response.data;
  }

  async createTask(taskData) {
    const response = await api.post(API_ENDPOINTS.TASKS.BASE, taskData);
    return response.data;
  }

  async updateTask(id, taskData) {
    const response = await api.put(API_ENDPOINTS.TASKS.BY_ID(id), taskData);
    return response.data;
  }

  async deleteTask(id) {
    const response = await api.delete(API_ENDPOINTS.TASKS.BY_ID(id));
    return response.data;
  }

  async filterTasks(filters) {
    const response = await api.post(API_ENDPOINTS.TASKS.FILTER, filters);
    return response.data;
  }

  async getProjectStatistics(projectId) {
    const response = await api.get(API_ENDPOINTS.TASKS.STATISTICS(projectId));
    return response.data;
  }

  async addComment(taskId, commentData) {
    const response = await api.post(API_ENDPOINTS.TASKS.COMMENTS(taskId), commentData);
    return response.data;
  }

  async getTaskComments(taskId) {
    const response = await api.get(API_ENDPOINTS.TASKS.COMMENTS(taskId));
    return response.data;
  }

  async likeComment(commentId) {
    const response = await api.post(API_ENDPOINTS.TASKS.LIKE_COMMENT(commentId));
    return response.data;
  }

  async dislikeComment(commentId) {
    const response = await api.post(API_ENDPOINTS.TASKS.DISLIKE_COMMENT(commentId));
    return response.data;
  }
}

export default new TaskService();
