# GitHub Repository Setup Guide

Step-by-step guide to create and push your Task Manager project to GitHub.

---

## Prerequisites

- GitHub account (create one at https://github.com/join if you don't have one)
- Git installed on your computer
- Your project cleaned and ready (logs and build artifacts removed)

---

## Step 1: Create a New GitHub Repository

### 1.1 Go to GitHub

1. Log in to https://github.com
2. Click the "+" icon in the top right corner
3. Select "New repository"

### 1.2 Configure Repository Settings

Fill in the following details:

**Repository name:** `task-manager`
- Or choose your own name (must be URL-friendly)
- Example: `task-management-app`, `full-stack-task-manager`, etc.

**Description:** (Recommended)
```
Full-stack task management application built with Spring Boot, React, and PostgreSQL. Features JWT authentication, Kanban boards, team collaboration, and real-time statistics.
```

**Visibility:**
- ✅ **Public** (required for free Vercel/Render hosting)
- ❌ **Private** (won't work with free deployment tiers)

**Initialize repository:**
- ❌ Do NOT check "Add a README file"
- ❌ Do NOT add .gitignore (you already have one)
- ❌ Do NOT choose a license yet (you can add later)

### 1.3 Create Repository

Click "Create repository" button.

You'll see a page with setup instructions - keep this tab open!

---

## Step 2: Prepare Your Local Project

### 2.1 Navigate to Project Directory

```bash
cd /Users/sametozturk/Desktop/Task-Manager
```

### 2.2 Verify Clean State

Check that unnecessary files are removed:

```bash
# Should NOT see these:
# - backend.log
# - frontend.log
# - target/
# - dist/
# - .idea/
# - node_modules/ (will be in .gitignore)

ls -la
ls -la backend/
ls -la frontend/
```

### 2.3 Check Current Git Status

```bash
git status
```

**Expected output:**
- If you see "not a git repository", that's fine - we'll initialize it
- If you see a list of files, that's also fine

---

## Step 3: Initialize Git (if needed)

If you don't have git initialized yet:

```bash
# Initialize git repository
git init

# Verify
git status
```

You should now see a list of untracked files.

---

## Step 4: Review What Will Be Committed

### 4.1 Check .gitignore

Verify your .gitignore is working:

```bash
# These files should NOT appear in git status
cat .gitignore

# Verify .env files are ignored
ls backend/.env
# Should exist locally but NOT appear in git status
```

### 4.2 Verify Sensitive Files Are Excluded

Run this to ensure no secrets will be committed:

```bash
git status | grep -E "\.env|\.log|node_modules|target|dist"
```

**Expected:** No output (these files should be gitignored)

If you see any .env files, STOP and fix your .gitignore first!

---

## Step 5: Commit Your Code

### 5.1 Stage All Files

```bash
git add .
```

### 5.2 Verify What's Staged

```bash
git status
```

**What you SHOULD see:**
- ✅ .gitignore
- ✅ README.md
- ✅ DEPLOYMENT.md
- ✅ GITHUB_SETUP.md
- ✅ backend/src/ files
- ✅ backend/pom.xml
- ✅ backend/render.yaml
- ✅ backend/.env.example
- ✅ frontend/src/ files
- ✅ frontend/package.json
- ✅ frontend/.env.example
- ✅ All other source code files

**What you should NOT see:**
- ❌ .env files
- ❌ .log files
- ❌ node_modules/
- ❌ target/
- ❌ dist/
- ❌ .idea/
- ❌ .DS_Store

### 5.3 Create Initial Commit

```bash
git commit -m "Initial commit - Task Manager v1.0

Full-stack task management application featuring:
- Spring Boot 3.3.3 backend with JWT authentication
- React 19 frontend with Tailwind CSS
- PostgreSQL database with Flyway migrations
- Kanban board with drag-and-drop
- Team collaboration and role-based access control
- Real-time statistics dashboard
- 30+ RESTful API endpoints

Ready for deployment to Vercel + Render + Supabase"
```

---

## Step 6: Connect to GitHub

### 6.1 Add GitHub Remote

Copy the commands from your GitHub repository page, or use this template:

```bash
# Replace YOUR_USERNAME with your actual GitHub username
git remote add origin https://github.com/YOUR_USERNAME/task-manager.git
```

**Example:**
```bash
git remote add origin https://github.com/johndoe/task-manager.git
```

### 6.2 Verify Remote

```bash
git remote -v
```

**Expected output:**
```
origin  https://github.com/YOUR_USERNAME/task-manager.git (fetch)
origin  https://github.com/YOUR_USERNAME/task-manager.git (push)
```

---

## Step 7: Push to GitHub

### 7.1 Set Default Branch Name

```bash
git branch -M main
```

### 7.2 Push Your Code

```bash
git push -u origin main
```

**What happens:**
- Git will ask for your GitHub credentials
- For password, use a Personal Access Token (not your GitHub password)
- All your code will be uploaded to GitHub

### 7.3 Create Personal Access Token (if needed)

If prompted for password:

1. Go to GitHub.com > Settings > Developer settings > Personal access tokens > Tokens (classic)
2. Click "Generate new token (classic)"
3. Name: `Task Manager Deploy`
4. Expiration: 90 days (or your preference)
5. Scopes: Check ✅ `repo` (gives access to push code)
6. Click "Generate token"
7. **COPY THE TOKEN** (you won't see it again!)
8. Use this token as your password when pushing

### 7.4 Verify Upload

Go to your GitHub repository page and refresh - you should see all your files!

---

## Step 8: Configure Repository Settings

### 8.1 Add Topics/Tags

Make your repository discoverable:

1. On your GitHub repository page, click the gear icon next to "About"
2. Add topics:
   - `spring-boot`
   - `react`
   - `postgresql`
   - `jwt-authentication`
   - `kanban-board`
   - `task-manager`
   - `full-stack`
   - `rest-api`
   - `tailwindcss`
   - `maven`
   - `vite`
3. Save changes

### 8.2 Update Repository Description

1. Click the gear icon next to "About" (if not already there)
2. Add website URL: (Leave blank for now, add after Vercel deployment)
3. Check ✅ "Releases"
4. Check ✅ "Packages"
5. Save

### 8.3 Add a License (Optional but Recommended)

1. On your repository page, click "Add file" > "Create new file"
2. Name the file: `LICENSE`
3. Click "Choose a license template"
4. Select "MIT License" (most common for portfolio projects)
5. Fill in your name
6. Click "Review and submit"
7. Commit the file

---

## Step 9: Create a Release (Optional)

Create a v1.0 release:

1. Go to your repository
2. Click "Releases" on the right sidebar
3. Click "Create a new release"
4. Tag version: `v1.0.0`
5. Release title: `Task Manager v1.0.0 - Initial Release`
6. Description:
```markdown
# Task Manager v1.0.0

First stable release of the Task Manager application!

## Features
- ✅ User authentication with JWT
- ✅ Project management with team collaboration
- ✅ Task management with Kanban board
- ✅ Real-time statistics dashboard
- ✅ Comment system
- ✅ Role-based access control

## Tech Stack
- Backend: Spring Boot 3.3.3 + PostgreSQL
- Frontend: React 19 + Tailwind CSS
- Deployment: Vercel + Render + Supabase

## Deployment
See [DEPLOYMENT.md](./DEPLOYMENT.md) for deployment instructions.
```
7. Click "Publish release"

---

## Step 10: Update README with Actual URLs

After deploying (see DEPLOYMENT.md), come back and update:

```bash
# Edit README.md
# Update these lines:
# - **Application URL:** [your-actual-vercel-url]
# - **API Documentation:** [your-actual-render-url/swagger-ui.html]
```

Then commit and push:

```bash
git add README.md
git commit -m "Update README with live deployment URLs"
git push
```

---

## Common Issues and Solutions

### Issue: "Permission denied (publickey)"

**Solution:**
Use HTTPS instead of SSH:
```bash
git remote set-url origin https://github.com/YOUR_USERNAME/task-manager.git
```

### Issue: ".env file appeared in GitHub"

**Solution:**
1. **IMMEDIATELY** remove it:
```bash
git rm --cached backend/.env
git rm --cached frontend/.env
git commit -m "Remove accidentally committed .env files"
git push
```

2. Verify .gitignore includes:
```
.env
.env.local
.env.production
```

3. **IMPORTANT:** Your secrets are now public!
   - Generate new JWT_SECRET
   - Change database password (if you used real credentials)
   - Update Supabase keys

### Issue: "Repository too large"

**Solution:**
Likely node_modules or build artifacts weren't ignored:

```bash
# Remove from git (but keep locally)
git rm -r --cached node_modules/
git rm -r --cached backend/target/
git rm -r --cached frontend/dist/

# Commit
git commit -m "Remove node_modules and build artifacts"
git push
```

### Issue: "Merge conflict" when pushing

**Solution:**
```bash
# Pull first
git pull origin main --rebase

# Resolve any conflicts
# Then push
git push
```

---

## Next Steps

✅ **Congratulations!** Your code is now on GitHub!

**What's next:**

1. **Deploy your application** - See [DEPLOYMENT.md](./DEPLOYMENT.md)
2. **Add screenshots** - Create a `screenshots/` folder and add images to README
3. **Create demo video** - Record a 2-3 minute walkthrough
4. **Share on LinkedIn** - Announce your project!
5. **Add to resume** - Include GitHub link
6. **Star your own repo** - Why not? 😄

---

## Repository Best Practices

### Regular Commits

After making changes:
```bash
git add .
git commit -m "Descriptive message about what changed"
git push
```

### Good Commit Messages

**Good examples:**
- `Add notification feature with real-time updates`
- `Fix authentication bug when token expires`
- `Update README with deployment instructions`
- `Improve dashboard performance with memoization`

**Bad examples:**
- `update` (too vague)
- `fix bug` (which bug?)
- `changes` (what changes?)
- `asdf` (not helpful)

### Keep Your Repository Clean

- Never commit `.env` files
- Never commit `node_modules/`
- Never commit build artifacts (`target/`, `dist/`)
- Never commit large binary files
- Never commit API keys or passwords
- Use `.gitignore` properly

### Protect Your Main Branch

After deployment, consider:
1. Creating a `develop` branch for new features
2. Using Pull Requests for changes
3. Setting up branch protection rules

---

**Happy coding!** 🚀

*Last Updated: October 28, 2025*
