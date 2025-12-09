import { useState, useEffect } from 'react';
import { Clock, Calendar, Play, Square, Edit2, Trash2, TrendingUp, AlertCircle } from 'lucide-react';
import { useTimeTracker } from '../hooks/useTimeTracker';
import { useToast } from '../context/ToastContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Timer from '../components/common/Timer';
import timeTrackerService from '../services/timeTrackerService';
import EditTimeEntryModal from '../components/modals/EditTimeEntryModal';
import DeleteTimeEntryModal from '../components/modals/DeleteTimeEntryModal';

const TimeTracking = () => {
  const { activeTimer, stopTimer } = useTimeTracker();
  const toast = useToast();
  const [timeEntries, setTimeEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [todayStats, setTodayStats] = useState(null);
  const [weekStats, setWeekStats] = useState(null);
  const [overtimeStats, setOvertimeStats] = useState(null);
  const [missedHoursStats, setMissedHoursStats] = useState(null);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [deleteModalOpen, setDeleteModalOpen] = useState(false);
  const [selectedEntry, setSelectedEntry] = useState(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);

      // Calculate date ranges
      const now = new Date();
      const startOfWeek = new Date(now);
      startOfWeek.setDate(now.getDate() - 7);

      const [entries, today, week, overtime, missedHours] = await Promise.all([
        timeTrackerService.getTimeEntries(),
        timeTrackerService.getTodayTime(),
        timeTrackerService.getWeekTime(),
        timeTrackerService.getOvertimeReport(
          startOfWeek.toISOString(),
          now.toISOString()
        ),
        timeTrackerService.getMissedHoursReport(
          startOfWeek.toISOString(),
          now.toISOString()
        )
      ]);

      setTimeEntries(entries);
      setTodayStats(today);
      setWeekStats(week);
      setOvertimeStats(overtime);
      setMissedHoursStats(missedHours);
    } catch (error) {
      console.error('Failed to load time tracking data:', error);
      toast.error('Failed to load time tracking data');
    } finally {
      setLoading(false);
    }
  };

  const handleStopTimer = async () => {
    try {
      await stopTimer();
      toast.success('Timer stopped successfully!');
      loadData(); // Reload data
    } catch (error) {
      toast.error('Failed to stop timer');
    }
  };

  const handleEditEntry = (entry) => {
    setSelectedEntry(entry);
    setEditModalOpen(true);
  };

  const handleDeleteEntry = (entry) => {
    setSelectedEntry(entry);
    setDeleteModalOpen(true);
  };

  const handleModalSuccess = () => {
    loadData(); // Reload data after edit/delete
  };

  const formatDuration = (minutes) => {
    if (!minutes) return '0m';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours === 0) return `${mins}m`;
    if (mins === 0) return `${hours}h`;
    return `${hours}h ${mins}m`;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-6 max-w-7xl">
      {/* Page Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Time Tracking</h1>
          <p className="text-gray-600 mt-1">Track your time and view your time entries</p>
        </div>
      </div>

      {/* Active Timer Banner */}
      {activeTimer && (
        <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="relative">
                <Clock className="w-6 h-6 text-blue-600" />
                <span className="absolute top-0 right-0 block h-2 w-2 rounded-full bg-red-500 animate-pulse"></span>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Timer Running</h3>
                {activeTimer.taskTitle ? (
                  <p className="text-sm text-gray-600">Working on: {activeTimer.taskTitle}</p>
                ) : (
                  <p className="text-sm text-gray-600">No task assigned</p>
                )}
              </div>
            </div>
            <div className="flex items-center gap-4">
              <Timer
                startTime={activeTimer.startTime}
                className="text-2xl font-bold text-blue-600"
              />
              <button
                onClick={handleStopTimer}
                className="flex items-center gap-2 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
              >
                <Square className="w-4 h-4" fill="currentColor" />
                <span>Stop Timer</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4 mb-8">
        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-blue-100 rounded-lg">
              <Calendar className="w-5 h-5 text-blue-600" />
            </div>
            <h3 className="font-semibold text-gray-700">Today</h3>
          </div>
          <p className="text-3xl font-bold text-gray-900">
            {todayStats?.totalHours?.toFixed(2) || 0}h
          </p>
          <p className="text-sm text-gray-500 mt-1">
            {todayStats?.entryCount || 0} entries
          </p>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-green-100 rounded-lg">
              <Calendar className="w-5 h-5 text-green-600" />
            </div>
            <h3 className="font-semibold text-gray-700">This Week</h3>
          </div>
          <p className="text-3xl font-bold text-gray-900">
            {weekStats?.totalHours?.toFixed(2) || 0}h
          </p>
          <p className="text-sm text-gray-500 mt-1">
            {weekStats?.entryCount || 0} entries
          </p>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-purple-100 rounded-lg">
              <Clock className="w-5 h-5 text-purple-600" />
            </div>
            <h3 className="font-semibold text-gray-700">All Time</h3>
          </div>
          <p className="text-3xl font-bold text-gray-900">
            {timeEntries
              .filter(e => e.durationMinutes)
              .reduce((acc, e) => acc + (e.durationMinutes / 60), 0)
              .toFixed(2)}h
          </p>
          <p className="text-sm text-gray-500 mt-1">
            {timeEntries.filter(e => e.durationMinutes).length} entries
          </p>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-green-100 rounded-lg">
              <TrendingUp className="w-5 h-5 text-green-600" />
            </div>
            <h3 className="font-semibold text-gray-700">Overtime</h3>
          </div>
          <p className="text-3xl font-bold text-green-600">
            +{overtimeStats?.totalOvertimeHours?.toFixed(2) || 0}h
          </p>
          <p className="text-sm text-gray-500 mt-1">
            Last 7 days
          </p>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
          <div className="flex items-center gap-3 mb-2">
            <div className="p-2 bg-red-100 rounded-lg">
              <AlertCircle className="w-5 h-5 text-red-600" />
            </div>
            <h3 className="font-semibold text-gray-700">Missed Hours</h3>
          </div>
          <p className="text-3xl font-bold text-red-600">
            -{missedHoursStats?.totalMissedHours?.toFixed(2) || 0}h
          </p>
          <p className="text-sm text-gray-500 mt-1">
            {missedHoursStats?.daysWithMissedHours || 0} days
          </p>
        </div>
      </div>

      {/* Time Entries List */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        <div className="p-6 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Recent Time Entries</h2>
        </div>

        {timeEntries.length === 0 ? (
          <div className="p-12 text-center">
            <Clock className="w-12 h-12 text-gray-400 mx-auto mb-3" />
            <h3 className="text-lg font-medium text-gray-900 mb-1">No time entries yet</h3>
            <p className="text-gray-600">
              Start a timer on a task to begin tracking your time
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Date
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Task
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Start Time
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    End Time
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Duration
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {timeEntries.slice(0, 20).map((entry) => (
                  <tr key={entry.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {new Date(entry.startTime).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      {entry.taskTitle || (
                        <span className="text-gray-400 italic">No task</span>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      {new Date(entry.startTime).toLocaleTimeString([], {
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                      {entry.endTime ? (
                        new Date(entry.endTime).toLocaleTimeString([], {
                          hour: '2-digit',
                          minute: '2-digit'
                        })
                      ) : (
                        <span className="flex items-center gap-1 text-blue-600">
                          <Play className="w-3 h-3" />
                          Running
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {entry.durationMinutes ? formatDuration(entry.durationMinutes) : '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => handleEditEntry(entry)}
                          className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="Edit entry"
                        >
                          <Edit2 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDeleteEntry(entry)}
                          className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                          title="Delete entry"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Modals */}
      <EditTimeEntryModal
        entry={selectedEntry}
        isOpen={editModalOpen}
        onClose={() => setEditModalOpen(false)}
        onSuccess={handleModalSuccess}
      />
      <DeleteTimeEntryModal
        entry={selectedEntry}
        isOpen={deleteModalOpen}
        onClose={() => setDeleteModalOpen(false)}
        onSuccess={handleModalSuccess}
      />
    </div>
  );
};

export default TimeTracking;
