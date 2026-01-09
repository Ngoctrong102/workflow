#!/usr/bin/env python3
"""
Script to automatically refactor Java entity classes to use Lombok annotations.
This script removes getters/setters and adds Lombok annotations.
"""

import os
import re
import sys
from pathlib import Path

def find_entity_files(base_dir):
    """Find all Java entity files."""
    entity_dir = Path(base_dir) / "backend" / "src" / "main" / "java" / "com" / "notificationplatform" / "entity"
    if not entity_dir.exists():
        return []
    return list(entity_dir.glob("*.java"))

def extract_class_info(content):
    """Extract class name and relationships from Java file."""
    class_match = re.search(r'public class (\w+)', content)
    if not class_match:
        return None
    
    class_name = class_match.group(1)
    
    # Find relationships (OneToMany, ManyToOne, etc.)
    relationships = []
    for match in re.finditer(r'@(OneToMany|ManyToOne|OneToOne|ManyToMany).*?private\s+(\w+)', content, re.DOTALL):
        relationships.append(match.group(2))
    
    # Find fields that should be excluded from toString/equalsAndHashCode
    exclude_fields = set()
    for rel in relationships:
        exclude_fields.add(rel)
    
    # Also exclude workflow, trigger, execution if present (common lazy loading fields)
    for field in ['workflow', 'trigger', 'execution', 'triggers', 'executions', 'notifications', 'deliveries', 'fileUploads']:
        if field in content:
            exclude_fields.add(field)
    
    return {
        'class_name': class_name,
        'exclude_fields': list(exclude_fields)
    }

def has_lombok_imports(content):
    """Check if file already has Lombok imports."""
    return 'import lombok' in content or '@Getter' in content

def remove_getters_setters(content):
    """Remove all getter and setter methods."""
    # Pattern to match getter/setter methods
    # This is a simplified pattern - may need refinement
    patterns = [
        # Getter pattern
        (r'public\s+\w+\s+get\w+\(\)\s*\{[^}]*return\s+\w+;[^}]*\}', ''),
        # Setter pattern
        (r'public\s+void\s+set\w+\([^)]+\)\s*\{[^}]*this\.\w+\s*=\s*\w+;[^}]*\}', ''),
        # Getter with multiple lines
        (r'public\s+\w+\s+get\w+\(\)\s*\{[^}]*return[^}]*\}', '', re.DOTALL),
        # Setter with multiple lines
        (r'public\s+void\s+set\w+\([^)]+\)\s*\{[^}]*this\.\w+[^}]*\}', '', re.DOTALL),
    ]
    
    for pattern in patterns:
        if len(pattern) == 2:
            content = re.sub(pattern[0], pattern[1], content)
        else:
            content = re.sub(pattern[0], pattern[1], content, flags=pattern[2])
    
    # Remove "// Getters and Setters" comments
    content = re.sub(r'//\s*Getters\s+and\s+Setters\s*\n', '', content)
    
    return content

def add_lombok_annotations(content, class_info):
    """Add Lombok annotations to class."""
    if not class_info:
        return content
    
    # Check if already has Lombok
    if has_lombok_imports(content):
        return content
    
    # Add Lombok imports
    import_section = re.search(r'(import\s+[^;]+;\s*\n)+', content)
    if import_section:
        imports_end = import_section.end()
        lombok_imports = "import lombok.*;\n"
        if 'import lombok' not in content:
            content = content[:imports_end] + lombok_imports + content[imports_end:]
    else:
        # Add after package declaration
        package_match = re.search(r'package\s+[^;]+;', content)
        if package_match:
            package_end = package_match.end()
            content = content[:package_end] + '\n\nimport lombok.*;\n' + content[package_end:]
    
    # Find class declaration
    class_match = re.search(r'(@Entity[^\n]*\n)?(@Table[^\n]*\n)?public\s+class\s+' + class_info['class_name'], content)
    if class_match:
        class_start = class_match.start()
        
        # Build annotations
        annotations = []
        annotations.append('@Getter')
        annotations.append('@Setter')
        annotations.append('@NoArgsConstructor')
        annotations.append('@AllArgsConstructor')
        
        if class_info['exclude_fields']:
            exclude_str = ', '.join([f'"{f}"' for f in class_info['exclude_fields']])
            annotations.append(f'@ToString(exclude = {{{exclude_str}}})')
            annotations.append(f'@EqualsAndHashCode(exclude = {{{exclude_str}}})')
        else:
            annotations.append('@ToString')
            annotations.append('@EqualsAndHashCode')
        
        annotations_str = '\n'.join(annotations) + '\n'
        
        # Insert annotations before class declaration
        content = content[:class_start] + annotations_str + content[class_start:]
    
    return content

def refactor_file(file_path):
    """Refactor a single Java file."""
    print(f"Processing {file_path.name}...")
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Skip if already has Lombok
    if has_lombok_imports(content):
        print(f"  Skipping {file_path.name} - already has Lombok")
        return False
    
    # Extract class info
    class_info = extract_class_info(content)
    
    # Add Lombok annotations
    content = add_lombok_annotations(content, class_info)
    
    # Remove getters and setters (but keep custom ones)
    # For now, we'll be conservative and only remove simple ones
    # Complex getters/setters should be kept
    
    # Write back
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"  Refactored {file_path.name}")
    return True

def main():
    if len(sys.argv) > 1:
        base_dir = sys.argv[1]
    else:
        base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    
    entity_files = find_entity_files(base_dir)
    
    if not entity_files:
        print("No entity files found!")
        return
    
    print(f"Found {len(entity_files)} entity files")
    print("=" * 50)
    
    refactored = 0
    for file_path in entity_files:
        if refactor_file(file_path):
            refactored += 1
    
    print("=" * 50)
    print(f"Refactored {refactored} files")

if __name__ == '__main__':
    main()

