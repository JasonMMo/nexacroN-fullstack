#!/usr/bin/env bash
# lane-transform.sh — rewrite jakarta -> javax imports in *.java files
# Usage: scripts/lane-transform.sh <path-to-scan>
#
# Patterns (most-specific first, all anchored on ^import):
#   1. com.nexacro.uiadapter.jakarta.core. -> com.nexacro.uiadapter.spring.core.
#   2. com.nexacro.uiadapter.jakarta.      -> com.nexacro.uiadapter.spring.   (generic fallback)
#   3. jakarta.servlet.                    -> javax.servlet.
#   4. jakarta.validation.                 -> javax.validation.
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $(basename "$0") <path-to-scan>" >&2; exit 1
fi

SCAN_PATH="$1"
if [[ ! -d "$SCAN_PATH" ]]; then
  echo "ERROR: path not found: $SCAN_PATH" >&2; exit 1
fi

find "$SCAN_PATH" -name '*.java' -print0 | xargs -0 sed -i.bak \
  -e 's|^import com\.nexacro\.uiadapter\.jakarta\.core\.|import com.nexacro.uiadapter.spring.core.|' \
  -e 's|^import com\.nexacro\.uiadapter\.jakarta\.|import com.nexacro.uiadapter.spring.|' \
  -e 's|^import jakarta\.servlet\.|import javax.servlet.|' \
  -e 's|^import jakarta\.validation\.|import javax.validation.|'

find "$SCAN_PATH" -name '*.java.bak' -delete

echo "[lane-transform] done: $SCAN_PATH"
