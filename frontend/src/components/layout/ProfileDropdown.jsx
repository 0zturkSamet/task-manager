import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { User, LogOut, ChevronDown } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { getInitials } from '../../utils/helpers';
import { getAvatarUrl } from '../../constants/avatars';

const ProfileDropdown = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  const handleLogout = () => {
    logout();
  };

  const handleSettings = () => {
    setIsOpen(false);
    navigate('/settings');
  };

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-3 hover:bg-secondary-100 rounded-lg p-2 smooth-transition"
      >
        <div className="text-right hidden sm:block">
          <p className="text-sm font-medium text-secondary-900">
            {user?.firstName} {user?.lastName}
          </p>
          <p className="text-xs text-secondary-600">{user?.email}</p>
        </div>

        {user?.profileImage ? (
          <img
            src={getAvatarUrl(user.profileImage)}
            alt="Profile"
            className="w-10 h-10 rounded-full object-cover shadow-sm ring-2 ring-primary-100"
          />
        ) : (
          <div className="w-10 h-10 rounded-full bg-primary-600 flex items-center justify-center text-white font-semibold shadow-sm">
            {getInitials(user?.firstName, user?.lastName)}
          </div>
        )}

        <ChevronDown
          className={`w-4 h-4 text-secondary-500 smooth-transition ${
            isOpen ? 'rotate-180' : ''
          }`}
        />
      </button>

      {isOpen && (
        <div className="absolute right-0 mt-2 w-64 bg-white rounded-xl shadow-lg border border-secondary-200 py-2 z-50">
          <div className="px-4 py-3 border-b border-secondary-100">
            <p className="text-sm font-medium text-secondary-900">
              {user?.firstName} {user?.lastName}
            </p>
            <p className="text-xs text-secondary-600 mt-1">{user?.email}</p>
          </div>

          <div className="py-1.5">
            <button
              onClick={handleSettings}
              className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-secondary-700 hover:bg-primary-50 hover:text-primary-700 smooth-transition"
            >
              <User className="w-4 h-4" />
              Profile Settings
            </button>

            <button
              onClick={handleLogout}
              className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-error hover:bg-error-light/10 smooth-transition"
            >
              <LogOut className="w-4 h-4" />
              Sign Out
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProfileDropdown;
