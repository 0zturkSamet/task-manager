import api from './api';
import { API_ENDPOINTS } from '../constants/api';

class ProjectService {
  async getAllProjects() {
    const response = await api.get(API_ENDPOINTS.PROJECTS.BASE);
    return response.data;
  }

  async getProjectById(id) {
    const response = await api.get(API_ENDPOINTS.PROJECTS.BY_ID(id));
    return response.data;
  }

  async createProject(projectData) {
    const response = await api.post(API_ENDPOINTS.PROJECTS.BASE, projectData);
    return response.data;
  }

  async updateProject(id, projectData) {
    const response = await api.put(API_ENDPOINTS.PROJECTS.BY_ID(id), projectData);
    return response.data;
  }

  async deleteProject(id) {
    const response = await api.delete(API_ENDPOINTS.PROJECTS.BY_ID(id));
    return response.data;
  }

  async getProjectMembers(id) {
    const response = await api.get(API_ENDPOINTS.PROJECTS.MEMBERS(id));
    return response.data;
  }

  async addMember(projectId, memberData) {
    const response = await api.post(API_ENDPOINTS.PROJECTS.MEMBERS(projectId), memberData);
    return response.data;
  }

  async updateMemberRole(projectId, memberId, role) {
    const response = await api.put(
      API_ENDPOINTS.PROJECTS.MEMBER_ROLE(projectId, memberId),
      { role }
    );
    return response.data;
  }

  async removeMember(projectId, memberId) {
    const response = await api.delete(
      API_ENDPOINTS.PROJECTS.MEMBER_BY_ID(projectId, memberId)
    );
    return response.data;
  }
}

export default new ProjectService();
