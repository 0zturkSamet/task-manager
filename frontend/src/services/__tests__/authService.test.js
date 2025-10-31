import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import authService from '../authService'
import { server } from '../../test/mocks/server'
import { http, HttpResponse } from 'msw'

describe('AuthService', () => {
  const API_BASE_URL = 'http://localhost:8080/api'

  beforeEach(() => {
    localStorage.clear()
  })

  afterEach(() => {
    localStorage.clear()
  })

  describe('register', () => {
    it('should register a new user and store auth data', async () => {
      const userData = {
        email: 'test@example.com',
        password: 'password123',
        firstName: 'John',
        lastName: 'Doe',
      }

      const response = await authService.register(userData)

      expect(response).toHaveProperty('token')
      expect(response).toHaveProperty('email', userData.email)
      expect(response).toHaveProperty('firstName', userData.firstName)
      expect(response).toHaveProperty('lastName', userData.lastName)

      // Check localStorage
      expect(localStorage.getItem('access_token')).toBe('mock-jwt-token')
      expect(localStorage.getItem('user_data')).toBeTruthy()
    })

    it('should handle registration with duplicate email', async () => {
      server.use(
        http.post(`${API_BASE_URL}/auth/register`, () => {
          return HttpResponse.json(
            { message: 'Email already registered' },
            { status: 409 }
          )
        })
      )

      const userData = {
        email: 'existing@example.com',
        password: 'password123',
        firstName: 'John',
        lastName: 'Doe',
      }

      await expect(authService.register(userData)).rejects.toThrow()
    })

    it('should handle network errors during registration', async () => {
      server.use(
        http.post(`${API_BASE_URL}/auth/register`, () => {
          return HttpResponse.error()
        })
      )

      const userData = {
        email: 'test@example.com',
        password: 'password123',
        firstName: 'John',
        lastName: 'Doe',
      }

      await expect(authService.register(userData)).rejects.toThrow()
    })
  })

  describe('login', () => {
    it('should login user and store auth data', async () => {
      const credentials = {
        email: 'test@example.com',
        password: 'password123',
      }

      const response = await authService.login(credentials)

      expect(response).toHaveProperty('token')
      expect(response).toHaveProperty('email', credentials.email)

      // Check localStorage
      expect(localStorage.getItem('access_token')).toBe('mock-jwt-token')
      expect(localStorage.getItem('user_data')).toBeTruthy()
    })

    it('should handle login with invalid credentials', async () => {
      const credentials = {
        email: 'test@example.com',
        password: 'wrongpassword',
      }

      await expect(authService.login(credentials)).rejects.toThrow()
    })

    it('should handle network errors during login', async () => {
      server.use(
        http.post(`${API_BASE_URL}/auth/login`, () => {
          return HttpResponse.error()
        })
      )

      const credentials = {
        email: 'test@example.com',
        password: 'password123',
      }

      await expect(authService.login(credentials)).rejects.toThrow()
    })
  })

  describe('logout', () => {
    it('should logout user and clear auth data', async () => {
      // Set initial auth data
      localStorage.setItem('access_token', 'mock-token')
      localStorage.setItem('user_data', JSON.stringify({ id: '123', email: 'test@example.com' }))

      await authService.logout()

      // Check localStorage is cleared
      expect(localStorage.getItem('access_token')).toBeNull()
      expect(localStorage.getItem('user_data')).toBeNull()
    })

    it('should clear auth data even if API call fails', async () => {
      server.use(
        http.post(`${API_BASE_URL}/auth/logout`, () => {
          return HttpResponse.error()
        })
      )

      localStorage.setItem('access_token', 'mock-token')
      localStorage.setItem('user_data', JSON.stringify({ id: '123' }))

      await authService.logout()

      // Should still clear localStorage
      expect(localStorage.getItem('access_token')).toBeNull()
      expect(localStorage.getItem('user_data')).toBeNull()
    })
  })

  describe('setAuthData', () => {
    it('should store token and user data in localStorage', () => {
      const token = 'test-token'
      const userData = { id: '123', email: 'test@example.com' }

      authService.setAuthData(token, userData)

      expect(localStorage.getItem('access_token')).toBe(token)
      expect(JSON.parse(localStorage.getItem('user_data'))).toEqual(userData)
    })
  })

  describe('clearAuthData', () => {
    it('should remove token and user data from localStorage', () => {
      localStorage.setItem('access_token', 'test-token')
      localStorage.setItem('user_data', JSON.stringify({ id: '123' }))

      authService.clearAuthData()

      expect(localStorage.getItem('access_token')).toBeNull()
      expect(localStorage.getItem('user_data')).toBeNull()
    })
  })

  describe('getToken', () => {
    it('should return token from localStorage', () => {
      const token = 'test-token'
      localStorage.setItem('access_token', token)

      expect(authService.getToken()).toBe(token)
    })

    it('should return null when no token exists', () => {
      expect(authService.getToken()).toBeNull()
    })
  })

  describe('getUserData', () => {
    it('should return parsed user data from localStorage', () => {
      const userData = { id: '123', email: 'test@example.com' }
      localStorage.setItem('user_data', JSON.stringify(userData))

      expect(authService.getUserData()).toEqual(userData)
    })

    it('should return null when no user data exists', () => {
      expect(authService.getUserData()).toBeNull()
    })

    it('should handle invalid JSON in localStorage', () => {
      localStorage.setItem('user_data', 'invalid-json')

      expect(() => authService.getUserData()).toThrow()
    })
  })

  describe('isAuthenticated', () => {
    it('should return true when token exists', () => {
      localStorage.setItem('access_token', 'test-token')

      expect(authService.isAuthenticated()).toBe(true)
    })

    it('should return false when token does not exist', () => {
      expect(authService.isAuthenticated()).toBe(false)
    })

    it('should return false when token is empty string', () => {
      localStorage.setItem('access_token', '')

      expect(authService.isAuthenticated()).toBe(false)
    })
  })
})
