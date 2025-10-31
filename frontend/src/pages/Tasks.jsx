import { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Plus, UserCheck } from 'lucide-react';
import { useTasks } from '../hooks/useTasks';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import Button from '../components/common/Button';
import LoadingSpinner from '../components/common/LoadingSpinner';
import TaskCard from '../components/tasks/TaskCard';
import CreateTaskModal from '../components/tasks/CreateTaskModal';
import EditTaskModal from '../components/tasks/EditTaskModal';
import ConfirmDialog from '../components/common/ConfirmDialog';

const Tasks = () => {
  const navigate = useNavigate();
  const { tasks, loading, createTask, updateTask, deleteTask } = useTasks();
  const { user } = useAuth();
  const toast = useToast();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editTask, setEditTask] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState({ isOpen: false, task: null });
  const [deleting, setDeleting] = useState(false);
  const [showMyTasksOnly, setShowMyTasksOnly] = useState(false);

  const filteredTasks = useMemo(() => {
    if (!showMyTasksOnly) return tasks;
    return tasks.filter(task => task.assignedToId === user?.id);
  }, [tasks, showMyTasksOnly, user?.id]);

  const handleCreate = async (taskData) => {
    try {
      await createTask(taskData);
      toast.success('Task created successfully!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create task');
      throw err;
    }
  };

  const handleEdit = (task) => {
    setEditTask(task);
  };

  const handleUpdate = async (taskData) => {
    if (!editTask) return;

    try {
      await updateTask(editTask.id, taskData);
      toast.success('Task updated successfully!');
      setEditTask(null);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update task');
      throw err;
    }
  };

  const handleView = (task) => {
    navigate(`/app/tasks/${task.id}`);
  };

  const handleDeleteConfirm = async () => {
    if (!deleteConfirm.task) return;

    setDeleting(true);
    try {
      await deleteTask(deleteConfirm.task.id);
      toast.success('Task deleted successfully!');
      setDeleteConfirm({ isOpen: false, task: null });
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete task');
    } finally {
      setDeleting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Tasks</h1>
          <p className="text-gray-600 mt-1">
            Manage and track your tasks across all projects
            {showMyTasksOnly && ' - Showing only tasks assigned to you'}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => setShowMyTasksOnly(!showMyTasksOnly)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg border transition-colors ${
              showMyTasksOnly
                ? 'bg-primary-50 border-primary-500 text-primary-700'
                : 'bg-white border-gray-300 text-gray-700 hover:bg-gray-50'
            }`}
          >
            <UserCheck className="w-4 h-4" />
            My Tasks
          </button>
          <Button
            variant="primary"
            onClick={() => setIsCreateModalOpen(true)}
            className="flex items-center gap-2"
          >
            <Plus className="w-5 h-5" />
            New Task
          </Button>
        </div>
      </div>

      {filteredTasks.length === 0 ? (
        <div className="text-center py-12">
          <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <Plus className="w-12 h-12 text-gray-400" />
          </div>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">
            {showMyTasksOnly ? 'No tasks assigned to you' : 'No tasks yet'}
          </h3>
          <p className="text-gray-600 mb-6">
            {showMyTasksOnly
              ? 'You don\'t have any tasks assigned to you yet'
              : 'Create your first task to get started'
            }
          </p>
          <Button
            variant="primary"
            onClick={() => setIsCreateModalOpen(true)}
            className="flex items-center gap-2"
          >
            <Plus className="w-5 h-5" />
            Create Your First Task
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredTasks.map((task) => (
            <TaskCard
              key={task.id}
              task={task}
              onEdit={handleEdit}
              onDelete={(task) => setDeleteConfirm({ isOpen: true, task })}
              onView={() => handleView(task)}
            />
          ))}
        </div>
      )}

      <CreateTaskModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSubmit={handleCreate}
      />

      <EditTaskModal
        isOpen={!!editTask}
        onClose={() => setEditTask(null)}
        onSubmit={handleUpdate}
        task={editTask}
      />

      <ConfirmDialog
        isOpen={deleteConfirm.isOpen}
        onClose={() => setDeleteConfirm({ isOpen: false, task: null })}
        onConfirm={handleDeleteConfirm}
        title="Delete Task"
        message={`Are you sure you want to delete "${deleteConfirm.task?.title}"? This action cannot be undone.`}
        loading={deleting}
      />
    </div>
  );
};

export default Tasks;
