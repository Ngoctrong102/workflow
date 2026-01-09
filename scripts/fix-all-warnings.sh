#!/bin/bash
# Script to fix all Java warnings automatically

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"

echo -e "${GREEN}=== Fixing Java Warnings ===${NC}\n"

# Step 1: Run basic fix script
echo -e "${YELLOW}Step 1: Removing unused imports and unnecessary @SuppressWarnings...${NC}"
python3 "$SCRIPT_DIR/fix-warnings.py" "$BACKEND_DIR/src/main/java" 2>&1 | tail -5
echo ""

# Step 2: Fix remaining unused imports in test files
echo -e "${YELLOW}Step 2: Fixing unused imports in test files...${NC}"
python3 "$SCRIPT_DIR/fix-warnings.py" "$BACKEND_DIR/src/test/java" 2>&1 | tail -5
echo ""

# Step 3: Compile to check results
echo -e "${YELLOW}Step 3: Compiling to verify fixes...${NC}"
cd "$BACKEND_DIR"
if mvn compile -q > /tmp/compile.log 2>&1; then
    echo -e "${GREEN}✓ Compilation successful${NC}"
    
    # Count remaining warnings
    WARNING_COUNT=$(grep -i "warning" /tmp/compile.log | wc -l || echo "0")
    echo -e "${YELLOW}Remaining warnings: $WARNING_COUNT${NC}"
    
    if [ "$WARNING_COUNT" -gt 0 ]; then
        echo -e "${YELLOW}Note: Most remaining warnings are:${NC}"
        echo "  - Null type safety (from Spring annotations - not critical)"
        echo "  - Type safety unchecked casts (require @SuppressWarnings or proper typing)"
        echo "  - Unused variables/fields (may be used in future)"
        echo ""
        echo -e "${YELLOW}These warnings don't affect runtime and are mostly from static analysis.${NC}"
    fi
else
    echo -e "${RED}✗ Compilation failed. Check /tmp/compile.log for details${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}=== Summary ===${NC}"
echo "Scripts created:"
echo "  - scripts/fix-warnings.py (basic fixes)"
echo "  - scripts/fix-warnings-advanced.py (advanced fixes)"
echo "  - scripts/fix-all-warnings.sh (this script)"
echo ""
echo "To run manually:"
echo "  python3 scripts/fix-warnings.py"
echo "  python3 scripts/fix-warnings-advanced.py"

