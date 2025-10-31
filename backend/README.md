# Task Manager Backend API

A modern RESTful API for task management built with Spring Boot 3.3, Spring Security, and PostgreSQL.

## Tech Stack

- **Java**: 17
- **Framework**: Spring Boot 3.3.3
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL (Supabase)
- **ORM**: Spring Data JPA + Hibernate
- **Migration**: Flyway
- **Documentation**: Springdoc OpenAPI (Swagger)
- **Build Tool**: Maven

## Features

- ✅ JWT-based authentication
- ✅ User registration and login
- ✅ Password encryption with BCrypt
- ✅ Global exception handling
- ✅ Database migrations with Flyway
- ✅ API documentation with Swagger UI
- ✅ CORS configuration for frontend
- ✅ Input validation
- ✅ Audit fields (created_at, updated_at)

## Architecture Overview

- **Layered flow**: HTTP requests enter through REST controllers, are validated against request DTOs, and delegate business rules to services. Services orchestrate repositories for persistence and return response DTOs that hide internal entities from the API surface.
- **Stateless auth**: `SecurityConfig` wires Spring Security for stateless JWT authentication. `JwtAuthenticationFilter` extracts bearer tokens, validates them with `JwtService`, and populates the security context before the request reaches a controller.
- **Persistence**: Spring Data JPA repositories encapsulate queries against PostgreSQL. Entities (`User`, `Project`, `ProjectMember`, `Task`, `TaskComment`) leverage auditing annotations so `created_at`/`updated_at` timestamps are filled automatically.
- **Configuration & ops**: `application.yml` defines datasource, Flyway, logging, and profile-specific overrides. `OpenApiConfig` publishes interactive docs at `/swagger-ui.html`.

### Request lifecycle
1. Incoming traffic passes through the JWT filter; anonymous endpoints (e.g., `/api/auth/**`) are excluded in `SecurityConfig`.
2. Controllers such as `AuthController`, `ProjectController`, `TaskController`, and `UserController` validate payloads (`@Valid`) and inject the authenticated `User`.
3. Services (`AuthService`, `ProjectService`, `TaskService`, `UserService`) enforce permissions (`ProjectRole`), perform business logic, and compose rich DTO responses.
4. Repositories (for example `TaskRepository#findAllUserTasks`) issue typed queries that filter soft-deleted rows and order results for the UI.
5. Failures bubble to `GlobalExceptionHandler`, which maps them to consistent `ErrorResponse` payloads with HTTP status codes and validation details.

### Business capabilities
- **Authentication**: `AuthService` handles registration and login with BCrypt hashing and JWT issuance; account state is tracked via the `isActive` column on `User`.
- **Projects & membership**: `ProjectService` creates projects, attaches the owner as an `OWNER`, and exposes member management while enforcing role-based rules.
- **Tasks & collaboration**: `TaskService` manages CRUD, filtering, ordering (`position`), statistics aggregation, and comment threads (`TaskCommentRepository`). It validates project access via repository helper queries before touching data.
- **User insights**: `UserService` surfaces profile management and productivity metrics (completion rates, workload breakdown) by aggregating task data.

### Validation and error handling
- Request DTOs define field-level constraints; violations raise `MethodArgumentNotValidException`, which the global handler converts into structured validation maps.
- Custom exceptions (`ResourceNotFoundException`, `ForbiddenException`, `DuplicateResourceException`, etc.) give explicit failure semantics that translate to appropriate HTTP codes.

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL database (or Supabase account)

### Environment Setup

1. Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```

2. Update the `.env` file with your configuration:
```properties
DATABASE_URL=jdbc:postgresql://your-supabase-host:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your-password
JWT_SECRET=your-secret-key-min-32-characters
JWT_EXPIRATION_MS=86400000
```

### Running the Application

1. **Build the project**:
```bash
mvn clean install
```

2. **Run the application**:
```bash
mvn spring-boot:run
```

Or with environment variables:
```bash
export $(cat .env | xargs) && mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### API Documentation

Once the application is running, access the Swagger UI at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login user | No |
| POST | `/api/auth/logout` | Logout user | Yes |

### User Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users/profile` | Get current user profile | Yes |
| PUT | `/api/users/profile` | Update user profile | Yes |
| DELETE | `/api/users/account` | Delete user account | Yes |

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    profile_image VARCHAR(500),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## Project Structure

```
backend/
├── src/main/java/com/taskmanager/
│   ├── config/           # Configuration classes
│   ├── controller/       # REST controllers
│   ├── dto/              # Data Transfer Objects
│   ├── entity/           # JPA entities
│   ├── exception/        # Custom exceptions & handlers
│   ├── repository/       # JPA repositories
│   ├── security/         # Security & JWT
│   └── service/          # Business logic
├── src/main/resources/
│   ├── db/migration/     # Flyway migrations
│   └── application.yml   # Application configuration
└── pom.xml               # Maven dependencies
```

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package -DskipTests
```

The JAR file will be created in the `target/` directory.

## Deployment

### Using Railway/Render

1. Push your code to GitHub
2. Connect your repository to Railway/Render
3. Set environment variables in the platform
4. Deploy automatically on push

### Using Docker (Optional)

```dockerfile
# Dockerfile example
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t task-manager-backend .
docker run -p 8080:8080 --env-file .env task-manager-backend
```

## Security Notes

- All passwords are encrypted using BCrypt (strength 12)
- JWT tokens expire after 24 hours by default
- CORS is configured for common development origins
- Update CORS configuration for production domains

## Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Submit a pull request

## License

This project is licensed under the MIT License.

## Contact

For questions or support, contact: support@taskmanager.com
