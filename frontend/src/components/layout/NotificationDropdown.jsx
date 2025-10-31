import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Check, CheckCheck } from 'lucide-react';
import notificationService from '../../services/notificationService';

const NotificationDropdown = ({ isOpen, onClose, onNotificationCountChange }) => {
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);

  // Fetch notifications when dropdown opens
  useEffect(() => {
    if (isOpen) {
      fetchNotifications();
    }
  }, [isOpen]);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        onClose();
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen, onClose]);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const data = await notificationService.getNotifications();
      setNotifications(data);
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleNotificationClick = async (notification) => {
    try {
      // Mark as read if not already read
      if (!notification.isRead) {
        await notificationService.markAsRead(notification.id);
        // Update local state
        setNotifications(prev =>
          prev.map(n => n.id === notification.id ? { ...n, isRead: true } : n)
        );
        // Update parent count
        if (onNotificationCountChange) {
          onNotificationCountChange();
        }
      }

      // Navigate to task detail if taskId exists
      if (notification.taskId) {
        navigate(`/app/tasks/${notification.taskId}`);
        onClose();
      }
    } catch (error) {
      console.error('Failed to mark notification as read:', error);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await notificationService.markAllAsRead();
      // Update local state
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      // Update parent count
      if (onNotificationCountChange) {
        onNotificationCountChange();
      }
    } catch (error) {
      console.error('Failed to mark all as read:', error);
    }
  };

  const getNotificationIcon = (type) => {
    return <Bell className="w-4 h-4" />;
  };

  const formatTime = (timestamp) => {
    try {
      const now = new Date();
      const notificationTime = new Date(timestamp);
      const diffMs = now - notificationTime;
      const diffSeconds = Math.floor(diffMs / 1000);
      const diffMinutes = Math.floor(diffSeconds / 60);
      const diffHours = Math.floor(diffMinutes / 60);
      const diffDays = Math.floor(diffHours / 24);

      if (diffSeconds < 60) return 'Just now';
      if (diffMinutes < 60) return `${diffMinutes}m ago`;
      if (diffHours < 24) return `${diffHours}h ago`;
      if (diffDays < 7) return `${diffDays}d ago`;
      return notificationTime.toLocaleDateString();
    } catch (error) {
      return 'Recently';
    }
  };

  if (!isOpen) return null;

  return (
    <div
      ref={dropdownRef}
      className="absolute right-0 mt-2 w-96 bg-white rounded-xl shadow-lg border border-secondary-200 z-50"
    >
      {/* Header */}
      <div className="flex items-center justify-between px-4 py-3 border-b border-secondary-100">
        <h3 className="text-sm font-semibold text-secondary-900">Notifications</h3>
        {notifications.some(n => !n.isRead) && (
          <button
            onClick={handleMarkAllAsRead}
            className="flex items-center gap-1 text-xs text-primary-600 hover:text-primary-700 smooth-transition"
          >
            <CheckCheck className="w-4 h-4" />
            Mark all read
          </button>
        )}
      </div>

      {/* Notification List */}
      <div className="max-h-96 overflow-y-auto">
        {loading ? (
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
          </div>
        ) : notifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-8 px-4 text-center">
            <Bell className="w-12 h-12 text-secondary-300 mb-3" />
            <p className="text-sm text-secondary-600">No notifications yet</p>
            <p className="text-xs text-secondary-500 mt-1">
              You'll be notified when tasks are assigned to you
            </p>
          </div>
        ) : (
          <div className="divide-y divide-secondary-100">
            {notifications.map((notification) => (
              <button
                key={notification.id}
                onClick={() => handleNotificationClick(notification)}
                className={`w-full text-left px-4 py-3 hover:bg-secondary-50 smooth-transition ${
                  !notification.isRead ? 'bg-primary-50/30' : ''
                }`}
              >
                <div className="flex gap-3">
                  <div className={`flex-shrink-0 mt-1 ${!notification.isRead ? 'text-primary-600' : 'text-secondary-400'}`}>
                    {getNotificationIcon(notification.type)}
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-2">
                      <p className={`text-sm ${!notification.isRead ? 'font-semibold text-secondary-900' : 'font-medium text-secondary-700'}`}>
                        {notification.title}
                      </p>
                      {!notification.isRead && (
                        <div className="w-2 h-2 bg-primary-600 rounded-full flex-shrink-0 mt-1.5"></div>
                      )}
                    </div>
                    <p className="text-xs text-secondary-600 mt-1 line-clamp-2">
                      {notification.message}
                    </p>
                    <p className="text-xs text-secondary-500 mt-1">
                      {formatTime(notification.createdAt)}
                    </p>
                  </div>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Footer - Show View All if there are notifications */}
      {notifications.length > 5 && (
        <div className="border-t border-secondary-100 px-4 py-2">
          <button
            onClick={() => {
              navigate('/app/notifications');
              onClose();
            }}
            className="text-xs text-primary-600 hover:text-primary-700 font-medium w-full text-center"
          >
            View all notifications
          </button>
        </div>
      )}
    </div>
  );
};

export default NotificationDropdown;
