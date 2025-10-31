import { STATUS_COLORS, PRIORITY_COLORS, ROLE_COLORS } from '../../constants/api';
import { getStatusLabel, getPriorityLabel, getRoleLabel } from '../../utils/helpers';

const Badge = ({ type, value, className = '' }) => {
  let colorClass = '';
  let label = value;

  if (type === 'status') {
    colorClass = STATUS_COLORS[value] || 'bg-gray-100 text-gray-800';
    label = getStatusLabel(value);
  } else if (type === 'priority') {
    colorClass = PRIORITY_COLORS[value] || 'bg-gray-100 text-gray-800';
    label = getPriorityLabel(value);
  } else if (type === 'role') {
    colorClass = ROLE_COLORS[value] || 'bg-gray-100 text-gray-800';
    label = getRoleLabel(value);
  }

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${colorClass} ${className}`}
    >
      {label}
    </span>
  );
};

export default Badge;
