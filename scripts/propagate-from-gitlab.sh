#!/usr/bin/env bash
# propagate-from-gitlab.sh
#
# Two-stage propagation:
#   Stage 1  GitLab canonical clone  ->  samples/runners/boot-jdk17-jakarta/  (mirror)
#   Stage 2  samples/runners/boot-jdk17-jakarta/  ->  6 sibling runners  (sync-from-canonical.sh)
#
# Stage 1 mirrors the same whitelist sync-from-canonical.sh uses (Java tree + selected
# resources), so monorepo-specific files (pom.xml, application.yml, xeni.properties,
# NexacroN_server_license.xml, logback/log4j2.xml) are NEVER overwritten by GitLab.
#
# Usage:
#   scripts/propagate-from-gitlab.sh <gitlab-repo-path> [options]
#   scripts/propagate-from-gitlab.sh --skip-stage1 [options]
#
# Options:
#   --dry-run         Show plan; modify nothing.
#   --no-compile      Skip the mvn compile gate in stage 2.
#   --skip-stage1     GitLab canonical already mirrored manually; only run stage 2.
#   --skip-stage2     Only mirror GitLab -> canonical; do not propagate to siblings.
#   --only <runner>   In stage 2, sync only this runner (default: all 6 siblings).
#   -h, --help        This help.
#
# Examples:
#   scripts/propagate-from-gitlab.sh /d/AI/workspace/gitlab-nexacron
#   scripts/propagate-from-gitlab.sh ../gitlab-nexacron --no-compile
#   scripts/propagate-from-gitlab.sh --skip-stage1                 # canonical already updated
#   scripts/propagate-from-gitlab.sh ../gitlab-nexacron --only boot-jdk8-javax

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
CANONICAL="boot-jdk17-jakarta"
CANONICAL_DIR="$REPO_ROOT/samples/runners/$CANONICAL"

ALL_SIBLINGS=(
  boot-jdk8-javax
  mvc-jdk17-jakarta
  mvc-jdk8-javax
  egov5-boot-jdk17-jakarta
  egov4-boot-jdk8-javax
  egov4-mvc-jdk8-javax
)

GITLAB_PATH=""
DRY_RUN=0
NO_COMPILE=0
SKIP_STAGE1=0
SKIP_STAGE2=0
ONLY=""

usage() { sed -n '2,30p' "$0" | sed 's/^# \{0,1\}//'; }

while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--help)      usage; exit 0 ;;
    --dry-run)      DRY_RUN=1 ;;
    --no-compile)   NO_COMPILE=1 ;;
    --skip-stage1)  SKIP_STAGE1=1 ;;
    --skip-stage2)  SKIP_STAGE2=1 ;;
    --only)         shift; ONLY="${1:-}";;
    -*)             echo "Unknown flag: $1" >&2; usage; exit 1 ;;
    *)              [[ -z "$GITLAB_PATH" ]] && GITLAB_PATH="$1" || { echo "Unexpected: $1" >&2; exit 1; } ;;
  esac
  shift
done

# Validate ONLY value
if [[ -n "$ONLY" ]]; then
  found=0
  for r in "${ALL_SIBLINGS[@]}"; do [[ "$r" == "$ONLY" ]] && found=1; done
  [[ $found -eq 0 ]] && { echo "ERROR: --only must be one of: ${ALL_SIBLINGS[*]}" >&2; exit 1; }
fi

# Resolve sibling list
if [[ -n "$ONLY" ]]; then
  SIBLINGS=("$ONLY")
else
  SIBLINGS=("${ALL_SIBLINGS[@]}")
fi

# ---------------------------------------------------------------------------
# Stage 1: GitLab -> monorepo canonical (boot-jdk17-jakarta)
# ---------------------------------------------------------------------------
mirror_stage1() {
  [[ -z "$GITLAB_PATH" ]] && { echo "ERROR: GitLab repo path required for stage 1." >&2; usage; exit 1; }
  local gl
  gl="$(cd "$GITLAB_PATH" 2>/dev/null && pwd || true)"
  [[ -z "$gl" || ! -d "$gl" ]] && { echo "ERROR: GitLab path not found: $GITLAB_PATH" >&2; exit 1; }

  local src_java="$gl/src/main/java/com/nexacro/uiadapter"
  [[ ! -d "$src_java" ]] && { echo "ERROR: GitLab repo missing $src_java" >&2; exit 1; }

  echo "[stage1] mirror: $gl  ->  $CANONICAL_DIR"

  local dst_java="$CANONICAL_DIR/src/main/java/com/nexacro/uiadapter"
  mirror_tree "$src_java" "$dst_java" "src/main/java/com/nexacro/uiadapter"

  local src_res="$gl/src/main/resources"
  local dst_res="$CANONICAL_DIR/src/main/resources"
  for item in data.sql schema.sql mybatis static; do
    if [[ -e "$src_res/$item" ]]; then
      if [[ -d "$src_res/$item" ]]; then
        mirror_tree "$src_res/$item" "$dst_res/$item" "src/main/resources/$item"
      else
        mirror_file "$src_res/$item" "$dst_res/$item" "src/main/resources/$item"
      fi
    fi
  done

  echo "[stage1] done."
}

