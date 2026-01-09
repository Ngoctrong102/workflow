# Documentation Rules

## Documentation as Source of Truth

**CRITICAL RULE**: All code, features, and implementations MUST reference and align with the documentation in the `/docs` folder. The documentation is the single source of truth for this project.

### Documentation Discovery

**IMPORTANT**: Always check `docs/README.md` and `docs/docs-index.md` first to discover the current documentation structure. These files are the authoritative source for available documentation modules.

**Discovery Process:**
1. Read `docs/README.md` to understand current structure
2. Check `docs/docs-index.md` for import patterns and available modules
3. Use pattern matching to find relevant docs (e.g., `docs/**/*equipment*.md`)
4. If a specific file doesn't exist, search for similar files in the same category

### Documentation Structure Pattern

The documentation follows a modular structure organized by category:

```
docs/
├── README.md                    # Main index - ALWAYS CHECK FIRST
├── docs-index.md                # Import system reference - CHECK FOR PATTERNS
├── [category]/                  # Category folders (features, api, security, etc.)
│   └── [topic].md               # Topic files within category
└── technical/                   # Technical specifications
    ├── frontend/
    ├── backend/
    └── integration/
```

**Categories are discovered dynamically** - do not hard-code category names. Common categories include:
- `features/` - Feature descriptions
- `api/` - API specifications
- `database-schema/` - Database schemas
- `security/` - Security documentation
- `architecture/` - Architecture documentation
- `user-flows/` - User flow diagrams
- `technical/` - Technical specifications

### Import System

Use the import system to load relevant documentation context:

**Syntax:**
- `@import('path/to/file.md')` - Import entire file
- `@if(condition) then @import('path/to/file.md')` - Conditional import
- `@import('category/*')` - Import all files in category (use sparingly)

**Pattern-Based Imports:**
- Use wildcards when exact file names are unknown: `@import('features/*equipment*.md')`
- Use category patterns: `@import('[category]/[topic].md')` where category and topic are discovered
- Always verify file existence before importing

**Examples (Pattern-Based):**
- `@import('database-schema/*user*.md')` - Find user-related schema files
- `@import('features/*rental*.md')` - Find rental-related features
- `@if("working on authentication?") then @import('security/*auth*.md') @import('api/*auth*.md')`
- `@if("implementing payment?") then @import('features/*payment*.md') @import('api/*payment*.md') @import('technical/integration/*payment*.md')`

### Context Loading Rules (Pattern-Based)

1. **Before implementing any feature:**
   - Discover feature docs: Search `docs/features/` for relevant files
   - Load related API specs: Search `docs/api/` for matching endpoints
   - Load database schema: Search `docs/database-schema/` for related tables
   - Load security requirements: `@if("security related?") then @import('security/*[relevant]*.md')`

2. **Before modifying database:**
   - Discover schema docs: Search `docs/database-schema/` for table files
   - Load relationships: `@import('database-schema/*relationship*.md')` or `@import('database-schema/overview.md')`
   - Check architecture: `@import('architecture/*.md')`

3. **Before adding API endpoints:**
   - Discover API specs: Search `docs/api/` for relevant endpoint files
   - Load common patterns: `@import('api/common.md')` (if exists)
   - Check security: `@import('security/*auth*.md')`

4. **Before implementing security features:**
   - Discover security docs: Search `docs/security/` for relevant files
   - Load compliance: `@import('security/*compliance*.md')` or `@import('security/overview.md')`

5. **Before implementing video features:**
   - Search for fraud prevention: `@import('features/*fraud*.md')` or `@import('features/*video*.md')`
   - Search for video verification: `@import('security/*video*.md')`
   - Search for user flows: `@import('user-flows/*rental*.md')` or `@import('user-flows/*lifecycle*.md')`

6. **Before frontend development (Frontend Expert):**
   - **MANDATORY**: Read `.cursor-rules/frontend-expert.md` first
   - Load quick start: `@import('technical/frontend/quick-start.md')`
   - Load implementation guide: `@import('technical/frontend/implementation-guide.md')`
   - Load technical specs: `@import('technical/frontend/overview.md')`
   - Load design system: `@import('technical/frontend/design-system.json')`
   - Load components: `@import('technical/frontend/*component*.md')`
   - Load routing: `@import('technical/frontend/*routing*.md')`
   - Load API contract: `@import('technical/integration/api-contract.md')`

7. **Before backend development (Backend Expert):**
   - **MANDATORY**: Read `.cursor-rules/backend-expert.md` first
   - Load quick start: `@import('technical/backend/quick-start.md')`
   - Load implementation guide: `@import('technical/backend/implementation-guide.md')`
   - Load technical specs: `@import('technical/backend/overview.md')`
   - Load project structure: `@import('technical/backend/*structure*.md')`
   - Load service interfaces: `@import('technical/backend/*interface*.md')`
   - Load configuration: `@import('technical/backend/*config*.md')`
   - Load API contract: `@import('technical/integration/api-contract.md')`

8. **Before third-party integration:**
   - Load service interfaces: `@import('technical/backend/*interface*.md')`
   - Load integration specs: `@import('technical/integration/*[provider]*.md')`
   - Load related features: Search `docs/features/` for integration-related features

### Implementation Guidelines

1. **Always check documentation first** before implementing new features
   - Start with `docs/README.md` to understand structure
   - Use `docs/docs-index.md` for import patterns
   - Search for relevant docs using patterns

2. **Update documentation** when making design changes
   - Find the appropriate category folder
   - Update or create relevant documentation files
   - Maintain consistency with existing structure

