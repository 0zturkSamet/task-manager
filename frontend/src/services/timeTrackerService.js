import api from './api';
import { API_ENDPOINTS } from '../constants/api';

class TimeTrackerService {
  // ===== Timer Management =====

  /**
   * Start a new timer
   * @param {Object} data - { taskId, description }
   * @returns {Promise} Timer entry data
   */
  async startTimer(data) {
    const response = await api.post(API_ENDPOINTS.TIME_TRACKER.START_TIMER, data);
    return response.data;
  }

  /**
   * Stop the currently running timer
   * @param {Object} data - { description } (optional)
   * @returns {Promise} Stopped timer entry data
   */
  async stopTimer(data = {}) {
    const response = await api.post(API_ENDPOINTS.TIME_TRACKER.STOP_TIMER, data);
    return response.data;
  }

  /**
   * Get the currently active timer
   * @returns {Promise} Active timer data or null
   */
  async getActiveTimer() {
    const response = await api.get(API_ENDPOINTS.TIME_TRACKER.ACTIVE_TIMER);
    return response.data.activeTimer === 'null' ? null : response.data.activeTimer;
  }

  // ===== Time Entry CRUD =====

  /**
   * Create a manual time entry
   * @param {Object} data - { taskId, startTime, endTime, description }
   * @returns {Promise} Created time entry
   */
  async createTimeEntry(data) {
    const response = await api.post(API_ENDPOINTS.TIME_TRACKER.TIME_ENTRIES, data);
    return response.data;
  }

  /**
   * Get time entries with optional filters
   * @param {Object} params - { taskId, startDate, endDate }
   * @returns {Promise} Array of time entries
   */
  async getTimeEntries(params = {}) {
    const response = await api.get(API_ENDPOINTS.TIME_TRACKER.TIME_ENTRIES, { params });
    return response.data;
  }

  /**
   * Get a specific time entry by ID
   * @param {string} id - Time entry ID
   * @returns {Promise} Time entry data
   */
  async getTimeEntryById(id) {
    const response = await api.get(API_ENDPOINTS.TIME_TRACKER.TIME_ENTRY_BY_ID(id));
    return response.data;
  }

  /**
   * Update a time entry
   * @param {string} id - Time entry ID
   * @param {Object} data - Updated fields
   * @returns {Promise} Updated time entry
   */
  async updateTimeEntry(id, data) {
    const response = await api.put(API_ENDPOINTS.TIME_TRACKER.TIME_ENTRY_BY_ID(id), data);
    return response.data;
  }

  /**
   * Delete a time entry
   * @param {string} id - Time entry ID
   * @returns {Promise}
   */
  async deleteTimeEntry(id) {
    const response = await api.delete(API_ENDPOINTS.TIME_TRACKER.TIME_ENTRY_BY_ID(id));
    return response.data;
  }

  // ===== Reports & Analytics =====

  /**
   * Get time summary for a date range
   * @param {string} startDate - ISO date string
   * @param {string} endDate - ISO date string
   * @returns {Promise} Time summary data
   */
  async getTimeSummary(startDate, endDate) {
    const response = await api.get(API_ENDPOINTS.TIME_TRACKER.TIME_SUMMARY, {
      params: { startDate, endDate }
    });
    return response.data;
  }

  /**
   * Get time summary for a specific task
   * @param {string} taskId - Task ID
   * @returns {Promise} Task time summary
   */
  async getTaskTimeSummary(taskId) {
    const response = await api.get(API_ENDPOINTS.TIME_TRACKER.TASK_TIME_SUMMARY(taskId));
    return response.data;
  }

  /**
   * Get today's time tracking summary
   * @returns {Promise} Today's summary
   */
  async getTodayTime() {
    const response = await api.get(API_ENDPOINTS.TIME_TRACKER.TODAY);
    return response.data;
  }

  /**
   * Get this week's time tracking summary
   * @returns {Promise} Week's summary
   */
  async getWeekTime() {
    const response = await api.get(API_ENDPOINTS.TIME_TRACKER.WEEK);
    return response.data;
  }

  /**
   * Get monthly time tracking report
   * @param {string} month - Month in format YYYY-MM (e.g., "2025-01")
   * @returns {Promise} Monthly summary
   */
  async getMonthlyReport(month) {
    const response = await api.get(API_ENDPOINTS.TIME_TRACKER.MONTHLY, {
      params: { month }
    });
    return response.data;
  }

  /**
   * Get yearly time tracking report
   * @param {number} year - Year (e.g., 2025)
   * @returns {Promise} Yearly summary
   */
  async getYearlyReport(year) {
    const response = await api.get(API_ENDPOINTS.TIME_TRACKER.YEARLY, {
      params: { year }
    });
    return response.data;
  }

  /**
   * Get overtime report
   * @param {string} startDate - ISO date string
   * @param {string} endDate - ISO date string
   * @returns {Promise} Overtime report data
   */
  async getOvertimeReport(startDate, endDate) {
    const response = await api.get(API_ENDPOINTS.TIME_TRACKER.OVERTIME, {
      params: { startDate, endDate }
    });
    return response.data;
  }

  /**
   * Get missed hours report
   * @param {string} startDate - ISO date string
   * @param {string} endDate - ISO date string
   * @returns {Promise} Missed hours report data
   */
  async getMissedHoursReport(startDate, endDate) {
    const response = await api.get(API_ENDPOINTS.TIME_TRACKER.MISSED_HOURS, {
      params: { startDate, endDate }
    });
    return response.data;
  }

  /**
   * Get working hours configuration
   * @returns {Promise} Working hours config
   */
  async getWorkingHoursConfig() {
    const response = await api.get(API_ENDPOINTS.WORKING_HOURS.CONFIG);
    return response.data;
  }

  /**
   * Update working hours configuration
   * @param {Object} config - Working hours configuration
   * @returns {Promise} Updated config
   */
  async updateWorkingHoursConfig(config) {
    const response = await api.put(API_ENDPOINTS.WORKING_HOURS.CONFIG, config);
    return response.data;
  }

  // ===== Helper Methods =====

  /**
   * Format duration in minutes to human-readable format
   * @param {number} minutes - Duration in minutes
   * @returns {string} Formatted duration (e.g., "2h 30m")
   */
  formatDuration(minutes) {
    if (!minutes || minutes === 0) return '0m';

    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;

    if (hours === 0) {
      return `${mins}m`;
    } else if (mins === 0) {
      return `${hours}h`;
    } else {
      return `${hours}h ${mins}m`;
    }
  }

  /**
   * Calculate elapsed time from start time to now
   * @param {string} startTime - ISO timestamp
   * @returns {number} Elapsed seconds
   */
  calculateElapsedSeconds(startTime) {
    const start = new Date(startTime).getTime();
    const now = Date.now();
    return Math.floor((now - start) / 1000);
  }

  /**
   * Format elapsed seconds to HH:MM:SS
   * @param {number} seconds - Elapsed seconds
   * @returns {string} Formatted time (e.g., "02:30:45")
   */
  formatElapsedTime(seconds) {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    return [hours, minutes, secs]
      .map(val => String(val).padStart(2, '0'))
      .join(':');
  }
}

export default new TimeTrackerService();
