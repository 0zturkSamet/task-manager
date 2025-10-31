import PropTypes from 'prop-types';

const TaskStatistics = ({ statistics }) => {
  if (!statistics) {
    return null;
  }

  const statusData = [
    { label: 'To Do', count: statistics.todoCount, color: 'bg-gray-500' },
    { label: 'In Progress', count: statistics.inProgressCount, color: 'bg-blue-500' },
    { label: 'In Review', count: statistics.inReviewCount, color: 'bg-yellow-500' },
    { label: 'Done', count: statistics.doneCount, color: 'bg-green-500' },
  ];

  const priorityData = [
    { label: 'Low', count: statistics.lowPriorityCount, color: 'bg-gray-400' },
    { label: 'Medium', count: statistics.mediumPriorityCount, color: 'bg-blue-400' },
    { label: 'High', count: statistics.highPriorityCount, color: 'bg-orange-400' },
    { label: 'Urgent', count: statistics.urgentPriorityCount, color: 'bg-red-500' },
  ];

  const renderBar = (data) => {
    const total = data.reduce((sum, item) => sum + item.count, 0);
    if (total === 0) return <div className="text-gray-500 text-sm">No tasks yet</div>;

    return (
      <div className="space-y-2">
        {data.map((item) => {
          const percentage = (item.count / total) * 100;
          return (
            <div key={item.label}>
              <div className="flex justify-between text-sm mb-1">
                <span className="text-gray-700">{item.label}</span>
                <span className="text-gray-600">{item.count}</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div
                  className={`${item.color} h-2 rounded-full transition-all`}
                  style={{ width: `${percentage}%` }}
                ></div>
              </div>
            </div>
          );
        })}
      </div>
    );
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
      <div className="card">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Tasks by Status</h3>
        {renderBar(statusData)}
      </div>
      <div className="card">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Tasks by Priority</h3>
        {renderBar(priorityData)}
      </div>
    </div>
  );
};

TaskStatistics.propTypes = {
  statistics: PropTypes.shape({
    todoCount: PropTypes.number,
    inProgressCount: PropTypes.number,
    inReviewCount: PropTypes.number,
    doneCount: PropTypes.number,
    lowPriorityCount: PropTypes.number,
    mediumPriorityCount: PropTypes.number,
    highPriorityCount: PropTypes.number,
    urgentPriorityCount: PropTypes.number,
  }),
};

export default TaskStatistics;
