import { useState } from 'react';
import { Plus, Shield } from 'lucide-react';
import { useProjects } from '../hooks/useProjects';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import Button from '../components/common/Button';
import LoadingSpinner from '../components/common/LoadingSpinner';
import ProjectCard from '../components/projects/ProjectCard';
import CreateProjectModal from '../components/projects/CreateProjectModal';
import EditProjectModal from '../components/projects/EditProjectModal';
import ConfirmDialog from '../components/common/ConfirmDialog';

const Projects = () => {
  const { projects, loading, createProject, updateProject, deleteProject } = useProjects();
  const { isAdmin } = useAuth();
  const toast = useToast();
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editProject, setEditProject] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState({ isOpen: false, project: null });
  const [deleting, setDeleting] = useState(false);

  const handleCreate = async (projectData) => {
    try {
      await createProject(projectData);
      toast.success('Project created successfully!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create project');
      throw err;
    }
  };

  const handleEdit = (project) => {
    setEditProject(project);
  };

  const handleUpdate = async (projectData) => {
    if (!editProject) return;

    try {
      await updateProject(editProject.id, projectData);
      toast.success('Project updated successfully!');
      setEditProject(null);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update project');
      throw err;
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteConfirm.project) return;

    setDeleting(true);
    try {
      await deleteProject(deleteConfirm.project.id);
      toast.success('Project deleted successfully!');
      setDeleteConfirm({ isOpen: false, project: null });
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete project');
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
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-secondary-900">Projects</h1>
            {isAdmin && (
              <span className="px-3 py-1 bg-primary-600 text-white text-xs font-semibold rounded-full flex items-center gap-1.5">
                <Shield className="w-3 h-3" />
                ADMIN
              </span>
            )}
          </div>
          <p className="text-secondary-600 mt-1">
            {isAdmin ? 'Viewing all projects in the system' : 'Manage your projects and collaborate with your team'}
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => setIsCreateModalOpen(true)}
          className="flex items-center gap-2"
        >
          <Plus className="w-5 h-5" />
          New Project
        </Button>
      </div>

      {projects.length === 0 ? (
        <div className="text-center py-12">
          <div className="w-24 h-24 bg-secondary-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <Plus className="w-12 h-12 text-secondary-400" />
          </div>
          <h3 className="text-lg font-semibold text-secondary-900 mb-2">
            No projects yet
          </h3>
          <p className="text-secondary-600 mb-6">
            Get started by creating your first project
          </p>
          <Button
            variant="primary"
            onClick={() => setIsCreateModalOpen(true)}
            className="flex items-center gap-2"
          >
            <Plus className="w-5 h-5" />
            Create Your First Project
          </Button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {projects.map((project) => (
            <ProjectCard
              key={project.id}
              project={project}
              onEdit={handleEdit}
              onDelete={(project) => setDeleteConfirm({ isOpen: true, project })}
            />
          ))}
        </div>
      )}

      <CreateProjectModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSubmit={handleCreate}
      />

      <EditProjectModal
        isOpen={!!editProject}
        onClose={() => setEditProject(null)}
        onSubmit={handleUpdate}
        project={editProject}
      />

      <ConfirmDialog
        isOpen={deleteConfirm.isOpen}
        onClose={() => setDeleteConfirm({ isOpen: false, project: null })}
        onConfirm={handleDeleteConfirm}
        title="Delete Project"
        message={`Are you sure you want to delete "${deleteConfirm.project?.name}"? This action cannot be undone.`}
        loading={deleting}
      />
    </div>
  );
};

export default Projects;
