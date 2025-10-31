import { useState, useEffect } from 'react';
import { User } from 'lucide-react';
import MemberAvatar from './MemberAvatar';

const AssigneeSelect = ({
  projectId,
  value,
  onChange,
  members = [],
  allUsers = [],
  isAdmin = false,
  disabled = false
}) => {
  const [selectedUser, setSelectedUser] = useState(null);

  // Use all users if admin, otherwise use project members
  const availableUsers = isAdmin ? allUsers : members;

  useEffect(() => {
    if (value && availableUsers.length > 0) {
      const user = availableUsers.find(m => m.userId === value || m.id === value);
      setSelectedUser(user);
    } else {
      setSelectedUser(null);
    }
  }, [value, availableUsers]);

  const handleChange = (e) => {
    const memberId = e.target.value;
    onChange(memberId === '' ? null : memberId);
  };

  // Helper function to get user name
  const getUserName = (user) => {
    if (user.userName) return user.userName;
    if (user.firstName && user.lastName) return `${user.firstName} ${user.lastName}`;
    return user.email;
  };

  // Helper function to get user email
  const getUserEmail = (user) => {
    return user.userEmail || user.email;
  };

  // Helper function to get user ID
  const getUserId = (user) => {
    return user.userId || user.id;
  };

  return (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-2">
        <User className="w-4 h-4 inline mr-1" />
        Assignee {isAdmin && <span className="text-xs text-gray-500">(All Users)</span>}
        {!isAdmin && <span className="text-xs text-gray-500">(Project Members)</span>}
      </label>
      <div className="flex items-center gap-2">
        {selectedUser && (
          <MemberAvatar
            user={{
              firstName: getUserName(selectedUser).split(' ')[0] || '',
              lastName: getUserName(selectedUser).split(' ')[1] || '',
              email: getUserEmail(selectedUser)
            }}
            size="sm"
          />
        )}
        <select
          value={value || ''}
          onChange={handleChange}
          disabled={disabled}
          className="flex-1 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent disabled:bg-gray-100"
        >
          <option value="">Unassigned</option>
          {availableUsers.map((user) => (
            <option key={getUserId(user)} value={getUserId(user)}>
              {getUserName(user)} {user.role && !isAdmin && `(${user.role})`}
            </option>
          ))}
        </select>
      </div>
      {selectedUser && (
        <p className="text-xs text-gray-500 mt-1">
          {getUserEmail(selectedUser)}
        </p>
      )}
    </div>
  );
};

export default AssigneeSelect;
