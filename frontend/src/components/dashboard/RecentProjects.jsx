import { Link } from 'react-router-dom';
import { FolderKanban } from 'lucide-react';
import PropTypes from 'prop-types';
import Button from '../common/Button';

const RecentProjects = ({ projects, onCreateProject }) => {
  return (
    <div className="card">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-lg font-semibold text-gray-900">Recent Projects</h2>
        <Link to="/projects" className="text-primary-600 hover:text-primary-700 text-sm font-medium">
          View All
        </Link>
      </div>
      {projects.length === 0 ? (
        <div className="text-center py-8">
          <FolderKanban className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-gray-500">No projects yet</p>
          <Button
            variant="primary"
            onClick={onCreateProject}
            className="mt-4"
          >
            Create Your First Project
          </Button>
        </div>
      ) : (
        <div className="space-y-3">
          {projects.slice(0, 5).map((project) => (
            <Link
              key={project.id}
              to={`/projects/${project.id}`}
              className="flex items-center justify-between p-3 hover:bg-gray-50 rounded-lg transition-colors"
            >
              <div className="flex items-center gap-3">
                <div
                  className="w-3 h-3 rounded-full"
                  style={{ backgroundColor: project.color }}
                ></div>
                <div>
                  <p className="font-medium text-gray-900">{project.name}</p>
                  <p className="text-sm text-gray-500">
                    {project.memberCount} members
                  </p>
                </div>
              </div>
              <div className="text-sm text-gray-500">
                {project.taskCount} tasks
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};

RecentProjects.propTypes = {
  projects: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
      color: PropTypes.string.isRequired,
      memberCount: PropTypes.number.isRequired,
      taskCount: PropTypes.number.isRequired,
    })
  ).isRequired,
  onCreateProject: PropTypes.func.isRequired,
};

export default RecentProjects;
