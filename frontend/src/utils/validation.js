// Email validation
export const isValidEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

// Password validation
export const isValidPassword = (password) => {
  return password && password.length >= 6;
};

// Name validation
export const isValidName = (name) => {
  return name && name.trim().length >= 2;
};

// Form validation helpers
export const validateLoginForm = (email, password) => {
  const errors = {};

  if (!email) {
    errors.email = 'Email is required';
  } else if (!isValidEmail(email)) {
    errors.email = 'Invalid email format';
  }

  if (!password) {
    errors.password = 'Password is required';
  }

  return errors;
};

export const validateRegisterForm = (formData) => {
  const errors = {};

  if (!formData.firstName || !isValidName(formData.firstName)) {
    errors.firstName = 'First name must be at least 2 characters';
  }

  if (!formData.lastName || !isValidName(formData.lastName)) {
    errors.lastName = 'Last name must be at least 2 characters';
  }

  if (!formData.email) {
    errors.email = 'Email is required';
  } else if (!isValidEmail(formData.email)) {
    errors.email = 'Invalid email format';
  }

  if (!formData.password) {
    errors.password = 'Password is required';
  } else if (!isValidPassword(formData.password)) {
    errors.password = 'Password must be at least 6 characters';
  }

  if (formData.password !== formData.confirmPassword) {
    errors.confirmPassword = 'Passwords do not match';
  }

  return errors;
};

export const validateProjectForm = (formData) => {
  const errors = {};

  if (!formData.name || formData.name.trim().length < 3) {
    errors.name = 'Project name must be at least 3 characters';
  }

  if (!formData.description || formData.description.trim().length < 10) {
    errors.description = 'Description must be at least 10 characters';
  }

  return errors;
};

export const validateTaskForm = (formData) => {
  const errors = {};

  if (!formData.title || formData.title.trim().length < 3) {
    errors.title = 'Task title must be at least 3 characters';
  }

  if (!formData.projectId) {
    errors.projectId = 'Project is required';
  }

  if (!formData.status) {
    errors.status = 'Status is required';
  }

  if (!formData.priority) {
    errors.priority = 'Priority is required';
  }

  return errors;
};
