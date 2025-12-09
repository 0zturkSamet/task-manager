import { useState, useEffect, useCallback } from 'react';
import timeTrackerService from '../services/timeTrackerService';

export const useTimeTracker = () => {
  const [activeTimer, setActiveTimer] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Load active timer on mount
  const loadActiveTimer = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const timer = await timeTrackerService.getActiveTimer();
      setActiveTimer(timer);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load active timer');
      console.error('Error loading active timer:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadActiveTimer();
  }, [loadActiveTimer]);

  /**
   * Start a new timer
   * @param {string} taskId - Optional task ID to link timer to
   * @param {string} description - Optional description
   */
  const startTimer = async (taskId = null, description = '') => {
    try {
      setError(null);
      const timer = await timeTrackerService.startTimer({ taskId, description });
      setActiveTimer(timer);
      return timer;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Failed to start timer';
      setError(errorMsg);
      console.error('Error starting timer:', err);
      throw new Error(errorMsg);
    }
  };

  /**
   * Stop the currently running timer
   * @param {string} description - Optional description to update
   */
  const stopTimer = async (description = '') => {
    try {
      setError(null);
      const stoppedTimer = await timeTrackerService.stopTimer({ description });
      setActiveTimer(null);
      return stoppedTimer;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Failed to stop timer';
      setError(errorMsg);
      console.error('Error stopping timer:', err);
      throw new Error(errorMsg);
    }
  };

  /**
   * Check if a specific task has an active timer
   * @param {string} taskId - Task ID to check
   * @returns {boolean}
   */
  const isTimerActive = (taskId) => {
    return activeTimer?.taskId === taskId;
  };

  /**
   * Get elapsed time in seconds for the active timer
   * @returns {number} Elapsed seconds or 0 if no timer
   */
  const getElapsedSeconds = () => {
    if (!activeTimer?.startTime) return 0;
    return timeTrackerService.calculateElapsedSeconds(activeTimer.startTime);
  };

  return {
    activeTimer,
    loading,
    error,
    startTimer,
    stopTimer,
    isTimerActive,
    getElapsedSeconds,
    refreshActiveTimer: loadActiveTimer,
  };
};

export default useTimeTracker;
