import { Calendar, Clock, MoreVertical, User } from 'lucide-react';
import { useState } from 'react';
import Badge from '../common/Badge';
import MemberAvatar from '../common/MemberAvatar';
import { formatDate, isOverdue } from '../../utils/helpers';

const TaskCard = ({ task, onEdit, onDelete, onView }) => {
  const [showMenu, setShowMenu] = useState(false);

  return (
    <div className="card hover:shadow-md transition-shadow cursor-pointer" onClick={onView}>
      <div className="flex items-start justify-between mb-3">
        <h3 className="text-base font-semibold text-gray-900 flex-1 pr-2">
          {task.title}
        </h3>
        <div className="relative" onClick={(e) => e.stopPropagation()}>
          <button
            onClick={() => setShowMenu(!showMenu)}
            className="p-1 hover:bg-gray-100 rounded transition-colors"
          >
            <MoreVertical className="w-4 h-4 text-gray-400" />
          </button>

          {showMenu && (
            <>
              <div
                className="fixed inset-0 z-10"
                onClick={() => setShowMenu(false)}
              ></div>
              <div className="absolute right-0 mt-1 w-40 bg-white rounded-lg shadow-lg border border-gray-200 py-1 z-20">
                <button
                  onClick={() => {
                    onEdit(task);
                    setShowMenu(false);
                  }}
                  className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700 text-sm"
                >
                  Edit Task
                </button>
                <button
                  onClick={() => {
                    onDelete(task);
                    setShowMenu(false);
                  }}
                  className="w-full text-left px-4 py-2 hover:bg-gray-100 text-red-600 text-sm"
                >
                  Delete Task
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      {task.description && (
        <p className="text-sm text-gray-600 mb-3 line-clamp-2">{task.description}</p>
      )}

      <div className="flex items-center gap-2 mb-3">
        <Badge type="status" value={task.status} />
        <Badge type="priority" value={task.priority} />
      </div>

      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4 text-xs text-gray-500">
          {task.dueDate && (
            <div className={`flex items-center gap-1 ${isOverdue(task.dueDate, task.status) ? 'text-red-600 font-medium' : ''}`}>
              <Calendar className="w-3 h-3" />
              <span>{formatDate(task.dueDate)}</span>
            </div>
          )}
          {task.estimatedHours && (
            <div className="flex items-center gap-1">
              <Clock className="w-3 h-3" />
              <span>{task.estimatedHours}h</span>
            </div>
          )}
        </div>

        {task.assignedToId ? (
          <div className="flex items-center gap-1" title={task.assignedToName}>
            <MemberAvatar
              user={{
                firstName: task.assignedToName?.split(' ')[0] || '',
                lastName: task.assignedToName?.split(' ')[1] || '',
                email: task.assignedToEmail || ''
              }}
              size="sm"
            />
          </div>
        ) : (
          <div className="flex items-center gap-1 text-gray-400" title="Unassigned">
            <User className="w-4 h-4" />
          </div>
        )}
      </div>
    </div>
  );
};

export default TaskCard;
