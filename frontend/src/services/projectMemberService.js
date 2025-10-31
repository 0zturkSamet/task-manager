import api from './api';

const projectMemberService = {
  // Get all members of a project
  getProjectMembers: async (projectId) => {
    const response = await api.get(`/projects/${projectId}/members`);
    return response.data;
  },

  // Add a member to a project
  addMember: async (projectId, memberData) => {
    const response = await api.post(`/projects/${projectId}/members`, memberData);
    return response.data;
  },

  // Update member role
  updateMemberRole: async (projectId, memberId, role) => {
    const response = await api.put(`/projects/${projectId}/members/${memberId}/role`, { role });
    return response.data;
  },

  // Remove member from project
  removeMember: async (projectId, memberId) => {
    const response = await api.delete(`/projects/${projectId}/members/${memberId}`);
    return response.data;
  },
};

export default projectMemberService;
