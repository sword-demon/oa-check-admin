#!/usr/bin/env bash
set -euo pipefail

# ============================================================
# OA Check Admin - Project Initialization Script
# Replaces package names, ports, database names for a fresh fork.
# Idempotent: re-running is safe (checks .init-done marker).
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
MARKER_FILE="$SCRIPT_DIR/.init-done"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }

# ---- Parse args ----
FORCE=false
while [[ $# -gt 0 ]]; do
  case $1 in
    --force) FORCE=true; shift ;;
    -h|--help)
      echo "Usage: $0 [--force]"
      echo "  --force  Re-initialize even if already done"
      exit 0
      ;;
    *) error "Unknown option: $1" ;;
  esac
  shift
done

# ---- Idempotency check ----
if [ -f "$MARKER_FILE" ] && [ "$FORCE" = false ]; then
  warn "Already initialized ($(cat "$MARKER_FILE")). Use --force to re-initialize."
  exit 0
fi

# ---- Interactive input ----
echo "=========================================="
echo "  OA Check Admin - Project Setup"
echo "=========================================="
echo ""

read -rp "Group ID [com.example]: " GROUP_ID
GROUP_ID="${GROUP_ID:-com.example}"

read -rp "Artifact ID [oa-admin]: " ARTIFACT_ID
ARTIFACT_ID="${ARTIFACT_ID:-oa-admin}"

read -rp "Package name [$GROUP_ID]: " PACKAGE_NAME
PACKAGE_NAME="${PACKAGE_NAME:-$GROUP_ID}"

read -rp "Backend port [8080]: " BACKEND_PORT
BACKEND_PORT="${BACKEND_PORT:-8080}"

read -rp "Frontend port [80]: " FRONTEND_PORT
FRONTEND_PORT="${FRONTEND_PORT:-80}"

read -rp "Database name [oa_admin]: " DB_NAME
DB_NAME="${DB_NAME:-oa_admin}"

read -rp "Database password [changeme]: " DB_PASSWORD
DB_PASSWORD="${DB_PASSWORD:-changeme}"

read -rp "MySQL port [3306]: " MYSQL_PORT
MYSQL_PORT="${MYSQL_PORT:-3306}"

read -rp "Redis port [6379]: " REDIS_PORT
REDIS_PORT="${REDIS_PORT:-6379}"

echo ""
echo "---- Summary ----"
echo "  Group ID:       $GROUP_ID"
echo "  Artifact ID:    $ARTIFACT_ID"
echo "  Package:        $PACKAGE_NAME"
echo "  Backend port:   $BACKEND_PORT"
echo "  Frontend port:  $FRONTEND_PORT"
echo "  Database:       $DB_NAME"
echo "  DB password:    ********"
echo "  MySQL port:     $MYSQL_PORT"
echo "  Redis port:     $REDIS_PORT"
echo ""

read -rp "Proceed? [Y/n]: " CONFIRM
CONFIRM="${CONFIRM:-Y}"
if [[ ! "$CONFIRM" =~ ^[Yy] ]]; then
  info "Aborted."
  exit 0
fi

# ---- Replace package structure ----
OLD_PACKAGE="com.oa.admin"
OLD_PACKAGE_PATH="com/oa/admin"
NEW_PACKAGE_PATH=$(echo "$PACKAGE_NAME" | tr '.' '/')

info "Replacing package: $OLD_PACKAGE → $PACKAGE_NAME"

# Move Java source directories
for MODULE in oa-common oa-system oa-approval oa-app; do
  SRC_DIR="$SCRIPT_DIR/$MODULE/src/main/java/$OLD_PACKAGE_PATH"
  DEST_DIR="$SCRIPT_DIR/$MODULE/src/main/java/$NEW_PACKAGE_PATH"
  if [ -d "$SRC_DIR" ]; then
    mkdir -p "$(dirname "$DEST_DIR")"
    # Copy and update package declarations
    if [ "$SRC_DIR" != "$DEST_DIR" ]; then
      cp -r "$SRC_DIR/." "$DEST_DIR/"
      rm -rf "$SRC_DIR"
      # Remove empty parent dirs
      find "$SCRIPT_DIR/$MODULE/src/main/java/com" -type d -empty -delete 2>/dev/null || true
    fi
    # Update package declarations in all Java files
    find "$DEST_DIR" -name '*.java' -exec sed -i.bak "s/package $OLD_PACKAGE/package $PACKAGE_NAME/g" {} +
    find "$DEST_DIR" -name '*.java' -exec sed -i.bak "s/import $OLD_PACKAGE/import $PACKAGE_NAME/g" {} +
    find "$DEST_DIR" -name '*.bak' -delete
  fi

  # Also handle test directories
  TEST_SRC_DIR="$SCRIPT_DIR/$MODULE/src/test/java/$OLD_PACKAGE_PATH"
  TEST_DEST_DIR="$SCRIPT_DIR/$MODULE/src/test/java/$NEW_PACKAGE_PATH"
  if [ -d "$TEST_SRC_DIR" ]; then
    mkdir -p "$(dirname "$TEST_DEST_DIR")"
    if [ "$TEST_SRC_DIR" != "$TEST_DEST_DIR" ]; then
      cp -r "$TEST_SRC_DIR/." "$TEST_DEST_DIR/"
      rm -rf "$TEST_SRC_DIR"
      find "$SCRIPT_DIR/$MODULE/src/test/java/com" -type d -empty -delete 2>/dev/null || true
    fi
    find "$TEST_DEST_DIR" -name '*.java' -exec sed -i.bak "s/package $OLD_PACKAGE/package $PACKAGE_NAME/g" {} +
    find "$TEST_DEST_DIR" -name '*.java' -exec sed -i.bak "s/import $OLD_PACKAGE/import $PACKAGE_NAME/g" {} +
    find "$TEST_DEST_DIR" -name '*.bak' -delete
  fi
