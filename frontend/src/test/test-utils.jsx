import { render } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import { AuthProvider } from '../context/AuthContext'

/**
 * Custom render function that wraps components with necessary providers
 */
export function renderWithProviders(
  ui,
  {
    route = '/',
    authValue = null,
    ...renderOptions
  } = {}
) {
  window.history.pushState({}, 'Test page', route)

  const AllProviders = ({ children }) => {
    return (
      <BrowserRouter>
        <AuthProvider value={authValue}>
          {children}
        </AuthProvider>
      </BrowserRouter>
    )
  }

  return render(ui, { wrapper: AllProviders, ...renderOptions })
}

/**
 * Custom render function with only AuthProvider
 */
export function renderWithAuth(ui, { authValue = null, route = '/', ...renderOptions } = {}) {
  window.history.pushState({}, 'Test page', route)

  const Wrapper = ({ children }) => (
    <BrowserRouter>
      <AuthProvider value={authValue}>
        {children}
      </AuthProvider>
    </BrowserRouter>
  )

  return render(ui, { wrapper: Wrapper, ...renderOptions })
}

/**
 * Custom render function with Router only
 */
export function renderWithRouter(ui, { route = '/', ...renderOptions } = {}) {
  window.history.pushState({}, 'Test page', route)

  const Wrapper = ({ children }) => (
    <BrowserRouter>
      {children}
    </BrowserRouter>
  )

  return render(ui, { wrapper: Wrapper, ...renderOptions })
}

/**
 * Mock user data for testing
 */
export const mockUser = {
  id: '123e4567-e89b-12d3-a456-426614174000',
  email: 'test@example.com',
  firstName: 'John',
  lastName: 'Doe',
  profileImage: null,
  isActive: true,
  createdAt: '2024-01-01T00:00:00',
}

/**
 * Mock project data for testing
 */
export const mockProject = {
  id: '123e4567-e89b-12d3-a456-426614174001',
  name: 'Test Project',
  description: 'Test project description',
  color: '#FF0000',
  ownerId: mockUser.id,
  members: [],
  createdAt: '2024-01-01T00:00:00',
  updatedAt: '2024-01-01T00:00:00',
}

/**
 * Mock task data for testing
 */
export const mockTask = {
  id: '123e4567-e89b-12d3-a456-426614174002',
  title: 'Test Task',
  description: 'Test task description',
  status: 'TODO',
  priority: 'MEDIUM',
  dueDate: '2024-12-31T00:00:00',
  projectId: mockProject.id,
  projectName: mockProject.name,
  assigneeId: mockUser.id,
  assigneeName: `${mockUser.firstName} ${mockUser.lastName}`,
  createdByUserId: mockUser.id,
  createdByUserName: `${mockUser.firstName} ${mockUser.lastName}`,
  commentCount: 0,
  createdAt: '2024-01-01T00:00:00',
  updatedAt: '2024-01-01T00:00:00',
}

/**
 * Wait for async updates
 */
export const waitForLoadingToFinish = () => {
  return new Promise(resolve => setTimeout(resolve, 0))
}

// Re-export everything from @testing-library/react
export * from '@testing-library/react'
