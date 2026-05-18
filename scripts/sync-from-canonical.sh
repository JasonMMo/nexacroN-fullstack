#!/usr/bin/env bash
# sync-from-canonical.sh
# Usage: scripts/sync-from-canonical.sh <target-runner> [--dry-run] [--no-compile]
set -euo pipefail

CANONICAL="boot-jdk17-jakarta"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
RUNNERS_DIR="$REPO_ROOT/samples/runners"
RUNNER_CONFIG="$SCRIPT_DIR/runner-config"

TARGET=""
DRY_RUN=""
NO_COMPILE=0

for arg in "$@"; do
  case "$arg" in
    --dry-run)    DRY_RUN="yes" ;;
    --no-compile) NO_COMPILE=1 ;;
    -*) echo "Unknown flag: $arg" >&2; exit 1 ;;
    *)  TARGET="$arg" ;;
  esac
done

if [[ -z "$TARGET" ]]; then
  echo "Usage: $(basename "$0") <target-runner> [--dry-run] [--no-compile]" >&2; exit 1
fi
if [[ "$TARGET" == "$CANONICAL" ]]; then
  echo "ERROR: cannot sync canonical onto itself ($CANONICAL)." >&2; exit 1
fi

SOURCE="$RUNNERS_DIR/$CANONICAL"
DEST="$RUNNERS_DIR/$TARGET"
EXCLUDE_FILE="$RUNNER_CONFIG/${TARGET}.exclude"

[[ ! -d "$SOURCE" ]] && { echo "ERROR: canonical source not found: $SOURCE" >&2; exit 1; }
[[ ! -d "$DEST"   ]] && { echo "ERROR: target runner not found: $DEST" >&2; exit 1; }
[[ ! -f "$EXCLUDE_FILE" ]] && { echo "ERROR: exclude config not found: $EXCLUDE_FILE" >&2; exit 1; }

# Common baseline excludes (basename-matched)
COMMON_EXACT=( "pom.xml" "application.yml" "application.properties" "xeni.properties" "NexacroN_server_license.xml" )
COMMON_GLOBS=( "logback*.xml" "log4j2*.xml" )

# Read per-target filter file
TARGET_EXCLUDES=()
TARGET_PROTECTS=()
while IFS= read -r line; do
  [[ "$line" =~ ^# ]] && continue
  [[ -z "${line// }" ]] && continue
  if [[ "$line" =~ ^-[[:space:]]+(.*) ]]; then
    TARGET_EXCLUDES+=("${BASH_REMATCH[1]}")
  elif [[ "$line" =~ ^P[[:space:]]+(.*) ]]; then
    TARGET_PROTECTS+=("${BASH_REMATCH[1]}")
  fi
done < "$EXCLUDE_FILE"

# is_excluded_rr: runner-root-relative path
is_excluded_rr() {
  local rr="$1"
  local base; base="$(basename "$rr")"
  for e in "${COMMON_EXACT[@]}";  do [[ "$base" == "$e" ]] && return 0; done
  for g in "${COMMON_GLOBS[@]}";  do case "$base" in $g) return 0;; esac; done
  for p in "${TARGET_EXCLUDES[@]}"; do
    local cp="${p%/}"
    case "$rr" in "$cp"|"$cp/"*|*"/$cp"|*"/$cp/"*) return 0;; esac
    [[ "$base" == "$cp" ]] && return 0
  done
  return 1
}

is_protected_rr() {
  local rr="$1"
  for p in "${TARGET_PROTECTS[@]}"; do
    local cp="${p%/}"
    case "$rr" in "$cp"|"$cp/"*|*"/$cp"|*"/$cp/"*) return 0;; esac
  done
  return 1
}

TOTAL_CHANGED=0

cp_sync() {
  local src="$1" dst="$2" prefix="$3"
  local changed=0
  mkdir -p "$dst"
  while IFS= read -r -d '' f; do
    local rel="${f#$src/}"
    local rr="$prefix/$rel"
    is_excluded_rr "$rr" && continue
    local df="$dst/$rel"
    mkdir -p "$(dirname "$df")"
    if [[ -z "$DRY_RUN" ]]; then
      if ! diff -q "$f" "$df" >/dev/null 2>&1; then cp "$f" "$df"; changed=$((changed+1)); fi
    else
      # Dry-run plan goes to stderr — stdout is reserved for the final count
      # captured by the caller via $(do_sync ...).
      echo "[dry-run] copy: $rr" >&2; changed=$((changed+1))
    fi
  done < <(find "$src" -type f -print0)
  while IFS= read -r -d '' df; do
    local rel="${df#$dst/}"
    local rr="$prefix/$rel"
    [[ -f "$src/$rel" ]] && continue
    is_protected_rr "$rr" && continue
    is_excluded_rr  "$rr" && continue
    if [[ -z "$DRY_RUN" ]]; then rm -f "$df"; else echo "[dry-run] delete: $rr" >&2; fi
    changed=$((changed+1))
  done < <(find "$dst" -type f -print0)
  echo "$changed"
}

