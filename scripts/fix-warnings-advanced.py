#!/usr/bin/env python3
"""
Advanced script to fix more Java warnings:
- Fix null safety warnings by adding null checks
- Remove unused variables
- Fix type safety issues
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
        if 'test' in root:
            continue
        for file in files:
            if file.endswith('.java'):
                java_files.append(os.path.join(root, file))
    return java_files

def remove_unused_variables(content):
    """Remove unused local variables (conservative approach)"""
    lines = content.split('\n')
    result_lines = []
    removed_count = 0
    
    # Pattern: variable declaration followed by no usage
    # This is complex, so we'll be very conservative
    # Only remove obvious cases like: Type variable = ...; // never used
    
    i = 0
    while i < len(lines):
        line = lines[i]
        
        # Look for variable declarations
        # Pattern: Type varName = ...;
        var_match = re.search(r'(\w+)\s+(\w+)\s*=\s*[^;]+;', line)
        if var_match:
            var_type = var_match.group(1)
            var_name = var_match.group(2)
            
            # Check if variable is used later in the method
            # This is simplified - in reality we'd need to parse the method scope
            method_content = '\n'.join(lines[i:min(i+50, len(lines))])
            
            # Count occurrences
            occurrences = len(re.findall(r'\b' + re.escape(var_name) + r'\b', method_content))
            
            # If only appears once (the declaration), it's unused
            if occurrences == 1 and '//' not in line and '@' not in line:
                # Check if it's a common pattern we should keep
                if any(pattern in line for pattern in ['logger', 'result', 'response', 'request', 'config']):
                    result_lines.append(line)
                    i += 1
                    continue
                
                # Skip if it's a field assignment
                if 'this.' in line or 'super.' in line:
                    result_lines.append(line)
                    i += 1
                    continue
                
                print(f"  {YELLOW}Removing unused variable: {var_name}{RESET}")
                removed_count += 1
                i += 1
                continue
        
        result_lines.append(line)
        i += 1
    
    return '\n'.join(result_lines), removed_count

def fix_null_safety_warnings(content):
    """Add null checks for potential null pointer access"""
    lines = content.split('\n')
    result_lines = []
    fixed_count = 0
    
    i = 0
    while i < len(lines):
        line = lines[i]
        
        # Pattern: if (condition && obj.getMethod() != null) { var = obj.getMethod(); }
        # Should extract first
        if i + 1 < len(lines):
            next_line = lines[i + 1].strip()
            
            # Check for getBody() pattern
            if 'getBody()' in line and 'getBody()' in next_line and '=' in next_line:
                # Extract variable and method call
                var_match = re.search(r'(\w+)\s*=\s*(.*?)\.getBody\(\)', next_line)
                if var_match:
                    var_name = var_match.group(1)
                    obj_expr = var_match.group(2)
                    
                    # Rewrite
                    indent = len(line) - len(line.lstrip())
                    indent_str = ' ' * indent
                    
                    # Move assignment before if
                    result_lines.append(indent_str + next_line)
                    
                    # Update if condition
                    new_line = re.sub(
                        r'\.getBody\(\)\s*!=\s*null',
                        f'{var_name} != null',
                        line
                    )
                    result_lines.append(new_line)
                    
                    print(f"  {GREEN}Fixed null safety warning{RESET}")
                    fixed_count += 1
                    i += 2
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
        
        # Remove unused variables (conservative)
        new_content, removed_count = remove_unused_variables(content)
        if removed_count > 0:
            content = new_content
            changes_made = True
        
        # Fix null safety
        new_content, fixed_count = fix_null_safety_warnings(content)
        if fixed_count > 0:
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
        root_dir = os.path.join(os.path.dirname(__file__), '..', 'backend', 'src', 'main', 'java')
    
    if not os.path.exists(root_dir):
        print(f"{RED}Directory not found: {root_dir}{RESET}")
        sys.exit(1)
    
    print(f"{GREEN}Advanced warning fixes in: {root_dir}{RESET}")
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

if __name__ == '__main__':
    main()

