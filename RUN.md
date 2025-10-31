# Task Manager - Run Guide

Quick reference for stopping and starting the Task Manager application.

## Quick Commands

### Stop Everything
```bash
# Kill both backend and frontend processes
lsof -ti:8080,5173 | xargs kill -9
```

### Start Everything
```bash
# Start backend (from project root)
cd backend
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
mvn spring-boot:run > backend.log 2>&1 &

# Start frontend (from project root)
cd frontend
npm run dev > frontend.log 2>&1 &
```

## Detailed Instructions

### Stop Services

**Stop Backend (Port 8080)**
```bash
lsof -ti:8080 | xargs kill -9
```

**Stop Frontend (Port 5173)**
```bash
lsof -ti:5173 | xargs kill -9
```

**Stop Both at Once**
```bash
lsof -ti:8080,5173 | xargs kill -9
```

### Start Services

**Prerequisites**
- Java 17 installed at `/opt/homebrew/opt/openjdk@17`
- Maven 3.9.11 installed
- Node.js and npm installed
- Database credentials configured in `backend/src/main/resources/application.properties`

**1. Start Backend**
```bash
cd /Users/sametozturk/Desktop/Task-Manager/backend

# Set Java environment
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"

# Start Spring Boot application
mvn spring-boot:run > backend.log 2>&1 &

# Or run in foreground to see logs
mvn spring-boot:run
```

**2. Start Frontend**
```bash
cd /Users/sametozturk/Desktop/Task-Manager/frontend

# Start Vite dev server
npm run dev > frontend.log 2>&1 &

# Or run in foreground to see logs
npm run dev
```

**3. Verify Services are Running**
```bash
# Check backend
curl http://localhost:8080/actuator/health

# Check frontend
curl http://localhost:5173
```

### Check Logs

**Backend Logs**
```bash
tail -f /Users/sametozturk/Desktop/Task-Manager/backend/backend.log
```

**Frontend Logs**
```bash
tail -f /Users/sametozturk/Desktop/Task-Manager/frontend/frontend.log
```

## Database Configuration

**Database**: Supabase PostgreSQL
**Connection String**: Configured in `backend/src/main/resources/application.properties`

**Key Properties**:
- `spring.datasource.url` - Supabase PostgreSQL connection URL
- `spring.datasource.username` - Database username
- `spring.datasource.password` - Database password
- `spring.flyway.enabled=true` - Auto-migration on startup

**Flyway Migrations**: Located in `backend/src/main/resources/db/migration/`
- V1: Initial schema
- V2: Project members
- V3: Task management
- V4: Comments
- V5: Notifications
- V6: Additional constraints
- V7: User role (system-wide admin)

## Admin Setup

By default, all users are created with the `USER` role. To promote a user to admin:

**1. Connect to your Supabase database**

**2. Run this SQL query**:
```sql
-- Replace 'user@example.com' with the actual email
UPDATE users
SET role = 'ADMIN'
WHERE email = 'user@example.com';
```

**3. User needs to log out and log back in** for the admin role to take effect.

**Admin Capabilities**:
- View ALL projects in the system (not just owned/member projects)
- View system-wide statistics on dashboard
- Assign tasks to ANY user (not limited to project members)
- Delete any task in the system
- Full access to all resources

**Visual Indicators**:
- Dashboard shows "ADMIN" badge with shield icon
- Dashboard subtitle: "System-wide view - All projects and tasks"
- Projects page shows "ADMIN" badge
- Projects subtitle: "Viewing all projects in the system"

## Troubleshooting

### Port Already in Use

**Error**: `Web server failed to start. Port 8080 was already in use.`

**Solution**:
```bash
# Kill the process using port 8080
lsof -ti:8080 | xargs kill -9

# Then restart backend
cd backend
mvn spring-boot:run
```

### Java Not Found

**Error**: `java: command not found` or wrong Java version

**Solution**:
```bash
# Set Java 17 environment
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"

# Verify Java version
java -version  # Should show Java 17
```

### Database Connection Failed

**Error**: Database connection errors on startup

**Solution**:
1. Verify Supabase database is running
2. Check credentials in `application.properties`
3. Verify network connectivity to Supabase
4. Check Supabase dashboard for connection limits

### Flyway Migration Errors

**Error**: Flyway migration validation failed

**Solution**:
```bash
# Clean and rebuild
cd backend
mvn clean package
mvn spring-boot:run
```

### Frontend Build Errors

**Error**: Vite build errors or dependency issues

**Solution**:
```bash
cd frontend

# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install

# Restart dev server
npm run dev
```

### CSS Errors (Tailwind)

**Error**: `The 'ease-smooth' class does not exist` or similar Tailwind errors

**Solution**: These errors were fixed in the codebase. If you see them:
1. Ensure you're using standard Tailwind classes
2. Custom classes must be defined in `@layer` directives
3. Check `tailwind.config.js` for custom theme configuration

## Project Structure

```
Task-Manager/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/      # Java source code
│   ├── src/main/resources/ # Configuration & migrations
│   │   ├── application.properties
│   │   └── db/migration/   # Flyway migration scripts
│   ├── pom.xml             # Maven dependencies
│   └── backend.log         # Runtime logs
│
├── frontend/               # React frontend
│   ├── src/               # React source code
│   │   ├── components/    # Reusable components
│   │   ├── pages/         # Page components
│   │   ├── context/       # React contexts
│   │   ├── hooks/         # Custom hooks
│   │   ├── services/      # API services
│   │   └── App.jsx        # Main app component
│   ├── package.json       # npm dependencies
│   └── frontend.log       # Runtime logs
│
└── RUN.md                 # This file
```

## Development Workflow

**Daily Startup**:
```bash
# 1. Navigate to project
cd /Users/sametozturk/Desktop/Task-Manager

# 2. Start backend
cd backend
export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
mvn spring-boot:run > backend.log 2>&1 &

# 3. Start frontend
cd ../frontend
npm run dev > frontend.log 2>&1 &

# 4. Access application
# Frontend: http://localhost:5173
# Backend API: http://localhost:8080/api
```

**End of Day Shutdown**:
```bash
# Stop all services
lsof -ti:8080,5173 | xargs kill -9
```

## Access URLs

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/actuator/health

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.3.3, Maven 3.9.11
- **Frontend**: React 19, Vite, Tailwind CSS
- **Database**: Supabase PostgreSQL
- **Migration**: Flyway
- **Security**: JWT Authentication
- **Design**: Google Material Design palette

## Notes

- Backend runs on port 8080
- Frontend dev server runs on port 5173
- Logs are written to `backend.log` and `frontend.log`
- Database migrations run automatically on backend startup
- Hot Module Replacement (HMR) enabled in Vite for instant frontend updates
- Soft deletes used (isActive flag) instead of physical deletion
