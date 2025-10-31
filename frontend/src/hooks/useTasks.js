import { useState, useEffect } from 'react';
import taskService from '../services/taskService';

export const useTasks = (projectId = null) => {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchTasks = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = projectId
        ? await taskService.getProjectTasks(projectId)
        : await taskService.getAllTasks();
      setTasks(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch tasks');
      console.error('Error fetching tasks:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTasks();
  }, [projectId]);

  const createTask = async (taskData) => {
    try {
      const newTask = await taskService.createTask(taskData);
      setTasks([...tasks, newTask]);
      return newTask;
    } catch (err) {
      throw err;
    }
  };

  const updateTask = async (id, taskData) => {
    try {
      const updatedTask = await taskService.updateTask(id, taskData);
      setTasks(tasks.map((t) => (t.id === id ? updatedTask : t)));
      return updatedTask;
    } catch (err) {
      throw err;
    }
  };

  const deleteTask = async (id) => {
    try {
      await taskService.deleteTask(id);
      setTasks(tasks.filter((t) => t.id !== id));
    } catch (err) {
      throw err;
    }
  };

  const filterTasks = async (filters) => {
    try {
      setLoading(true);
      setError(null);
      const data = await taskService.filterTasks(filters);
      setTasks(data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to filter tasks');
      console.error('Error filtering tasks:', err);
    } finally {
      setLoading(false);
    }
  };

  return {
    tasks,
    loading,
    error,
    fetchTasks,
    createTask,
    updateTask,
    deleteTask,
    filterTasks,
  };
};
