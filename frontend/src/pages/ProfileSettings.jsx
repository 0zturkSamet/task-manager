import { useState, useEffect } from 'react';
import { User, Mail, Save, Loader, CheckCircle, BarChart3, Camera } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import userService from '../services/userService';
import LoadingSpinner from '../components/common/LoadingSpinner';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import ProfilePictureSelector from '../components/profile/ProfilePictureSelector';
import { getAvatarUrl } from '../constants/avatars';

const ProfileSettings = () => {
  const { user, updateUser } = useAuth();
  const toast = useToast();

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [stats, setStats] = useState(null);
  const [showPictureSelector, setShowPictureSelector] = useState(false);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    profileImage: '',
  });

  useEffect(() => {
    if (user) {
      setFormData({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || '',
        profileImage: user.profileImage || '',
      });
    }
    fetchStatistics();
  }, [user]);

  const fetchStatistics = async () => {
    try {
      setLoading(true);
      const data = await userService.getStatistics();
      setStats(data);
    } catch (err) {
      console.error('Error fetching statistics:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleProfilePictureSelect = (imageUrl) => {
    setFormData(prev => ({ ...prev, profileImage: imageUrl }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.firstName || !formData.lastName) {
      toast.error('First name and last name are required');
      return;
    }

    setSaving(true);
    try {
      const updated = await userService.updateProfile({
        firstName: formData.firstName,
        lastName: formData.lastName,
        profileImage: formData.profileImage,
      });

      updateUser(updated);
      toast.success('Profile updated successfully!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  if (!user) {
    return (
      <div className="flex items-center justify-center h-full">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-secondary-900 tracking-tight">Profile Settings</h1>
        <p className="text-secondary-600 mt-2">Manage your account settings and preferences</p>
      </div>

      <div className="grid gap-6">
        {/* Profile Picture Section */}
        <div className="card">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <img
                src={getAvatarUrl(formData.profileImage)}
                alt="Profile"
                className="w-20 h-20 rounded-full object-cover border-4 border-primary-100 shadow-md"
              />
              <div>
                <h2 className="text-lg font-semibold text-secondary-900">Profile Picture</h2>
                <p className="text-sm text-secondary-600">Choose an avatar or upload your own</p>
              </div>
            </div>
            <Button
              variant="secondary"
              onClick={() => setShowPictureSelector(true)}
              className="flex items-center gap-2"
            >
              <Camera className="w-4 h-4" />
              Change Picture
            </Button>
          </div>
        </div>

        {/* Profile Information Card */}
        <div className="card">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 rounded-lg bg-primary-100 flex items-center justify-center">
              <User className="w-5 h-5 text-primary-600" />
            </div>
            <div>
              <h2 className="text-lg font-semibold text-secondary-900">Personal Information</h2>
              <p className="text-sm text-secondary-600">Update your personal details</p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label htmlFor="firstName" className="block text-sm font-medium text-secondary-700 mb-2">
                  First Name
                </label>
                <Input
                  id="firstName"
                  name="firstName"
                  type="text"
                  value={formData.firstName}
                  onChange={handleChange}
                  placeholder="Enter your first name"
                  required
                />
              </div>

              <div>
                <label htmlFor="lastName" className="block text-sm font-medium text-secondary-700 mb-2">
                  Last Name
                </label>
                <Input
                  id="lastName"
                  name="lastName"
                  type="text"
                  value={formData.lastName}
                  onChange={handleChange}
                  placeholder="Enter your last name"
                  required
                />
              </div>
            </div>

            <div>
              <label htmlFor="email" className="block text-sm font-medium text-secondary-700 mb-2">
                Email Address
              </label>
              <div className="relative">
                <div className="absolute left-3 top-1/2 -translate-y-1/2">
                  <Mail className="w-5 h-5 text-secondary-400" />
                </div>
                <Input
                  id="email"
                  name="email"
                  type="email"
                  value={formData.email}
                  disabled
                  className="pl-10 bg-secondary-50 cursor-not-allowed"
                />
              </div>
              <p className="text-xs text-secondary-500 mt-1.5">
                Email cannot be changed. Contact support if you need to update it.
              </p>
            </div>

            <div className="flex justify-end pt-2">
              <Button
                type="submit"
                variant="primary"
                loading={saving}
                disabled={saving}
                className="flex items-center gap-2"
              >
                {saving ? (
                  <>
                    <Loader className="w-4 h-4 animate-spin" />
                    Saving...
                  </>
                ) : (
                  <>
                    <Save className="w-4 h-4" />
                    Save Changes
                  </>
                )}
              </Button>
            </div>
          </form>
        </div>

        {/* Statistics Card */}
        {stats && (
          <div className="card">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 rounded-lg bg-success-light/20 flex items-center justify-center">
                <BarChart3 className="w-5 h-5 text-success" />
              </div>
              <div>
                <h2 className="text-lg font-semibold text-secondary-900">Your Statistics</h2>
                <p className="text-sm text-secondary-600">Overview of your activity</p>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="p-4 rounded-lg bg-primary-50 border border-primary-100">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-lg bg-primary-600 flex items-center justify-center shadow-sm">
                    <CheckCircle className="w-6 h-6 text-white" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-secondary-600">Total Projects</p>
                    <p className="text-2xl font-bold text-secondary-900 mt-0.5">{stats.totalProjects || 0}</p>
                  </div>
                </div>
              </div>

              <div className="p-4 rounded-lg bg-success-light/10 border border-success-light/30">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-lg bg-success flex items-center justify-center shadow-sm">
                    <CheckCircle className="w-6 h-6 text-white" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-secondary-600">Completed Tasks</p>
                    <p className="text-2xl font-bold text-secondary-900 mt-0.5">{stats.completedTasks || 0}</p>
                  </div>
                </div>
              </div>

              <div className="p-4 rounded-lg bg-warning-light/10 border border-warning-light/30">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-lg bg-warning flex items-center justify-center shadow-sm">
                    <Loader className="w-6 h-6 text-white" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-secondary-600">Active Tasks</p>
                    <p className="text-2xl font-bold text-secondary-900 mt-0.5">{stats.activeTasks || 0}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Account Info */}
        <div className="card bg-secondary-50 border-secondary-200">
          <div className="flex items-start gap-3">
            <div className="w-10 h-10 rounded-lg bg-info-light/20 flex items-center justify-center flex-shrink-0">
              <Mail className="w-5 h-5 text-info" />
            </div>
            <div>
              <h3 className="font-semibold text-secondary-900 mb-1">Need Help?</h3>
              <p className="text-sm text-secondary-600 leading-relaxed">
                If you need to change your email address or have any issues with your account,
                please contact our support team at{' '}
                <a href="mailto:support@taskmanager.com" className="text-primary-600 hover:text-primary-700 font-medium">
                  support@taskmanager.com
                </a>
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Profile Picture Selector Modal */}
      {showPictureSelector && (
        <ProfilePictureSelector
          currentImage={formData.profileImage}
          onSelect={handleProfilePictureSelect}
          onClose={() => setShowPictureSelector(false)}
        />
      )}
    </div>
  );
};

export default ProfileSettings;
