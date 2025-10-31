import { FolderKanban, Users, CheckSquare, MoreVertical } from 'lucide-react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { formatDate } from '../../utils/helpers';

const ProjectCard = ({ project, onEdit, onDelete }) => {
  const [showMenu, setShowMenu] = useState(false);
  const navigate = useNavigate();
  const { user } = useAuth();

  const handleCardClick = () => {
    navigate(`/app/projects/${project.id}`);
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
    const userMember = project.members?.find(m => m.userId === user.id);
    return userMember && (userMember.role === 'ADMIN' || userMember.role === 'OWNER');
  };

  const canDelete = () => {
    return isOwner();
  };

  return (
    <div
      className="card hover:shadow-lg transition-shadow relative cursor-pointer"
      onClick={handleCardClick}
    >
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-3">
          <div
            className="w-12 h-12 rounded-lg flex items-center justify-center"
            style={{ backgroundColor: project.color }}
          >
            <FolderKanban className="w-6 h-6 text-white" />
          </div>
          <div>
            <h3 className="text-lg font-semibold text-secondary-900">{project.name}</h3>
            <p className="text-sm text-secondary-500">
              Created {formatDate(project.createdAt)}
            </p>
          </div>
        </div>

        <div className="relative">
          <button
            onClick={(e) => {
              e.stopPropagation();
              setShowMenu(!showMenu);
            }}
            className="p-2 hover:bg-secondary-100 rounded-lg transition-colors"
          >
            <MoreVertical className="w-5 h-5 text-secondary-400" />
          </button>

          {showMenu && (
            <>
              <div
                className="fixed inset-0 z-10"
                onClick={() => setShowMenu(false)}
              ></div>
              <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-secondary-200 py-1 z-20">
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    if (canEdit()) {
                      onEdit(project);
                      setShowMenu(false);
                    }
                  }}
                  disabled={!canEdit()}
                  className={`w-full text-left px-4 py-2 text-secondary-700 ${
                    canEdit()
                      ? 'hover:bg-secondary-100 cursor-pointer'
                      : 'opacity-50 cursor-not-allowed'
                  }`}
                  title={!canEdit() ? "You don't have permission to edit this project" : ""}
                >
                  Edit Project
                </button>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    if (canDelete()) {
                      onDelete(project);
                      setShowMenu(false);
                    }
                  }}
                  disabled={!canDelete()}
                  className={`w-full text-left px-4 py-2 text-error ${
                    canDelete()
                      ? 'hover:bg-secondary-100 cursor-pointer'
                      : 'opacity-50 cursor-not-allowed'
                  }`}
                  title={!canDelete() ? "Only the project owner can delete this project" : ""}
                >
                  Delete Project
                </button>
              </div>
            </>
          )}
        </div>
      </div>

      <p className="text-secondary-600 text-sm mb-4 line-clamp-2">
        {project.description || 'No description provided'}
      </p>

      <div className="flex items-center gap-4 text-sm text-secondary-500">
        <div className="flex items-center gap-1">
          <Users className="w-4 h-4" />
          <span>{project.memberCount || 0} members</span>
        </div>
        <div className="flex items-center gap-1">
          <CheckSquare className="w-4 h-4" />
          <span>{project.taskCount || 0} tasks</span>
        </div>
      </div>
    </div>
  );
};

export default ProjectCard;
