import { useState, useEffect } from 'react';
import timeTrackerService from '../../services/timeTrackerService';

/**
 * Timer component that displays elapsed time from a start time
 * Updates every second
 */
const Timer = ({ startTime, className = '' }) => {
  const [elapsed, setElapsed] = useState(0);

  useEffect(() => {
    if (!startTime) return;

    // Calculate initial elapsed time
    const calculateElapsed = () => {
      return timeTrackerService.calculateElapsedSeconds(startTime);
    };

    // Set initial value
    setElapsed(calculateElapsed());

    // Update every second
    const interval = setInterval(() => {
      setElapsed(calculateElapsed());
    }, 1000);

    return () => clearInterval(interval);
  }, [startTime]);

  const formatTime = (seconds) => {
    return timeTrackerService.formatElapsedTime(seconds);
  };

  if (!startTime) {
    return <span className={className}>00:00:00</span>;
  }

  return (
    <span className={`font-mono font-semibold ${className}`}>
      {formatTime(elapsed)}
    </span>
  );
};

export default Timer;
