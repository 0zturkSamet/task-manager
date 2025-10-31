# Task Manager - Full Stack Application

> A modern, production-ready task management system with team collaboration, real-time updates, and comprehensive project tracking capabilities.

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen?style=flat&logo=spring)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-blue?style=flat&logo=react)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue?style=flat&logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Status**: ✅ **COMPLETE** - Backend + Frontend Fully Implemented
**Version**: 1.0.0
**Developer**: Samet Ozturk
**Last Updated**: October 31, 2025

**Note:** Built with modern development tools including Claude Code for AI-assisted development.

---

## 🌐 Live Demo

**Application URL:** [https://taskify-maskify.netlify.app](https://taskify-maskify.netlify.app)

**Demo Credentials:**
```
Create your own account or use demo credentials if provided
```

**Note:** First load may take ~30 seconds as the free-tier backend wakes from sleep (Render.com free tier limitation).

**API Documentation (Swagger):** [https://task-manager-backend-5pwe.onrender.com/swagger-ui.html](https://task-manager-backend-5pwe.onrender.com/swagger-ui.html)

---

## 🚀 Quick Start for Recruiters

Want to see this project in action?

1. **Visit the live demo** (link above)
2. **Login with demo credentials** (provided above)
3. **Explore features:**
   - View Dashboard with statistics
   - Browse sample projects
   - Try the Kanban drag-and-drop board
   - Create and manage tasks
   - Add comments to tasks

**Total time:** 5 minutes to see everything!

---

## 📋 Table of Contents

- [Executive Summary](#executive-summary)
- [Tech Stack](#tech-stack)
- [System Architecture](#system-architecture)
- [Features](#features)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Frontend Architecture](#frontend-architecture)
- [Installation Guide](#installation-guide)
- [Development Workflow](#development-workflow)
- [Testing](#testing)
- [Deployment](#deployment)
- [Project Metrics](#project-metrics)

---

## 🎯 Executive Summary

**Task Manager** is a full-stack web application designed for modern team collaboration and project management. It provides a complete suite of features including:

- 🔐 **Secure Authentication** - JWT-based auth with role-based access control
- 📊 **Real-time Dashboard** - Statistics and insights across all projects
- 📋 **Kanban Board** - Drag-and-drop task management with visual workflow
- 👥 **Team Collaboration** - Multi-user projects with role-based permissions
- ⏱️ **Time Tracking** - Estimated vs actual hours for tasks
- 💬 **Task Comments** - Team communication and activity tracking
- 🎨 **Modern UI** - Responsive design built with React and Tailwind CSS

### Key Highlights
- **30+ RESTful API endpoints** with comprehensive Swagger documentation
- **Clean Architecture** - Separation of concerns with layered backend design
- **Type Safety** - Java 17 with strong typing and validation
- **Modern Frontend** - React 19 with hooks, context API, and component-based architecture
- **Database Migrations** - Flyway for version-controlled schema management
- **Production Ready** - Built with security, scalability, and maintainability in mind

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|-----------|---------|---------|
| **Java** | 17 | Programming language |
| **Spring Boot** | 3.3.3 | Application framework |
| **Spring Security** | 6.x | Authentication & authorization |
| **Spring Data JPA** | 3.x | Data access layer |
| **Hibernate** | 6.x | ORM implementation |
| **PostgreSQL** | 14+ | Relational database |
| **Flyway** | 9.x | Database migrations |
| **JWT (jjwt)** | 0.12.3 | Token-based authentication |
| **Springdoc OpenAPI** | 2.3.0 | API documentation (Swagger) |
| **Lombok** | 1.18.34 | Boilerplate code reduction |
| **Maven** | 3.9.11 | Build & dependency management |
| **BCrypt** | - | Password hashing |

### Frontend
| Technology | Version | Purpose |
|-----------|---------|---------|
| **React** | 19.1.1 | UI library |
| **Vite** | 7.1.7 | Build tool & dev server |
| **Tailwind CSS** | 3.4.18 | Utility-first CSS framework |
| **React Router** | 7.9.4 | Client-side routing |
| **Axios** | 1.12.2 | HTTP client |
| **@dnd-kit** | 6.3.1 | Drag & drop functionality |
| **Lucide React** | 0.546.0 | Icon library |
| **React Hook Form** | 7.65.0 | Form handling |
| **Vitest** | 3.2.4 | Testing framework |
| **MSW** | 2.11.5 | API mocking for tests |

### Development Tools
- **VS Code / IntelliJ IDEA** - IDEs
- **Postman** - API testing
- **Git** - Version control
- **ESLint** - Code linting
- **Prettier** - Code formatting

---

## 🏗 System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │            React Application (Port 5173)             │   │
│  │  • Pages (Dashboard, Projects, Tasks, Kanban)       │   │
│  │  • Components (Reusable UI elements)                 │   │
│  │  • Context API (Global state management)            │   │
│  │  • Axios (HTTP client with interceptors)            │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              ↓ HTTP/REST
┌─────────────────────────────────────────────────────────────┐
│                        API GATEWAY                           │
│                    (Port 8080 - CORS enabled)                │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      SECURITY LAYER                          │
│  ┌─────────────────────────────────────────────────────┐   │
│  │     JWT Authentication Filter                        │   │
│  │  • Token validation                                  │   │
│  │  • User authentication                               │   │
│  │  • Role-based authorization                          │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                     CONTROLLER LAYER                         │
│  • AuthController      • TaskController                      │
│  • UserController      • ProjectController                   │
│  ↓ Request validation & DTO mapping                         │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                           │
│  • Business logic      • Data validation                     │
│  • Authorization       • Transaction management              │
│  ↓ Domain operations                                         │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                          │
│  • Spring Data JPA repositories                              │
│  • Custom queries                                            │
│  ↓ Database operations                                       │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                              │
│              PostgreSQL Database (Supabase)                  │
│  • Users • Projects • Tasks • Comments • Members             │
└─────────────────────────────────────────────────────────────┘
```

### Backend Package Structure

```
com.taskmanager/
├── config/
│   ├── SecurityConfig.java          # Security configuration
│   ├── JwtAuthenticationFilter.java # JWT filter
│   ├── CorsConfig.java              # CORS settings
│   └── OpenApiConfig.java           # Swagger config
├── controller/
│   ├── AuthController.java          # Auth endpoints
│   ├── UserController.java          # User management
│   ├── ProjectController.java       # Project CRUD
│   └── TaskController.java          # Task operations
├── dto/
│   ├── request/  (20+ DTOs)         # API request objects
│   └── response/ (15+ DTOs)         # API response objects
├── entity/
│   ├── User.java
│   ├── Project.java
│   ├── ProjectMember.java
│   ├── Task.java
│   ├── TaskComment.java
│   └── enums/ (Status, Priority, Role)
├── repository/
│   ├── UserRepository.java
│   ├── ProjectRepository.java
│   ├── TaskRepository.java
│   ├── ProjectMemberRepository.java
│   └── TaskCommentRepository.java
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── ProjectService.java
│   └── TaskService.java
├── security/
│   └── JwtService.java              # Token generation/validation
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── ForbiddenException.java
└── TaskManagerApplication.java      # Main class
```

---

## ✨ Features

### 1. Authentication & User Management ✅

#### Features
- User registration with email validation
- Secure login with JWT token generation
- Password encryption using BCrypt (strength 12)
- Token-based authentication (24-hour expiry)
- User profile management (update name, profile image)
- Soft delete for user accounts
- User search functionality

#### Technical Implementation
- JWT stored in localStorage on client
- Axios interceptor for automatic token attachment
- Protected routes with PrivateRoute component
- Secure password validation (min 8 characters)

---

### 2. Project Management ✅

#### Features
- Create unlimited projects with custom colors
- Update project details (name, description, color)
- Soft delete projects
- Invite team members via email
- Role-based access control:
  - **OWNER**: Full control (create/edit/delete project, manage members)
  - **ADMIN**: Edit project, manage tasks, add members
  - **VIEWER**: Read-only access
- View all projects user is member of
- Project-level statistics

#### Permissions Matrix
| Action | Owner | Admin | Viewer |
|--------|-------|-------|--------|
| View project | ✅ | ✅ | ✅ |
| Edit project | ✅ | ✅ | ❌ |
| Delete project | ✅ | ❌ | ❌ |
| Add members | ✅ | ✅ | ❌ |
| Remove members | ✅ | ✅ | ❌ |
| Change member roles | ✅ | ❌ | ❌ |
| Create tasks | ✅ | ✅ | ❌ |
| Edit tasks | ✅ | ✅ | ❌ |
| Delete tasks | ✅ | ✅* | ❌ |

*Admin can only delete own tasks

---

### 3. Task Management ✅

#### Features
- Create tasks with title, description, priority, due date
- Assign tasks to project members
- Task status workflow: `TODO → IN_PROGRESS → IN_REVIEW → DONE`
- Priority levels: `LOW → MEDIUM → HIGH → URGENT`
- Time tracking (estimated hours vs actual hours)
- Due date tracking with overdue indicators
- Task comments for team collaboration
- Advanced filtering:
  - By status, priority, assignee, creator
  - By due date range
  - By text search (title + description)
  - Overdue tasks filter
- Task position for Kanban ordering
- Soft delete with is_active flag

#### Task Lifecycle
```
TODO → IN_PROGRESS → IN_REVIEW → DONE
  ↓
CANCELLED (alternative endpoint)
```

---

### 4. Dashboard with Statistics ✅

#### Features
- **Overview Cards**:
  - Total Projects count
  - Total Tasks count
  - In Progress tasks count
  - Overdue tasks count

- **Visual Statistics**:
  - Tasks by Status (bar chart)
  - Tasks by Priority (bar chart)

- **Recent Activity**:
  - 5 most recent projects
  - 5 most recent tasks

- **Advanced Metrics**:
  - Completion rate (%)
  - My tasks completion rate (%)
  - Time tracking summary
  - Tasks assigned to me vs created by me

#### Technical Implementation
- `GET /api/users/statistics` endpoint
- Real-time calculation from database
- Reusable dashboard components
- Responsive grid layout

---

### 5. Kanban Board with Drag & Drop ✅

#### Features
- Visual board with 4 columns: TODO, IN_PROGRESS, IN_REVIEW, DONE
- Drag and drop tasks between columns
- Real-time status updates
- Task cards display:
  - Title & description
  - Priority badge
  - Assigned user
  - Due date
  - Overdue indicator
- Column task counts
- Smooth animations and transitions
- Mobile-responsive design

#### Technical Implementation
- Built with `@dnd-kit/core` for accessibility
- `@dnd-kit/sortable` for list sorting
- Optimistic UI updates
- Server sync on drop
- Touch device support

---

### 6. Comments & Collaboration ✅

#### Features
- Add comments to any task
- View comment history
- User attribution (name + timestamp)
- Chronological ordering
- Comment count on tasks

---

## 📡 API Documentation

### Base URL
```
Development: http://localhost:8080/api
Production: https://your-domain.com/api
```

### Authentication
All protected endpoints require JWT token in Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

---

### Authentication Endpoints

#### POST `/api/auth/register`
Register a new user account.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:** `201 Created`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

---

#### POST `/api/auth/login`
Login with email and password.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

---

### User Management Endpoints

#### GET `/api/users/profile`
Get current user profile. **[Protected]**

**Response:** `200 OK`
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "profileImage": "https://...",
  "createdAt": "2025-10-01T10:00:00",
  "isActive": true
}
```

---

#### GET `/api/users/statistics`
Get comprehensive user statistics. **[Protected]**

**Response:** `200 OK`
```json
{
  "totalProjects": 5,
  "ownedProjects": 3,
  "memberProjects": 2,
  "totalTasks": 45,
  "todoCount": 10,
  "inProgressCount": 8,
  "inReviewCount": 5,
  "doneCount": 20,
  "cancelledCount": 2,
  "lowPriorityCount": 15,
  "mediumPriorityCount": 20,
  "highPriorityCount": 8,
  "urgentPriorityCount": 2,
  "overdueCount": 3,
  "dueTodayCount": 2,
  "dueThisWeekCount": 7,
  "unassignedCount": 5,
  "assignedToMeCount": 12,
  "createdByMeCount": 30,
  "completionRate": 44.44,
  "myTasksCompletionRate": 58.33,
  "totalEstimatedHours": 150.5,
  "totalActualHours": 142.75,
  "myTasksEstimatedHours": 45.0,
  "myTasksActualHours": 38.5
}
```

---

#### PUT `/api/users/profile`
Update user profile. **[Protected]**

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "profileImage": "https://..."
}
```

---

#### GET `/api/users/search?q={searchTerm}`
Search users by email, first name, or last name. **[Protected]**

---

### Project Management Endpoints

#### POST `/api/projects`
Create a new project. **[Protected]**

**Request Body:**
```json
{
  "name": "Website Redesign",
  "description": "Q4 website redesign project",
  "color": "#3B82F6"
}
```

**Response:** `201 Created`
```json
{
  "id": "uuid",
  "name": "Website Redesign",
  "description": "Q4 website redesign project",
  "color": "#3B82F6",
  "ownerId": "uuid",
  "ownerName": "John Doe",
  "memberCount": 1,
  "taskCount": 0,
  "createdAt": "2025-10-19T10:00:00"
}
```

---

#### GET `/api/projects`
Get all projects user has access to. **[Protected]**

---

#### GET `/api/projects/{id}`
Get project details. **[Protected]**

---

#### PUT `/api/projects/{id}`
Update project. **[Protected - Owner/Admin only]**

---

#### DELETE `/api/projects/{id}`
Soft delete project. **[Protected - Owner only]**

---

#### GET `/api/projects/{id}/members`
Get all project members with roles. **[Protected]**

**Response:** `200 OK`
```json
[
  {
    "id": "uuid",
    "userId": "uuid",
    "userName": "John Doe",
    "userEmail": "john@example.com",
    "role": "OWNER",
    "joinedAt": "2025-10-19T10:00:00"
  }
]
```

---

#### POST `/api/projects/{id}/members`
Add member to project. **[Protected - Owner/Admin only]**

**Request Body:**
```json
{
  "email": "newmember@example.com",
  "role": "ADMIN"
}
```

---

#### PUT `/api/projects/{projectId}/members/{memberId}/role`
Update member role. **[Protected - Owner only]**

**Request Body:**
```json
{
  "role": "VIEWER"
}
```

---

#### DELETE `/api/projects/{projectId}/members/{memberId}`
Remove member from project. **[Protected - Owner/Admin only]**

---

### Task Management Endpoints

#### POST `/api/tasks`
Create a new task. **[Protected - Owner/Admin only]**

**Request Body:**
```json
{
  "title": "Design homepage mockup",
  "description": "Create high-fidelity mockups",
  "projectId": "uuid",
  "assignedToId": "uuid",
  "priority": "HIGH",
  "status": "TODO",
  "estimatedHours": 8.0,
  "dueDate": "2025-10-25T17:00:00",
  "position": 0
}
```

**Response:** `201 Created`
```json
{
  "id": "uuid",
  "title": "Design homepage mockup",
  "description": "Create high-fidelity mockups",
  "status": "TODO",
  "priority": "HIGH",
  "projectId": "uuid",
  "projectName": "Website Redesign",
  "assignedToId": "uuid",
  "assignedToName": "Jane Smith",
  "assignedToEmail": "jane@example.com",
  "createdByUserId": "uuid",
  "createdByUserName": "John Doe",
  "estimatedHours": 8.0,
  "actualHours": null,
  "dueDate": "2025-10-25T17:00:00",
  "completedAt": null,
  "position": 0,
  "isOverdue": false,
  "commentCount": 0,
  "createdAt": "2025-10-19T10:00:00",
  "updatedAt": "2025-10-19T10:00:00"
}
```

---

#### GET `/api/tasks`
Get all tasks across all projects user has access to. **[Protected]**

---

#### GET `/api/projects/{projectId}/tasks`
Get all tasks for a specific project. **[Protected]**

---

#### GET `/api/tasks/{id}`
Get task details. **[Protected]**

---

#### PUT `/api/tasks/{id}`
Update task. **[Protected - Owner/Admin only]**

**Request Body:**
```json
{
  "title": "Updated title",
  "status": "IN_PROGRESS",
  "priority": "URGENT",
  "actualHours": 4.5,
  "position": 1
}
```

---

#### DELETE `/api/tasks/{id}`
Soft delete task. **[Protected - Owner/Admin/Creator only]**

---

#### POST `/api/tasks/filter`
Filter tasks with multiple criteria. **[Protected]**

**Request Body:**
```json
{
  "projectId": "uuid",
  "statuses": ["TODO", "IN_PROGRESS"],
  "priorities": ["HIGH", "URGENT"],
  "assignedToId": "uuid",
  "createdByUserId": "uuid",
  "dueDateFrom": "2025-10-01T00:00:00",
  "dueDateTo": "2025-10-31T23:59:59",
  "searchText": "design",
  "overdue": true
}
```

---

#### GET `/api/projects/{projectId}/tasks/statistics`
Get comprehensive task statistics for a project. **[Protected]**

**Response:** Similar to user statistics but project-specific.

---

#### POST `/api/tasks/{taskId}/comments`
Add comment to task. **[Protected]**

**Request Body:**
```json
{
  "commentText": "Great work on this task!"
}
```

---

#### GET `/api/tasks/{taskId}/comments`
Get all comments for a task. **[Protected]**

---

### Error Responses

All endpoints return consistent error responses:

**400 Bad Request** - Validation error
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email is required",
    "password": "Password must be at least 8 characters"
  }
}
```

**401 Unauthorized** - Authentication required
```json
{
  "status": 401,
  "message": "Authentication required"
}
```

**403 Forbidden** - Insufficient permissions
```json
{
  "status": 403,
  "message": "You don't have permission to perform this action"
}
```

**404 Not Found** - Resource not found
```json
{
  "status": 404,
  "message": "Project not found"
}
```

---

## 💾 Database Schema

### Entity Relationship Diagram

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│     USERS       │         │  PROJECT_MEMBERS │         │    PROJECTS     │
├─────────────────┤         ├──────────────────┤         ├─────────────────┤
│ id (PK)         │◄───────┤ user_id (FK)     │────────►│ id (PK)         │
│ email (UNIQUE)  │         │ project_id (FK)  │◄────────│ owner_id (FK)   │
│ password        │         │ role (ENUM)      │         │ name            │
│ first_name      │         │ joined_at        │         │ description     │
│ last_name       │         └──────────────────┘         │ color           │
│ profile_image   │                                       │ is_active       │
│ is_active       │                                       │ created_at      │
│ created_at      │                                       │ updated_at      │
│ updated_at      │                                       └─────────────────┘
└─────────────────┘                                                │
        │                                                           │
        │                                                           │
        │         ┌──────────────────┐                            │
        └────────►│      TASKS       │◄───────────────────────────┘
                  ├──────────────────┤
                  │ id (PK)          │
                  │ title            │
                  │ description      │
                  │ status (ENUM)    │
                  │ priority (ENUM)  │
                  │ project_id (FK)  │
                  │ assigned_to_id   │◄────┐
                  │ created_by_id    │     │ (FK to USERS)
                  │ estimated_hours  │     │
                  │ actual_hours     │     │
                  │ due_date         │     │
                  │ completed_at     │     │
                  │ position         │     │
                  │ is_active        │     │
                  │ created_at       │     │
                  │ updated_at       │     │
                  └──────────────────┘     │
                           │               │
                           │               │
                           ▼               │
                  ┌──────────────────┐    │
                  │  TASK_COMMENTS   │    │
                  ├──────────────────┤    │
                  │ id (PK)          │    │
                  │ task_id (FK)     │    │
                  │ user_id (FK)     │────┘
                  │ comment_text     │
                  │ created_at       │
                  │ updated_at       │
                  └──────────────────┘
```

### Table Definitions

#### USERS Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    profile_image VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_is_active ON users(is_active);
```

#### PROJECTS Table
```sql
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    color VARCHAR(20) NOT NULL DEFAULT '#3B82F6',
    owner_id UUID NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE INDEX idx_projects_owner_id ON projects(owner_id);
CREATE INDEX idx_projects_is_active ON projects(is_active);
```

#### PROJECT_MEMBERS Table
```sql
CREATE TABLE project_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'ADMIN', 'VIEWER')),
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (project_id, user_id)
);

CREATE INDEX idx_project_members_project_id ON project_members(project_id);
CREATE INDEX idx_project_members_user_id ON project_members(user_id);
```

#### TASKS Table
```sql
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO'
        CHECK (status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'CANCELLED')),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM'
        CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    project_id UUID NOT NULL,
    assigned_to_id UUID,
    created_by_user_id UUID NOT NULL,
    estimated_hours DECIMAL(5,2),
    actual_hours DECIMAL(5,2),
    due_date TIMESTAMP,
    completed_at TIMESTAMP,
    position INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to_id) REFERENCES users(id),
    FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_tasks_project_id ON tasks(project_id);
CREATE INDEX idx_tasks_assigned_to_id ON tasks(assigned_to_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_priority ON tasks(priority);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_is_active ON tasks(is_active);
```

#### TASK_COMMENTS Table
```sql
CREATE TABLE task_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_task_comments_task_id ON task_comments(task_id);
CREATE INDEX idx_task_comments_user_id ON task_comments(user_id);
```

### Flyway Migrations
```
V1__Create_User_Table.sql
V2__Create_Projects_Tables.sql
V3__Create_Tasks_Tables.sql
V4__Add_Project_Members_And_Task_Assignment.sql
```

---

## 🎨 Frontend Architecture

### Project Structure

```
frontend/
├── public/                         # Static assets
├── src/
│   ├── components/
│   │   ├── auth/
│   │   │   └── PrivateRoute.jsx   # Protected route wrapper
│   │   ├── common/                 # Reusable components
│   │   │   ├── Badge.jsx
│   │   │   ├── Button.jsx
│   │   │   ├── Input.jsx
│   │   │   ├── Select.jsx
│   │   │   ├── Textarea.jsx
│   │   │   ├── Modal.jsx
│   │   │   ├── LoadingSpinner.jsx
│   │   │   ├── ConfirmDialog.jsx
│   │   │   ├── AssigneeSelect.jsx
│   │   │   └── MemberAvatar.jsx
│   │   ├── dashboard/              # Dashboard components
│   │   │   ├── StatCard.jsx       # Statistics card
│   │   │   ├── RecentProjects.jsx # Recent projects list
│   │   │   ├── RecentTasks.jsx    # Recent tasks list
│   │   │   └── TaskStatistics.jsx # Visual charts
│   │   ├── kanban/                 # Kanban board
│   │   │   ├── KanbanBoard.jsx    # Main board container
│   │   │   ├── KanbanColumn.jsx   # Column component
│   │   │   └── KanbanCard.jsx     # Draggable task card
│   │   ├── layout/
│   │   │   ├── AppLayout.jsx      # Main layout wrapper
│   │   │   ├── Navbar.jsx         # Top navigation
│   │   │   ├── Sidebar.jsx        # Side navigation
│   │   │   └── ProfileDropdown.jsx
│   │   ├── projects/               # Project management
│   │   │   ├── ProjectCard.jsx
│   │   │   ├── CreateProjectModal.jsx
│   │   │   ├── EditProjectModal.jsx
│   │   │   └── AddMemberModal.jsx
│   │   └── tasks/                  # Task management
│   │       ├── TaskCard.jsx
│   │       ├── CreateTaskModal.jsx
│   │       └── EditTaskModal.jsx
│   ├── context/
│   │   ├── AuthContext.jsx         # Authentication state
│   │   └── ToastContext.jsx        # Toast notifications
│   ├── hooks/
│   │   ├── useProjects.js          # Projects data hook
│   │   ├── useTasks.js             # Tasks data hook
│   │   └── useProjectMembers.js    # Members data hook
│   ├── pages/
│   │   ├── Login.jsx               # Login page
│   │   ├── Register.jsx            # Registration page
│   │   ├── Dashboard.jsx           # Main dashboard
│   │   ├── Projects.jsx            # Projects list
│   │   ├── Tasks.jsx               # Tasks list
│   │   └── Kanban.jsx              # Kanban board page
│   ├── services/
│   │   ├── api.js                  # Axios instance
│   │   ├── authService.js          # Auth API calls
│   │   ├── userService.js          # User API calls
│   │   ├── projectService.js       # Project API calls
│   │   ├── taskService.js          # Task API calls
│   │   └── projectMemberService.js # Members API calls
│   ├── constants/
│   │   └── api.js                  # API endpoints & constants
│   ├── utils/
│   │   ├── validation.js           # Form validators
│   │   └── helpers.js              # Utility functions
│   ├── test/
│   │   ├── setup.js                # Test configuration
│   │   ├── test-utils.jsx          # Testing utilities
│   │   └── mocks/                  # MSW mocks
│   ├── App.jsx                     # Root component
│   ├── main.jsx                    # Entry point
│   └── index.css                   # Global styles
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
└── eslint.config.js
```

### Key Frontend Patterns

#### 1. Context API for State Management
```javascript
// AuthContext provides user state globally
<AuthProvider>
  <App />
</AuthProvider>
```

#### 2. Custom Hooks for Data Fetching
```javascript
// useProjects hook handles all project operations
const { projects, loading, createProject, updateProject } = useProjects();
```

#### 3. Axios Interceptors for Auth
```javascript
// Auto-attach JWT token to all requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

#### 4. Protected Routes
```javascript
<Route path="/dashboard" element={
  <PrivateRoute>
    <Dashboard />
  </PrivateRoute>
} />
```

#### 5. Component Composition
```javascript
// Reusable Button component with variants
<Button variant="primary" onClick={handleSave}>
  Save Changes
</Button>
```

---

## 🚀 Installation Guide

### Prerequisites

- **Java 17** (JDK 17 or higher)
- **Maven 3.9.11** (or higher)
- **Node.js 18+** and npm
- **PostgreSQL 14+** (or Supabase account)
- **Git** for version control

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/task-manager.git
cd task-manager
```

### 2. Backend Setup

#### Step 1: Configure Environment Variables

```bash
cd backend
cp .env.example .env
```

Edit `.env` file:
```properties
# Database Configuration
DATABASE_URL=jdbc:postgresql://your-host:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-secure-password

# JWT Configuration
JWT_SECRET=your-secret-key-must-be-at-least-32-characters-long
JWT_EXPIRATION=86400000

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080

# CORS Configuration (comma-separated)
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

#### Step 2: Build the Application

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package
```

#### Step 3: Run the Application

```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Using JAR
java -jar target/task-manager-backend-1.0.0.jar

# Option 3: With custom profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The backend will start on `http://localhost:8080`

#### Step 4: Verify Installation

- **Health Check**: http://localhost:8080/actuator/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/v3/api-docs

---

### 3. Frontend Setup

#### Step 1: Install Dependencies

```bash
cd frontend
npm install
```

#### Step 2: Configure Environment

Create `.env` file:
```properties
VITE_API_BASE_URL=http://localhost:8080/api
```

#### Step 3: Run Development Server

```bash
# Start dev server
npm run dev

# Run tests
npm run test

# Run tests with UI
npm run test:ui

# Generate coverage report
npm run test:coverage

# Lint code
npm run lint

# Build for production
npm run build

# Preview production build
npm run preview
```

The frontend will start on `http://localhost:5173`

---

### 4. Using Supabase (Cloud PostgreSQL)

#### Step 1: Create Account
1. Go to https://supabase.com
2. Create a new account (free tier available)
3. Create a new project

#### Step 2: Get Connection String
1. Go to Project Settings > Database
2. Copy the connection string
3. Update your `.env` file:

```properties
DATABASE_URL=jdbc:postgresql://db.xxxxx.supabase.co:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-supabase-password
```

#### Step 3: Configure SSL (if required)

Add to `application.yml`:
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}?sslmode=require
```

---

## 🔧 Development Workflow

### Backend Development

#### IDE Setup (IntelliJ IDEA)
1. Open `backend` folder
2. IntelliJ auto-detects Maven project
3. Set up Run Configuration:
   - Main class: `com.taskmanager.TaskManagerApplication`
   - Environment variables: Load from `.env`
4. Enable Lombok annotation processing:
   - Settings > Build > Compiler > Annotation Processors
   - Enable annotation processing

#### Code Style
- Use Google Java Style Guide
- 4 spaces for indentation
- Max line length: 120 characters
- Use Lombok to reduce boilerplate

#### Database Migrations

Create new migration:
```sql
-- File: V5__Your_Migration_Name.sql
ALTER TABLE tasks ADD COLUMN tags VARCHAR(255);
```

Flyway automatically runs on startup in development mode.

#### Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report
```

---

### Frontend Development

#### Code Style
- 2 spaces for indentation
- Use functional components with hooks
- Prefer named exports
- Use PropTypes for type checking

#### Component Development

1. **Create Component**:
```jsx
// components/MyComponent.jsx
import PropTypes from 'prop-types';

const MyComponent = ({ title, onClick }) => {
  return (
    <div onClick={onClick}>
      <h2>{title}</h2>
    </div>
  );
};

MyComponent.propTypes = {
  title: PropTypes.string.isRequired,
  onClick: PropTypes.func,
};

export default MyComponent;
```

2. **Add Tests**:
```jsx
// components/__tests__/MyComponent.test.jsx
import { render, screen } from '@testing-library/react';
import MyComponent from '../MyComponent';

describe('MyComponent', () => {
  it('renders title', () => {
    render(<MyComponent title="Hello" />);
    expect(screen.getByText('Hello')).toBeInTheDocument();
  });
});
```

#### Hot Module Replacement
Vite provides instant HMR - save files and see changes immediately.

---

## 🧪 Testing

### Backend Testing

#### Test Structure
```
src/test/java/com/taskmanager/
├── controller/
│   ├── AuthControllerTest.java
│   ├── UserControllerTest.java
│   ├── ProjectControllerTest.java
│   └── TaskControllerTest.java
├── service/
│   ├── AuthServiceTest.java
│   ├── UserServiceTest.java
│   ├── ProjectServiceTest.java
│   └── TaskServiceTest.java
└── repository/
    ├── UserRepositoryTest.java
    ├── ProjectRepositoryTest.java
    └── TaskRepositoryTest.java
```

#### Running Tests
```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=UserServiceTest

# Integration tests only
mvn verify -P integration-tests

# Generate coverage report
mvn test jacoco:report
# View report at: target/site/jacoco/index.html
```

---

### Frontend Testing

#### Test Structure
```
src/
├── components/
│   └── common/
│       ├── Button.jsx
│       └── __tests__/
│           └── Button.test.jsx
├── services/
│   └── __tests__/
│       └── userService.test.js
└── utils/
    └── __tests__/
        └── validation.test.js
```

#### Running Tests
```bash
# Watch mode (default)
npm run test

# Run once
npm run test:run

# With UI
npm run test:ui

# Coverage
npm run test:coverage
# View report at: coverage/index.html
```

#### Test Examples

**Component Test:**
```javascript
import { render, screen, fireEvent } from '@testing-library/react';
import Button from '../Button';

test('calls onClick when clicked', () => {
  const handleClick = vi.fn();
  render(<Button onClick={handleClick}>Click Me</Button>);

  fireEvent.click(screen.getByText('Click Me'));
  expect(handleClick).toHaveBeenCalledTimes(1);
});
```

**API Mock with MSW:**
```javascript
import { rest } from 'msw';
import { server } from './mocks/server';

test('fetches user profile', async () => {
  server.use(
    rest.get('/api/users/profile', (req, res, ctx) => {
      return res(ctx.json({ email: 'test@example.com' }));
    })
  );

  // Test code...
});
```

---

## 🌐 Deployment

This project is configured for **FREE** deployment using:
- **Frontend:** Vercel (free tier)
- **Backend:** Render (free tier)
- **Database:** Supabase (free tier)

### Quick Deployment Guide

See **[DEPLOYMENT.md](./DEPLOYMENT.md)** for complete step-by-step deployment instructions.

**Overview:**
1. Push your code to public GitHub repository
2. Deploy backend to Render (5-7 minutes)
3. Deploy frontend to Vercel (1-2 minutes)
4. Enable Row Level Security on database
5. Test and enjoy your live application!

**Total Cost:** $0/month (all free tiers)

**Deployment Configuration Files:**
- `backend/render.yaml` - Render deployment config
- `backend/.env.example` - Backend environment template
- `frontend/.env.example` - Frontend environment template

### Alternative Deployment Options

**Backend:**
- Railway (free with $5 credit)
- Fly.io (free tier available)
- Docker (self-hosted)
- AWS Elastic Beanstalk
- Heroku (no longer free)

**Frontend:**
- Netlify (free tier)
- Cloudflare Pages (free tier)
- GitHub Pages (static only)
- AWS S3 + CloudFront

**Database:**
- PostgreSQL on Railway (free tier)
- Neon (free tier)
- ElephantSQL (free tier)
- Self-hosted PostgreSQL

---

## 📊 Project Metrics

### Codebase Statistics

| Metric | Count |
|--------|-------|
| **Backend** | |
| Java Classes | 51 |
| REST Endpoints | 30+ |
| Database Tables | 5 |
| Flyway Migrations | 4 |
| Test Files | 11 |
| Lines of Code | ~5,000 |
| **Frontend** | |
| React Components | 40+ |
| Pages | 5 |
| Custom Hooks | 3 |
| Service Files | 6 |
| Test Files | 10+ |
| Lines of Code | ~4,500 |
| **Total** | ~9,500 LOC |

### API Endpoints Summary

| Category | Endpoints |
|----------|-----------|
| Authentication | 3 |
| User Management | 4 |
| Project Management | 9 |
| Task Management | 10 |
| Comments | 2 |
| Statistics | 2 |
| **Total** | **30** |

---

## 🛡️ Security Features

### Authentication & Authorization
- ✅ JWT-based stateless authentication
- ✅ BCrypt password hashing (strength 12)
- ✅ Token expiration (24 hours)
- ✅ Role-based access control (RBAC)
- ✅ Protected routes on frontend
- ✅ Automatic token refresh handling

### Data Protection
- ✅ SQL injection prevention (JPA/Hibernate)
- ✅ XSS protection
- ✅ CSRF protection (for non-stateless endpoints)
- ✅ CORS configuration
- ✅ Input validation on all endpoints
- ✅ Soft deletes (data retention)

### Best Practices
- ✅ Environment variables for secrets
- ✅ HTTPS in production
- ✅ Secure headers (Content-Security-Policy, X-Frame-Options)
- ✅ Rate limiting (configured in production)
- ✅ Error messages don't expose sensitive data

---

## 📝 API Testing Examples

### Using cURL

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Get Profile:**
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Create Project:**
```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "My Project",
    "description": "Project description",
    "color": "#3B82F6"
  }'
```

---

## 🤝 Contributing

This is a portfolio project, but feedback and suggestions are welcome!

### How to Contribute
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Standards
- Follow existing code style
- Write tests for new features
- Update documentation
- Ensure all tests pass

---

## 📞 Contact & Links

**Developer**: Samet Ozturk
**Purpose**: Full-stack portfolio project
**Tech Focus**: Spring Boot, React, PostgreSQL, JWT Authentication

### Links
- **Live Demo**: [https://taskify-maskify.netlify.app](https://taskify-maskify.netlify.app)
- **Backend API**: [https://task-manager-backend-5pwe.onrender.com/swagger-ui.html](https://task-manager-backend-5pwe.onrender.com/swagger-ui.html)
- **GitHub**: [https://github.com/0zturkSamet/task-manager](https://github.com/0zturkSamet/task-manager)
- **LinkedIn**: [Add your LinkedIn URL here]
- **Portfolio**: [Add your portfolio URL here]

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Spring Boot team for excellent documentation
- React team for the amazing library
- Tailwind CSS for beautiful styling utilities
- @dnd-kit for accessible drag-and-drop
- Supabase for managed PostgreSQL hosting

---

**⭐ Star this repository if you find it helpful!**

---

## 📸 Screenshots

*Screenshots coming soon! Feel free to explore the [live demo](https://taskify-maskify.netlify.app) in the meantime.*

---

**Last Updated**: October 31, 2025
**Project Status**: ✅ Complete - Production Ready - Deployed
