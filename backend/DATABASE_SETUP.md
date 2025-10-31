# Database Setup & Testing Guide

Your Supabase database is now configured and ready to use!

## ✅ What's Already Set Up

- **Supabase Project**: `ddshegifomdsomywqhkr`
- **Database Connection**: `db.ddshegifomdsomywqhkr.supabase.co:5432`
- **Environment File**: `backend/.env` (created with your credentials)
- **JWT Secret**: Configured and ready
- **RLS Script**: Available in `setup-rls.sql` (optional)

## 🚀 Quick Start (5 Steps)

### Step 1: Install Maven (if not already installed)

```bash
# macOS
brew install maven

# Verify installation
mvn --version
```

### Step 2: Navigate to Backend Directory

```bash
cd /Users/sametozturk/Desktop/Task-Manager/backend
```

### Step 3: Load Environment Variables

```bash
# Export all variables from .env file
export $(cat .env | xargs)

# Verify (should show your DATABASE_URL)
echo $DATABASE_URL
```

### Step 4: Build and Run the Application

```bash
# Clean and compile
mvn clean install

# Run the application
mvn spring-boot:run
```

**Expected Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.3.3)

2024-XX-XX INFO  --- Starting TaskManagerApplication
2024-XX-XX INFO  --- Flyway migration starting...
2024-XX-XX INFO  --- Successfully applied 1 migration
2024-XX-XX INFO  --- Started TaskManagerApplication in X.XXX seconds
```

### Step 5: Access Swagger UI

Open your browser and go to:
```
http://localhost:8080/swagger-ui.html
```

You should see the API documentation with all your endpoints!

## 🧪 Testing Your Setup

### Test 1: Register a New User

1. Go to **Swagger UI**: http://localhost:8080/swagger-ui.html
2. Expand **Authentication** section
3. Click on **POST /api/auth/register**
4. Click **"Try it out"**
5. Enter this JSON:

```json
{
  "email": "test@taskmanager.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

6. Click **"Execute"**
7. **Expected Response** (Status 201):

```json
{
  "id": "a1b2c3d4-...",
  "email": "test@taskmanager.com",
  "firstName": "John",
  "lastName": "Doe",
  "profileImage": null,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400000
}
```

8. **Copy the token** - you'll need it for the next tests!

### Test 2: Verify in Supabase Dashboard

1. Go to: https://supabase.com/dashboard/project/ddshegifomdsomywqhkr/editor
2. Click **"Table Editor"** in the left sidebar
3. Click on **"users"** table
4. You should see your new user with:
   - ✅ Email: test@taskmanager.com
   - ✅ Password: encrypted (starts with `$2a$`)
   - ✅ First Name: John
   - ✅ Last Name: Doe
   - ✅ is_active: true

### Test 3: Login with Existing User

1. In Swagger UI, go to **POST /api/auth/login**
2. Click **"Try it out"**
3. Enter:

```json
{
  "email": "test@taskmanager.com",
  "password": "password123"
}
```

4. Click **"Execute"**
5. **Expected Response** (Status 200):

```json
{
  "id": "a1b2c3d4-...",
  "email": "test@taskmanager.com",
  "firstName": "John",
  "lastName": "Doe",
  "profileImage": null,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400000
}
```

### Test 4: Access Protected Endpoint

1. **Click the "Authorize" button** at the top right of Swagger UI (lock icon)
2. In the modal, enter: `Bearer YOUR_TOKEN_HERE`
   - Replace `YOUR_TOKEN_HERE` with the token from Test 1 or Test 3
   - Make sure to include the word "Bearer" followed by a space
3. Click **"Authorize"**, then **"Close"**
4. Now go to **GET /api/users/profile**
5. Click **"Try it out"**, then **"Execute"**
6. **Expected Response** (Status 200):

```json
{
  "id": "a1b2c3d4-...",
  "email": "test@taskmanager.com",
  "firstName": "John",
  "lastName": "Doe",
  "profileImage": null,
  "createdAt": "2024-XX-XXTXX:XX:XX",
  "isActive": true
}
```

### Test 5: Update User Profile

1. Go to **PUT /api/users/profile**
2. Make sure you're authorized (green lock icon)
3. Click **"Try it out"**
4. Enter:

```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "profileImage": "https://i.pravatar.cc/150?img=1"
}
```

5. Click **"Execute"**
6. Verify the response shows updated data
7. Check Supabase Table Editor - data should be updated!

## 🛡️ Optional: Enable Row Level Security

RLS is **not required** for your Spring Boot backend, but it's good for extra security.

### When to Enable RLS:

- ✅ If you want defense-in-depth security
- ✅ If you plan to access Supabase directly from frontend later
- ✅ If you're using Supabase Auth alongside your custom auth
- ❌ Not needed if only using Spring Boot API

### How to Enable:

1. Go to: https://supabase.com/dashboard/project/ddshegifomdsomywqhkr/sql/new
2. Open the file `setup-rls.sql` from your backend directory
3. Copy the SQL for the users table (first section)
4. Paste into Supabase SQL Editor
5. Click **"Run"**
6. Verify: Go to **Authentication > Policies** - you should see the policies

**Note**: Your Spring Boot backend uses the `postgres` service role which **bypasses RLS automatically**, so it won't affect your API.

## 🔧 Troubleshooting

### Issue 1: "Connection refused" or "Unknown host"

**Problem**: Application can't connect to Supabase

**Solution**:
```bash
# Verify your .env file is correct
cat .env

# Check if DATABASE_URL is correct
echo $DATABASE_URL

# Make sure it looks like:
# jdbc:postgresql://db.ddshegifomdsomywqhkr.supabase.co:5432/postgres
```

### Issue 2: "Authentication failed for user postgres"

**Problem**: Wrong password

**Solution**:
1. Go to Supabase Dashboard > Settings > Database
2. Click "Reset Database Password"
3. Set a new password
4. Update `backend/.env` file with the new password
5. Restart the application

### Issue 3: Flyway Migration Fails

**Problem**: `Flyway migration failed` error

**Solution**:
```sql
-- Go to Supabase SQL Editor and run:
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- Then restart your Spring Boot application
```

### Issue 4: SSL/TLS Connection Error

**Problem**: SSL certificate validation fails

**Solution**: Add SSL parameter to `DATABASE_URL` in `.env`:
```properties
DATABASE_URL=jdbc:postgresql://db.ddshegifomdsomywqhkr.supabase.co:5432/postgres?sslmode=require
```

### Issue 5: "Table already exists"

**Problem**: Running the app multiple times creates duplicate migration error

**Solution**:
1. This is normal if you restart the app
2. Flyway will skip already-applied migrations
3. If you need to reset, drop the `flyway_schema_history` table in Supabase

## 📊 Verify Database Tables

Go to Supabase Dashboard > Table Editor. You should see:

### `users` table
- **Columns**: id, email, password, first_name, last_name, profile_image, is_active, created_at, updated_at
- **Indexes**: idx_users_email, idx_users_is_active
- **Primary Key**: id (UUID)

### `flyway_schema_history` table
- Tracks all database migrations
- Should have 1 row: `V1__Create_User_Table.sql`

## 🔍 Direct Database Connection Test (Optional)

If you have `psql` installed:

```bash
# Install psql (macOS)
brew install postgresql

# Connect directly to Supabase
psql "postgresql://postgres:datapass.datapass@db.ddshegifomdsomywqhkr.supabase.co:5432/postgres"

# Inside psql:
\dt                    # List all tables
\d users              # Describe users table
SELECT * FROM users;  # View all users
\q                    # Quit
```

## ✅ Success Checklist

Before moving to Phase 2, confirm:

- [ ] Maven is installed (`mvn --version`)
- [ ] `.env` file exists with correct Supabase credentials
- [ ] Application starts without errors (`mvn spring-boot:run`)
- [ ] Flyway creates `users` table in Supabase
- [ ] Swagger UI is accessible at http://localhost:8080/swagger-ui.html
- [ ] User registration works (POST /api/auth/register)
- [ ] User login works (POST /api/auth/login)
- [ ] Protected endpoint works with JWT token (GET /api/users/profile)
- [ ] User data appears in Supabase Table Editor
- [ ] Password is encrypted in database (starts with `$2a$`)

## 🎯 Next Steps

Once all tests pass:

1. **You're ready for Phase 2!**
   - We'll add Projects, Team Members, and Tasks

2. **Keep the app running** during development:
   - Swagger UI will update automatically as we add new endpoints

3. **Commit your work** (optional):
   ```bash
   git add .
   git commit -m "Phase 1 complete: Authentication & User Management"
   ```

## 📝 Environment Variables Reference

Your `.env` file contains:

| Variable | Purpose | Used By |
|----------|---------|---------|
| `DATABASE_URL` | PostgreSQL connection | Spring Boot backend |
| `DATABASE_USERNAME` | DB username (postgres) | Spring Boot backend |
| `DATABASE_PASSWORD` | DB password | Spring Boot backend |
| `JWT_SECRET` | Token signing key | Spring Boot JWT service |
| `JWT_EXPIRATION_MS` | Token expiry (24 hours) | Spring Boot JWT service |
| `SUPABASE_URL` | API endpoint | Frontend (Phase 4+) |
| `SUPABASE_ANON_KEY` | Public API key | Frontend (Phase 4+) |

## 🆘 Need Help?

If you encounter any issues:

1. **Check the logs** in your terminal where `mvn spring-boot:run` is running
2. **Verify Supabase** is accessible: https://supabase.com/dashboard
3. **Check this guide** for troubleshooting section above
4. **Review backend/README.md** for additional documentation

---

**Status**: Database configured ✅ | Ready for Phase 2 🚀
