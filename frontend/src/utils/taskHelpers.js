/**
 * Transforms task form data to match backend API expectations
 * - Converts date strings to LocalDateTime format
 * - Converts numeric strings to numbers
 * - Removes empty/null values
 */
export const transformTaskFormData = (formData) => {
  const transformed = { ...formData };

  // Convert date from "YYYY-MM-DD" to "YYYY-MM-DDTHH:mm:ss" for LocalDateTime
  if (transformed.dueDate && transformed.dueDate.trim() !== '') {
    // If it's already in datetime format, keep it
    if (!transformed.dueDate.includes('T')) {
      transformed.dueDate = transformed.dueDate + 'T00:00:00';
    }
  } else {
    delete transformed.dueDate;
  }

  // Convert estimatedHours to number
  if (transformed.estimatedHours !== null && transformed.estimatedHours !== undefined && transformed.estimatedHours !== '') {
    transformed.estimatedHours = parseFloat(transformed.estimatedHours);
  } else {
    delete transformed.estimatedHours;
  }

  // Convert actualHours to number (for edit modal)
  if (transformed.actualHours !== null && transformed.actualHours !== undefined && transformed.actualHours !== '') {
    transformed.actualHours = parseFloat(transformed.actualHours);
  } else {
    delete transformed.actualHours;
  }

  // Remove empty strings and convert to null for optional UUID fields
  if (transformed.assignedToId === '' || transformed.assignedToId === null) {
    delete transformed.assignedToId;
  }

  // Remove empty description
  if (transformed.description === '') {
    delete transformed.description;
  }

  return transformed;
};

/**
 * Formats a LocalDateTime string from backend to "YYYY-MM-DD" for date input
 */
export const formatDateForInput = (dateTimeString) => {
  if (!dateTimeString) return '';
  // Extract just the date part from "YYYY-MM-DDTHH:mm:ss"
  return dateTimeString.split('T')[0];
};
