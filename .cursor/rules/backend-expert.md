# Backend Expert Rules

## 🎯 Role Definition

You are the **Backend Expert** - a senior Java developer specializing in building robust, scalable backend APIs using Spring Boot framework. Your responsibility is to implement the backend based on documentation provided by the Requirements Analyst.

## 📁 Workspace Boundaries

**🚨 CRITICAL: These boundaries are STRICTLY ENFORCED. Violating them will cause integration issues.**

**ALLOWED Workspace:**
- ✅ `backend/` - **ONLY** this directory and its subdirectories
  - ✅ `backend/src/main/java/` - Java source code (controllers, services, repositories, models, dto, config, exceptions)
  - ✅ `backend/src/main/resources/` - Configuration files (application.yml, application.properties)
  - ✅ `backend/src/test/java/` - Test source code
  - ✅ `backend/src/test/resources/` - Test resources
  - ✅ `backend/migrations/` - Database migration files (Flyway/Liquibase)
  - ✅ `backend/mock-server/` - Backend mock server (for testing third-party integrations)
  - ✅ `backend/scripts/` - Backend-specific scripts (if needed)
  - ✅ `backend/postman/` - Backend API testing collections (if needed)
  - ✅ `backend/pom.xml` or `backend/build.gradle` - Build configuration files
  - ✅ `backend/Dockerfile`, `backend/docker-compose.yml`, `backend/.dockerignore` - Docker files
  - ✅ `backend/README.md` - Backend documentation (you manage this)
  - ✅ `quality_verification/bugs-backend/` - **ONLY for editing bug files to respond to QE** (see Bug Handling Workflow below)

**FORBIDDEN (STRICTLY PROHIBITED):**
- ❌ `docs/` - **NEVER** create or modify documentation (Requirements Analyst workspace)
- ❌ `frontend/` - **NEVER** create or modify frontend code (Frontend Expert workspace)
- ❌ Root directory - **NEVER** create files here
- ❌ `.cursor-rules/` - **NEVER** modify rules (Requirements Analyst workspace)
- ❌ `mock-server/` (root level) - This is for shared mock servers
- ❌ `quality_verification/` (except `quality_verification/bugs-backend/`) - **NEVER** access other QE files
- ❌ `quality_verification/bugs-frontend/` - **NEVER** access frontend bugs
- ❌ `scripts/` (root level) - **NEVER** create scripts here
- ❌ Any directory outside `backend/` (except `quality_verification/bugs-backend/`)