3. **Follow the architecture** defined in `docs/architecture/`
   - Load architecture docs dynamically: `@import('architecture/*.md')`

4. **Adhere to database schema** defined in `docs/database-schema/`
   - Load schema docs dynamically: `@import('database-schema/*[table-name]*.md')`

5. **Implement security measures** as specified in `docs/security/`
   - Load security docs dynamically: `@import('security/*[aspect]*.md')`

6. **Follow API specifications** in `docs/api/`
   - Load API docs dynamically: `@import('api/*[endpoint]*.md')`

7. **Respect user flows** defined in `docs/user-flows/`
   - Load user flow docs dynamically: `@import('user-flows/*[flow-name]*.md')`

8. **Follow technical specifications** in `docs/technical/`
   - Frontend: `@import('technical/frontend/*.md')` or specific files
   - Backend: `@import('technical/backend/*.md')` or specific files
   - Integration: `@import('technical/integration/*.md')` or specific files

### Video Verification Requirements

**CRITICAL**: The three-point video verification system is mandatory:
1. Registration video (equipment registration)
2. Pickup video (rental start)
3. Return video (rental end)

All video-related implementations MUST reference:
- Search for fraud prevention: `@import('features/*fraud*.md')` or `@import('features/*prevention*.md')`
- Search for video verification: `@import('security/*video*.md')` or `@import('security/*verification*.md')`
- Search for user flows: `@import('user-flows/*rental*.md')` or `@import('user-flows/*lifecycle*.md')`

### Documentation Updates

When making changes that affect documentation:

1. **Discover the appropriate location:**
   - Check `docs/README.md` for category structure
   - Use pattern matching to find existing files
   - Create new files following existing naming conventions

2. **Update relevant documentation:**
   - **Features**: Update or create in `docs/features/[feature-name].md`
   - **API**: Update or create in `docs/api/[endpoint-category].md`
   - **Database**: Update or create in `docs/database-schema/[table-name].md`
   - **Security**: Update or create in `docs/security/[aspect].md`
   - **Architecture**: Update or create in `docs/architecture/[component].md`
   - **Technical**: Update or create in `docs/technical/[category]/[topic].md`
   - **User Flows**: Update or create in `docs/user-flows/[flow-name].md`

3. **Maintain consistency:**
   - Follow existing file naming patterns
   - Use consistent markdown structure
   - Include cross-references with `@import()` syntax
   - Update `docs/README.md` if adding new categories

### Quick Reference Patterns

**Common import patterns (use pattern matching):**

Equipment Management:
```
@import('features/*equipment*.md')
@import('api/*equipment*.md')
@import('database-schema/*equipment*.md')
@import('user-flows/*owner*.md')
```

Rental Process:
```
@import('features/*rental*.md')
@import('api/*rental*.md')
@import('database-schema/*rental*.md')
@import('user-flows/*renter*.md')
@import('features/*fraud*.md')
```

Payment Processing:
```
@import('features/*payment*.md')
@import('api/*payment*.md')
@import('database-schema/*payment*.md')
@import('security/*compliance*.md')
@import('technical/integration/*payment*.md')
```

Authentication/Security:
```
@import('security/*auth*.md')
@import('security/*ekyc*.md')
@import('api/*auth*.md')
@import('user-flows/*ekyc*.md')
```

Frontend Development (Frontend Expert):
```
# MANDATORY: Read role rules first
@import('../../.cursor-rules/frontend-expert.md')

# Load implementation guide
@import('technical/frontend/implementation-guide.md')
@import('technical/frontend/quick-start.md')

# Load technical specs
@import('technical/frontend/overview.md')
@import('technical/frontend/design-system.json')
@import('technical/frontend/*component*.md')

# Load feature-specific docs
@import('features/*[relevant-feature]*.md')
@import('api/*[relevant-endpoint]*.md')

# Load API contract (CRITICAL for integration)
@import('technical/integration/api-contract.md')
```

Backend Development (Backend Expert):
```
# MANDATORY: Read role rules first
@import('../../.cursor-rules/backend-expert.md')

# Load implementation guide
@import('technical/backend/implementation-guide.md')
@import('technical/backend/quick-start.md')

# Load technical specs
@import('technical/backend/overview.md')
@import('technical/backend/*structure*.md')
@import('technical/backend/*interface*.md')

# Load feature-specific docs
@import('features/*[relevant-feature]*.md')
@import('api/*[relevant-endpoint]*.md')
@import('database-schema/*[relevant-table]*.md')

# Load API contract (CRITICAL for integration)
@import('technical/integration/api-contract.md')
```

### Error Handling for Missing Documentation

If a specific documentation file cannot be found:

1. **Search for similar files** using pattern matching
2. **Check `docs/README.md`** for current structure
3. **Check `docs/docs-index.md`** for import patterns
4. **Use category-level imports** as fallback: `@import('[category]/*.md')`
5. **Create missing documentation** following existing patterns
6. **Update `docs/README.md`** if adding new categories

### Documentation Structure Evolution

**Rules are resilient to documentation changes:**
- Use pattern matching instead of hard-coded paths
- Always check `docs/README.md` first for current structure
- Use wildcards and category patterns
- Discover files dynamically rather than assuming specific names
- Adapt to new categories automatically

**When new categories are added:**
- Rules automatically work with new categories
- Use pattern matching: `@import('[new-category]/*[topic]*.md')`
- Update `docs/README.md` to include new category
- Follow existing naming conventions

