import { useState, useCallback } from 'react';
import projectMemberService from '../services/projectMemberService';

export const useProjectMembers = (projectId) => {
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchMembers = useCallback(async () => {
    if (!projectId) return;

    try {
      setLoading(true);
      setError(null);
      const data = await projectMemberService.getProjectMembers(projectId);
      setMembers(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch project members');
      console.error('Error fetching project members:', err);
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  const addMember = async (memberData) => {
    try {
      const newMember = await projectMemberService.addMember(projectId, memberData);
      setMembers([...members, newMember]);
      return newMember;
    } catch (err) {
      throw err;
    }
  };

  const updateMemberRole = async (memberId, role) => {
    try {
      const updatedMember = await projectMemberService.updateMemberRole(projectId, memberId, role);
      setMembers(members.map((m) => (m.userId === memberId ? updatedMember : m)));
      return updatedMember;
    } catch (err) {
      throw err;
    }
  };

  const removeMember = async (memberId) => {
    try {
      await projectMemberService.removeMember(projectId, memberId);
      setMembers(members.filter((m) => m.userId !== memberId));
    } catch (err) {
      throw err;
    }
  };

  return {
    members,
    loading,
    error,
    fetchMembers,
    addMember,
    updateMemberRole,
    removeMember,
  };
};
