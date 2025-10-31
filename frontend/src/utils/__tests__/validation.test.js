import { describe, it, expect } from 'vitest'
import {
  isValidEmail,
  isValidPassword,
  isValidName,
  validateLoginForm,
  validateRegisterForm,
  validateProjectForm,
  validateTaskForm,
} from '../validation'

describe('validation.js', () => {
  describe('isValidEmail', () => {
    it('should return true for valid email', () => {
      expect(isValidEmail('test@example.com')).toBe(true)
      expect(isValidEmail('user.name@domain.co.uk')).toBe(true)
      expect(isValidEmail('user+tag@example.com')).toBe(true)
    })

    it('should return false for invalid email', () => {
      expect(isValidEmail('invalid')).toBe(false)
      expect(isValidEmail('invalid@')).toBe(false)
      expect(isValidEmail('@example.com')).toBe(false)
      expect(isValidEmail('invalid@example')).toBe(false)
      expect(isValidEmail('invalid @example.com')).toBe(false)
    })

    it('should return false for empty string', () => {
      expect(isValidEmail('')).toBe(false)
    })

    it('should return false for null', () => {
      expect(isValidEmail(null)).toBe(false)
    })
  })

  describe('isValidPassword', () => {
    it('should return true for valid password', () => {
      expect(isValidPassword('password123')).toBe(true)
      expect(isValidPassword('123456')).toBe(true)
      expect(isValidPassword('abcdef')).toBe(true)
    })

    it('should return false for short password', () => {
      expect(isValidPassword('12345')).toBe(false)
      expect(isValidPassword('abc')).toBe(false)
    })

    it('should return false for empty string', () => {
      expect(isValidPassword('')).toBeFalsy()
    })

    it('should return false for null', () => {
      expect(isValidPassword(null)).toBeFalsy()
    })

    it('should accept exactly 6 characters', () => {
      expect(isValidPassword('123456')).toBe(true)
    })
  })

  describe('isValidName', () => {
    it('should return true for valid name', () => {
      expect(isValidName('John')).toBe(true)
      expect(isValidName('Jo')).toBe(true)
      expect(isValidName('  John  ')).toBe(true)
    })

    it('should return false for short name', () => {
      expect(isValidName('J')).toBe(false)
      expect(isValidName('  A  ')).toBe(false)
    })

    it('should return false for empty string', () => {
      expect(isValidName('')).toBeFalsy()
      expect(isValidName('  ')).toBe(false)
    })

    it('should return false for null', () => {
      expect(isValidName(null)).toBeFalsy()
    })
  })

  describe('validateLoginForm', () => {
    it('should return no errors for valid credentials', () => {
      const errors = validateLoginForm('test@example.com', 'password123')
      expect(Object.keys(errors)).toHaveLength(0)
    })

    it('should return email error for missing email', () => {
      const errors = validateLoginForm('', 'password123')
      expect(errors.email).toBe('Email is required')
    })

    it('should return email error for invalid email', () => {
      const errors = validateLoginForm('invalid-email', 'password123')
      expect(errors.email).toBe('Invalid email format')
    })

    it('should return password error for missing password', () => {
      const errors = validateLoginForm('test@example.com', '')
      expect(errors.password).toBe('Password is required')
    })

    it('should return both errors for missing both', () => {
      const errors = validateLoginForm('', '')
      expect(errors.email).toBe('Email is required')
      expect(errors.password).toBe('Password is required')
    })
  })

  describe('validateRegisterForm', () => {
    const validFormData = {
      firstName: 'John',
      lastName: 'Doe',
      email: 'test@example.com',
      password: 'password123',
      confirmPassword: 'password123',
    }

    it('should return no errors for valid form data', () => {
      const errors = validateRegisterForm(validFormData)
      expect(Object.keys(errors)).toHaveLength(0)
    })

    it('should return error for missing firstName', () => {
      const errors = validateRegisterForm({
        ...validFormData,
        firstName: '',
      })
      expect(errors.firstName).toBe('First name must be at least 2 characters')
    })

    it('should return error for short firstName', () => {
      const errors = validateRegisterForm({
        ...validFormData,
        firstName: 'J',
      })
      expect(errors.firstName).toBe('First name must be at least 2 characters')
    })

    it('should return error for missing lastName', () => {
      const errors = validateRegisterForm({
        ...validFormData,
        lastName: '',
      })
      expect(errors.lastName).toBe('Last name must be at least 2 characters')
    })

    it('should return error for short lastName', () => {
      const errors = validateRegisterForm({
        ...validFormData,
        lastName: 'D',
      })
      expect(errors.lastName).toBe('Last name must be at least 2 characters')
    })

    it('should return error for missing email', () => {
      const errors = validateRegisterForm({
        ...validFormData,
        email: '',
      })
      expect(errors.email).toBe('Email is required')
    })

    it('should return error for invalid email', () => {
      const errors = validateRegisterForm({
        ...validFormData,
        email: 'invalid-email',
      })
      expect(errors.email).toBe('Invalid email format')
    })

    it('should return error for missing password', () => {
      const errors = validateRegisterForm({
        ...validFormData,
        password: '',
      })
      expect(errors.password).toBe('Password is required')
    })

    it('should return error for short password', () => {
      const errors = validateRegisterForm({
        ...validFormData,
        password: '12345',
        confirmPassword: '12345',
      })
      expect(errors.password).toBe('Password must be at least 6 characters')
    })

    it('should return error for non-matching passwords', () => {
      const errors = validateRegisterForm({
        ...validFormData,
        confirmPassword: 'different',
      })
      expect(errors.confirmPassword).toBe('Passwords do not match')
    })

    it('should return multiple errors for invalid form', () => {
      const errors = validateRegisterForm({
        firstName: 'J',
        lastName: 'D',
        email: 'invalid',
        password: '123',
        confirmPassword: '456',
      })
      expect(Object.keys(errors).length).toBeGreaterThan(1)
    })
  })

  describe('validateProjectForm', () => {
    const validFormData = {
      name: 'Test Project',
      description: 'This is a test project description',
    }

    it('should return no errors for valid form data', () => {
      const errors = validateProjectForm(validFormData)
      expect(Object.keys(errors)).toHaveLength(0)
    })

    it('should return error for missing name', () => {
      const errors = validateProjectForm({
        ...validFormData,
        name: '',
      })
      expect(errors.name).toBe('Project name must be at least 3 characters')
    })

    it('should return error for short name', () => {
      const errors = validateProjectForm({
        ...validFormData,
        name: 'AB',
      })
      expect(errors.name).toBe('Project name must be at least 3 characters')
    })

    it('should return error for name with only whitespace', () => {
      const errors = validateProjectForm({
        ...validFormData,
        name: '   ',
      })
      expect(errors.name).toBe('Project name must be at least 3 characters')
    })

    it('should return error for missing description', () => {
      const errors = validateProjectForm({
        ...validFormData,
        description: '',
      })
      expect(errors.description).toBe('Description must be at least 10 characters')
    })

    it('should return error for short description', () => {
      const errors = validateProjectForm({
        ...validFormData,
        description: 'Short',
      })
      expect(errors.description).toBe('Description must be at least 10 characters')
    })

    it('should return error for description with only whitespace', () => {
      const errors = validateProjectForm({
        ...validFormData,
        description: '          ',
      })
      expect(errors.description).toBe('Description must be at least 10 characters')
    })

    it('should accept exactly 3 characters for name', () => {
      const errors = validateProjectForm({
        ...validFormData,
        name: 'ABC',
      })
      expect(errors.name).toBeUndefined()
    })

    it('should accept exactly 10 characters for description', () => {
      const errors = validateProjectForm({
        ...validFormData,
        description: 'A'.repeat(10),
      })
      expect(errors.description).toBeUndefined()
    })
  })

  describe('validateTaskForm', () => {
    const validFormData = {
      title: 'Test Task',
      projectId: '123e4567-e89b-12d3-a456-426614174000',
      status: 'TODO',
      priority: 'MEDIUM',
    }

    it('should return no errors for valid form data', () => {
      const errors = validateTaskForm(validFormData)
      expect(Object.keys(errors)).toHaveLength(0)
    })

    it('should return error for missing title', () => {
      const errors = validateTaskForm({
        ...validFormData,
        title: '',
      })
      expect(errors.title).toBe('Task title must be at least 3 characters')
    })

    it('should return error for short title', () => {
      const errors = validateTaskForm({
        ...validFormData,
        title: 'AB',
      })
      expect(errors.title).toBe('Task title must be at least 3 characters')
    })

    it('should return error for title with only whitespace', () => {
      const errors = validateTaskForm({
        ...validFormData,
        title: '   ',
      })
      expect(errors.title).toBe('Task title must be at least 3 characters')
    })

    it('should return error for missing projectId', () => {
      const errors = validateTaskForm({
        ...validFormData,
        projectId: '',
      })
      expect(errors.projectId).toBe('Project is required')
    })

    it('should return error for missing status', () => {
      const errors = validateTaskForm({
        ...validFormData,
        status: '',
      })
      expect(errors.status).toBe('Status is required')
    })

    it('should return error for missing priority', () => {
      const errors = validateTaskForm({
        ...validFormData,
        priority: '',
      })
      expect(errors.priority).toBe('Priority is required')
    })

    it('should return multiple errors for invalid form', () => {
      const errors = validateTaskForm({
        title: 'AB',
        projectId: '',
        status: '',
        priority: '',
      })
      expect(Object.keys(errors)).toHaveLength(4)
    })

    it('should accept exactly 3 characters for title', () => {
      const errors = validateTaskForm({
        ...validFormData,
        title: 'ABC',
      })
      expect(errors.title).toBeUndefined()
    })
  })
})
