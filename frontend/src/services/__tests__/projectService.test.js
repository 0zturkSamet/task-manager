import { describe, it, expect, beforeEach } from 'vitest'
import projectService from '../projectService'
import { server } from '../../test/mocks/server'
import { http, HttpResponse } from 'msw'
import { mockProject, mockUser } from '../../test/test-utils'

describe('ProjectService', () => {
  const API_BASE_URL = 'http://localhost:8080/api'

  beforeEach(() => {
    localStorage.setItem('access_token', 'mock-token')
  })

  describe('getAllProjects', () => {
    it('should fetch all user projects', async () => {
      const projects = await projectService.getAllProjects()

      expect(Array.isArray(projects)).toBe(true)
      expect(projects.length).toBeGreaterThan(0)
    })

    it('should return empty array when user has no projects', async () => {
      server.use(
        http.get(`${API_BASE_URL}/projects`, () => {
          return HttpResponse.json([])
        })
      )

      const projects = await projectService.getAllProjects()

      expect(Array.isArray(projects)).toBe(true)
      expect(projects.length).toBe(0)
    })

    it('should handle unauthorized error', async () => {
      server.use(
        http.get(`${API_BASE_URL}/projects`, () => {
          return HttpResponse.json(
            { message: 'Unauthorized' },
            { status: 401 }
          )
        })
      )

      await expect(projectService.getAllProjects()).rejects.toThrow()
    })
  })

  describe('getProjectById', () => {
    it('should fetch project by ID', async () => {
      const projectId = mockProject.id
      const project = await projectService.getProjectById(projectId)

      expect(project).toHaveProperty('id')
      expect(project).toHaveProperty('name')
      expect(project).toHaveProperty('description')
    })

    it('should handle not found error', async () => {
      const projectId = 'non-existent-id'
      server.use(
        http.get(`${API_BASE_URL}/projects/${projectId}`, () => {
          return HttpResponse.json(
            { message: 'Project not found' },
            { status: 404 }
          )
        })
      )

      await expect(projectService.getProjectById(projectId)).rejects.toThrow()
    })

    it('should handle forbidden error', async () => {
      const projectId = mockProject.id
      server.use(
        http.get(`${API_BASE_URL}/projects/${projectId}`, () => {
          return HttpResponse.json(
            { message: 'Access denied' },
            { status: 403 }
          )
        })
      )

      await expect(projectService.getProjectById(projectId)).rejects.toThrow()
    })
  })

  describe('createProject', () => {
    it('should create a new project', async () => {
      const projectData = {
        name: 'New Project',
        description: 'Project description',
        color: '#FF0000',
      }

      const project = await projectService.createProject(projectData)

      expect(project).toHaveProperty('id')
      expect(project).toHaveProperty('name', projectData.name)
      expect(project).toHaveProperty('description', projectData.description)
    })

    it('should handle validation errors', async () => {
      server.use(
        http.post(`${API_BASE_URL}/projects`, () => {
          return HttpResponse.json(
            { message: 'Invalid project data' },
            { status: 400 }
          )
        })
      )

      const invalidData = {
        name: 'AB', // Too short
      }

      await expect(projectService.createProject(invalidData)).rejects.toThrow()
    })
  })

  describe('updateProject', () => {
    it('should update existing project', async () => {
      const projectId = mockProject.id
      const updateData = {
        name: 'Updated Project',
        description: 'Updated description',
      }

      const project = await projectService.updateProject(projectId, updateData)

      expect(project).toHaveProperty('name', updateData.name)
      expect(project).toHaveProperty('description', updateData.description)
    })

    it('should handle not found error', async () => {
      const projectId = 'non-existent-id'
      server.use(
        http.put(`${API_BASE_URL}/projects/${projectId}`, () => {
          return HttpResponse.json(
            { message: 'Project not found' },
            { status: 404 }
          )
        })
      )

      await expect(projectService.updateProject(projectId, {})).rejects.toThrow()
    })

    it('should handle forbidden error for non-owner', async () => {
      const projectId = mockProject.id
      server.use(
        http.put(`${API_BASE_URL}/projects/${projectId}`, () => {
          return HttpResponse.json(
            { message: 'Only owner can update project' },
            { status: 403 }
          )
        })
      )

      await expect(projectService.updateProject(projectId, {})).rejects.toThrow()
    })
  })

  describe('deleteProject', () => {
    it('should delete project', async () => {
      const projectId = mockProject.id
      const result = await projectService.deleteProject(projectId)

      expect(result).toHaveProperty('message')
    })

    it('should handle not found error', async () => {
      const projectId = 'non-existent-id'
      server.use(
        http.delete(`${API_BASE_URL}/projects/${projectId}`, () => {
          return HttpResponse.json(
            { message: 'Project not found' },
            { status: 404 }
          )
        })
      )

      await expect(projectService.deleteProject(projectId)).rejects.toThrow()
    })

    it('should handle forbidden error for non-owner', async () => {
      const projectId = mockProject.id
      server.use(
        http.delete(`${API_BASE_URL}/projects/${projectId}`, () => {
          return HttpResponse.json(
            { message: 'Only owner can delete project' },
            { status: 403 }
          )
        })
      )

      await expect(projectService.deleteProject(projectId)).rejects.toThrow()
    })
  })

  describe('getProjectMembers', () => {
    it('should fetch project members', async () => {
      const projectId = mockProject.id
      const members = await projectService.getProjectMembers(projectId)

      expect(Array.isArray(members)).toBe(true)
      expect(members.length).toBeGreaterThan(0)
      expect(members[0]).toHaveProperty('userId')
      expect(members[0]).toHaveProperty('userEmail')
      expect(members[0]).toHaveProperty('role')
    })

    it('should handle not found error', async () => {
      const projectId = 'non-existent-id'
      server.use(
        http.get(`${API_BASE_URL}/projects/${projectId}/members`, () => {
          return HttpResponse.json(
            { message: 'Project not found' },
            { status: 404 }
          )
        })
      )

      await expect(projectService.getProjectMembers(projectId)).rejects.toThrow()
    })
  })

  describe('addMember', () => {
    it('should add member to project', async () => {
      const projectId = mockProject.id
      const memberData = {
        email: 'newmember@example.com',
        role: 'MEMBER',
      }

      const member = await projectService.addMember(projectId, memberData)

      expect(member).toHaveProperty('userEmail', memberData.email)
      expect(member).toHaveProperty('role', memberData.role)
    })

    it('should handle user not found error', async () => {
      const projectId = mockProject.id
      server.use(
        http.post(`${API_BASE_URL}/projects/${projectId}/members`, () => {
          return HttpResponse.json(
            { message: 'User not found' },
            { status: 404 }
          )
        })
      )

      await expect(projectService.addMember(projectId, { email: 'nonexistent@example.com' })).rejects.toThrow()
    })

    it('should handle already member error', async () => {
      const projectId = mockProject.id
      server.use(
        http.post(`${API_BASE_URL}/projects/${projectId}/members`, () => {
          return HttpResponse.json(
            { message: 'User is already a member' },
            { status: 409 }
          )
        })
      )

      await expect(projectService.addMember(projectId, { email: 'existing@example.com' })).rejects.toThrow()
    })
  })

  describe('updateMemberRole', () => {
    it('should update member role', async () => {
      const projectId = mockProject.id
      const memberId = mockUser.id
      const newRole = 'ADMIN'

      const member = await projectService.updateMemberRole(projectId, memberId, newRole)

      expect(member).toHaveProperty('role', newRole)
    })

    it('should handle not found error', async () => {
      const projectId = mockProject.id
      const memberId = 'non-existent-id'
      server.use(
        http.put(`${API_BASE_URL}/projects/${projectId}/members/${memberId}/role`, () => {
          return HttpResponse.json(
            { message: 'Member not found' },
            { status: 404 }
          )
        })
      )

      await expect(projectService.updateMemberRole(projectId, memberId, 'ADMIN')).rejects.toThrow()
    })

    it('should handle forbidden error for non-owner', async () => {
      const projectId = mockProject.id
      const memberId = mockUser.id
      server.use(
        http.put(`${API_BASE_URL}/projects/${projectId}/members/${memberId}/role`, () => {
          return HttpResponse.json(
            { message: 'Only owner can update roles' },
            { status: 403 }
          )
        })
      )

      await expect(projectService.updateMemberRole(projectId, memberId, 'ADMIN')).rejects.toThrow()
    })
  })

  describe('removeMember', () => {
    it('should remove member from project', async () => {
      const projectId = mockProject.id
      const memberId = mockUser.id

      const result = await projectService.removeMember(projectId, memberId)

      expect(result).toHaveProperty('message')
    })

    it('should handle not found error', async () => {
      const projectId = mockProject.id
      const memberId = 'non-existent-id'
      server.use(
        http.delete(`${API_BASE_URL}/projects/${projectId}/members/${memberId}`, () => {
          return HttpResponse.json(
            { message: 'Member not found' },
            { status: 404 }
          )
        })
      )

      await expect(projectService.removeMember(projectId, memberId)).rejects.toThrow()
    })

    it('should handle forbidden error for non-owner', async () => {
      const projectId = mockProject.id
      const memberId = mockUser.id
      server.use(
        http.delete(`${API_BASE_URL}/projects/${projectId}/members/${memberId}`, () => {
          return HttpResponse.json(
            { message: 'Only owner can remove members' },
            { status: 403 }
          )
        })
      )

      await expect(projectService.removeMember(projectId, memberId)).rejects.toThrow()
    })
  })
})
