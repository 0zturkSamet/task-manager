import { http, HttpResponse } from 'msw'
import { mockUser, mockProject, mockTask } from '../test-utils'

const API_BASE_URL = 'http://localhost:8080/api'

export const handlers = [
  // Auth endpoints
  http.post(`${API_BASE_URL}/auth/register`, async ({ request }) => {
    const body = await request.json()
    return HttpResponse.json({
      ...mockUser,
      email: body.email,
      firstName: body.firstName,
      lastName: body.lastName,
      token: 'mock-jwt-token',
      expiresIn: 3600000,
    }, { status: 201 })
  }),

  http.post(`${API_BASE_URL}/auth/login`, async ({ request }) => {
    const body = await request.json()
    if (body.email === 'test@example.com' && body.password === 'password123') {
      return HttpResponse.json({
        ...mockUser,
        token: 'mock-jwt-token',
        expiresIn: 3600000,
      })
    }
    return HttpResponse.json(
      { message: 'Invalid credentials' },
      { status: 401 }
    )
  }),

  http.post(`${API_BASE_URL}/auth/logout`, () => {
    return HttpResponse.json({ message: 'Logged out successfully' })
  }),

  // User endpoints
  http.get(`${API_BASE_URL}/users/profile`, () => {
    return HttpResponse.json(mockUser)
  }),

  http.put(`${API_BASE_URL}/users/profile`, async ({ request }) => {
    const body = await request.json()
    return HttpResponse.json({
      ...mockUser,
      ...body,
    })
  }),

  http.delete(`${API_BASE_URL}/users/account`, () => {
    return HttpResponse.json({ message: 'Account deleted successfully' })
  }),

  http.get(`${API_BASE_URL}/users/search`, ({ request }) => {
    const url = new URL(request.url)
    const query = url.searchParams.get('q')
    if (query === 'john') {
      return HttpResponse.json([mockUser])
    }
    return HttpResponse.json([])
  }),

  // Project endpoints
  http.post(`${API_BASE_URL}/projects`, async ({ request }) => {
    const body = await request.json()
    return HttpResponse.json({
      ...mockProject,
      ...body,
    }, { status: 201 })
  }),

  http.get(`${API_BASE_URL}/projects`, () => {
    return HttpResponse.json([mockProject])
  }),

  http.get(`${API_BASE_URL}/projects/:projectId`, ({ params }) => {
    return HttpResponse.json({
      ...mockProject,
      id: params.projectId,
    })
  }),

  http.put(`${API_BASE_URL}/projects/:projectId`, async ({ params, request }) => {
    const body = await request.json()
    return HttpResponse.json({
      ...mockProject,
      id: params.projectId,
      ...body,
    })
  }),

  http.delete(`${API_BASE_URL}/projects/:projectId`, () => {
    return HttpResponse.json({ message: 'Project deleted successfully' })
  }),

  http.get(`${API_BASE_URL}/projects/:projectId/members`, () => {
    return HttpResponse.json([
      {
        userId: mockUser.id,
        userEmail: mockUser.email,
        userName: `${mockUser.firstName} ${mockUser.lastName}`,
        role: 'OWNER',
        joinedAt: mockUser.createdAt,
      },
    ])
  }),

  http.post(`${API_BASE_URL}/projects/:projectId/members`, async ({ request }) => {
    const body = await request.json()
    return HttpResponse.json({
      userId: mockUser.id,
      userEmail: body.email,
      userName: `${mockUser.firstName} ${mockUser.lastName}`,
      role: body.role,
      joinedAt: new Date().toISOString(),
    }, { status: 201 })
  }),

  http.put(`${API_BASE_URL}/projects/:projectId/members/:memberId/role`, async ({ request }) => {
    const body = await request.json()
    return HttpResponse.json({
      userId: mockUser.id,
      userEmail: mockUser.email,
      userName: `${mockUser.firstName} ${mockUser.lastName}`,
      role: body.role,
      joinedAt: mockUser.createdAt,
    })
  }),

  http.delete(`${API_BASE_URL}/projects/:projectId/members/:memberId`, () => {
    return HttpResponse.json({ message: 'Member removed successfully' })
  }),

  // Task endpoints
  http.post(`${API_BASE_URL}/tasks`, async ({ request }) => {
    const body = await request.json()
    return HttpResponse.json({
      ...mockTask,
      ...body,
    }, { status: 201 })
  }),

  http.get(`${API_BASE_URL}/tasks`, ({ request }) => {
    const url = new URL(request.url)
    const projectId = url.searchParams.get('projectId')
    if (projectId) {
      return HttpResponse.json([{ ...mockTask, projectId }])
    }
    return HttpResponse.json([mockTask])
  }),

  http.get(`${API_BASE_URL}/tasks/:taskId`, ({ params }) => {
    return HttpResponse.json({
      ...mockTask,
      id: params.taskId,
    })
  }),

  http.put(`${API_BASE_URL}/tasks/:taskId`, async ({ params, request }) => {
    const body = await request.json()
    return HttpResponse.json({
      ...mockTask,
      id: params.taskId,
      ...body,
    })
  }),

  http.delete(`${API_BASE_URL}/tasks/:taskId`, () => {
    return HttpResponse.json({ message: 'Task deleted successfully' })
  }),

  http.get(`${API_BASE_URL}/tasks/statistics`, ({ request }) => {
    const url = new URL(request.url)
    const projectId = url.searchParams.get('projectId')
    return HttpResponse.json({
      totalTasks: 10,
      todoCount: 3,
      inProgressCount: 4,
      doneCount: 3,
      overdueCount: 1,
      highPriorityCount: 2,
      projectId: projectId || null,
    })
  }),

  http.get(`${API_BASE_URL}/tasks/:taskId/comments`, () => {
    return HttpResponse.json([])
  }),

  http.post(`${API_BASE_URL}/tasks/:taskId/comments`, async ({ request }) => {
    const body = await request.json()
    return HttpResponse.json({
      id: '123e4567-e89b-12d3-a456-426614174003',
      content: body.content,
      userId: mockUser.id,
      userName: `${mockUser.firstName} ${mockUser.lastName}`,
      createdAt: new Date().toISOString(),
    }, { status: 201 })
  }),
]
