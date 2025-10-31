import { getInitials } from '../../utils/helpers';

const MemberAvatar = ({ user, size = 'md' }) => {
  const sizeClasses = {
    sm: 'w-6 h-6 text-xs',
    md: 'w-8 h-8 text-sm',
    lg: 'w-10 h-10 text-base',
  };

  if (!user) {
    return (
      <div className={`${sizeClasses[size]} rounded-full bg-gray-300 flex items-center justify-center text-gray-600 font-semibold`}>
        ?
      </div>
    );
  }

  return (
    <div
      className={`${sizeClasses[size]} rounded-full bg-primary-600 flex items-center justify-center text-white font-semibold`}
      title={`${user.firstName || ''} ${user.lastName || ''} (${user.email || ''})`}
    >
      {getInitials(user.firstName, user.lastName)}
    </div>
  );
};

export default MemberAvatar;
