import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import PropTypes from 'prop-types';
import KanbanCard from './KanbanCard';

const KanbanColumn = ({ status, title, tasks, color }) => {
  const { setNodeRef, isOver } = useDroppable({
    id: status,
  });

  return (
    <div className="flex flex-col min-h-0 flex-1">
      <div className={`rounded-t-lg px-4 py-3 ${color}`}>
        <div className="flex items-center justify-between">
          <h3 className="font-semibold text-white">{title}</h3>
          <span className="bg-white bg-opacity-30 text-white text-sm px-2 py-1 rounded">
            {tasks.length}
          </span>
        </div>
      </div>
      <div
        ref={setNodeRef}
        className={`flex-1 p-4 space-y-3 bg-gray-50 rounded-b-lg min-h-[400px] max-h-[calc(100vh-300px)] overflow-y-auto transition-colors ${
          isOver ? 'bg-blue-50' : ''
        }`}
      >
        <SortableContext
          items={tasks.map((task) => task.id)}
          strategy={verticalListSortingStrategy}
        >
          {tasks.map((task) => (
            <KanbanCard key={task.id} task={task} />
          ))}
        </SortableContext>
        {tasks.length === 0 && (
          <div className="text-center text-gray-400 py-8">
            No tasks
          </div>
        )}
      </div>
    </div>
  );
};

KanbanColumn.propTypes = {
  status: PropTypes.string.isRequired,
  title: PropTypes.string.isRequired,
  tasks: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
    })
  ).isRequired,
  color: PropTypes.string.isRequired,
};

export default KanbanColumn;
