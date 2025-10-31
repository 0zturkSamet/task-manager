import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft,
  Calendar,
  Clock,
  Edit2,
  Trash2,
  User,
  MessageSquare,
  Send,
  ThumbsUp,
  ThumbsDown
} from 'lucide-react';
import taskService from '../services/taskService';
import { useToast } from '../context/ToastContext';
import { useAuth } from '../context/AuthContext';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Badge from '../components/common/Badge';
import MemberAvatar from '../components/common/MemberAvatar';
import Button from '../components/common/Button';
import Textarea from '../components/common/Textarea';
import EditTaskModal from '../components/tasks/EditTaskModal';
import ConfirmDialog from '../components/common/ConfirmDialog';
import { formatDate, formatDateTime, isOverdue } from '../utils/helpers';

const TaskDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const toast = useToast();
  const { user } = useAuth();

  const [task, setTask] = useState(null);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [commentText, setCommentText] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    fetchTaskDetails();
    fetchComments();
  }, [id]);

  const fetchTaskDetails = async () => {
    try {
      setLoading(true);
      const data = await taskService.getTaskById(id);
      setTask(data);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to load task details');
      navigate('/tasks');
    } finally {
      setLoading(false);
    }
  };

  const fetchComments = async () => {
    try {
      const data = await taskService.getTaskComments(id);
      setComments(data);
    } catch (err) {
      console.error('Error fetching comments:', err);
    }
  };

  const handleUpdate = async (taskData) => {
    try {
      const updatedTask = await taskService.updateTask(id, taskData);
      setTask(updatedTask);
      toast.success('Task updated successfully!');
      setIsEditModalOpen(false);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update task');
      throw err;
    }
  };

  const handleDelete = async () => {
    setDeleting(true);
    try {
      await taskService.deleteTask(id);
      toast.success('Task deleted successfully!');
      navigate('/tasks');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to delete task');
    } finally {
      setDeleting(false);
    }
  };

  const handleAddComment = async (e) => {
    e.preventDefault();
    if (!commentText.trim()) return;

    setSubmittingComment(true);
    try {
      const newComment = await taskService.addComment(id, { commentText: commentText });
      setComments([...comments, newComment]);
      setCommentText('');
      toast.success('Comment added!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to add comment');
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleLikeComment = async (commentId) => {
    try {
      const updatedComment = await taskService.likeComment(commentId);
      setComments(comments.map(c => c.id === commentId ? updatedComment : c));
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to like comment');
    }
  };

  const handleDislikeComment = async (commentId) => {
    try {
      const updatedComment = await taskService.dislikeComment(commentId);
      setComments(comments.map(c => c.id === commentId ? updatedComment : c));
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to dislike comment');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!task) {
    return null;
  }

  return (
    <div className="max-w-5xl">
      {/* Header */}
      <div className="mb-6">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4"
        >
          <ArrowLeft className="w-4 h-4" />
          Back
        </button>

        <div className="flex items-start justify-between">
          <div className="flex-1">
            <h1 className="text-2xl font-bold text-gray-900 mb-2">{task.title}</h1>
            <div className="flex items-center gap-2">
              <Badge type="status" value={task.status} />
              <Badge type="priority" value={task.priority} />
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Button
              variant="secondary"
              onClick={() => setIsEditModalOpen(true)}
              className="flex items-center gap-2"
            >
              <Edit2 className="w-4 h-4" />
              Edit
            </Button>
            <Button
              variant="danger"
              onClick={() => setDeleteConfirm(true)}
              className="flex items-center gap-2"
            >
              <Trash2 className="w-4 h-4" />
              Delete
            </Button>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-6">
        {/* Main Content */}
        <div className="col-span-2 space-y-6">
          {/* Description */}
          <div className="card">
            <h2 className="text-lg font-semibold text-gray-900 mb-3">Description</h2>
            <p className="text-gray-700 whitespace-pre-wrap">
              {task.description || 'No description provided'}
            </p>
          </div>

          {/* Comments */}
          <div className="card">
            <h2 className="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <MessageSquare className="w-5 h-5" />
              Comments ({comments.length})
            </h2>

            {/* Comment List */}
            <div className="space-y-4 mb-4">
              {comments.length === 0 ? (
                <p className="text-gray-500 text-sm text-center py-4">
                  No comments yet. Be the first to comment!
                </p>
              ) : (
                comments.map((comment) => (
                  <div key={comment.id} className="flex gap-3">
                    <MemberAvatar
                      user={{
                        firstName: comment.userName?.split(' ')[0] || '',
                        lastName: comment.userName?.split(' ')[1] || '',
                        email: comment.userEmail || ''
                      }}
                      size="sm"
                    />
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="font-medium text-sm text-gray-900">
                          {comment.userName}
                        </span>
                        <span className="text-xs text-gray-500">
                          {formatDateTime(comment.createdAt)}
                        </span>
                      </div>
                      <p className="text-sm text-gray-700 whitespace-pre-wrap mb-2">
                        {comment.commentText}
                      </p>
                      <div className="flex items-center gap-4">
                        <button
                          onClick={() => handleLikeComment(comment.id)}
                          className={`flex items-center gap-1 transition-colors ${
                            comment.userReaction === 'LIKE'
                              ? 'text-blue-600 font-semibold'
                              : 'text-gray-600 hover:text-blue-600'
                          }`}
                        >
                          <ThumbsUp
                            className="w-4 h-4"
                            fill={comment.userReaction === 'LIKE' ? 'currentColor' : 'none'}
                          />
                          <span className="text-xs font-medium">{comment.likesCount || 0}</span>
                        </button>
                        <button
                          onClick={() => handleDislikeComment(comment.id)}
                          className={`flex items-center gap-1 transition-colors ${
                            comment.userReaction === 'DISLIKE'
                              ? 'text-red-600 font-semibold'
                              : 'text-gray-600 hover:text-red-600'
                          }`}
                        >
                          <ThumbsDown
                            className="w-4 h-4"
                            fill={comment.userReaction === 'DISLIKE' ? 'currentColor' : 'none'}
                          />
                          <span className="text-xs font-medium">{comment.dislikesCount || 0}</span>
                        </button>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>

            {/* Add Comment Form */}
            <form onSubmit={handleAddComment} className="border-t pt-4">
              <div className="flex gap-3">
                <MemberAvatar
                  user={{
                    firstName: user?.firstName || '',
                    lastName: user?.lastName || '',
                    email: user?.email || ''
                  }}
                  size="sm"
                />
                <div className="flex-1">
                  <Textarea
                    value={commentText}
                    onChange={(e) => setCommentText(e.target.value)}
                    placeholder="Add a comment..."
                    rows={3}
                    className="mb-2"
                  />
                  <div className="flex justify-end">
                    <Button
                      type="submit"
                      variant="primary"
                      loading={submittingComment}
                      disabled={!commentText.trim()}
                      className="flex items-center gap-2"
                    >
                      <Send className="w-4 h-4" />
                      Comment
                    </Button>
                  </div>
                </div>
              </div>
            </form>
          </div>
        </div>

        {/* Sidebar */}
        <div className="col-span-1 space-y-6">
          {/* Details */}
          <div className="card">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Details</h2>

            <div className="space-y-4">
              {/* Assignee */}
              <div>
                <label className="text-sm font-medium text-gray-700 mb-2 block">
                  Assignee
                </label>
                {task.assignedToId ? (
                  <div className="flex items-center gap-2">
                    <MemberAvatar
                      user={{
                        firstName: task.assignedToName?.split(' ')[0] || '',
                        lastName: task.assignedToName?.split(' ')[1] || '',
                        email: task.assignedToEmail || ''
                      }}
                      size="sm"
                    />
                    <span className="text-sm text-gray-900">{task.assignedToName}</span>
                  </div>
                ) : (
                  <div className="flex items-center gap-2 text-gray-500">
                    <User className="w-4 h-4" />
                    <span className="text-sm">Unassigned</span>
                  </div>
                )}
              </div>

              {/* Due Date */}
              {task.dueDate && (
                <div>
                  <label className="text-sm font-medium text-gray-700 mb-2 block">
                    Due Date
                  </label>
                  <div className={`flex items-center gap-2 ${
                    isOverdue(task.dueDate, task.status)
                      ? 'text-red-600 font-medium'
                      : 'text-gray-900'
                  }`}>
                    <Calendar className="w-4 h-4" />
                    <span className="text-sm">{formatDate(task.dueDate)}</span>
                  </div>
                </div>
              )}

              {/* Estimated Hours */}
              {task.estimatedHours && (
                <div>
                  <label className="text-sm font-medium text-gray-700 mb-2 block">
                    Estimated Hours
                  </label>
                  <div className="flex items-center gap-2 text-gray-900">
                    <Clock className="w-4 h-4" />
                    <span className="text-sm">{task.estimatedHours}h</span>
                  </div>
                </div>
              )}

              {/* Actual Hours */}
              {task.actualHours && (
                <div>
                  <label className="text-sm font-medium text-gray-700 mb-2 block">
                    Actual Hours
                  </label>
                  <div className="flex items-center gap-2 text-gray-900">
                    <Clock className="w-4 h-4" />
                    <span className="text-sm">{task.actualHours}h</span>
                  </div>
                </div>
              )}

              {/* Project */}
              {task.projectName && (
                <div>
                  <label className="text-sm font-medium text-gray-700 mb-2 block">
                    Project
                  </label>
                  <span className="text-sm text-gray-900">{task.projectName}</span>
                </div>
              )}

              {/* Created */}
              <div>
                <label className="text-sm font-medium text-gray-700 mb-2 block">
                  Created
                </label>
                <span className="text-sm text-gray-900">
                  {formatDateTime(task.createdAt)}
                </span>
              </div>

              {/* Updated */}
              {task.updatedAt && task.updatedAt !== task.createdAt && (
                <div>
                  <label className="text-sm font-medium text-gray-700 mb-2 block">
                    Last Updated
                  </label>
                  <span className="text-sm text-gray-900">
                    {formatDateTime(task.updatedAt)}
                  </span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Edit Modal */}
      <EditTaskModal
        isOpen={isEditModalOpen}
        onClose={() => setIsEditModalOpen(false)}
        onSubmit={handleUpdate}
        task={task}
      />

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={deleteConfirm}
        onClose={() => setDeleteConfirm(false)}
        onConfirm={handleDelete}
        title="Delete Task"
        message={`Are you sure you want to delete "${task.title}"? This action cannot be undone.`}
        loading={deleting}
      />
    </div>
  );
};

export default TaskDetail;