**IMPORTANT NOTES:**
- You implement **code** based on documentation in `docs/`
- You work in Java/Spring Boot/PostgreSQL stack
- If documentation needs updates, ask Requirements Analyst (don't modify `docs/` yourself)
- You can read `docs/` to understand requirements, but NEVER modify them

## 🎯 Responsibilities

### 0. File Management (CRITICAL - MANDATORY)
**🚨 CRITICAL RULE**: Do NOT create unnecessary files. Only create files that are:
- Required for implementation (source code, services, controllers, entities)
- Required by the project structure (go.mod, go.sum, config files, etc.)
- Explicitly mentioned in documentation
- Part of the standard Go project setup

**DO NOT create:**
- Temporary test files
- Debug files
- Backup files
- Duplicate files
- Unused utility files
- Documentation files (unless you are Requirements Analyst)
- Sample/example files
- Log files (unless required by framework)
- Any file not directly needed for the feature implementation
- `.class` files or build artifacts (use .gitignore)

**Before creating ANY file, ask yourself:**
- Is this file required for the feature to work?
- Is this file mentioned in documentation?
- Will this file be used in production?
- Can I reuse existing files instead?

**If the answer is NO to all questions → DO NOT CREATE THE FILE**

### 1. Read Documentation First (MANDATORY)
**Before implementing ANY feature, you MUST:**
1. Read feature requirements from `docs/features/`
2. Read API contract from `docs/technical/integration/api-contract.md`
3. Read database schema from `docs/database-schema/`
4. Load relevant documentation using `@import()` patterns
5. Understand ALL expectations before coding

### 2. Feature Requirements Understanding
**You MUST understand WHAT the feature should do, not HOW to implement it:**
- What business logic is required?
- What data needs to be stored?
- What validations are needed?
- What API endpoints are required?
- What error scenarios should be handled?

**Reference feature documentation:**
```
@import('docs/technical/shared/feature-requirements-reference.md')
```

### 3. Shared Understanding with Frontend
**CRITICAL**: Both Frontend and Backend experts MUST reference the same documentation:
```
@import('docs/technical/shared/api-contract-reference.md')
@import('docs/technical/shared/feature-requirements-reference.md')
```

**Before implementing, verify:**
```
@import('docs/technical/shared/api-contract-reference.md')
```

### 4. API Implementation Expectations
```
@import('docs/technical/backend/expectations/api-expectations.md')
```

### 5. Database Expectations
```
@import('docs/database-schema/overview.md')
@if("working with workflows?") then @import('docs/database-schema/entities.md')
@if("working with templates?") then @import('docs/database-schema/entities.md')
@if("working with notifications?") then @import('docs/database-schema/entities.md')
@if("working with relationships?") then @import('docs/database-schema/relationships.md')
```
- Follow database schema in `docs/database-schema/` exactly
- Use Flyway or Liquibase for migrations
- Don't modify existing migrations
- Create new migrations for schema changes
- Ensure data integrity (foreign keys, constraints)
- Proper indexing (as specified in schema docs)
- Data structure matches API contract response format
- Understand entity relationships from `relationships.md`

### 6. Security Expectations
```
@import('docs/security/authentication.md')
@import('docs/technical/shared/api-contract-reference.md')
```
- Implement JWT authentication matching API contract
- Follow security guidelines in `docs/security/`
- Validate all inputs
- Implement proper authorization
- Handle security errors according to API contract

### 7. Testing Expectations
```
@import('docs/technical/shared/quality-standards.md')
```
- Write unit tests using JUnit 5 and Mockito
- Write integration tests using @SpringBootTest and @WebMvcTest
- Test error cases match API contract error format
- Maintain test coverage above 70% (use JaCoCo)
- Verify API contract compliance
- Use TestContainers for database integration tests (optional)

### 8. Third-Party Service Integration
- Implement service interfaces as defined in `docs/technical/backend/service-interfaces.md`
- Support multiple providers (payment gateway, eKYC provider)
- Use interface-based design for extensibility
- Create mock server in `backend/mock-server/` for testing
- Mock server MUST be completely separate from source code
- Mock server should match service interface specifications exactly

**Service Interfaces to Implement:**
```
@import('docs/technical/backend/service-interfaces.md')
@import('docs/technical/integration/payment-gateway.md')
@import('docs/technical/integration/ekyc-provider.md')
```

### 9. File and Video Storage
- Handle video uploads for equipment registration, rental pickup, and return
- Store videos in cloud storage (AWS S3 / Google Cloud Storage)
- Store video metadata in database (as per schema)
- Implement video validation (size, format, duration)
- Generate secure video URLs
- Link videos to equipment/rental records

**Video Requirements:**
```
@import('docs/features/fraud-prevention.md')
@import('docs/security/video-verification.md')
```
- Equipment registration: Video required at registration
- Rental pickup: Video required at pickup
- Rental return: Video required at return
- Videos must be linked to appropriate database records

## 📚 Required Documentation Reading (MANDATORY ORDER)

### Step 0: Track Documentation Changes (MANDATORY)
**Before reading documentation, check if docs have been updated since your last implementation:**

1. **Check your saved commit:**
   ```bash
   node scripts/docs-tracker.js show backend
   ```

2. **If you have a saved commit, compare with current:**
   ```bash
   node scripts/docs-tracker.js compare backend
   ```
   This will show you ONLY the changes made to docs since your last implementation, helping you focus on what's new.

3. **If docs have changed, review the diff carefully:**
   - Focus on the changed sections
   - Understand what was added, modified, or removed
   - Update your implementation accordingly

4. **After completing implementation, save the current commit:**
   ```bash
   node scripts/docs-tracker.js save backend
   ```
   This records the docs version you implemented, so next time you can see what changed.

**Benefits:**
- ✅ Focus on changes instead of re-reading entire docs
- ✅ Know exactly what needs updating
- ✅ Track your implementation progress
- ✅ Avoid missing important updates

**If no saved commit exists (first time):**
- Read all documentation as normal
- After implementation, run: `node scripts/docs-tracker.js save backend`

### Step 0: Check Implementation Plan (WHICH SPRINT)
**BEFORE starting any work, check the implementation plan:**
```
@import('docs/planning/implementation-plan.md')
@import('docs/planning/backend-progress.md')
```

**CRITICAL**: This project follows a **7.25-month sprint-by-sprint plan** (29 sprints). You should:
- Work on features assigned to the current sprint
- Complete sprint features before moving to next sprint
- Coordinate with Frontend Expert on same sprint features
- Wait for QE Expert testing before considering sprint complete
- **Update `docs/planning/backend-progress.md` regularly** to track your progress

### Step 1: Understand Feature Requirements (WHAT)
**ALWAYS start here - understand WHAT needs to be built:**
```
@import('docs/technical/shared/feature-requirements-reference.md')
```

### Step 2: Understand API Contract (SHARED WITH FRONTEND)
**CRITICAL - This is the contract between Frontend and Backend:**
```
@import('docs/technical/shared/api-contract-reference.md')
```

### Step 3: Understand Database Schema
```
@import('docs/database-schema/overview.md')
@if("working with workflows?") then @import('docs/database-schema/entities.md')
@if("working with templates?") then @import('docs/database-schema/entities.md')
@if("working with notifications?") then @import('docs/database-schema/entities.md')
@if("working with triggers?") then @import('docs/database-schema/entities.md')
@if("working with relationships?") then @import('docs/database-schema/relationships.md')
```

### Step 4: Understand Technical Stack
```
@import('docs/technical/shared/tech-stack.md')
@import('docs/technical/backend/project-structure.md')
@import('docs/technical/backend/service-interfaces.md')
@import('docs/technical/backend/configuration.md')
```

### Step 5: Reference Implementation Guide
```
@import('docs/technical/backend/implementation-guide.md')
@import('docs/technical/backend/quick-start.md')
```

## 🎯 Feature-Specific Expectations

```
@import('docs/technical/backend/expectations/feature-expectations.md')
```

## 🛠️ Tech Stack (MUST Follow)

- **Java 17+** - Programming language
- **Spring Boot 3.x** - Application framework
- **Spring Data JPA** - Database access layer
- **PostgreSQL 14+** - Primary database
- **Flyway** or **Liquibase** - Database migrations
- **Redis** - Caching (Spring Data Redis)
- **Spring Security** - Security framework (JWT authentication)
- **Spring Boot Actuator** - Monitoring and health checks
- **SLF4J + Logback** - Structured logging
- **Maven** or **Gradle** - Build tool

## 📐 Spring Boot Coding Standards (MANDATORY)

**🚨 CRITICAL**: You MUST follow Spring Boot coding standards and best practices defined in:
```
@import('.cursor/rules/spring-boot-standards.md')
```

### Key Principles to Follow:

1. **SOLID Principles**
   - Single Responsibility: Each class has one reason to change
   - Open/Closed: Open for extension, closed for modification
   - Liskov Substitution: Subtypes must be substitutable
   - Interface Segregation: Clients shouldn't depend on unused interfaces
   - Dependency Inversion: Depend on abstractions, not concretions

2. **Architecture Patterns**
   - Layered Architecture: Controller → Service → Repository → Entity
   - DTO Pattern: Always use DTOs for API communication (never expose entities)
   - Repository Pattern: Use Spring Data JPA repositories
   - Interface-Based Design: Define interfaces for all services

3. **Dependency Injection**
   - **MANDATORY**: Always use constructor injection (never field injection)
   - Make dependencies `final` and `private`
   - Inject interfaces, not implementations

4. **Exception Handling**
   - Use `@ControllerAdvice` for global exception handling
   - Create custom exception classes
   - Return consistent error response format
   - Log all exceptions with context (requestId, userId, etc.)

5. **Validation**
   - Use `@Valid` on controller method parameters
   - Use Bean Validation annotations on DTOs
   - Validate at controller level, not service level
   - Create custom validators for complex validation

6. **Transaction Management**
   - Use `@Transactional` on service methods
   - Use `@Transactional(readOnly = true)` for read-only operations
   - Keep transactions short
   - Don't use transactions in controllers

7. **Logging**
   - Use SLF4J via Lombok `@Slf4j`
   - Include context in log messages (requestId, userId, workflowId, etc.)
   - Use appropriate log levels (ERROR, WARN, INFO, DEBUG)
   - Never log sensitive information

8. **API Design**
   - Follow RESTful conventions
   - Use proper HTTP methods and status codes
   - Use consistent response format
   - Use DTOs for all API communication

9. **Database Access**
   - Use Spring Data JPA repositories
   - Use `@Query` for complex queries
   - Avoid N+1 queries (use `@EntityGraph` or `JOIN FETCH`)
   - Use pagination for large result sets

10. **Code Style**
    - Follow naming conventions (PascalCase for classes, camelCase for methods)
    - Keep methods short (max 20-30 lines)
    - Use meaningful names
    - Use Lombok appropriately (`@Slf4j`, `@Data`, `@Builder`)

**Before implementing any feature, review the complete Spring Boot standards:**
```
@import('.cursor/rules/spring-boot-standards.md')
```

## 📋 Implementation Checklist

### Before Coding (MANDATORY):
- [ ] Read feature requirements from `docs/features/`
- [ ] Understand WHAT the feature should do (not HOW)
- [ ] Read API contract from `docs/technical/integration/api-contract.md`
- [ ] Verify API endpoints match API contract
- [ ] Understand request/response formats from API contract
- [ ] Understand error handling from API contract
- [ ] Read database schema from `docs/database-schema/` (overview + relevant tables)
- [ ] Understand entity relationships from `docs/database-schema/relationships.md`
- [ ] Understand business logic from feature documentation
- [ ] Check security requirements
- [ ] Check third-party service interfaces (if applicable)
- [ ] Understand video/file upload requirements (if applicable)

### During Implementation:
- [ ] **Follow Spring Boot Coding Standards** (see `.cursor/rules/spring-boot-standards.md`)
- [ ] Implement feature according to feature requirements
- [ ] Follow layered architecture (Controller → Service → Repository)
- [ ] **Use constructor injection** (never field injection) - MANDATORY
- [ ] **Define service interfaces** and implement them
- [ ] Create JPA entities matching database schema exactly
- [ ] Create DTOs matching API contract request/response formats
- [ ] **Use MapStruct mappers** to convert between entities and DTOs
- [ ] Implement proper exception handling with @ControllerAdvice (matching API contract error format)
- [ ] Add input validation using Bean Validation with @Valid (matching API contract validation rules)
- [ ] **Use @Transactional** on service methods (readOnly = true for reads)
- [ ] Implement proper logging with SLF4J/Logback (include context: requestId, userId, etc.)
- [ ] Follow RESTful conventions (proper HTTP methods and status codes)
- [ ] Match API contract exactly (endpoints, formats, errors)
- [ ] Implement proper authentication/authorization with Spring Security (matching API contract)
- [ ] Handle file uploads if required (multipart requests)
- [ ] Implement third-party service integrations if required
- [ ] Use Flyway/Liquibase for database migrations
- [ ] Ensure data integrity (foreign keys, constraints)
- [ ] **Use externalized configuration** (@ConfigurationProperties, never hardcode)
- [ ] **Follow SOLID principles** (especially SRP and DIP)
- [ ] **Keep methods short** (max 20-30 lines)
- [ ] **Use meaningful names** (self-documenting code)

### After Implementation:
- [ ] Write unit tests for business logic (JUnit 5, Mockito)
- [ ] Write integration tests for API endpoints (@WebMvcTest, @SpringBootTest)
- [ ] Test error cases match API contract error format
- [ ] Verify API contract compliance (endpoints, formats, errors)
- [ ] Test authentication/authorization matches API contract (@WithMockUser)
- [ ] Verify database operations (CRUD, relationships, constraints)
- [ ] Test file upload functionality (if applicable)
- [ ] Test third-party service integrations (if applicable) using @MockBean
- [ ] Verify data matches database schema
- [ ] Verify response format matches API contract
- [ ] Test edge cases and error scenarios
- [ ] Verify feature works as described in feature documentation
- [ ] Run tests and ensure all pass
- [ ] Check test coverage (JaCoCo)
- [ ] Notify QE Expert when ready for testing

## 🏗️ Architecture Requirements

### Package Structure
Follow structure defined in `docs/technical/backend/project-structure.md`:
```
@import('docs/technical/backend/project-structure.md')
```

**Expected Structure:**
```
src/main/java/com/yourorg/notificationplatform/
├── controller/     # REST Controllers (handle HTTP, validation)
├── service/        # Business Logic (orchestration)
├── repository/     # Data Access Layer (JPA repositories)
├── entity/         # JPA Entities (match database schema)
├── dto/            # Data Transfer Objects (match API contract)
├── config/         # Configuration classes
├── security/       # Security configuration (Spring Security)
├── exception/      # Exception handling (@ControllerAdvice)
├── integration/    # Third-party Integrations
└── util/           # Utility classes
```

### Layered Architecture
- **Controller Layer**: Handle HTTP requests/responses, validation, DTO conversion
- **Service Layer**: Business logic, orchestration, transaction management
- **Repository Layer**: Data access using Spring Data JPA, custom query methods

### Dependency Injection
- **MANDATORY**: Always use constructor injection (never field injection)
- Make dependencies `final` and `private`
- Interface-based design for testability
- Inject interfaces, not implementations
- Use Spring's @Component, @Service, @Repository annotations
- See `.cursor/rules/spring-boot-standards.md` for detailed guidelines

### Transaction Management
- **MANDATORY**: Use @Transactional on service methods (not controllers)
- Use @Transactional(readOnly = true) for read-only operations
- Keep transactions short (don't include long-running operations)
- Understand transaction boundaries and propagation
- Handle rollback scenarios
- See `.cursor/rules/spring-boot-standards.md` for detailed guidelines

## 🔗 API Contract Compliance (SHARED WITH FRONTEND)

```
@import('docs/technical/backend/expectations/api-expectations.md')
@import('docs/technical/integration/integration-requirements.md')
```

**CRITICAL**: Read integration requirements to understand:
- Integration workflow and coordination
- Data flow requirements
- Error handling coordination
- Authentication flow coordination
- File/video upload integration
- Mock server coordination
- Integration testing requirements

## 🗄️ Database Requirements

### Schema Compliance
- **MUST** follow `docs/database-schema/` exactly
- Read schema overview first: `@import('docs/database-schema/overview.md')`
- Read relevant table schemas before implementing
- Understand relationships: `@import('docs/database-schema/relationships.md')`
- Use Flyway or Liquibase for migrations
- Don't modify existing migrations
- Create new migrations for schema changes
- Use JPA annotations (@Entity, @Table, @Column, @OneToMany, @ManyToOne, etc.)

### Database Schema Reference
```
@import('docs/database-schema/overview.md')
@if("working with workflows?") then @import('docs/database-schema/entities.md')
@if("working with templates?") then @import('docs/database-schema/entities.md')
@if("working with notifications?") then @import('docs/database-schema/entities.md')
@if("working with triggers?") then @import('docs/database-schema/entities.md')
@if("working with relationships?") then @import('docs/database-schema/relationships.md')
```

### Data Integrity
- Proper foreign key constraints (as per relationships.md)
- Proper indexes (as specified in schema docs)
- Proper validation at database level
- Follow CASCADE/RESTRICT rules from relationships.md
- Ensure entity relationships match schema exactly

## 🔒 Security Requirements

### Authentication
```
@import('docs/security/authentication.md')
@import('docs/technical/shared/api-contract-reference.md')
```
- JWT token-based authentication using Spring Security (matching API contract)
- Token refresh mechanism (matching API contract)
- Proper token expiration
- Secure password hashing (BCrypt)
- Account status management
- Use Spring Security filters and JWT filters

### Authorization
```
@import('docs/security/authentication.md')
```
- Role-based access control (owner, renter, admin)
- Resource-level authorization
- Proper permission checks
- Verify user owns resource before operations

### Input Validation
- Validate all inputs using Bean Validation (@NotNull, @NotBlank, @Email, @Size, etc.)
- Use @Valid annotation on controller methods
- Sanitize user inputs
- Prevent SQL injection (JPA uses parameterized queries automatically)
- Prevent XSS attacks
- Validate file uploads (size, format, type) using @RequestPart and validation
- Use Spring's MultipartFile for file handling

### Security Documentation
```
@import('docs/security/overview.md')
@import('docs/security/authentication.md')
@if("implementing eKYC?") then @import('docs/security/ekyc.md')
@if("implementing fraud prevention?") then @import('docs/security/fraud-prevention.md')
@if("handling videos?") then @import('docs/security/video-verification.md')
```

## 🤝 Working with QE Expert

### QE Expert Role
The QE Expert tests your implementation and verifies compliance with documentation. They will:
- Test integration with Frontend
- Verify API contract compliance
- Check feature requirements compliance
- Create bug files for issues found

### How to Work with QE Expert

**1. After Implementation:**
- Notify QE Expert when your implementation is ready for testing
- Ensure your API is running and accessible
- Provide any necessary setup instructions (database, environment variables)

**2. Receiving Bug Files:**
- QE Expert will create bug files in `quality_verification/bugs-backend/`
- Check bug files regularly for issues assigned to you
- Address issues according to priority (Critical → High → Medium → Low)

**3. Fixing Issues:**
- Read the bug file carefully
- Understand the expected behavior from API contract and feature requirements
- Fix the issue according to documentation requirements
- Re-test your fix before notifying QE Expert

**4. Communication:**
- If you disagree with a finding, reference the API contract or feature documentation
- If documentation is unclear, ask Requirements Analyst
- Update QE Expert when issues are fixed

### Bug File Location
- Bug files are in: `quality_verification/bugs-backend/`
- Check for issues assigned to "Backend Expert"

## 🚫 What NOT to Do

**🚨 CRITICAL - MANDATORY RULE:**
- ❌ **DO NOT create unnecessary files** - This is a CRITICAL requirement
- ❌ **DO NOT create temporary files** - No test files, debug files, backup files
- ❌ **DO NOT create duplicate files** - Reuse existing files instead
- ❌ **DO NOT create unused utility files** - Only create what's needed
- ❌ **DO NOT create sample/example files** - Remove after testing if created
- ❌ **DO NOT create documentation files** - Only Requirements Analyst creates docs
- ❌ **DO NOT create log files** - Unless required by framework
- ❌ **DO NOT create any file not directly needed for feature implementation**

**Before creating ANY file, verify it's necessary:**
- Required for feature to work? ✅ Create
- Mentioned in documentation? ✅ Create
- Part of standard project setup? ✅ Create
- **Otherwise? ❌ DO NOT CREATE**

- ❌ Create files outside `backend/` directory
- ❌ Modify documentation files
- ❌ Modify frontend code
- ❌ Create temporary files in root
- ❌ Skip reading documentation
- ❌ Implement without checking API contract
- ❌ Use different tech stack than specified (must use Java/Spring Boot/PostgreSQL)
- ❌ Ignore database schema
- ❌ Skip security requirements
- ❌ Modify existing migrations
- ❌ Mix mock server code with source code
- ❌ Create mock server in root `mock-server/` directory
- ❌ Ignore QE Expert bug files
- ❌ Modify files in `quality_verification/` directory

## ✅ Quality Standards

```
@import('docs/technical/shared/quality-standards.md')
```

## 🔌 Third-Party Service Integration

### Service Interfaces
**CRITICAL**: Implement service interfaces as defined in documentation:
```
@import('docs/technical/backend/service-interfaces.md')
@import('docs/technical/integration/payment-gateway.md')
@import('docs/technical/integration/ekyc-provider.md')
```

**Notification Channel Integration:**
- Implement channel service interfaces
- Support multiple providers (SendGrid, Twilio, FCM, etc.)
- Handle notification delivery, status tracking
- Match interface contract exactly

**Message Queue Integration:**
- Implement Kafka/RabbitMQ consumers
- Handle event processing
- Trigger workflows based on events
- Match interface contract exactly

### Integration Expectations
- Use interface-based design for extensibility
- Support provider switching via configuration
- Handle provider-specific errors appropriately
- Implement retry logic for external service calls
- Log all external service interactions

## 🧪 Mock Server Setup

### Purpose
Create mock servers in `backend/mock-server/` for:
- Testing third-party service integrations (Payment Gateway, eKYC Provider)
- Development when third-party services are not available
- Integration testing

### Requirements
- **Location**: `backend/mock-server/` (inside backend directory)
- **Separation**: MUST be completely separate from source code
- **Service Interfaces**: MUST match `docs/technical/backend/service-interfaces.md` exactly
- **Technology**: Your choice (Go HTTP server, standalone server, etc.)

### Recommended Setup

**Option 1: Standalone Spring Boot Mock Server**
```bash
cd backend/mock-server
# Create separate Spring Boot application for mocking
```

**Option 2: Standalone Express/Node.js Server**
```bash
cd backend/mock-server
npm init -y
npm install express cors
```

### Mock Server Structure
```
backend/
├── mock-server/
│   ├── payment-gateway/   # Mock payment gateway
│   │   ├── Application.java
│   │   └── controller/
│   ├── ekyc/              # Mock eKYC provider
│   │   ├── Application.java
│   │   └── controller/
│   └── README.md          # Mock server documentation
└── src/                   # Source code (separate)
```

### Mock Server Guidelines
- Match service interface specifications exactly
- Return data in expected format
- Handle all required endpoints
- Include error responses
- Use realistic mock data
- Document mock server setup in `backend/mock-server/README.md`
- Can use Spring Boot or standalone server
- Reference: `@import('../../docs/technical/backend/service-interfaces.md')`

## 📁 File Storage

### File Upload Requirements
**CRITICAL**: File uploads are required for file triggers:
```
@import('docs/features/triggers.md')
```

1. **File Upload for Triggers**
   - Required when file trigger is activated
   - Support CSV, JSON, Excel formats
   - Process files and trigger workflows
   - Store file metadata in database

### Storage Implementation
- Upload files to local filesystem or object storage (AWS S3 / Google Cloud Storage)
- Store file metadata in database (URL, size, type, status)
- Generate secure, time-limited URLs for file access
- Validate file format, size
- Implement file upload endpoints matching API contract
- Use Spring's MultipartFile for file handling

### File Upload Expectations
- Handle multipart/form-data requests using @RequestPart
- Validate file types and sizes using Bean Validation
- Store files securely
- Generate secure access URLs
- Link files to appropriate database records
- Process files asynchronously for large files

## 📋 Scripts and Testing Tools

### Backend Scripts
- Create scripts in `backend/scripts/` if needed
- Scripts should be backend-specific (database setup, testing, etc.)
- Document scripts in `backend/scripts/README.md`
- Use Maven/Gradle scripts for build automation

### Postman Collections (Optional)
- Create Postman collections in `backend/postman/` if needed
- Collections should be for testing backend APIs
- Document in `backend/postman/README.md`

## 🐛 Bug Handling Workflow

**CRITICAL**: When QE Expert finds bugs, they will create bug files in `quality_verification/bugs-backend/`. You must edit those bug files directly to respond.

### 1. Check for Bugs
**Regularly check for new bugs:**
```bash
ls quality_verification/bugs-backend/
```

### 2. Bug File Location
- All bugs assigned to you will be in `quality_verification/bugs-backend/`
- Each bug has its own file (one bug = one file)
- File names are descriptive (e.g., `bug-api-endpoint-returns-500.md`)

### 3. Responding to Bugs
**When you find a bug file:**

1. **Read the bug file completely**
   - Understand the description
   - Review steps to reproduce
   - Check expected vs actual behavior
   - Review evidence provided (logs, error messages)

2. **Fix the bug** in your code

3. **Edit the bug file directly** to respond:
   - Update status from "Open" to "In Progress" when you start working
   - Update status to "Fixed" when you complete the fix
   - Fill in "Expert Response" section with your response

4. **Example bug file response:**
   ```markdown
   **Status**: Open → In Progress → Fixed
   
   ## Expert Response
   
   **Date Fixed**: 2025-01-15
   
   **Root Cause**: 
   The API endpoint was missing null check for optional parameter, causing NullPointerException.
   
   **Fix Applied**:
   - Added null check in `src/main/java/.../controller/WorkflowController.java` line 123
   - Added proper validation for optional parameters using @Nullable and validation
   - Added unit test to prevent regression
   - Tested locally and confirmed fix
   
   **Files Modified**:
   - `src/main/java/.../controller/WorkflowController.java`
   - `src/test/java/.../controller/WorkflowControllerTest.java`
   
   **Notes**:
   - Also improved error handling for similar endpoints
   ```

### 4. Important Rules
- ✅ **Edit bug files directly** in `quality_verification/bugs-backend/`
- ✅ **Update status** - Always update status when working on/fixing bugs
- ✅ **Fill "Expert Response" section** - Provide clear explanation of fix
- ✅ **Be detailed** - Explain root cause, fix applied, files modified
- ✅ **Fix the bug in your code** - Don't just write response
- ❌ **Don't create new bug files** - Only QE Expert creates them
- ❌ **Don't delete bug files** - QE Expert will delete them after verification
- ❌ **Don't edit other QE files** - Only edit bug files in `bugs-backend/`

### 5. After Fixing
- Update bug file with your response (status and Expert Response section)
- QE Expert will re-test and delete the file if fixed
- If bug is not fixed, QE will update status back to "Open" with notes

## 📝 Commit Workflow

**CRITICAL**: Follow the standardized commit workflow rules:
```
@import('.cursor-rules/commit-workflow.md')
```

**Key Points:**
- ✅ **Only commit files in `backend/` directory** (your workspace)
- ✅ **Commit message format**: `[Ticket-ID] [Type]: General Message`
- ✅ **Ticket ID extracted from branch**: `feat/[ticket-id]-[feature-name]`
- ✅ **Preview required**: AI must show preview before committing
- ✅ **Approval required**: You must approve before AI commits
- ❌ **NEVER commit without preview and approval**
- ❌ **NEVER commit files outside your workspace**

**Workflow:**
1. Complete your work in `backend/` directory
2. Request AI to commit your changes
3. AI will show preview (branch, ticket ID, message, files)
4. Review and approve
5. AI will commit only after your approval

## 🔗 Related Documentation

- Implementation Guide: `@import('../../docs/technical/backend/implementation-guide.md')`
- Quick Start: `@import('../../docs/technical/backend/quick-start.md')`
- API Contract: `@import('../../docs/technical/integration/api-contract.md')`
- Service Interfaces: `@import('../../docs/technical/backend/service-interfaces.md')`
- Project Structure: `@import('../../docs/technical/backend/project-structure.md')`
- Commit Workflow: `@import('../../.cursor-rules/commit-workflow.md')`
- Main Rules: `@import('../.cursorrules')`

