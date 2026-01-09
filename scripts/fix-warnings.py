#!/usr/bin/env python3
"""
Script to automatically fix common Java warnings:
- Remove unused imports
- Remove unnecessary @SuppressWarnings
- Fix simple null safety issues
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

def find_java_files(root_dir):
    """Find all Java files in the project"""
    java_files = []
    for root, dirs, files in os.walk(root_dir):
        # Skip test files for now, focus on main code
        if 'test' in root:
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
        # Use word boundaries to avoid partial matches
        pattern = r'\b' + re.escape(class_name) + r'\b'
        
        # Special cases for common imports that might appear in comments
        if class_name in ['List', 'Map', 'Set', 'HashMap', 'ArrayList', 'HashSet']:
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
                if re.search(r'\b' + re.escape(class_name.split('.')[-1]) + r'\s*\(', code_content):
                    used_imports.append(full_line)
                else:
                    print(f"  {YELLOW}Removing unused import: {import_name}{RESET}")
            else:
                print(f"  {YELLOW}Removing unused import: {import_name}{RESET}")
    
    # Rebuild content with only used imports
    result_lines = []
    in_imports = False
    import_section_end = -1
    
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
        
        # Check for @SuppressWarnings("unchecked")
        if '@SuppressWarnings("unchecked")' in stripped:
            # Check if next line has actual code or another annotation
            if i + 1 < len(lines):
                next_line = lines[i + 1].strip()
                # If next line is empty or another annotation, might be necessary
                if next_line and not next_line.startswith('@'):
                    # Check if it's really unnecessary by looking for unchecked operations
                    # For now, we'll be conservative and only remove if it's on same line as code
                    if '{' in stripped or ';' in stripped:
                        # It's on same line, might be removable but be conservative
                        result_lines.append(line)
                        i += 1
                        continue
            # Remove standalone @SuppressWarnings("unchecked")
            print(f"  {YELLOW}Removing @SuppressWarnings(\"unchecked\"){RESET}")
            removed_count += 1
            i += 1
            continue
        
        result_lines.append(line)
        i += 1
    
    return '\n'.join(result_lines), removed_count

def fix_raw_type_map(content):
    """Fix raw type Map warnings by parameterizing"""
    # This is more complex and might break code, so we'll be conservative
    # Only fix obvious cases like ResponseEntity<Map> -> ResponseEntity<Map<String, Object>>
    patterns = [
        (r'ResponseEntity<Map>', r'ResponseEntity<Map<String, Object>>'),
        (r'ResponseEntity<Map\s*>', r'ResponseEntity<Map<String, Object>>'),
    ]
    
    for pattern, replacement in patterns:
        if re.search(pattern, content):
            content = re.sub(pattern, replacement, content)
            print(f"  {GREEN}Fixed raw type Map{RESET}")
    
    return content

def fix_potential_null_pointer(content):
    """Fix potential null pointer access by reordering checks"""
    lines = content.split('\n')
    result_lines = []
    fixed_count = 0
    
    i = 0
    while i < len(lines):
        line = lines[i]
        
        # Pattern: if (condition && response.getBody() != null) { body = response.getBody(); }
        # Should be: body = response.getBody(); if (condition && body != null) {
        if i + 2 < len(lines):
            current = lines[i].strip()
            next_line = lines[i + 1].strip()
            next_next = lines[i + 2].strip()
            
            # Check for pattern
            if ('getBody() != null' in current or 'getBody() != null' in next_line) and \
               'getBody()' in next_next and '=' in next_next:
                # Extract variable name
                match = re.search(r'(\w+)\s*=\s*.*\.getBody\(\)', next_next)
                if match:
                    var_name = match.group(1)
                    # Rewrite to extract first, then check
                    if_match = re.search(r'if\s*\((.*?)\)\s*\{', current)
                    if if_match:
                        condition = if_match.group(1)
                        # Replace getBody() != null with var_name != null
                        new_condition = re.sub(r'\.getBody\(\)\s*!=\s*null', f'{var_name} != null', condition)
                        # Move assignment before if
                        result_lines.append('            ' + next_next)
                        result_lines.append('            if (' + new_condition + ') {')
                        print(f"  {GREEN}Fixed potential null pointer access{RESET}")
                        fixed_count += 1
                        i += 3
                        continue
        
        result_lines.append(line)
        i += 1
    
    return '\n'.join(result_lines), fixed_count

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
        
        # Fix raw type Map
        new_content = fix_raw_type_map(content)
        if new_content != content:
            content = new_content
            changes_made = True
        
        # Fix potential null pointer (conservative)
        # Skip for now as it's complex
        
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
        root_dir = os.path.join(os.path.dirname(__file__), '..', 'backend', 'src', 'main', 'java')
    
    if not os.path.exists(root_dir):
        print(f"{RED}Directory not found: {root_dir}{RESET}")
        sys.exit(1)
    
    print(f"{GREEN}Scanning Java files in: {root_dir}{RESET}")
    java_files = find_java_files(root_dir)
    print(f"Found {len(java_files)} Java files\n")
    
    fixed_count = 0
    for file_path in java_files:
        rel_path = os.path.relpath(file_path, root_dir)
        print(f"Processing: {rel_path}")
        if process_file(file_path):
            fixed_count += 1
            print(f"  {GREEN}✓ Fixed{RESET}\n")
        else:
            print(f"  {YELLOW}No changes needed{RESET}\n")
    
    print(f"\n{GREEN}Summary:{RESET}")
    print(f"  Files processed: {len(java_files)}")
    print(f"  Files modified: {fixed_count}")
    print(f"\n{YELLOW}Note: Some warnings may require manual review{RESET}")

if __name__ == '__main__':
    main()

