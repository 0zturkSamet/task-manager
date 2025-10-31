import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Users,
  CheckSquare,
  Calendar,
  Edit2,
  Trash2,
  Plus,
  User,
  Clock
} from 'lucide-react';
import projectService from '../services/projectService';
import taskService from '../services/taskService';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import { formatDate, formatDateTime } from '../utils/helpers';
import EditProjectModal from '../components/projects/EditProjectModal';
import ConfirmDialog from '../components/common/ConfirmDialog';
import CreateTaskModal from '../components/tasks/CreateTaskModal';

const ProjectDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const { user } = useAuth();

  const [project, setProject] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [isCreateTaskModalOpen, setIsCreateTaskModalOpen] = useState(false);

  useEffect(() => {
    fetchProjectData();
  }, [id]);

  const fetchProjectData = async () => {
    try {
      setLoading(true);
      const [projectData, tasksData, membersData] = await Promise.all([
        projectService.getProjectById(id),
        taskService.getProjectTasks(id),
        projectService.getProjectMembers(id)
      ]);
      setProject(projectData);
      setTasks(tasksData);
      setMembers(membersData);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to load project details');
      navigate('/app/projects');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdate = async (projectData) => {
    try {
      const updated = await projectService.updateProject(id, projectData);
      setProject(updated);
      toast.success('Project updated successfully!');
      setIsEditModalOpen(false);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update project');
      throw err;
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await projectService.deleteProject(id);
      toast.success('Project deleted successfully!');
      navigate('/app/projects');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete project');
    } finally {
      setDeleting(false);
    }
  };

  const handleCreateTask = async (taskData) => {
    try {
      await taskService.createTask({ ...taskData, projectId: id });
      toast.success('Task created successfully!');
      setIsCreateTaskModalOpen(false);
      fetchProjectData();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create task');
      throw err;
    }
  };

  const handleTaskClick = (taskId) => {
    navigate(`/app/tasks/${taskId}`);
  };

  // Permission checks
  const isOwner = () => {
    return user && project && user.id === project.ownerId;
  };

  const canEdit = () => {
    if (!user || !project) return false;
    // Owner can always edit
    if (isOwner()) return true;
    // Check if user has ADMIN or OWNER role in the project
    const userMember = members.find(m => m.userId === user.id);
    return userMember && (userMember.role === 'ADMIN' || userMember.role === 'OWNER');
  };

  const canDelete = () => {
    return isOwner();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!project) {
    return null;
  }

  const completedTasks = tasks.filter(t => t.status === 'DONE').length;
  const completionRate = tasks.length > 0 ? Math.round((completedTasks / tasks.length) * 100) : 0;

  return (
    <div className="max-w-7xl mx-auto">
      {/* Header */}
      <div className="mb-6">
        <button
          onClick={() => navigate('/app/projects')}
          className="flex items-center gap-2 text-secondary-600 hover:text-secondary-900 mb-4 smooth-transition"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Projects
        </button>

        <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
          <div className="flex items-start gap-4">
            <div
              className="w-16 h-16 rounded-xl flex items-center justify-center flex-shrink-0"
              style={{ backgroundColor: project.color }}
            >
              <Users className="w-8 h-8 text-white" />
            </div>
            <div>
              <h1 className="text-3xl font-bold text-secondary-900 tracking-tight">{project.name}</h1>
              <p className="text-secondary-600 mt-1">{project.description || 'No description'}</p>
              <p className="text-sm text-secondary-500 mt-2">
                Created {formatDate(project.createdAt)}
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Button
              variant="secondary"
              onClick={() => setIsEditModalOpen(true)}
              disabled={!canEdit()}
              className="flex items-center gap-2"
              title={!canEdit() ? "You don't have permission to edit this project" : ""}
            >
              <Edit2 className="w-4 h-4" />
              Edit
            </Button>
            <Button
              variant="danger"
              onClick={() => setDeleteConfirm(true)}
              disabled={!canDelete()}
              className="flex items-center gap-2"
              title={!canDelete() ? "Only the project owner can delete this project" : ""}
            >
              <Trash2 className="w-4 h-4" />
              Delete
            </Button>
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div className="card">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-lg bg-primary-100 flex items-center justify-center">
              <CheckSquare className="w-6 h-6 text-primary-600" />
            </div>
            <div>
              <p className="text-sm text-secondary-600">Total Tasks</p>
              <p className="text-2xl font-bold text-secondary-900">{tasks.length}</p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-lg bg-success-light/20 flex items-center justify-center">
              <CheckSquare className="w-6 h-6 text-success" />
            </div>
            <div>
              <p className="text-sm text-secondary-600">Completed</p>
              <p className="text-2xl font-bold text-secondary-900">{completedTasks}</p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-lg bg-info-light/20 flex items-center justify-center">
              <Users className="w-6 h-6 text-info" />
            </div>
            <div>
              <p className="text-sm text-secondary-600">Members</p>
              <p className="text-2xl font-bold text-secondary-900">{members.length}</p>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-lg bg-warning-light/20 flex items-center justify-center">
              <Clock className="w-6 h-6 text-warning" />
            </div>
            <div>
              <p className="text-sm text-secondary-600">Progress</p>
              <p className="text-2xl font-bold text-secondary-900">{completionRate}%</p>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="border-b border-secondary-200 mb-6">
        <nav className="flex gap-4 overflow-x-auto">
          {['overview', 'tasks', 'members'].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-3 font-medium capitalize smooth-transition border-b-2 whitespace-nowrap ${
                activeTab === tab
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-secondary-600 hover:text-secondary-900'
              }`}
            >
              {tab}
            </button>
          ))}
        </nav>
      </div>

      {/* Tab Content */}
      {activeTab === 'overview' && (
        <div className="space-y-6">
          <div className="card">
            <h2 className="text-lg font-semibold text-secondary-900 mb-4">Project Overview</h2>
            <div className="space-y-4">
              <div>
                <p className="text-sm font-medium text-secondary-700">Description</p>
                <p className="text-secondary-900 mt-1">{project.description || 'No description provided'}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-secondary-700">Status</p>
                <Badge type="status" value={project.status || 'ACTIVE'} className="mt-1" />
              </div>
              <div>
                <p className="text-sm font-medium text-secondary-700">Created</p>
                <p className="text-secondary-900 mt-1">{formatDateTime(project.createdAt)}</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'tasks' && (
        <div className="space-y-4">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold text-secondary-900">Tasks ({tasks.length})</h2>
            <Button
              variant="primary"
              onClick={() => setIsCreateTaskModalOpen(true)}
              className="flex items-center gap-2"
            >
              <Plus className="w-4 h-4" />
              New Task
            </Button>
          </div>

          {tasks.length === 0 ? (
            <div className="card text-center py-12">
              <CheckSquare className="w-12 h-12 text-secondary-400 mx-auto mb-4" />
              <p className="text-secondary-600">No tasks yet. Create your first task!</p>
            </div>
          ) : (
            <div className="space-y-3">
              {tasks.map((task) => (
                <div
                  key={task.id}
                  onClick={() => handleTaskClick(task.id)}
                  className="card hover:shadow-md cursor-pointer smooth-transition"
                >
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1 min-w-0">
                      <h3 className="font-semibold text-secondary-900 mb-1 truncate">{task.title}</h3>
                      <p className="text-sm text-secondary-600 line-clamp-1">{task.description}</p>
                      <div className="flex items-center gap-2 mt-2 flex-wrap">
                        <Badge type="status" value={task.status} />
                        <Badge type="priority" value={task.priority} />
                      </div>
                    </div>
                    {task.assignedToName && (
                      <div className="flex items-center gap-2 text-sm text-secondary-600">
                        <User className="w-4 h-4" />
                        <span className="hidden sm:inline">{task.assignedToName}</span>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {activeTab === 'members' && (
        <div className="space-y-4">
          <h2 className="text-lg font-semibold text-secondary-900 mb-4">Team Members ({members.length})</h2>

          {members.length === 0 ? (
            <div className="card text-center py-12">
              <Users className="w-12 h-12 text-secondary-400 mx-auto mb-4" />
              <p className="text-secondary-600">No members yet.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {members.map((member) => (
                <div key={member.userId} className="card">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-full bg-primary-600 flex items-center justify-center text-white font-semibold">
                      {member.userName?.charAt(0) || 'U'}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-secondary-900 truncate">{member.userName}</p>
                      <p className="text-sm text-secondary-600 truncate">{member.userEmail}</p>
                      <Badge type="role" value={member.role} className="mt-1" />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Modals */}
      <EditProjectModal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        onSubmit={handleUpdate}
        project={project}
      />

      <CreateTaskModal
        isOpen={isCreateTaskModalOpen}
        onClose={() => setIsCreateTaskModalOpen(false)}
        onSubmit={handleCreateTask}
        projectId={id}
      />

      <ConfirmDialog
        isOpen={deleteConfirm}
        onClose={() => setDeleteConfirm(false)}
        onConfirm={handleDelete}
        title="Delete Project"
        message={`Are you sure you want to delete "${project.name}"? This action cannot be undone.`}
        loading={deleting}
      />
    </div>
  );
};

export default ProjectDetail;
