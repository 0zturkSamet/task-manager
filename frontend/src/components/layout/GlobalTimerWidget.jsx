import { Clock, Square } from 'lucide-react';
import { useTimeTracker } from '../../hooks/useTimeTracker';
import Timer from '../common/Timer';
import Button from '../common/Button';
import { useState } from 'react';

/**
 * Global timer widget that displays in the navbar
 * Shows the currently running timer and allows stopping it
 */
const GlobalTimerWidget = () => {
  const { activeTimer, stopTimer, loading } = useTimeTracker();
  const [stopping, setStopping] = useState(false);

  const handleStopTimer = async () => {
    try {
      setStopping(true);
      await stopTimer();
    } catch (error) {
      console.error('Failed to stop timer:', error);
    } finally {
      setStopping(false);
    }
  };

  // Don't render anything if there's no active timer
  if (!activeTimer || loading) {
    return null;
  }

  return (
    <div className="flex items-center gap-2 px-3 py-2 bg-blue-50 border border-blue-200 rounded-lg">
      {/* Timer icon with pulse animation */}
      <div className="relative">
        <Clock className="w-5 h-5 text-blue-600" />
        <span className="absolute top-0 right-0 block h-2 w-2 rounded-full bg-red-500 animate-pulse"></span>
      </div>

      {/* Timer info */}
      <div className="flex flex-col">
        {activeTimer.taskTitle ? (
          <div className="flex flex-col">
            <span className="text-xs text-gray-600 truncate max-w-[150px]" title={activeTimer.taskTitle}>
              {activeTimer.taskTitle}
            </span>
            <Timer
              startTime={activeTimer.startTime}
              className="text-sm font-semibold text-blue-600"
            />
          </div>
        ) : (
          <div className="flex flex-col">
            <span className="text-xs text-gray-600">No task</span>
            <Timer
              startTime={activeTimer.startTime}
              className="text-sm font-semibold text-blue-600"
            />
          </div>
        )}
      </div>

      {/* Stop button */}
      <button
        onClick={handleStopTimer}
        disabled={stopping}
        className="ml-2 p-1.5 text-gray-600 hover:text-red-600 hover:bg-red-50 rounded transition-colors disabled:opacity-50"
        title="Stop timer"
      >
        <Square className="w-4 h-4" fill="currentColor" />
      </button>
    </div>
  );
};

export default GlobalTimerWidget;
