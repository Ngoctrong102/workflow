# Project Directory Structure Rules

**🚨 CRITICAL ENFORCEMENT RULE**: All files MUST be placed in the correct directory according to the project structure. **NO EXCEPTIONS**. Files placed in the wrong location will be automatically rejected or moved immediately.

## ⚠️ MANDATORY FILE PLACEMENT VALIDATION

**BEFORE creating ANY file, you MUST:**

1. ✅ **Identify file type** (documentation, code, script, config, test, etc.)
2. ✅ **Check the file placement rules below** for the correct directory
3. ✅ **Verify the target directory exists** or create it if needed
4. ✅ **Confirm the file does NOT belong in project root** (unless explicitly allowed)
5. ✅ **Create the file in the correct location** from the start

**If you create a file in the wrong location, you MUST move it immediately before proceeding.**

## Standard Project Structure

```
Zenty/
├── docs/                    # Documentation (source of truth)
│   ├── README.md            # Main documentation index
│   ├── docs-index.md        # Import system reference
│   ├── features/            # Feature descriptions
│   ├── api/                 # API specifications
│   ├── architecture/        # Architecture documentation
│   ├── database-schema/     # Database schemas
│   ├── security/            # Security documentation
│   ├── technical/           # Technical specifications
│   └── user-flows/          # User flow diagrams
│
├── backend/                 # Backend application (Go/Gin)
│   ├── cmd/                 # Application entry points
│   ├── internal/            # Internal packages (source code)
│   ├── pkg/                 # Public packages (if needed)
│   ├── migrations/          # Database migrations
│   ├── go.mod               # Go module definition
│   ├── go.sum               # Go dependencies checksum
│   ├── Dockerfile           # Backend Dockerfile
│   ├── docker-compose.yml   # Docker Compose configuration
│   ├── .dockerignore        # Docker ignore rules
│   └── README.md            # Backend-specific README
│
├── frontend/                 # Frontend application (React)
│   ├── src/                 # Source code
│   │   ├── components/      # React components
│   │   ├── pages/           # Page components
│   │   ├── services/        # API services
│   │   ├── utils/           # Utility functions
│   │   └── __tests__/       # Frontend tests (if exists)
│   ├── package.json         # NPM dependencies
│   └── vite.config.ts       # Vite configuration
│
├── frontend/
│   ├── src/                 # Source code
│   ├── mock-server/         # Frontend mock server (created by Frontend Expert)
│   ├── scripts/              # Frontend-specific scripts (created by Frontend Expert)
│   └── postman/             # Frontend API testing (created by Frontend Expert)
│
├── backend/
│   ├── src/                 # Source code
│   ├── mock-server/         # Backend mock server for third-party services (created by Backend Expert)
│   ├── scripts/              # Backend-specific scripts (created by Backend Expert)
│   └── postman/             # Backend API testing (created by Backend Expert)
│
├── mock-server/            # Shared mock services (if needed, separate from expert mock servers)
│   ├── api/                 # Main API mock server (port 3001) - Legacy, experts should create their own
│   ├── ekyc/                # Mock eKYC service (port 8082) - Legacy
│   └── payment-gateway/     # Mock payment gateway (port 8081) - Legacy
│
├── README.md                # Main project README (ONLY .md file allowed in root)
├── .cursorrules              # Main cursor rules file
├── .cursor-rules/           # Cursor rules (modular files)
│   ├── docs.md              # Documentation rules
│   ├── structure.md         # Project structure rules
│   └── code-quality.md      # Code quality rules
├── .gitignore               # Git ignore rules
└── [Other essential root configs only: .env.example, .editorconfig, etc.]
```

## 📋 File Placement Rules by Type

### 1. Documentation Files (`.md`)

**MANDATORY LOCATION**: `docs/` or appropriate subdirectory

- ✅ `docs/features/*.md` - Feature descriptions
- ✅ `docs/api/*.md` - API specifications
- ✅ `docs/architecture/*.md` - Architecture docs
- ✅ `docs/database-schema/*.md` - Database schemas
- ✅ `docs/security/*.md` - Security docs
- ✅ `docs/technical/*/*.md` - Technical specs
- ✅ `docs/user-flows/*.md` - User flow diagrams
- ✅ `README.md` - Main project README (ONLY exception in root)
- ✅ `backend/README.md` - Backend-specific README
- ✅ `scripts/README.md` - Scripts documentation
- ✅ `postman/README.md` - Postman documentation

**❌ FORBIDDEN**:
- ❌ Any `.md` file in project root (except `README.md`)
- ❌ Documentation files in `backend/`, `frontend/`, `scripts/` (except their own README.md)
- ❌ Temporary documentation (checklists, migration summaries) in root

### 2. Code Files

**Backend Code** (`.go`, `.yml`, `.sql`, `.env`):
- ✅ `backend/internal/` - Go source code (handlers, services, repositories, models)
- ✅ `backend/cmd/` - Application entry points
- ✅ `backend/migrations/` - Database migration files
- ✅ `backend/go.mod` - Go module definition
- ✅ `backend/go.sum` - Go dependencies checksum
- ✅ `backend/Dockerfile` - Backend Dockerfile