rsync_sync() {
  local src="$1" dst="$2"
  mkdir -p "$dst"
  local filters=()
  for e in "${COMMON_EXACT[@]}"; do filters+=("--filter=- $e" "--filter=P $e"); done
  for g in "${COMMON_GLOBS[@]}"; do filters+=("--filter=- $g" "--filter=P $g"); done
  filters+=("--filter=merge $EXCLUDE_FILE")
  local dry=""; [[ -n "$DRY_RUN" ]] && dry="--dry-run"
  # shellcheck disable=SC2086
  rsync -av --delete-after "${filters[@]}" $dry "$src/" "$dst/" | grep -c '^>' || true
}

do_sync() {
  if command -v rsync &>/dev/null; then rsync_sync "$1" "$2"; else cp_sync "$1" "$2" "$3"; fi
}

SRC_JAVA="$SOURCE/src/main/java/com/nexacro/uiadapter"
DEST_JAVA="$DEST/src/main/java/com/nexacro/uiadapter"
C=$(do_sync "$SRC_JAVA" "$DEST_JAVA" "src/main/java/com/nexacro/uiadapter")
TOTAL_CHANGED=$((TOTAL_CHANGED + C))

for ITEM in data.sql schema.sql mybatis static; do
  SRC_ITEM="$SOURCE/src/main/resources/$ITEM"
  DEST_RES="$DEST/src/main/resources"
  if [[ -e "$SRC_ITEM" ]]; then
    if [[ -d "$SRC_ITEM" ]]; then
      IC=$(do_sync "$SRC_ITEM" "$DEST_RES/$ITEM" "src/main/resources/$ITEM")
    else
      IC=0
      if ! is_excluded_rr "src/main/resources/$ITEM"; then
        if ! diff -q "$SRC_ITEM" "$DEST_RES/$ITEM" >/dev/null 2>&1; then
          if [[ -z "$DRY_RUN" ]]; then
            cp "$SRC_ITEM" "$DEST_RES/$ITEM"; IC=1
          else
            echo "[dry-run] copy: src/main/resources/$ITEM" >&2; IC=1
          fi
        fi
      fi
    fi
    TOTAL_CHANGED=$((TOTAL_CHANGED + IC))
  fi
done

IS_JAVAX=0; case "$TARGET" in *javax*) IS_JAVAX=1;; esac
if [[ $IS_JAVAX -eq 1 && -z "$DRY_RUN" ]]; then
  "$SCRIPT_DIR/lane-transform.sh" "$DEST/src/main/java"
fi

# Resolve mvn: prefer system mvn, fall back to known local install locations
find_mvn() {
  if command -v mvn &>/dev/null; then echo mvn; return; fi
  local candidates=(
    "$HOME/AppData/Local/Programs/IntelliJ IDEA Ultimate/plugins/maven/lib/maven3/bin/mvn"
    "/c/Users/mo/AppData/Local/Programs/IntelliJ IDEA Ultimate/plugins/maven/lib/maven3/bin/mvn"
  )
  for c in "${candidates[@]}"; do
    [[ -f "$c" ]] && { echo "$c"; return; }
  done
  echo ""
}

COMPILE_STATUS="skipped"
if [[ $NO_COMPILE -eq 0 && -z "$DRY_RUN" ]]; then
  MVN_CMD=$(find_mvn)
  if [[ -z "$MVN_CMD" ]]; then
    echo "WARNING: mvn not found — skipping compile gate" >&2
    COMPILE_STATUS="skipped (mvn not found)"
  elif (cd "$DEST" && "$MVN_CMD" -o -q -DskipTests compile 2>/dev/null); then
    COMPILE_STATUS="OK"
  elif (cd "$DEST" && "$MVN_CMD" -q -DskipTests compile 2>/dev/null); then
    COMPILE_STATUS="OK (online)"
  else
    echo "ERROR: mvn compile failed for $TARGET" >&2; exit 1
  fi
fi

echo "[sync-from-canonical] target=$TARGET files-changed=$TOTAL_CHANGED compile=$COMPILE_STATUS"
