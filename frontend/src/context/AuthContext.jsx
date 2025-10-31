import { createContext, useContext, useState, useEffect } from 'react';
import authService from '../services/authService';
import userService from '../services/userService';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Initialize auth state on mount
  useEffect(() => {
    const initAuth = async () => {
      try {
        if (authService.isAuthenticated()) {
          const userData = await userService.getProfile();
          setUser(userData);
        }
      } catch (err) {
        console.error('Failed to initialize auth:', err);
        // Only clear auth data on 401 (unauthorized), not on network errors
        if (err.response?.status === 401) {
          authService.clearAuthData();
        }
        // For other errors (network, etc.), keep the token and try again later
      } finally {
        setLoading(false);
      }
    };

    initAuth();
  }, []);

  const register = async (userData) => {
    try {
      setError(null);
      const response = await authService.register(userData);

      // Fetch full user profile after registration
      const profile = await userService.getProfile();
      setUser(profile);

      return response;
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Registration failed';
      setError(errorMessage);
      throw err;
    }
  };

  const login = async (credentials) => {
    try {
      setError(null);
      const response = await authService.login(credentials);

      // Fetch full user profile after login
      const profile = await userService.getProfile();
      setUser(profile);

      return response;
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Login failed';
      setError(errorMessage);
      throw err;
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      setUser(null);
      setError(null);
      // Navigate to landing page
      window.location.href = '/';
    }
  };

  const updateUser = async (userData) => {
    try {
      setError(null);
      const updatedUser = await userService.updateProfile(userData);
      setUser(updatedUser);
      return updatedUser;
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Update failed';
      setError(errorMessage);
      throw err;
    }
  };

  const deleteAccount = async () => {
    try {
      setError(null);
      await userService.deleteAccount();
      setUser(null);
      authService.clearAuthData();
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Delete account failed';
      setError(errorMessage);
      throw err;
    }
  };

  const isAdmin = () => {
    return user?.role === 'ADMIN';
  };

  const value = {
    user,
    loading,
    error,
    isAuthenticated: !!user,
    isAdmin: isAdmin(),
    register,
    login,
    logout,
    updateUser,
    deleteAccount,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
