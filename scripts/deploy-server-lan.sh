#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [[ -f .env.backend ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env.backend
  set +a
fi

BIND_HOST="${BIND_HOST:-0.0.0.0}"
BACKEND_PORT="${BACKEND_PORT:-8081}"
ASSIGN_SERVER_DATA_DIR="${ASSIGN_SERVER_DATA_DIR:-assign-server-data}"
ASSIGN_DB_URL="${ASSIGN_DB_URL:-jdbc:mysql://localhost:3306/exam_scheduler?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8}"
ASSIGN_DB_USER="${ASSIGN_DB_USER:-root}"
ASSIGN_DB_PASSWORD="${ASSIGN_DB_PASSWORD:-}"

echo "Building assign-server..."
mvn -q -f assign-server/pom.xml clean compile \
  -Dassign.dataDir="${ASSIGN_SERVER_DATA_DIR}" \
  -Dassign.dbUrl="${ASSIGN_DB_URL}" \
  -Dassign.dbUser="${ASSIGN_DB_USER}" \
  -Dassign.dbPassword="${ASSIGN_DB_PASSWORD}"

echo "Starting assign-server on ${BIND_HOST}:${BACKEND_PORT}"
exec mvn -f assign-server/pom.xml jetty:run \
  -Djetty.http.host="${BIND_HOST}" \
  -Djetty.host="${BIND_HOST}" \
  -Djetty.http.port="${BACKEND_PORT}" \
  -Dassign.dataDir="${ASSIGN_SERVER_DATA_DIR}" \
  -Dassign.dbUrl="${ASSIGN_DB_URL}" \
  -Dassign.dbUser="${ASSIGN_DB_USER}" \
  -Dassign.dbPassword="${ASSIGN_DB_PASSWORD}"