done

# Update MapperScan in Application class
APP_FILE=$(find "$SCRIPT_DIR/oa-app/src" -name 'OaAdminApplication.java' 2>/dev/null | head -1)
if [ -n "$APP_FILE" ]; then
  sed -i.bak "s/$OLD_PACKAGE/$PACKAGE_NAME/g" "$APP_FILE"
  rm -f "$APP_FILE.bak"
  # Rename the file to match the new class name convention if desired
  info "Updated Application class"
fi

# ---- Update POM files ----
info "Updating POM files..."
find "$SCRIPT_DIR" -name 'pom.xml' -not -path '*/target/*' -exec sed -i.bak \
  -e "s|<groupId>com.oa</groupId>|<groupId>${GROUP_ID}</groupId>|g" \
  -e "s|<artifactId>oa-common</artifactId>|<artifactId>${ARTIFACT_ID}-common</artifactId>|g" \
  -e "s|<artifactId>oa-system</artifactId>|<artifactId>${ARTIFACT_ID}-system</artifactId>|g" \
  -e "s|<artifactId>oa-approval</artifactId>|<artifactId>${ARTIFACT_ID}-approval</artifactId>|g" \
  -e "s|<artifactId>oa-app</artifactId>|<artifactId>${ARTIFACT_ID}-app</artifactId>|g" \
  -e "s|<artifactId>oa-check-admin</artifactId>|<artifactId>${ARTIFACT_ID}</artifactId>|g" \
  {} +
find "$SCRIPT_DIR" -name 'pom.xml.bak' -delete

# ---- Update application.yml ----
info "Updating application.yml..."
APP_YML="$SCRIPT_DIR/oa-app/src/main/resources/application.yml"
if [ -f "$APP_YML" ]; then
  sed -i.bak \
    -e "s|oa_admin|${DB_NAME}|g" \
    -e "s|changeme|${DB_PASSWORD}|g" \
    -e "s|port: 8080|port: ${BACKEND_PORT}|g" \
    "$APP_YML"
  rm -f "$APP_YML.bak"
fi

# ---- Update docker-compose.yml ----
info "Updating docker-compose.yml..."
DC_FILE="$SCRIPT_DIR/docker-compose.yml"
if [ -f "$DC_FILE" ]; then
  sed -i.bak \
    -e "s|oa_admin|${DB_NAME}|g" \
    -e "s|changeme|${DB_PASSWORD}|g" \
    -e "s|\"3306:3306\"|\"${MYSQL_PORT}:3306\"|g" \
    -e "s|\"6379:6379\"|\"${REDIS_PORT}:6379\"|g" \
    -e "s|\"8080:8080\"|\"${BACKEND_PORT}:8080\"|g" \
    -e "s|\"80:80\"|\"${FRONTEND_PORT}:80\"|g" \
    "$DC_FILE"
  rm -f "$DC_FILE.bak"
fi

# ---- Update Flyway seed data default password if needed ----
SEED_SQL="$SCRIPT_DIR/oa-app/src/main/resources/db/migration/V3__seed_data.sql"
if [ -f "$SEED_SQL" ] && [ "$DB_PASSWORD" != "changeme" ]; then
  # The admin user password is BCrypt hashed admin123 - no change needed here
  info "Seed data kept (admin password is BCrypt hashed, unchanged)"
fi

# ---- Update module directories to match new artifactId ----
info "Renaming module directories..."
for MODULE in common system approval app; do
  OLD_DIR="$SCRIPT_DIR/oa-${MODULE}"
  NEW_DIR="$SCRIPT_DIR/${ARTIFACT_ID}-${MODULE}"
  if [ -d "$OLD_DIR" ] && [ "$OLD_DIR" != "$NEW_DIR" ]; then
    mv "$OLD_DIR" "$NEW_DIR"
  fi
done

# Update module references in parent POM
POM_FILE="$SCRIPT_DIR/pom.xml"
if [ -f "$POM_FILE" ]; then
  for MODULE in common system approval app; do
    sed -i.bak "s|<module>oa-${MODULE}</module>|<module>${ARTIFACT_ID}-${MODULE}</module>|g" "$POM_FILE"
  done
  rm -f "$POM_FILE.bak"
fi

# ---- Update frontend ----
info "Updating frontend config..."
UI_PKG="$SCRIPT_DIR/oa-ui/package.json"
if [ -f "$UI_PKG" ]; then
  sed -i.bak "s|\"name\": \"oa-ui\"|\"name\": \"${ARTIFACT_ID}-ui\"|g" "$UI_PKG"
  rm -f "$UI_PKG.bak"
fi

# ---- Write marker ----
date -u +%Y-%m-%dT%H:%M:%SZ > "$MARKER_FILE"
echo "groupId=$GROUP_ID artifactId=$ARTIFACT_ID package=$PACKAGE_NAME" >> "$MARKER_FILE"

echo ""
info "=========================================="
info "  Initialization complete!"
info "=========================================="
echo ""
info "Next steps:"
echo "  1. cd $SCRIPT_DIR"
echo "  2. docker compose up -d"
echo "  3. Wait for MySQL to be ready (~30s)"
echo "  4. Backend starts on port $BACKEND_PORT"
echo "  5. Frontend starts on port $FRONTEND_PORT"
echo "  6. Login: admin / admin123"
echo ""
info "To re-initialize: ./init.sh --force"
