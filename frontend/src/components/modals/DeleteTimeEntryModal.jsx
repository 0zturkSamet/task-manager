import { useState } from 'react';
import { X, AlertTriangle, Trash2 } from 'lucide-react';
import { useToast } from '../../context/ToastContext';
import timeTrackerService from '../../services/timeTrackerService';

const DeleteTimeEntryModal = ({ entry, isOpen, onClose, onSuccess }) => {
  const toast = useToast();
  const [loading, setLoading] = useState(false);

  const handleDelete = async () => {
    setLoading(true);
    try {
      await timeTrackerService.deleteTimeEntry(entry.id);
      toast.success('Time entry deleted successfully!');
      onSuccess();
      onClose();
    } catch (error) {
      console.error('Failed to delete time entry:', error);
      toast.error(error.response?.data?.message || 'Failed to delete time entry');
    } finally {
      setLoading(false);
    }
  };

  const formatDuration = (minutes) => {
    if (!minutes) return '0m';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours === 0) return `${mins}m`;
    if (mins === 0) return `${hours}h`;
    return `${hours}h ${mins}m`;
  };

  if (!isOpen || !entry) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-red-100 rounded-lg">
              <AlertTriangle className="w-5 h-5 text-red-600" />
            </div>
            <h2 className="text-xl font-semibold text-gray-900">Delete Time Entry</h2>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-4">
          <p className="text-gray-600">
            Are you sure you want to delete this time entry? This action cannot be undone.
          </p>

          {/* Entry Details */}
          <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-600">Date:</span>
                <span className="font-medium text-gray-900">
                  {new Date(entry.startTime).toLocaleDateString()}
                </span>
              </div>
              {entry.taskTitle && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Task:</span>
                  <span className="font-medium text-gray-900">{entry.taskTitle}</span>
                </div>
              )}
              <div className="flex justify-between">
                <span className="text-gray-600">Duration:</span>
                <span className="font-medium text-gray-900">
                  {entry.durationMinutes ? formatDuration(entry.durationMinutes) : 'Running'}
                </span>
              </div>
              {entry.description && (
                <div className="pt-2 border-t border-gray-200">
                  <span className="text-gray-600 block mb-1">Description:</span>
                  <span className="text-gray-900">{entry.description}</span>
                </div>
              )}
            </div>
          </div>

          {/* Warning */}
          {entry.taskTitle && (
            <div className="flex items-start gap-2 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
              <AlertTriangle className="w-4 h-4 text-yellow-600 mt-0.5 flex-shrink-0" />
              <p className="text-sm text-yellow-800">
                The task's actual hours will be adjusted after deleting this entry.
              </p>
            </div>
          )}
        </div>

        {/* Actions */}
        <div className="flex gap-3 p-6 border-t border-gray-200">
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleDelete}
            disabled={loading}
            className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <Trash2 className="w-4 h-4" />
            {loading ? 'Deleting...' : 'Delete Entry'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default DeleteTimeEntryModal;
