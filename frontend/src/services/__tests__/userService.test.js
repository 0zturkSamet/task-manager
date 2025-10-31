import { describe, it, expect, beforeEach } from 'vitest'
import userService from '../userService'
import { server } from '../../test/mocks/server'
import { http, HttpResponse } from 'msw'
import { mockUser } from '../../test/test-utils'

describe('UserService', () => {
  const API_BASE_URL = 'http://localhost:8080/api'

  beforeEach(() => {
    localStorage.setItem('access_token', 'mock-token')
  })

  describe('getProfile', () => {
    it('should fetch user profile', async () => {
      const profile = await userService.getProfile()

      expect(profile).toHaveProperty('id')
      expect(profile).toHaveProperty('email')
      expect(profile).toHaveProperty('firstName')
      expect(profile).toHaveProperty('lastName')
    })

    it('should handle not found error', async () => {
      server.use(
        http.get(`${API_BASE_URL}/users/profile`, () => {
          return HttpResponse.json(
            { message: 'User not found' },
            { status: 404 }
          )
        })
      )

      await expect(userService.getProfile()).rejects.toThrow()
    })

    it('should handle unauthorized error', async () => {
      server.use(
        http.get(`${API_BASE_URL}/users/profile`, () => {
          return HttpResponse.json(
            { message: 'Unauthorized' },
            { status: 401 }
          )
        })
      )

      await expect(userService.getProfile()).rejects.toThrow()
    })
  })

  describe('updateProfile', () => {
    it('should update user profile', async () => {
      const updateData = {
        firstName: 'Jane',
        lastName: 'Smith',
      }

      const updatedProfile = await userService.updateProfile(updateData)

      expect(updatedProfile).toHaveProperty('firstName', 'Jane')
      expect(updatedProfile).toHaveProperty('lastName', 'Smith')
    })

    it('should update only provided fields', async () => {
      const updateData = {
        firstName: 'Jane',
      }

      const updatedProfile = await userService.updateProfile(updateData)

      expect(updatedProfile).toHaveProperty('firstName', 'Jane')
      expect(updatedProfile).toHaveProperty('email') // Other fields should remain
    })

    it('should handle validation errors', async () => {
      server.use(
        http.put(`${API_BASE_URL}/users/profile`, () => {
          return HttpResponse.json(
            { message: 'Invalid data' },
            { status: 400 }
          )
        })
      )

      await expect(userService.updateProfile({ firstName: 'J' })).rejects.toThrow()
    })
  })

  describe('deleteAccount', () => {
    it('should delete user account', async () => {
      const result = await userService.deleteAccount()

      expect(result).toHaveProperty('message')
    })

    it('should handle not found error', async () => {
      server.use(
        http.delete(`${API_BASE_URL}/users/account`, () => {
          return HttpResponse.json(
            { message: 'User not found' },
            { status: 404 }
          )
        })
      )

      await expect(userService.deleteAccount()).rejects.toThrow()
    })

    it('should handle unauthorized error', async () => {
      server.use(
        http.delete(`${API_BASE_URL}/users/account`, () => {
          return HttpResponse.json(
            { message: 'Unauthorized' },
            { status: 401 }
          )
        })
      )

      await expect(userService.deleteAccount()).rejects.toThrow()
    })
  })

  describe('searchUsers', () => {
    it('should search users by query', async () => {
      const users = await userService.searchUsers('john')

      expect(Array.isArray(users)).toBe(true)
      expect(users.length).toBeGreaterThan(0)
    })

    it('should return empty array for no matches', async () => {
      server.use(
        http.get(`${API_BASE_URL}/users/search`, () => {
          return HttpResponse.json([])
        })
      )

      const users = await userService.searchUsers('nonexistent')

      expect(Array.isArray(users)).toBe(true)
      expect(users.length).toBe(0)
    })

    it('should encode search term properly', async () => {
      const searchTerm = 'john@example.com'
      const users = await userService.searchUsers(searchTerm)

      expect(Array.isArray(users)).toBe(true)
    })

    it('should handle special characters in search', async () => {
      const specialChars = 'john+doe@example.com'
      const users = await userService.searchUsers(specialChars)

      expect(Array.isArray(users)).toBe(true)
    })

    it('should handle unauthorized error', async () => {
      server.use(
        http.get(`${API_BASE_URL}/users/search`, () => {
          return HttpResponse.json(
            { message: 'Unauthorized' },
            { status: 401 }
          )
        })
      )

      await expect(userService.searchUsers('john')).rejects.toThrow()
    })
  })
})
