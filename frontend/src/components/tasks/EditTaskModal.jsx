import { useState, useEffect } from 'react';
import Modal from '../common/Modal';
import Input from '../common/Input';
import Textarea from '../common/Textarea';
import Select from '../common/Select';
import Button from '../common/Button';
import AssigneeSelect from '../common/AssigneeSelect';
import { validateTaskForm } from '../../utils/validation';
import { transformTaskFormData, formatDateForInput } from '../../utils/taskHelpers';
import { useProjects } from '../../hooks/useProjects';
import { useProjectMembers } from '../../hooks/useProjectMembers';
import { useAuth } from '../../context/AuthContext';
import userService from '../../services/userService';

const EditTaskModal = ({ isOpen, onClose, onSubmit, task }) => {
  const { projects, loading: projectsLoading } = useProjects();
  const { isAdmin } = useAuth();
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    projectId: '',
    assignedToId: null,
    status: 'TODO',
    priority: 'MEDIUM',
    dueDate: '',
    estimatedHours: '',
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [allUsers, setAllUsers] = useState([]);
  const { members, fetchMembers } = useProjectMembers(formData.projectId);

  useEffect(() => {
    if (formData.projectId) {
      fetchMembers();
    }
  }, [formData.projectId, fetchMembers]);

  // Fetch all users if admin
  useEffect(() => {
    const fetchAllUsers = async () => {
      if (isAdmin && isOpen) {
        try {
          const users = await userService.getAllUsers();
          setAllUsers(users);
        } catch (error) {
          console.error('Failed to fetch all users:', error);
        }
      }
    };
    fetchAllUsers();
  }, [isAdmin, isOpen]);

  const statusOptions = [
    { value: 'TODO', label: 'To Do' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'IN_REVIEW', label: 'In Review' },
    { value: 'DONE', label: 'Done' },
  ];

  const priorityOptions = [
    { value: 'LOW', label: 'Low' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HIGH', label: 'High' },
    { value: 'URGENT', label: 'Urgent' },
  ];

  const projectOptions = projectsLoading
    ? []
    : projects.map((p) => ({
        value: p.id,
        label: p.name,
      }));

  // Initialize form with task data when modal opens
  useEffect(() => {
    if (task && isOpen) {
      setFormData({
        title: task.title || '',
        description: task.description || '',
        projectId: task.projectId || '',
        assignedToId: task.assignedToId || null,
        status: task.status || 'TODO',
        priority: task.priority || 'MEDIUM',
        dueDate: formatDateForInput(task.dueDate) || '',
        estimatedHours: task.estimatedHours || '',
      });
      setErrors({});
    }
  }, [task, isOpen]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationErrors = validateTaskForm(formData);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    try {
      // Transform form data to match backend API expectations
      const transformedData = transformTaskFormData(formData);
      await onSubmit(transformedData);
      onClose();
    } catch (err) {
      console.error('Update task error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Edit Task" size="lg">
      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-2 gap-4">
          <div className="col-span-2">
            <Input
              label="Task Title"
              name="title"
              value={formData.title}
              onChange={handleChange}
              error={errors.title}
              placeholder="Enter task title"
            />
          </div>

          <div className="col-span-2">
            <Textarea
              label="Description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              error={errors.description}
              placeholder="Describe the task"
              rows={3}
            />
          </div>

          <Select
            label="Project"
            name="projectId"
            value={formData.projectId}
            onChange={handleChange}
            options={projectOptions}
            error={errors.projectId}
            placeholder={
              projectsLoading
                ? 'Loading projects...'
                : projects.length === 0
                ? 'No projects available'
                : 'Select a project'
            }
            disabled={projectsLoading || projects.length === 0}
          />

          <div className="col-span-2">
            <AssigneeSelect
              projectId={formData.projectId}
              value={formData.assignedToId}
              onChange={(value) => setFormData(prev => ({ ...prev, assignedToId: value }))}
              members={members}
              allUsers={allUsers}
              isAdmin={isAdmin}
              disabled={!formData.projectId}
            />
          </div>

          <Select
            label="Status"
            name="status"
            value={formData.status}
            onChange={handleChange}
            options={statusOptions}
            error={errors.status}
          />

          <Select
            label="Priority"
            name="priority"
            value={formData.priority}
            onChange={handleChange}
            options={priorityOptions}
            error={errors.priority}
          />

          <Input
            label="Due Date"
            type="date"
            name="dueDate"
            value={formData.dueDate}
            onChange={handleChange}
            error={errors.dueDate}
          />

          <Input
            label="Estimated Hours"
            type="number"
            name="estimatedHours"
            value={formData.estimatedHours}
            onChange={handleChange}
            error={errors.estimatedHours}
            placeholder="0"
            min="0"
          />
        </div>

        <div className="flex justify-end gap-3 mt-6">
          <Button variant="secondary" onClick={onClose} type="button">
            Cancel
          </Button>
          <Button variant="primary" type="submit" loading={loading}>
            Save Changes
          </Button>
        </div>
      </form>
    </Modal>
  );
};

export default EditTaskModal;
