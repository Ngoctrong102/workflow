# Scripts for Fixing Java Warnings

This directory contains scripts to automatically fix common Java warnings in the codebase.

## Scripts

### 1. `fix-warnings.py`
Basic script that fixes:
- Unused imports
- Unnecessary `@SuppressWarnings("unchecked")` annotations
- Raw type `Map` warnings (basic cases)

**Usage:**
```bash
python3 scripts/fix-warnings.py [path_to_java_files]
```

**Default:** Processes `backend/src/main/java`

### 2. `fix-all-warnings-complete.py`
Complete script that processes both main and test code:
- Removes unused imports (including test files)
- Removes unnecessary `@SuppressWarnings` annotations
- More comprehensive import detection

**Usage:**
```bash
python3 scripts/fix-all-warnings-complete.py [path_to_src_directory]
```

**Default:** Processes `backend/src` (both main and test)

### 3. `fix-all-warnings.sh`
Bash script that runs all fix scripts and compiles to verify:
- Runs basic fix script
- Runs complete fix script
- Compiles and reports remaining warnings

**Usage:**
```bash
bash scripts/fix-all-warnings.sh
```

## Warnings That Cannot Be Auto-Fixed

The following warnings require manual intervention or are not critical:

1. **Null Type Safety Warnings** (from Spring annotations)
   - These are static analysis warnings from Spring's `@NonNull` annotations
   - They don't affect runtime behavior
   - Example: `Null type safety: The expression of type 'String' needs unchecked conversion to conform to '@NonNull String'`

2. **Type Safety: Unchecked Cast**
   - These occur when casting from `Object` to generic types
   - Can be suppressed with `@SuppressWarnings("unchecked")` if the cast is safe
   - Example: `Type safety: Unchecked cast from Object to Map<String,Object>`

3. **Unused Variables/Fields**
   - May be used in future features
   - Should be reviewed manually before removal
   - Example: `The value of the local variable workflow is not used`

4. **Missing Non-Null Annotations**
   - Interface method annotations
   - Can be added manually if needed
   - Example: `Missing non-null annotation: inherited method specifies this parameter as @NonNull`

## Running All Fixes

To fix all warnings that can be automatically fixed:

```bash
cd /path/to/workflow
bash scripts/fix-all-warnings.sh
```

Or run individually:

```bash
# Fix main code
python3 scripts/fix-warnings.py backend/src/main/java

# Fix test code
python3 scripts/fix-warnings.py backend/src/test/java

# Or use complete script
python3 scripts/fix-all-warnings-complete.py
```

## Verification

After running scripts, verify compilation:

```bash
cd backend
mvn compile -q
```

## Notes

- Scripts are conservative and won't break code
- Always review changes before committing
- Some warnings are expected and don't need fixing
- Test files are processed separately to avoid breaking tests