**Frontend Code** (`.tsx`, `.ts`, `.css`, `.json`):
- ✅ `frontend/src/` - All source code
- ✅ `frontend/src/components/` - React components
- ✅ `frontend/src/pages/` - Page components
- ✅ `frontend/src/services/` - API services
- ✅ `frontend/src/utils/` - Utility functions
- ✅ `frontend/package.json` - NPM config
- ✅ `frontend/vite.config.ts` - Vite config
- ✅ `frontend/tailwind.config.js` - Tailwind config

**❌ FORBIDDEN**:
- ❌ Any code files in project root
- ❌ Code files in wrong subdirectories

### 3. Scripts (`.sh`, `.js`, `.py`, `.bat`)

**MANDATORY LOCATION**: 
- ✅ `frontend/scripts/` - Frontend-specific scripts (created by Frontend Expert)
- ✅ `backend/scripts/` - Backend-specific scripts (created by Backend Expert)

**Script Types**:
- Frontend scripts: Build, test, dev tools (in `frontend/scripts/`)
- Backend scripts: Database setup, testing, deployment (in `backend/scripts/`)

**❌ FORBIDDEN**:
- ❌ Scripts in project root
- ❌ Shared scripts in root `scripts/` directory (experts create their own)
- ❌ Scripts mixed with source code

### 4. Configuration Files

**Docker Files**:
- ✅ `backend/docker-compose.yml` - Docker Compose config
- ✅ `backend/.dockerignore` - Docker ignore rules
- ✅ `backend/Dockerfile` - Backend Dockerfile
- ✅ `mock-server/*/Dockerfile` - Mock service Dockerfiles

**Build/Project Config**:
- ✅ `backend/go.mod` - Go module definition
- ✅ `backend/go.sum` - Go dependencies checksum
- ✅ `frontend/package.json` - NPM config
- ✅ `frontend/vite.config.ts` - Vite config
- ✅ `frontend/tsconfig.json` - TypeScript config
- ✅ `frontend/tailwind.config.js` - Tailwind config

**Root Config Files** (ONLY these allowed):
- ✅ `.cursorrules` - Main cursor rules
- ✅ `.cursor-rules/` - Cursor rules modules
- ✅ `.gitignore` - Git ignore
- ✅ `.env.example` - Environment example (if needed)
- ✅ `.editorconfig` - Editor config (if needed)

**❌ FORBIDDEN**:
- ❌ `docker-compose.yml` in root (belongs to `backend/`)
- ❌ `.dockerignore` in root (belongs to `backend/`)
- ❌ Any other config files in root

### 5. Mock Services

**MANDATORY LOCATION**: 
- ✅ `frontend/mock-server/` - Frontend mock server (created by Frontend Expert)
- ✅ `backend/mock-server/` - Backend mock server for third-party services (created by Backend Expert)
- ✅ `mock-server/` (root) - Shared mock servers (legacy, experts should create their own)

**Mock Server Requirements**:
- MUST be completely separate from source code
- Frontend mock: For API mocking during development
- Backend mock: For third-party service mocking (payment, eKYC)

**❌ FORBIDDEN**:
- ❌ Mixing mock server code with source code
- ❌ Creating mock servers outside respective directories

### 6. Test Files

**Backend Tests**:
- ✅ `backend/internal/` - Go test code (alongside source files with `_test.go` suffix)
- ✅ `backend/src/test/resources/` - Test resources

**Frontend Tests**:
- ✅ `frontend/src/__tests__/` - React component tests
- ✅ `frontend/tests/` - Integration tests (if exists)

**Integration/Utility Tests**:
- ✅ `scripts/test-*.js` - Test scripts
- ✅ `scripts/test-*.sh` - Test scripts

**❌ FORBIDDEN**:
- ❌ Test files in project root
- ❌ Test files mixed with source code (unless in `__tests__/`)

### 7. Postman Collections

**MANDATORY LOCATION**: 
- ✅ `frontend/postman/` - Frontend API testing collections (created by Frontend Expert)
- ✅ `backend/postman/` - Backend API testing collections (created by Backend Expert)

**Postman Collections**:
- Frontend: For testing API integration from frontend perspective
- Backend: For testing backend APIs

**❌ FORBIDDEN**:
- ❌ Postman files in project root
- ❌ Shared Postman collections in root `postman/` directory (experts create their own)

## 🚫 Prohibited Locations

### Project Root - STRICTLY FORBIDDEN

**ONLY these files are allowed in project root:**
- ✅ `README.md` - Main project README
- ✅ `.cursorrules` - Main cursor rules file
- ✅ `.cursor-rules/` - Cursor rules directory
- ✅ `.gitignore` - Git ignore rules
- ✅ `.env.example` - Environment example (if needed)
- ✅ `.editorconfig` - Editor config (if needed)