# mirror_tree <src-dir> <dst-dir> <rel-prefix-for-logging>
mirror_tree() {
  local src="$1" dst="$2" prefix="$3"
  mkdir -p "$dst"
  if command -v rsync &>/dev/null; then
    local dry=""; [[ $DRY_RUN -eq 1 ]] && dry="--dry-run -v"
    # shellcheck disable=SC2086
    rsync -a --delete-after $dry "$src/" "$dst/" >/dev/null
    [[ $DRY_RUN -eq 1 ]] && rsync -av --dry-run "$src/" "$dst/" | sed -n 's/^/[stage1 plan] /p' | head -50 || true
  else
    # copy fresh files
    while IFS= read -r -d '' f; do
      local rel="${f#$src/}"
      local df="$dst/$rel"
      mkdir -p "$(dirname "$df")"
      if [[ $DRY_RUN -eq 1 ]]; then
        if ! diff -q "$f" "$df" >/dev/null 2>&1; then echo "[stage1 plan] copy: $prefix/$rel"; fi
      else
        if ! diff -q "$f" "$df" >/dev/null 2>&1; then cp "$f" "$df"; fi
      fi
    done < <(find "$src" -type f -print0)
    # delete orphans in dst
    while IFS= read -r -d '' df; do
      local rel="${df#$dst/}"
      if [[ ! -f "$src/$rel" ]]; then
        if [[ $DRY_RUN -eq 1 ]]; then echo "[stage1 plan] delete: $prefix/$rel"
        else rm -f "$df"; fi
      fi
    done < <(find "$dst" -type f -print0)
  fi
}

# mirror_file <src-file> <dst-file> <rel-for-logging>
mirror_file() {
  local f="$1" df="$2" rel="$3"
  mkdir -p "$(dirname "$df")"
  if ! diff -q "$f" "$df" >/dev/null 2>&1; then
    if [[ $DRY_RUN -eq 1 ]]; then echo "[stage1 plan] copy: $rel"
    else cp "$f" "$df"; fi
  fi
}

# ---------------------------------------------------------------------------
# Stage 2: canonical -> 6 siblings (delegate to sync-from-canonical.sh)
# ---------------------------------------------------------------------------
propagate_stage2() {
  local sync="$SCRIPT_DIR/sync-from-canonical.sh"
  [[ ! -x "$sync" ]] && { echo "ERROR: $sync not executable" >&2; exit 1; }

  local flags=()
  [[ $DRY_RUN    -eq 1 ]] && flags+=(--dry-run)
  [[ $NO_COMPILE -eq 1 ]] && flags+=(--no-compile)

  local fail=0
  for r in "${SIBLINGS[@]}"; do
    echo
    echo "===> [stage2] $r"
    if ! "$sync" "$r" "${flags[@]}"; then
      echo "[stage2] FAIL: $r — stop." >&2
      fail=1
      break
    fi
  done

  return $fail
}

# ---------------------------------------------------------------------------
# Drive
# ---------------------------------------------------------------------------
echo "============================================================"
echo " propagate-from-gitlab.sh"
echo "  GitLab path  : ${GITLAB_PATH:-<skip-stage1>}"
echo "  Canonical    : $CANONICAL"
echo "  Siblings     : ${SIBLINGS[*]}"
echo "  DRY_RUN=$DRY_RUN  NO_COMPILE=$NO_COMPILE  SKIP1=$SKIP_STAGE1  SKIP2=$SKIP_STAGE2"
echo "============================================================"

if [[ $SKIP_STAGE1 -eq 0 ]]; then
  mirror_stage1
else
  echo "[stage1] skipped (--skip-stage1)."
fi

if [[ $SKIP_STAGE2 -eq 0 ]]; then
  propagate_stage2
else
  echo "[stage2] skipped (--skip-stage2)."
fi

echo
echo "[done] propagate-from-gitlab finished."
if [[ $DRY_RUN -eq 0 ]]; then
  echo
  echo "Next:"
  echo "  git status"
  echo "  git diff --stat samples/runners"
  echo "  git add samples/runners && git commit -m 'sync: propagate <summary> from GitLab canonical'"
  echo "  git push -u origin HEAD"
  echo "  gh pr create --fill   # verify CI matrix is 7/7 green"
fi
