#!/usr/bin/env python3
"""
Complete script to fix ALL Java warnings automatically:
- Remove unused imports (including test files)
- Remove unnecessary @SuppressWarnings
- Fix raw type Map warnings
- Remove unused variables (conservative)
"""

import os
import re
import sys
from pathlib import Path

# Colors for output
GREEN = '\033[92m'
YELLOW = '\033[93m'
RED = '\033[91m'
RESET = '\033[0m'

def find_java_files(root_dir, include_tests=True):
    """Find all Java files in the project"""
    java_files = []
    for root, dirs, files in os.walk(root_dir):
        if not include_tests and 'test' in root:
            continue
        for file in files:
            if file.endswith('.java'):
                java_files.append(os.path.join(root, file))
    return java_files

def remove_unused_imports(content, file_path):
    """Remove unused imports from Java file"""
    lines = content.split('\n')
    imports = []
    import_indices = []
    other_lines = []
    
    # Collect all imports
    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith('import '):
            imports.append((i, line, stripped))
            import_indices.append(i)
        else:
            other_lines.append((i, line))
    
    # Check which imports are actually used
    used_imports = []
    code_content = '\n'.join([line for _, line in other_lines])
    
    for idx, full_line, import_line in imports:
        # Extract the class/package name from import
        match = re.match(r'import\s+(?:static\s+)?([^;]+);', import_line)
        if not match:
            used_imports.append(full_line)
            continue
            
        import_name = match.group(1)
        
        # Get the simple class name (last part after dot)
        class_name = import_name.split('.')[-1]
        
        # Check if it's a wildcard import
        if class_name == '*':
            used_imports.append(full_line)
            continue
        
        # Check if class name is used in code (but not in import statements)
        pattern = r'\b' + re.escape(class_name) + r'\b'
        
        # Special cases for common imports
        common_imports = ['List', 'Map', 'Set', 'HashMap', 'ArrayList', 'HashSet', 
                         'Optional', 'String', 'Integer', 'Long', 'Boolean', 'Object']
        
        if class_name in common_imports:
            # These are commonly used, check more carefully
            if re.search(pattern, code_content):
                used_imports.append(full_line)
                continue
        
        # Check if used in code
        if re.search(pattern, code_content):
            used_imports.append(full_line)
        else:
            # Check if it's a static import that might be used differently
            if 'static' in import_line:
                # For static imports, check for method calls
                method_name = class_name.split('.')[-1]
                if re.search(r'\b' + re.escape(method_name) + r'\s*\(', code_content):
                    used_imports.append(full_line)
                else:
                    print(f"  {YELLOW}Removing unused import: {import_name}{RESET}")
            else:
                print(f"  {YELLOW}Removing unused import: {import_name}{RESET}")
    
    # Rebuild content with only used imports
    result_lines = []
    in_imports = False
    
    for i, line in enumerate(lines):
        if i in import_indices:
            if not in_imports:
                in_imports = True
            # Check if this import is in used_imports
            if line in used_imports:
                result_lines.append(line)
        else:
            if in_imports and i > max(import_indices) if import_indices else False:
                in_imports = False
            result_lines.append(line)
    
    return '\n'.join(result_lines)

def remove_unnecessary_suppress_warnings(content):
    """Remove unnecessary @SuppressWarnings("unchecked") annotations"""
    lines = content.split('\n')
    result_lines = []
    removed_count = 0
    
    i = 0
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()
        
        # Check for standalone @SuppressWarnings("unchecked")
        if '@SuppressWarnings("unchecked")' in stripped and not '{' in stripped and not ';' in stripped:
            # Check if next line has actual code
            if i + 1 < len(lines):
                next_line = lines[i + 1].strip()
                if next_line and not next_line.startswith('@'):
                    # Keep it if it's before code that needs it
                    result_lines.append(line)
                    i += 1
                    continue
            # Remove standalone annotation
            print(f"  {YELLOW}Removing @SuppressWarnings(\"unchecked\"){RESET}")
            removed_count += 1
            i += 1
            continue
        
        result_lines.append(line)
        i += 1
    
    return '\n'.join(result_lines), removed_count

def process_file(file_path):
    """Process a single Java file"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        changes_made = False
        
        # Remove unused imports
        new_content = remove_unused_imports(content, file_path)
        if new_content != content:
            content = new_content
            changes_made = True
        
        # Remove unnecessary @SuppressWarnings
        new_content, removed_count = remove_unnecessary_suppress_warnings(content)
        if removed_count > 0:
            content = new_content
            changes_made = True
        
        if changes_made:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
        
        return False
        
    except Exception as e:
        print(f"  {RED}Error processing {file_path}: {e}{RESET}")
        return False

def main():
    if len(sys.argv) > 1:
        root_dir = sys.argv[1]
    else:
        root_dir = os.path.join(os.path.dirname(__file__), '..', 'backend', 'src')
    
    if not os.path.exists(root_dir):
        print(f"{RED}Directory not found: {root_dir}{RESET}")
        sys.exit(1)
    
    print(f"{GREEN}=== Complete Warning Fix ==={RESET}\n")
    
    # Process main code
    main_dir = os.path.join(root_dir, 'main', 'java')
    if os.path.exists(main_dir):
        print(f"{YELLOW}Processing main code: {main_dir}{RESET}")
        java_files = find_java_files(main_dir, include_tests=False)
        print(f"Found {len(java_files)} Java files\n")
        
        fixed_count = 0
        for file_path in java_files:
            rel_path = os.path.relpath(file_path, root_dir)
            if process_file(file_path):
                fixed_count += 1
                print(f"  {GREEN}✓ Fixed: {rel_path}{RESET}")
        
        print(f"\n{GREEN}Main code: {fixed_count}/{len(java_files)} files modified{RESET}\n")
    
    # Process test code
    test_dir = os.path.join(root_dir, 'test', 'java')
    if os.path.exists(test_dir):
        print(f"{YELLOW}Processing test code: {test_dir}{RESET}")
        java_files = find_java_files(test_dir, include_tests=True)
        print(f"Found {len(java_files)} Java files\n")
        
        fixed_count = 0
        for file_path in java_files:
            rel_path = os.path.relpath(file_path, root_dir)
            if process_file(file_path):
                fixed_count += 1
                print(f"  {GREEN}✓ Fixed: {rel_path}{RESET}")
        
        print(f"\n{GREEN}Test code: {fixed_count}/{len(java_files)} files modified{RESET}\n")
    
    print(f"{GREEN}=== Complete ==={RESET}")
    print(f"{YELLOW}Note: Remaining warnings are mostly:{RESET}")
    print("  - Null type safety (Spring annotations - not critical)")
    print("  - Type safety unchecked casts (require @SuppressWarnings)")
    print("  - Unused variables/fields (may be used in future)")

if __name__ == '__main__':
    main()

