# Task Manager - Deployment Guide

Complete step-by-step guide to deploy your Task Manager application for FREE using Vercel (frontend) + Render (backend) + Supabase (database).

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Overview](#overview)
- [Step 1: Prepare GitHub Repository](#step-1-prepare-github-repository)
- [Step 2: Deploy Backend to Render](#step-2-deploy-backend-to-render)
- [Step 3: Deploy Frontend to Vercel](#step-3-deploy-frontend-to-vercel)
- [Step 4: Finalize Database Security](#step-4-finalize-database-security)
- [Step 5: Testing](#step-5-testing)
- [Step 6: Create Demo Account](#step-6-create-demo-account)
- [Troubleshooting](#troubleshooting)
- [Optional Enhancements](#optional-enhancements)

---

## Prerequisites

Before you begin, make sure you have:

- [x] GitHub account (free)
- [x] Render account (free) - https://render.com
- [x] Vercel account (free) - https://vercel.com
- [x] Supabase account (free) - https://supabase.com
- [x] Your project pushed to GitHub (public repository)
- [x] Supabase database already configured with migrations

---

## Overview

**Deployment Stack:**
```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   Vercel    │ ───> │   Render    │ ───> │  Supabase   │
│  (Frontend) │      │  (Backend)  │      │ (Database)  │
│    FREE     │      │    FREE     │      │    FREE     │
└─────────────┘      └─────────────┘      └─────────────┘
```

**Total Monthly Cost:** $0 (all free tiers)

**Timeline:** 20-30 minutes total

---

## Step 1: Prepare GitHub Repository

### 1.1 Create Public Repository

1. Go to https://github.com/new
2. Repository name: `task-manager` (or your preferred name)
3. Description: "Full-stack task management application with React, Spring Boot, and PostgreSQL"
4. **Make it PUBLIC** (required for free hosting)
5. **Do NOT initialize with README** (you already have one)
6. Click "Create repository"

### 1.2 Push Your Code

```bash
# Navigate to your project
cd /Users/sametozturk/Desktop/Task-Manager

# Initialize git (if not already done)
git init

# Add all files
git add .

# Create initial commit
git commit -m "Initial commit - Task Manager v1.0"

# Add remote (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/task-manager.git

# Push to GitHub
git push -u origin main
```

### 1.3 Verify on GitHub

- Visit your repository on GitHub
- Verify all files are there
- Check that .env files are NOT visible (they should be gitignored)
- Confirm backend/render.yaml is present

---

## Step 2: Deploy Backend to Render

### 2.1 Create New Web Service

1. Go to https://dashboard.render.com
2. Click "New +" > "Web Service"
3. Connect your GitHub account (if not already connected)
4. Find your `task-manager` repository
5. Click "Connect"

### 2.2 Configure Service

**Basic Settings:**
- **Name:** `task-manager-backend` (or your preferred name)
- **Region:** Choose closest to you (e.g., Oregon, Frankfurt)
- **Branch:** `main`
- **Root Directory:** `backend`
- **Runtime:** Java
- **Build Command:** `mvn clean package -DskipTests`
- **Start Command:** `java -Dserver.port=$PORT -jar target/task-manager-backend-1.0.0.jar`

**Instance Type:**
- **Plan:** Free

### 2.3 Add Environment Variables

Click "Advanced" and add these environment variables:

| Key | Value | Notes |
|-----|-------|-------|
| `DATABASE_URL` | Your Supabase pooler URL (port 6543) | From backend/.env |
| `FLYWAY_URL` | Your Supabase direct URL (port 5432) | From backend/.env |
| `DATABASE_USERNAME` | `postgres.xxxxxx` | From backend/.env |
| `DATABASE_PASSWORD` | Your Supabase password | From backend/.env |
| `JWT_SECRET` | Your JWT secret | From backend/.env (or generate new) |
| `JWT_EXPIRATION_MS` | `86400000` | 24 hours |
| `SPRING_PROFILES_ACTIVE` | `prod` | Production mode |
| `PORT` | `8080` | (Actually set by Render automatically) |

**Where to find these:**
- Copy from your local `backend/.env` file
- OR get from Supabase: Settings > Database > Connection string

### 2.4 Deploy

1. Click "Create Web Service"
2. Wait for build to complete (5-7 minutes first time)
3. Watch the logs for any errors
4. When you see: `Started TaskManagerApplication in X seconds`
5. Your backend is live!

### 2.5 Get Your Backend URL

1. Find your backend URL (top of Render dashboard)
   Example: `https://task-manager-backend-abc123.onrender.com`
2. **Copy this URL** - you'll need it for frontend deployment
3. Test it: Visit `https://your-backend.onrender.com/actuator/health`
   Should return: `{"status":"UP"}`

---

## Step 3: Deploy Frontend to Vercel

### 3.1 Prepare Frontend

Before deploying, update CORS in your backend to allow your Vercel frontend.

**Option A: Update Now (recommended)**

Edit `backend/src/main/java/com/taskmanager/config/SecurityConfig.java`:

```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "http://localhost:5173",
    "http://localhost:5174",
    "https://*.vercel.app",  // Already there!
    "https://*.netlify.app"
));
```

Save, commit, and push - Render will auto-deploy.

**Option B: Update After Deployment**

You can also add the specific Vercel URL after deployment.

### 3.2 Deploy to Vercel

1. Go to https://vercel.com/dashboard
2. Click "Add New..." > "Project"
3. Import your GitHub repository
4. Configure:

**Project Settings:**
- **Framework Preset:** Vite
- **Root Directory:** `frontend`
- **Build Command:** `npm run build` (auto-detected)
- **Output Directory:** `dist` (auto-detected)
- **Install Command:** `npm install` (auto-detected)

**Environment Variables:**

Click "Environment Variables" and add:

| Name | Value |
|------|-------|
| `VITE_API_BASE_URL` | `https://task-manager-backend-abc123.onrender.com/api` |

⚠️ **IMPORTANT:** Replace with YOUR actual Render backend URL + `/api`

### 3.3 Deploy

1. Click "Deploy"
2. Wait 1-2 minutes for build to complete
3. Vercel will show you the live URL
4. Example: `https://task-manager-xyz123.vercel.app`

### 3.4 Verify Deployment

1. Visit your Vercel URL
2. You should see the login page
3. Open browser console (F12) - check for errors
4. If you see CORS errors, verify Step 3.1

---

## Step 4: Finalize Database Security

### 4.1 Enable RLS on Flyway Table

⚠️ **IMPORTANT:** This step was mentioned in V12 migration.

1. Go to Supabase Dashboard
2. Click "SQL Editor"
3. Run this command:

```sql
ALTER TABLE flyway_schema_history ENABLE ROW LEVEL SECURITY;
```

4. Click "Run"
5. Verify success (should show "Success. No rows returned")

### 4.2 Verify All Tables Have RLS

Run this query to check:

```sql
SELECT schemaname, tablename, rowsecurity
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename;
```

All tables should have `rowsecurity = true`.

---

## Step 5: Testing

### 5.1 Test Backend Health

```bash
curl https://your-backend.onrender.com/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### 5.2 Test Registration

1. Go to your Vercel frontend URL
2. Click "Register"
3. Create a test account:
   - Email: demo@taskmanager.com
   - Password: Demo123456
   - First Name: Demo
   - Last Name: User
4. Should redirect to dashboard

### 5.3 Test Full Flow

1. **Create a Project:**
   - Go to Projects page
   - Click "Create Project"
   - Name: "Demo Project"
   - Description: "Testing deployment"
   - Color: Blue
   - Save

2. **Create a Task:**
   - Open your project
   - Click "Create Task"
   - Fill in details
   - Save

3. **Test Kanban Board:**
   - Go to Kanban view
   - Drag task between columns
   - Verify status updates

4. **Check Dashboard:**
   - Go to Dashboard
   - Verify statistics show correct numbers

---

## Step 6: Create Demo Account

For recruiters and visitors, create a demo account with sample data.

### 6.1 Register Demo Account

1. Go to your live site
2. Register: demo@taskmanager.com / DemoPassword123
3. Create 2-3 sample projects:
   - "Website Redesign"
   - "Mobile App Development"
   - "Q1 Marketing Campaign"

### 6.2 Add Sample Tasks

Create 10-15 tasks across projects:
- Mix of different statuses (TODO, IN_PROGRESS, IN_REVIEW, DONE)
- Different priorities (LOW, MEDIUM, HIGH, URGENT)
- Some with due dates
- Some assigned to demo user
- Add comments to tasks

### 6.3 Update README

Add demo credentials to your README.md:

```markdown
## Live Demo

**Live Application:** https://your-app.vercel.app

**Demo Credentials:**
- Email: demo@taskmanager.com
- Password: DemoPassword123

**Note:** First load may take ~30 seconds (free tier wakes from sleep)
```

---

## Troubleshooting

### Backend Issues

**Problem:** Build fails on Render

**Solutions:**
```bash
# Verify locally first
cd backend
mvn clean package -DskipTests

# Check render.yaml is correct
# Check pom.xml has correct version number
# Check Java version (should be 17)
```

**Problem:** Backend starts but returns 500 errors

**Solutions:**
- Check Render logs for error messages
- Verify all environment variables are set
- Verify database credentials are correct
- Check Supabase database is not paused

**Problem:** Database connection failed

**Solutions:**
- Verify DATABASE_URL and FLYWAY_URL are different (ports 6543 vs 5432)
- Check Supabase project is not paused
- Verify IP whitelist in Supabase (should allow all for free tier)
- Test connection manually from Render logs

### Frontend Issues

**Problem:** CORS errors in browser console

**Solutions:**
- Update SecurityConfig.java with your Vercel URL
- Commit and push changes
- Wait for Render to auto-deploy
- Clear browser cache and retry

**Problem:** API calls failing with 404

**Solutions:**
- Verify VITE_API_BASE_URL ends with `/api`
- Check backend is actually running on Render
- Test backend health endpoint directly
- Check browser network tab for actual request URL

**Problem:** Environment variable not working

**Solutions:**
- Vercel: Environment variables are set at BUILD time
  - Update variable in Vercel dashboard
  - Trigger a new deployment (Deployments > ... > Redeploy)
- Variable names MUST start with `VITE_`
- Check for typos

### Database Issues

**Problem:** RLS blocking operations

**Solutions:**
```sql
-- Check if service_role policy exists
SELECT * FROM pg_policies WHERE schemaname = 'public';

-- Should see policy: "Enable all access for service role"
-- On all tables
```

**Problem:** Migrations failing

**Solutions:**
- Check Flyway logs in Render
- Verify FLYWAY_URL uses port 5432 (direct connection)
- Check for duplicate migration files
- Verify all migration files are committed to git

---

## Optional Enhancements

### Custom Domain (Free)

**Vercel:**
1. Buy domain (Namecheap, Google Domains, etc.)
2. Vercel Dashboard > Your Project > Settings > Domains
3. Add your domain
4. Update DNS records as instructed

**Render:**
1. Render Dashboard > Your Service > Settings > Custom Domain
2. Add your domain
3. Update DNS CNAME record

### Environment-Specific Deployments

**Staging Environment:**
- Create `dev` branch in GitHub
- Deploy to Render (staging instance)
- Use different Supabase project
- Test changes before production

### Monitoring

**Render:**
- Built-in logs and metrics
- Can integrate with DataDog, LogDNA, etc.

**Vercel:**
- Analytics: Vercel Dashboard > Analytics
- Real User Monitoring (RUM)

**Supabase:**
- Database Dashboard
- Query performance
- Connection pool status

### Performance Optimization

**Backend:**
- Enable Redis caching (requires paid tier)
- Optimize database queries
- Add indexes for frequently queried fields

**Frontend:**
- Enable Vercel Edge Network (automatic)
- Implement route-based code splitting
- Add service worker for offline support

---

## Post-Deployment Checklist

- [ ] Backend deployed and accessible
- [ ] Frontend deployed and accessible
- [ ] Can register new account
- [ ] Can create projects and tasks
- [ ] Kanban drag-and-drop works
- [ ] Dashboard statistics accurate
- [ ] RLS enabled on all tables
- [ ] Demo account created with sample data
- [ ] README updated with live URLs
- [ ] Demo credentials documented
- [ ] CORS configured for production domain
- [ ] Environment variables secured
- [ ] No .env files in git repository

---

## Support & Resources

**Render Documentation:**
- https://render.com/docs

**Vercel Documentation:**
- https://vercel.com/docs

**Supabase Documentation:**
- https://supabase.com/docs

**Need Help?**
- Check GitHub Issues for this project
- Render Community: https://community.render.com
- Vercel Community: https://github.com/vercel/vercel/discussions

---

**Deployment Complete!** 🎉

Your Task Manager is now live and accessible to the world!

**Next Steps:**
1. Add to your portfolio
2. Share on LinkedIn
3. Include in resume
4. Apply for jobs!

---

*Last Updated: October 2025*