**EVERYTHING ELSE IS FORBIDDEN:**
- ❌ **Scripts** (`.sh`, `.js`, `.py`) → MUST be in `scripts/`
- ❌ **Documentation** (`.md` except `README.md`) → MUST be in `docs/`
- ❌ **Code files** (`.tsx`, `.ts`, `.go`, `.yml`) → MUST be in respective source directories
- ❌ **Test files** → MUST be in test directories
- ❌ **Config files** (`.json`, `.yml` except root configs) → MUST be in respective project directories
- ❌ **Docker files** (`docker-compose.yml`, `.dockerignore`) → MUST be in `backend/`
- ❌ **Temporary files** (checklists, migration summaries) → MUST be deleted or moved to `docs/`

## ✅ Pre-Creation Checklist

**BEFORE creating ANY file, answer these questions:**

1. **What type of file is this?**
   - [ ] Documentation (`.md`)
   - [ ] Code (`.go`, `.tsx`, `.ts`, `.yml`)
   - [ ] Script (`.sh`, `.js`, `.py`)
   - [ ] Configuration (`.json`, `.yml`, `.xml`)
   - [ ] Test file
   - [ ] Other

2. **Does this file belong in project root?**
   - [ ] NO → Continue to step 3
   - [ ] YES → Is it one of the allowed root files? (README.md, .cursorrules, .gitignore, etc.)
     - [ ] YES → Create in root
     - [ ] NO → **STOP. Find the correct directory.**

3. **What is the correct directory?**
   - Documentation → `docs/[category]/`
   - Backend code → `backend/internal/` or `backend/cmd/`
   - Frontend code → `frontend/src/[subdirectory]/`
   - Scripts → `scripts/`
   - Tests → Appropriate test directory
   - Config → Respective project directory

4. **Verify the directory exists:**
   - [ ] Directory exists → Create file
   - [ ] Directory doesn't exist → Create directory first, then create file

5. **Final check:**
   - [ ] File type matches directory purpose
   - [ ] File is NOT in project root (unless explicitly allowed)
   - [ ] File follows naming conventions

## 🔄 Automatic File Organization

**When generating files, ALWAYS:**

1. **Determine file type FIRST**
2. **Identify correct directory from rules above**
3. **Create file in correct location from the start**
4. **If file is created in wrong location, move it IMMEDIATELY**

**File Type → Directory Mapping:**

| File Type | Extension | Correct Directory |
|-----------|-----------|-------------------|
| Documentation | `.md` | `docs/[category]/` or `[project]/README.md` |
| Backend Code | `.go` | `backend/internal/` or `backend/cmd/` |
| Backend Config | `.yml`, `.env` | `backend/` or `backend/config/` |
| Backend Migrations | `.sql` | `backend/migrations/` |
| Frontend Code | `.tsx`, `.ts` | `frontend/src/[subdirectory]/` |
| Frontend Config | `.json`, `.js` | `frontend/` |
| Scripts | `.sh`, `.js`, `.py` | `scripts/` |
| Tests | `*_test.go`, `*.test.ts` | `backend/internal/` (alongside code) or `frontend/src/__tests__/` |
| Docker | `docker-compose.yml`, `.dockerignore` | `backend/` |
| Postman | `*.postman_*.json` | `postman/` |

## 🎯 Enforcement Rules

**CRITICAL**: These rules are MANDATORY and NON-NEGOTIABLE:

1. **No file creation in root** without explicit permission from this document
2. **All file placements must be validated** before creation
3. **Wrong placements must be corrected immediately** before proceeding
4. **No exceptions** unless explicitly listed in "Allowed Root Files" section

## 📝 File Naming Conventions

1. **Scripts**: kebab-case (e.g., `test-services.sh`, `start-app.sh`)
2. **Documentation**: kebab-case (e.g., `equipment-management.md`)
3. **React Components**: PascalCase (e.g., `EquipmentCard.tsx`)
4. **Go Packages**: lowercase, no underscores (e.g., `authservice`)
5. **Go Structs**: PascalCase (e.g., `AuthService`)
6. **Go Functions**: PascalCase for exported, camelCase for private (e.g., `GetUserByID()`, `getUserByID()`)
7. **Test Files**: Descriptive with `_test.go` suffix (e.g., `auth_service_test.go`)
7. **Config Files**: Follow framework conventions (e.g., `package.json`, `go.mod`)

## ⚠️ Common Mistakes to Avoid

1. ❌ Creating scripts in root → ✅ Use `scripts/`
2. ❌ Creating documentation in root → ✅ Use `docs/`
3. ❌ Creating code files in root → ✅ Use respective `src/` directories
4. ❌ Creating docker-compose.yml in root → ✅ Use `backend/docker-compose.yml`
5. ❌ Creating test files in root → ✅ Use test directories
6. ❌ Creating temporary files in root → ✅ Delete or move to `docs/`

## 🔍 Validation Process

**Every file creation MUST go through this validation:**

```
1. Identify file type
   ↓
2. Check file placement rules
   ↓
3. Verify target directory
   ↓
4. Confirm NOT in root (unless allowed)
   ↓
5. Create in correct location
   ↓
6. Verify placement after creation
```

**If validation fails at any step, STOP and correct before proceeding.**
