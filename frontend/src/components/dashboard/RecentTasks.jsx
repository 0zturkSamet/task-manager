import { Link } from 'react-router-dom';
import { CheckSquare } from 'lucide-react';
import PropTypes from 'prop-types';
import Button from '../common/Button';

const RecentTasks = ({ tasks, onCreateTask }) => {
  return (
    <div className="card">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold text-gray-900">Recent Tasks</h2>
        <Link to="/app/tasks" className="text-primary-600 hover:text-primary-700 text-sm font-medium">
          View All
        </Link>
      </div>
      {tasks.length === 0 ? (
        <div className="text-center py-8">
          <CheckSquare className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-gray-500">No tasks yet</p>
          <Button
            variant="primary"
            onClick={onCreateTask}
            className="mt-4"
          >
            Create Your First Task
          </Button>
        </div>
      ) : (
        <div className="space-y-3">
          {tasks.slice(0, 5).map((task) => (
            <div
              key={task.id}
              className="flex items-center justify-between p-3 hover:bg-gray-50 rounded-lg transition-colors"
            >
              <div className="flex-1">
                <p className="font-medium text-gray-900">{task.title}</p>
                <div className="flex items-center gap-2 mt-1">
                  <span
                    className={`text-xs px-2 py-1 rounded ${
                      task.status === 'DONE'
                        ? 'bg-green-100 text-green-800'
                        : task.status === 'IN_PROGRESS'
                        ? 'bg-blue-100 text-blue-800'
                        : 'bg-gray-100 text-gray-800'
                    }`}
                  >
                    {task.status?.replace('_', ' ')}
                  </span>
                  <span
                    className={`text-xs px-2 py-1 rounded ${
                      task.priority === 'URGENT'
                        ? 'bg-red-100 text-red-800'
                        : task.priority === 'HIGH'
                        ? 'bg-orange-100 text-orange-800'
                        : 'bg-gray-100 text-gray-800'
                    }`}
                  >
                    {task.priority}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

RecentTasks.propTypes = {
  tasks: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      title: PropTypes.string.isRequired,
      status: PropTypes.string.isRequired,
      priority: PropTypes.string.isRequired,
    })
  ).isRequired,
  onCreateTask: PropTypes.func.isRequired,
};

export default RecentTasks;
