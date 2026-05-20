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

echo "Building assign-server..."
mvn -q -f assign-server/pom.xml clean compile -DdataDir="${ASSIGN_SERVER_DATA_DIR}"

echo "Starting assign-server on ${BIND_HOST}:${BACKEND_PORT}"
exec mvn -f assign-server/pom.xml jetty:run \
  -Djetty.http.host="${BIND_HOST}" \
  -Djetty.host="${BIND_HOST}" \
  -Djetty.http.port="${BACKEND_PORT}" \
  -DdataDir="${ASSIGN_SERVER_DATA_DIR}"
