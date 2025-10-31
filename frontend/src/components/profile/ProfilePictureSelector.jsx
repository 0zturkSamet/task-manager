import { useState, useRef } from 'react';
import { Upload, Check } from 'lucide-react';
import { NFT_AVATARS, getAvatarUrl } from '../../constants/avatars';
import Button from '../common/Button';

const ProfilePictureSelector = ({ currentImage, onSelect, onClose }) => {
  const [selectedImage, setSelectedImage] = useState(currentImage || '');
  const [previewImage, setPreviewImage] = useState(null);
  const [uploadError, setUploadError] = useState('');
  const fileInputRef = useRef(null);

  const handleNFTSelect = (avatarPath) => {
    setSelectedImage(avatarPath);
    setPreviewImage(null);
    setUploadError('');
  };

  const handleFileUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      setUploadError('Please upload a valid image file (JPG, PNG, GIF)');
      return;
    }

    // Validate file size (max 2MB)
    if (file.size > 2 * 1024 * 1024) {
      setUploadError('Image size must be less than 2MB');
      return;
    }

    // Convert to base64
    const reader = new FileReader();
    reader.onloadend = () => {
      setPreviewImage(reader.result);
      setSelectedImage(reader.result);
      setUploadError('');
    };
    reader.readAsDataURL(file);
  };

  const handleSave = () => {
    onSelect(selectedImage);
    onClose();
  };

  const currentAvatar = previewImage || getAvatarUrl(selectedImage);

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Choose Profile Picture</h2>
          <p className="text-gray-600 mb-6">Select an NFT avatar or upload your own image</p>

          {/* Current Selection Preview */}
          <div className="flex justify-center mb-6">
            <div className="relative">
              <img
                src={currentAvatar}
                alt="Selected avatar"
                className="w-32 h-32 rounded-full object-cover border-4 border-primary-500 shadow-lg"
              />
            </div>
          </div>

          {/* Custom Upload */}
          <div className="mb-6">
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              onChange={handleFileUpload}
              className="hidden"
            />
            <Button
              variant="secondary"
              onClick={() => fileInputRef.current?.click()}
              className="w-full flex items-center justify-center gap-2"
            >
              <Upload className="w-4 h-4" />
              Upload Custom Image
            </Button>
            {uploadError && (
              <p className="text-sm text-red-600 mt-2">{uploadError}</p>
            )}
            <p className="text-xs text-gray-500 mt-2">
              Supported formats: JPG, PNG, GIF (max 2MB)
            </p>
          </div>

          {/* NFT Avatars Grid */}
          <div className="mb-6">
            <h3 className="text-sm font-semibold text-gray-700 mb-3">NFT Avatars</h3>
            <div className="grid grid-cols-5 gap-3">
              {NFT_AVATARS.map((avatar) => (
                <button
                  key={avatar.id}
                  onClick={() => handleNFTSelect(avatar.path)}
                  className={`relative group rounded-xl overflow-hidden hover:scale-105 transition-transform ${
                    selectedImage === avatar.path ? 'ring-4 ring-primary-500' : 'ring-2 ring-gray-200'
                  }`}
                  title={avatar.name}
                >
                  <img
                    src={avatar.path}
                    alt={avatar.name}
                    className="w-full h-full object-cover"
                  />
                  {selectedImage === avatar.path && (
                    <div className="absolute inset-0 bg-primary-500/20 flex items-center justify-center">
                      <div className="bg-primary-500 rounded-full p-1">
                        <Check className="w-4 h-4 text-white" />
                      </div>
                    </div>
                  )}
                </button>
              ))}
            </div>
          </div>

          {/* Actions */}
          <div className="flex gap-3">
            <Button
              variant="secondary"
              onClick={onClose}
              className="flex-1"
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              onClick={handleSave}
              className="flex-1"
            >
              Save Picture
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfilePictureSelector;
