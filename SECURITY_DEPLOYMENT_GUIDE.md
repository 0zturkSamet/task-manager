# Security Deployment Guide

This guide provides step-by-step instructions for securely deploying the Task Manager application after implementing the security fixes.

---

## 🔒 Critical Security Requirements

Before deploying to production, you MUST complete these steps:

### 1. Generate and Set JWT Secret

**CRITICAL:** Never use the old hardcoded JWT secret!

```bash
# Generate a secure random JWT secret (run this on your local machine)
openssl rand -base64 64

# Example output:
# Kh8Yv5Zq3Wm9Tp2Xj7Rn4Gc6Fd1Bs0Aq8Lp5Vo3Un2Hm9Ek7Dw6Cs4Bt2Ar0==
```

**Set this as an environment variable in your backend deployment:**
- **Render/Heroku:** Add to Environment Variables section
- **Netlify Functions:** Add to Site Settings > Build & Deploy > Environment
- **Docker:** Pass as environment variable or use secrets

```bash
JWT_SECRET=<your-generated-secret-here>
```

### 2. Update CORS Configuration for Production

Edit `backend/src/main/java/com/taskmanager/config/SecurityConfig.java`:

```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:3000",
    "http://localhost:5173",
    "http://localhost:5174",
    "https://your-actual-frontend.netlify.app",  // Add your real domain
    "https://your-actual-frontend.vercel.app"     // Add your real domain
));
```

### 3. Set Spring Profile to Production

Add environment variable in your backend deployment:
```bash
SPRING_PROFILES_ACTIVE=prod
```

This will:
- Disable Swagger UI
- Set logging to WARN/INFO levels
- Use production-optimized settings

---

## 📋 Deployment Checklist

### Backend Deployment (Render/Heroku/Railway)

- [ ] Set `JWT_SECRET` environment variable (use generated secret from step 1)
- [ ] Set `DATABASE_URL` to your PostgreSQL connection string
- [ ] Set `DATABASE_USERNAME` to your database username
- [ ] Set `DATABASE_PASSWORD` to your database password
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Set `JWT_EXPIRATION_MS=86400000` (24 hours, or customize)
- [ ] Update CORS allowed origins in code to your frontend domain
- [ ] Deploy backend and verify it starts successfully
- [ ] Check logs for any startup errors

### Frontend Deployment (Netlify/Vercel)

- [ ] Set `VITE_API_URL` environment variable to your backend URL
  ```bash
  VITE_API_URL=https://your-backend.render.com
  ```
- [ ] Build and deploy frontend
- [ ] Test authentication flow
- [ ] Verify all API calls work correctly

---

## 🛡️ Security Features Implemented

### ✅ Authentication & Authorization
- JWT tokens with configurable expiration (default 24 hours)
- BCrypt password hashing with strength 12
- Password complexity requirements: min 12 chars, uppercase, lowercase, number, special char
- No hardcoded secrets - all externalized to environment variables

### ✅ Rate Limiting
- 5 login/register attempts per 15 minutes per IP address
- Automatic IP detection with proxy header support
- 429 Too Many Requests response when limit exceeded

### ✅ Input Validation & Sanitization
- Length limits on all text fields:
  - Task titles: 200 characters
  - Task descriptions: 5000 characters
  - Comments: 2000 characters
  - Project descriptions: 1000 characters
- OWASP HTML Sanitizer utility class available for XSS prevention
- Jakarta validation on all DTOs

### ✅ Authorization Controls
- IDOR vulnerabilities fixed:
  - `/api/users/all` requires authentication, limited to 50 results
  - `/api/users/search` requires authentication, min 2 char search, limited to 20 results
- Project membership checks on all project/task endpoints
- Role-based access control (OWNER, ADMIN, VIEWER)

### ✅ Security Headers
- Content Security Policy (CSP)
- X-Frame-Options: DENY (clickjacking protection)
- X-XSS-Protection: 1; mode=block
- X-Content-Type-Options: nosniff
- Restrictive CORS policy (specific domains only)

### ✅ Logging & Monitoring
- Production logging set to WARN/INFO (no sensitive data in logs)
- Debug logging only in development profile
- SQL logging disabled in production

### ✅ API Documentation
- Swagger UI disabled by default
- Only enabled in development profile
- Not accessible in production

---

## 🧪 Post-Deployment Testing

After deployment, test these security features:

### 1. Test Rate Limiting
```bash
# Try 6 login attempts quickly - 6th should be rate limited
for i in {1..6}; do
  curl -X POST https://your-backend.com/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"wrong"}'
  echo "\nAttempt $i"
done

# Expected: 5 attempts succeed (possibly with 401), 6th returns 429
```

### 2. Test Password Complexity
```bash
# Should fail - too weak
curl -X POST https://your-backend.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"test@example.com",
    "password":"simple",
    "firstName":"Test",
    "lastName":"User"
  }'

# Should succeed - strong password
curl -X POST https://your-backend.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"test@example.com",
    "password":"SecureP@ssw0rd123!",
    "firstName":"Test",
    "lastName":"User"
  }'
```

### 3. Test IDOR Protection
```bash
# Should require authentication (401)
curl https://your-backend.com/api/users/all
```

### 4. Test Swagger Disabled
```bash
# Should return 404 or 403 in production
curl https://your-backend.com/swagger-ui.html
```

### 5. Test Security Headers
```bash
# Check security headers are present
curl -I https://your-backend.com/api/auth/login | grep -i "x-frame-options\|content-security-policy\|x-xss-protection"
```

---

## 🔐 Password Requirements

