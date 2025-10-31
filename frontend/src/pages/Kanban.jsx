import { useState } from 'react';
import { Plus, Kanban as KanbanIcon } from 'lucide-react';
import { useTasks } from '../hooks/useTasks';
import { useToast } from '../context/ToastContext';
import Button from '../components/common/Button';
import CreateTaskModal from '../components/tasks/CreateTaskModal';
import KanbanBoard from '../components/kanban/KanbanBoard';
import LoadingSpinner from '../components/common/LoadingSpinner';

const Kanban = () => {
  const { tasks, loading, createTask, updateTask } = useTasks();
  const toast = useToast();
  const [isCreateTaskOpen, setIsCreateTaskOpen] = useState(false);

  const handleCreateTask = async (taskData) => {
    try {
      await createTask(taskData);
      toast.success('Task created successfully!');
      setIsCreateTaskOpen(false);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create task');
      throw err;
    }
  };

  const handleTaskUpdate = async (taskId, updates) => {
    try {
      await updateTask(taskId, updates);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update task');
      throw err;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <LoadingSpinner size="large" />
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-3">
          <KanbanIcon className="w-8 h-8 text-primary-600" />
          <h1 className="text-2xl font-bold text-gray-900">Kanban Board</h1>
        </div>
        <Button
          variant="primary"
          onClick={() => setIsCreateTaskOpen(true)}
          className="flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          New Task
        </Button>
      </div>

      <div className="flex-1 overflow-auto">
        {tasks.length === 0 ? (
          <div className="flex items-center justify-center h-full">
            <div className="text-center">
              <KanbanIcon className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">No tasks yet</h3>
              <p className="text-gray-500 mb-4">Create your first task to get started</p>
              <Button
                variant="primary"
                onClick={() => setIsCreateTaskOpen(true)}
                className="flex items-center gap-2 mx-auto"
              >
                <Plus className="w-4 h-4" />
                Create Task
              </Button>
            </div>
          </div>
        ) : (
          <KanbanBoard tasks={tasks} onTaskUpdate={handleTaskUpdate} />
        )}
      </div>

      <CreateTaskModal
        isOpen={isCreateTaskOpen}
        onClose={() => setIsCreateTaskOpen(false)}
        onSubmit={handleCreateTask}
      />
    </div>
  );
};

export default Kanban;
