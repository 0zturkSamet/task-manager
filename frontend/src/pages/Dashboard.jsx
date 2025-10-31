import { useState, useEffect } from 'react';
import {
  FolderKanban,
  CheckSquare,
  Clock,
  AlertCircle,
  Plus,
  Shield
} from 'lucide-react';
import { useProjects } from '../hooks/useProjects';
import { useTasks } from '../hooks/useTasks';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import Button from '../components/common/Button';
import CreateProjectModal from '../components/projects/CreateProjectModal';
import CreateTaskModal from '../components/tasks/CreateTaskModal';
import StatCard from '../components/dashboard/StatCard';
import RecentProjects from '../components/dashboard/RecentProjects';
import RecentTasks from '../components/dashboard/RecentTasks';
import TaskStatistics from '../components/dashboard/TaskStatistics';
import userService from '../services/userService';

const Dashboard = () => {
  const { projects, loading: projectsLoading, createProject } = useProjects();
  const { tasks, loading: tasksLoading, createTask } = useTasks();
  const { user, isAdmin } = useAuth();
  const toast = useToast();
  const [statistics, setStatistics] = useState(null);
  const [statsLoading, setStatsLoading] = useState(true);
  const [isCreateProjectOpen, setIsCreateProjectOpen] = useState(false);
  const [isCreateTaskOpen, setIsCreateTaskOpen] = useState(false);

  useEffect(() => {
    const fetchStatistics = async () => {
      try {
        const stats = await userService.getStatistics();
        setStatistics(stats);
      } catch (err) {
        console.error('Failed to fetch statistics:', err);
        toast.error('Failed to load statistics');
      } finally {
        setStatsLoading(false);
      }
    };

    fetchStatistics();
  }, [toast]);

  const handleCreateProject = async (projectData) => {
    try {
      await createProject(projectData);
      toast.success('Project created successfully!');
      setIsCreateProjectOpen(false);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create project');
      throw err;
    }
  };

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

  if (projectsLoading || tasksLoading || statsLoading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  // Filter projects and tasks based on user role
  // Admins see all projects/tasks (already filtered by backend)
  // Regular users see only their owned projects and assigned tasks
  const filteredProjects = isAdmin
    ? projects
    : projects.filter(project => project.ownerId === user?.id);

  const filteredTasks = isAdmin
    ? tasks
    : tasks.filter(task => task.assignedToId === user?.id);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-secondary-900">Dashboard</h1>
            {user?.role && (
              <span className={`px-3 py-1 text-white text-xs font-semibold rounded-full flex items-center gap-1.5 ${
                user.role === 'ADMIN' ? 'bg-primary-600' : 'bg-secondary-500'
              }`}>
                {user.role === 'ADMIN' && <Shield className="w-3 h-3" />}
                {user.role}
              </span>
            )}
          </div>
          <p className="text-sm text-secondary-600 mt-1">
            {isAdmin
              ? 'System-wide view - All projects and tasks'
              : 'Your owned projects and assigned tasks'}
          </p>
        </div>
        <div className="flex gap-3">
          <Button
            variant="secondary"
            onClick={() => setIsCreateProjectOpen(true)}
            className="flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            New Project
          </Button>
          <Button
            variant="primary"
            onClick={() => setIsCreateTaskOpen(true)}
            className="flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            New Task
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatCard
          icon={FolderKanban}
          label="Total Projects"
          value={statistics?.totalProjects || 0}
          color="bg-blue-500"
          link="/projects"
        />
        <StatCard
          icon={CheckSquare}
          label="Total Tasks"
          value={statistics?.totalTasks || 0}
          color="bg-green-500"
          link="/tasks"
        />
        <StatCard
          icon={Clock}
          label="In Progress"
          value={statistics?.inProgressCount || 0}
          color="bg-yellow-500"
          link="/tasks"
        />
        <StatCard
          icon={AlertCircle}
          label="Overdue"
          value={statistics?.overdueCount || 0}
          color="bg-red-500"
          link="/tasks"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <RecentProjects
          projects={filteredProjects}
          onCreateProject={() => setIsCreateProjectOpen(true)}
        />
        <RecentTasks
          tasks={filteredTasks}
          onCreateTask={() => setIsCreateTaskOpen(true)}
        />
      </div>

      <TaskStatistics statistics={statistics} />

      <CreateProjectModal
        isOpen={isCreateProjectOpen}
        onClose={() => setIsCreateProjectOpen(false)}
        onSubmit={handleCreateProject}
      />

      <CreateTaskModal
        isOpen={isCreateTaskOpen}
        onClose={() => setIsCreateTaskOpen(false)}
        onSubmit={handleCreateTask}
      />
    </div>
  );
};

export default Dashboard;
