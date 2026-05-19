#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

detect_lan_ip() {
  if [[ -n "${LAN_IP:-}" ]]; then
    echo "$LAN_IP"
    return 0
  fi
  if command -v ip >/dev/null 2>&1; then
    local candidate
    candidate="$(ip route get 1.1.1.1 2>/dev/null | awk '{for (i = 1; i <= NF; i++) if ($i == "src") {print $(i + 1); exit}}')"
    if [[ -n "${candidate:-}" ]]; then
      echo "$candidate"
      return 0
    fi
  fi
  if command -v hostname >/dev/null 2>&1; then
    local hostname_ip
    hostname_ip="$(hostname -I 2>/dev/null | awk '{print $1}')"
    if [[ -n "${hostname_ip:-}" ]]; then
      echo "$hostname_ip"
      return 0
    fi
  fi
  echo "127.0.0.1"
}

LAN_IP_VALUE="$(detect_lan_ip)"
SERVER_BASE_URL="${SERVER_BASE_URL:-http://${LAN_IP_VALUE}:8081/assign-server}"

echo "Using backend: ${SERVER_BASE_URL}"
echo "Building client-web..."
mvn -q -f client-web/pom.xml clean compile -DserverBaseUrl="${SERVER_BASE_URL}"

echo "Starting client-web on 0.0.0.0:8080"
exec mvn -f client-web/pom.xml jetty:run \
  -Djetty.http.host=0.0.0.0 \
  -Djetty.host=0.0.0.0 \
  -Djetty.http.port=8080 \
  -DserverBaseUrl="${SERVER_BASE_URL}"