Users must now create passwords that meet these criteria:
- **Minimum length:** 12 characters
- **Maximum length:** 128 characters
- **Required characters:**
  - At least one uppercase letter (A-Z)
  - At least one lowercase letter (a-z)
  - At least one number (0-9)
  - At least one special character (@$!%*?&)

### Example Valid Passwords:
- `MyP@ssw0rd123!`
- `Secure&P4ssword`
- `Tr0ng@Password2024`

### Example Invalid Passwords:
- `password` (too short, no uppercase, no number, no special char)
- `Password123` (no special character)
- `PASSWORD123!` (no lowercase)
- `MyPassword!` (no number)

---

## 📊 Monitoring & Alerts

### What to Monitor:

1. **Rate Limiting Events**
   - Watch for "Rate limit exceeded" log entries
   - High rate limiting may indicate brute force attack

2. **Failed Authentication Attempts**
   - Multiple failed logins from same IP
   - Could indicate credential stuffing attack

3. **Unusual API Access Patterns**
   - Requests to Swagger in production (should be 404)
   - High volume requests from single IP
   - Requests without proper authentication headers

4. **Application Errors**
   - JWT validation failures
   - Database connection issues
   - Any 500 errors (investigate immediately)

---

## 🚨 Incident Response

If you detect a security issue:

1. **Rotate JWT Secret Immediately**
   ```bash
   # Generate new secret
   openssl rand -base64 64

   # Update environment variable
   # Restart backend service
   ```
   This will invalidate all existing user sessions.

2. **Check Logs for Attack Patterns**
   - Look for repeated failed logins
   - Check for unusual IP addresses
   - Identify compromised accounts

3. **Block Malicious IPs (if needed)**
   - Add IP blocking at infrastructure level (Cloudflare, AWS WAF, etc.)

4. **Notify Users (if data breach)**
   - Inform users to change passwords
   - Explain what data was accessed
   - Follow GDPR/privacy law requirements

---

## 📦 Environment Variables Summary

### Backend (Required)

| Variable | Description | Example |
|----------|-------------|---------|
| `JWT_SECRET` | **CRITICAL** - Secret for signing JWT tokens | `<generated-64-char-base64>` |
| `DATABASE_URL` | PostgreSQL connection string | `jdbc:postgresql://host:5432/db` |
| `DATABASE_USERNAME` | Database username | `postgres` |
| `DATABASE_PASSWORD` | Database password | `<secure-password>` |
| `SPRING_PROFILES_ACTIVE` | Spring profile (use `prod` for production) | `prod` |

### Backend (Optional)

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_EXPIRATION_MS` | JWT token expiration in milliseconds | `86400000` (24 hours) |
| `PORT` | Server port | `8080` |

### Frontend (Required)

| Variable | Description | Example |
|----------|-------------|---------|
| `VITE_API_URL` | Backend API base URL | `https://api.yourdomain.com` |

---

## 🔄 Updating Production

When pushing security updates:

1. **Test locally first**
   ```bash
   # Backend
   cd backend
   JWT_SECRET=$(openssl rand -base64 64) ./mvnw spring-boot:run

   # Frontend
   cd frontend
   npm run dev
   ```

2. **Deploy backend first** (zero-downtime if possible)

3. **Deploy frontend** after backend is confirmed working

4. **Monitor logs** for first 30 minutes after deployment

5. **Test critical flows**:
   - User registration
   - User login
   - Create project
   - Create task
   - Add comment

---

## ✅ Security Compliance

The implemented fixes address:

### OWASP Top 10 2021
- ✅ **A01:2021 - Broken Access Control** - IDOR fixed, auth required
- ✅ **A02:2021 - Cryptographic Failures** - No hardcoded secrets, strong password hashing
- ✅ **A03:2021 - Injection** - Parameterized queries, input validation
- ✅ **A04:2021 - Insecure Design** - Rate limiting, password complexity
- ✅ **A05:2021 - Security Misconfiguration** - Logging fixed, Swagger disabled in prod
- ✅ **A06:2021 - Vulnerable Components** - Dependencies updated
- ✅ **A07:2021 - Authentication Failures** - Strong passwords, rate limiting

### General Security
- ✅ Password hashing: BCrypt strength 12
- ✅ Session management: Stateless JWT
- ✅ Security headers: CSP, X-Frame-Options, etc.
- ✅ CORS: Restrictive policy
- ✅ Rate limiting: Brute force protection
- ✅ Input validation: Length limits, type checking
- ✅ Error handling: No information leakage

---

## 📞 Support

If you encounter issues during deployment:

1. Check application logs first
2. Verify all environment variables are set correctly
3. Test locally with production-like configuration
4. Review the `SECURITY_AUDIT_REPORT.md` for detailed vulnerability information

---

## 📝 Changelog

### Security Fixes Implemented (Current Version)

1. **CRITICAL:** Removed hardcoded JWT secret
2. **CRITICAL:** Added rate limiting on authentication endpoints
3. **CRITICAL:** Fixed IDOR vulnerability in user endpoints
4. **HIGH:** Added comprehensive security headers
5. **HIGH:** Fixed logging configuration (no sensitive data)
6. **HIGH:** Disabled Swagger in production
7. **MEDIUM:** Updated vulnerable npm dependencies (glob, js-yaml, vite)
8. **MEDIUM:** Implemented password complexity requirements
9. **MEDIUM:** Tightened CORS configuration
10. **MEDIUM:** Added input length validation
11. **MEDIUM:** Created HTML sanitization utility

---

**Last Updated:** November 19, 2025
**Security Audit Report:** See `SECURITY_AUDIT_REPORT.md` for full details
