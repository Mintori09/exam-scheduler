#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "Building assign-server..."
mvn -q -f assign-server/pom.xml clean compile

echo "Starting assign-server on 0.0.0.0:8081"
exec mvn -f assign-server/pom.xml jetty:run \
  -Djetty.http.host=0.0.0.0 \
  -Djetty.host=0.0.0.0 \
  -Djetty.http.port=8081
