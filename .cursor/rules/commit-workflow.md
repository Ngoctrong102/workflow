# Commit Workflow Rules

## 🎯 Overview

This document defines the standardized commit workflow for all experts. **CRITICAL**: All experts must follow these rules when committing changes.

## 📋 Core Rules

### 1. Commit Only Your Work
**CRITICAL**: Each expert must **ONLY commit their own work**:
- ✅ **Frontend Expert**: Only commit files in `frontend/` directory
- ✅ **Backend Expert**: Only commit files in `backend/` directory
- ✅ **QE Expert**: Only commit files in `quality_verification/` directory
- ✅ **Requirements Analyst**: Only commit files in `docs/`, `.cursor-rules/`, or root allowed files

**NEVER commit:**
- ❌ Files outside your workspace
- ❌ Files from other experts' workspaces
- ❌ Unrelated changes mixed with your work

### 2. Commit Message Format

**MANDATORY Format:**
```
[Ticket-ID] [Type]: General Message

- Details 1
- Details 2
- Details 3
```

**Components:**
- **Ticket-ID**: Extracted from current branch name (see below)
- **Type**: One of `Feat`, `Fix`, `Hotfix`, `Release`, `Chore`
- **General Message**: Brief description of the change
- **Details**: Bullet points with specific changes (one per line, prefixed with `-`)

### 3. Ticket ID Extraction

**Branch Pattern**: `feat/[ticket-id]-[feature-name]`

**Examples:**
- Branch: `feat/zen12-equipment-management` → Ticket ID: `ZEN-12`
- Branch: `feat/zen45-payment-integration` → Ticket ID: `ZEN-45`
- Branch: `fix/zen78-bug-fix` → Ticket ID: `ZEN-78`

**Extraction Logic:**
1. Get current branch name
2. Extract ticket ID from branch pattern
3. Convert to uppercase (e.g., `zen12` → `ZEN-12`)

### 4. Commit Types

- **Feat**: New feature implementation
- **Fix**: Bug fix
- **Hotfix**: Critical production fix
- **Release**: Release preparation
- **Chore**: Maintenance, documentation, refactoring (no functional changes)

### 5. Preview and Approval Required

**MANDATORY Workflow:**

1. **Stage your changes** (only files in your workspace)
2. **AI will show preview:**
   - ✅ Current branch name
   - ✅ Extracted ticket ID
   - ✅ Commit message preview
   - ✅ List of files to be committed
   - ✅ Details from your description

3. **You MUST approve:**
   - Review commit message format
   - Verify ticket ID is correct
   - Verify only your files are included
   - Approve if everything is correct

4. **AI will commit ONLY after approval:**
   - Stage the approved files
   - Create commit with proper format
   - Show commit hash

**NEVER commit without:**
- ❌ Preview shown
- ❌ User approval
- ❌ Verification of files and message

## 📝 Workflow Steps

### Step 1: Complete Your Work
- Finish implementing your changes
- Ensure all files are in your workspace
- Test your changes (if applicable)

### Step 2: Prepare Commit Description
Describe your changes clearly:
- What was changed?
- Why was it changed?
- What are the specific details?

### Step 3: Request Commit
- Ask AI to commit your work
- AI will analyze current branch and changes
- AI will extract ticket ID from branch name

### Step 4: Review Preview
AI will show:
- Branch name
- Extracted ticket ID
- Commit message preview
- Files to be committed
- Details list

### Step 5: Approve or Reject
- **If correct**: Approve the commit
- **If incorrect**: Request corrections
- **AI will NOT commit until approved**

### Step 6: Commit Execution
After approval:
- AI stages the approved files
- AI creates commit with proper format
- AI shows commit hash and summary

## 📋 Examples

### Example 1: Frontend Expert

**Branch**: `feat/zen12-equipment-management`

**Changes**: Added equipment registration form component

**Commit Message**:
```
[ZEN-12] Feat: Add equipment registration form

- Create EquipmentRegistrationForm component
- Add form validation with Zod
- Integrate video recorder component
- Add image upload functionality
- Implement form submission handler
```

### Example 2: Backend Expert

**Branch**: `feat/zen12-equipment-management`

**Changes**: Implemented equipment registration API endpoint

**Commit Message**:
```
[ZEN-12] Feat: Add equipment registration endpoint

- Create equipment registration handler
- Add multipart form data support
- Implement file upload service integration
- Add validation for equipment data
- Create equipment repository methods
```

### Example 3: QE Expert

**Branch**: `feat/zen12-equipment-management`

**Changes**: Added test data for equipment testing

**Commit Message**:
```
[ZEN-12] Chore: Add equipment test data

- Create test data SQL scripts
- Add test equipment records
- Create test user accounts
- Add test data documentation
```

### Example 4: Requirements Analyst

**Branch**: `feat/zen12-equipment-management`

**Changes**: Updated documentation for equipment feature

**Commit Message**:
```
[ZEN-12] Chore: Update equipment feature documentation

- Update equipment API documentation
- Add equipment registration flow diagram
- Update database schema documentation
- Add equipment feature requirements
```

## ⚠️ Important Notes

### File Filtering
- AI must filter files to only include files in expert's workspace
- AI must exclude files from other experts' workspaces
- AI must verify workspace boundaries before committing

### Branch Detection
- AI must detect current branch name
- AI must extract ticket ID from branch pattern
- AI must handle edge cases (no branch, invalid pattern)

### Approval Required
- **NEVER commit without user approval**
- Always show preview first
- Wait for explicit approval before committing
- If user rejects, ask for corrections

### Workspace Boundaries
- Frontend Expert: `frontend/` only
- Backend Expert: `backend/` only
- QE Expert: `quality_verification/` only
- Requirements Analyst: `docs/`, `.cursor-rules/`, root allowed files only

## 🚫 What NOT to Do

- ❌ Commit without preview
- ❌ Commit without approval
- ❌ Commit files outside your workspace
- ❌ Mix multiple experts' work in one commit
- ❌ Use incorrect ticket ID
- ❌ Use incorrect commit type
- ❌ Skip details in commit message
- ❌ Commit unrelated changes

## ✅ Checklist Before Committing

- [ ] All changes are in my workspace
- [ ] No files from other experts included
- [ ] Commit message format is correct
- [ ] Ticket ID is extracted correctly
- [ ] Commit type is appropriate
- [ ] Details are clear and specific
- [ ] Preview has been shown
- [ ] User has approved
- [ ] Only approved files will be committed

