set shell := ["bash", "-cu"]

default:
    @just --list

backend:
    cd assign-server && mvn jetty:run -Djetty.http.port=8081

frontend:
    cd client-web && mvn jetty:run -Djetty.http.port=8080

test:
    cd assign-server && mvn test

dev:
    @trap 'kill 0' EXIT INT TERM; \
    (cd assign-server && mvn jetty:run -Djetty.http.port=8081) & \
    (cd client-web && mvn jetty:run -Djetty.http.port=8080) & \
    wait
