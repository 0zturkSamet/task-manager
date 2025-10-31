import { useState, useCallback } from 'react';
import {
  DndContext,
  DragOverlay,
  closestCorners,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import { sortableKeyboardCoordinates } from '@dnd-kit/sortable';
import PropTypes from 'prop-types';
import KanbanColumn from './KanbanColumn';
import KanbanCard from './KanbanCard';
import { useToast } from '../../context/ToastContext';

const KanbanBoard = ({ tasks, onTaskUpdate }) => {
  const [activeTask, setActiveTask] = useState(null);
  const toast = useToast();

  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const columns = [
    { id: 'TODO', title: 'To Do', color: 'bg-gray-500' },
    { id: 'IN_PROGRESS', title: 'In Progress', color: 'bg-blue-500' },
    { id: 'IN_REVIEW', title: 'In Review', color: 'bg-yellow-500' },
    { id: 'DONE', title: 'Done', color: 'bg-green-500' },
  ];

  const getTasksByStatus = (status) => {
    return tasks
      .filter((task) => task.status === status)
      .sort((a, b) => (a.position || 0) - (b.position || 0));
  };

  const handleDragStart = (event) => {
    const { active } = event;
    const task = tasks.find((t) => t.id === active.id);
    setActiveTask(task);
  };

  const handleDragEnd = useCallback(
    async (event) => {
      const { active, over } = event;
      setActiveTask(null);

      if (!over) return;

      const taskId = active.id;
      const task = tasks.find((t) => t.id === taskId);

      if (!task) return;

      // Determine the target status
      // Check if we dropped on a column (status) or on another task
      let newStatus = over.id;

      // If over.id is not a valid status, it means we dropped on a task
      // Find which column that task belongs to
      const validStatuses = ['TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE'];
      if (!validStatuses.includes(newStatus)) {
        const targetTask = tasks.find((t) => t.id === over.id);
        if (targetTask) {
          newStatus = targetTask.status;
        } else {
          return; // Invalid drop target
        }
      }

      // If the status hasn't changed, no need to update
      if (task.status === newStatus) return;

      try {
        // Update task status
        await onTaskUpdate(taskId, { status: newStatus });
        toast.success('Task moved successfully');
      } catch (error) {
        console.error('Failed to update task:', error);
        toast.error('Failed to move task');
      }
    },
    [tasks, onTaskUpdate, toast]
  );

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCorners}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
    >
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {columns.map((column) => (
          <KanbanColumn
            key={column.id}
            status={column.id}
            title={column.title}
            color={column.color}
            tasks={getTasksByStatus(column.id)}
          />
        ))}
      </div>
      <DragOverlay>
        {activeTask ? <KanbanCard task={activeTask} /> : null}
      </DragOverlay>
    </DndContext>
  );
};

KanbanBoard.propTypes = {
  tasks: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      status: PropTypes.string.isRequired,
      position: PropTypes.number,
    })
  ).isRequired,
  onTaskUpdate: PropTypes.func.isRequired,
};

export default KanbanBoard;
