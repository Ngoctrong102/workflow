# Commit Command

This command implements the standardized commit workflow for all experts.

## Workflow

When user requests to commit changes:

1. **Identify Current Role and Workspace**
   - Determine which expert role is active (Frontend, Backend, QE, or Requirements Analyst)
   - Identify workspace boundaries based on role:
     - Frontend Expert: `frontend/` directory only
     - Backend Expert: `backend/` directory only
     - QE Expert: `quality_verification/` directory only
     - Requirements Analyst: `docs/`, `.cursor-rules/`, or root allowed files only

2. **Get Current Branch**
   - Run: `git branch --show-current`
   - Extract branch name

3. **Extract Ticket ID**
   - Branch pattern: `feat/[ticket-id]-[feature-name]` or `fix/[ticket-id]-[feature-name]`
   - Extract ticket ID from branch name
   - Convert to uppercase format (e.g., `zen12` → `ZEN-12`)
   - Examples:
     - `feat/zen12-equipment-management` → `ZEN-12`
     - `feat/zen45-payment-integration` → `ZEN-45`
     - `fix/zen78-bug-fix` → `ZEN-78`

4. **Analyze Git Status**
   - Run: `git status --porcelain`
   - Filter files to ONLY include files in expert's workspace
   - Exclude files from other experts' workspaces
   - Identify staged and unstaged files

5. **Determine Commit Type**
   - Ask user or infer from changes:
     - **Feat**: New feature implementation
     - **Fix**: Bug fix
     - **Hotfix**: Critical production fix
     - **Release**: Release preparation
     - **Chore**: Maintenance, documentation, refactoring (no functional changes)

6. **Get Commit Details**
   - Ask user to describe their changes:
     - What was changed?
     - Why was it changed?
     - What are the specific details?
   - Convert description into bullet points (one per line, prefixed with `-`)

7. **Show Preview (MANDATORY)**
   Display the following information:
   ```
   📋 Commit Preview
   
   Branch: [branch-name]
   Ticket ID: [TICKET-ID]
   Commit Type: [Type]
   
   Commit Message:
   [TICKET-ID] [Type]: General Message
   
   - Detail 1
   - Detail 2
   - Detail 3
   
   Files to be committed:
   - [file1]
   - [file2]
   - [file3]
   
   ⚠️ Only files in your workspace will be committed.
   ```

8. **Request Approval**
   - Ask user: "Please review the commit preview above. Type 'approve' to proceed or describe any changes needed."
   - **DO NOT commit until user explicitly approves**

9. **After Approval**
   - Stage only approved files (in expert's workspace)
   - Create commit with format:
     ```
     [TICKET-ID] [Type]: General Message
     
     - Detail 1
     - Detail 2
     - Detail 3
     ```
   - Run: `git commit -m "[message]"`
   - Show commit hash and summary

10. **If User Rejects**
    - Ask what needs to be changed
    - Update preview with corrections
    - Show preview again
    - Request approval again

## Critical Rules

- ❌ **NEVER commit without preview**
- ❌ **NEVER commit without user approval**
- ❌ **NEVER commit files outside expert's workspace**
- ❌ **NEVER mix multiple experts' work in one commit**
- ✅ **ALWAYS filter files by workspace boundaries**
- ✅ **ALWAYS show preview before committing**
- ✅ **ALWAYS wait for explicit approval**

## Commit Message Format

**MANDATORY Format:**
```
[Ticket-ID] [Type]: General Message

- Details 1
- Details 2
- Details 3
```

**Components:**
- **Ticket-ID**: Extracted from current branch name (e.g., `ZEN-12`)
- **Type**: One of `Feat`, `Fix`, `Hotfix`, `Release`, `Chore`
- **General Message**: Brief description of the change
- **Details**: Bullet points with specific changes (one per line, prefixed with `-`)

## Examples

### Example 1: Frontend Expert
**Branch**: `feat/zen12-equipment-management`
**Files**: `frontend/src/components/EquipmentForm.tsx`, `frontend/src/components/EquipmentList.tsx`

**Preview:**
```
📋 Commit Preview

Branch: feat/zen12-equipment-management
Ticket ID: ZEN-12
Commit Type: Feat

Commit Message:
[ZEN-12] Feat: Add equipment registration form

- Create EquipmentRegistrationForm component
- Add form validation with Zod
- Integrate video recorder component
- Add image upload functionality
- Implement form submission handler

Files to be committed:
- frontend/src/components/EquipmentForm.tsx
- frontend/src/components/EquipmentList.tsx

⚠️ Only files in your workspace will be committed.
```

### Example 2: Backend Expert
**Branch**: `feat/zen12-equipment-management`
**Files**: `backend/src/internal/handler/equipment_handler.go`, `backend/src/internal/service/equipment_service.go`

**Preview:**
```
📋 Commit Preview

Branch: feat/zen12-equipment-management
Ticket ID: ZEN-12
Commit Type: Feat

Commit Message:
[ZEN-12] Feat: Add equipment registration endpoint

- Create equipment registration handler
- Add multipart form data support
- Implement file upload service integration
- Add validation for equipment data
- Create equipment repository methods

Files to be committed:
- backend/src/internal/handler/equipment_handler.go
- backend/src/internal/service/equipment_service.go

⚠️ Only files in your workspace will be committed.
```

### Example 3: Requirements Analyst
**Branch**: `feat/zen12-equipment-management`
**Files**: `docs/api/equipment.md`, `.cursor-rules/equipment-rules.md`

**Preview:**
```
📋 Commit Preview

Branch: feat/zen12-equipment-management
Ticket ID: ZEN-12
Commit Type: Chore

Commit Message:
[ZEN-12] Chore: Update equipment feature documentation

- Update equipment API documentation
- Add equipment registration flow diagram
- Update database schema documentation
- Add equipment feature requirements

Files to be committed:
- docs/api/equipment.md
- .cursor-rules/equipment-rules.md

⚠️ Only files in your workspace will be committed.
```
