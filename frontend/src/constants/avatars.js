// Available NFT avatars
export const NFT_AVATARS = [
  { id: 1, path: '/avatars/nft-1.svg', name: 'Cosmic Smile' },
  { id: 2, path: '/avatars/nft-2.svg', name: 'Geometric Soul' },
  { id: 3, path: '/avatars/nft-3.svg', name: 'Crystal Being' },
  { id: 4, path: '/avatars/nft-4.svg', name: 'Happy Face' },
  { id: 5, path: '/avatars/nft-5.svg', name: 'Hexagon Hero' },
  { id: 6, path: '/avatars/nft-6.svg', name: 'Sunset Vibes' },
  { id: 7, path: '/avatars/nft-7.svg', name: 'Blushy Buddy' },
  { id: 8, path: '/avatars/nft-8.svg', name: 'Ocean Wave' },
  { id: 9, path: '/avatars/nft-9.svg', name: 'Star Diamond' },
  { id: 10, path: '/avatars/nft-10.svg', name: 'Dreamy Pixel' },
];

// Default avatar
export const DEFAULT_AVATAR = '/avatars/default.svg';

// Get avatar URL with fallback
export const getAvatarUrl = (profileImage) => {
  if (!profileImage) return DEFAULT_AVATAR;
  // If it's a base64 data URL or starts with http, return as is
  if (profileImage.startsWith('data:') || profileImage.startsWith('http')) {
    return profileImage;
  }
  // Otherwise assume it's a path to an NFT avatar
  return profileImage;
};
