set shell := ["bash", "-cu"]

default:
    @just --list

backend:
    cd assign-server && mvn -q clean compile && mvn jetty:run -Djetty.http.port=8081

frontend:
    cd client-web && mvn -q clean compile && mvn jetty:run -Djetty.http.port=8080

test:
    cd assign-server && mvn test

dev:
    @if ! command -v watchexec >/dev/null 2>&1; then \
      echo "Missing 'watchexec'. Install it first: cargo install watchexec-cli (or your package manager)."; \
      exit 1; \
    fi
    watchexec \
      --restart \
      --watch assign-server/src \
      --watch client-web/src \
      --watch pom.xml \
      --watch assign-server/pom.xml \
      --watch client-web/pom.xml \
      --exts java,jsp,xml,properties \
      -- "bash -lc 'trap \"kill 0\" EXIT INT TERM; \
        (cd assign-server && mvn -q clean compile && mvn jetty:run -Djetty.http.port=8081) & \
        (cd client-web && mvn -q clean compile && mvn jetty:run -Djetty.http.port=8080) & \
        wait'"
